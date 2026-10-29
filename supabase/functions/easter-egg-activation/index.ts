import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "npm:@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-client-info",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Content-Type": "application/json",
};
const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: corsHeaders });

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json({ error: "POST required." }, 405);

  const authHeader = req.headers.get("Authorization") ?? "";
  const match = authHeader.match(/^Bearer\s+(.+)$/i);
  if (!match) return json({ error: "A valid Alpha Account session is required." }, 401);

  const url = Deno.env.get("SUPABASE_URL") ?? "";
  const publishableKey = Deno.env.get("SUPABASE_ANON_KEY") ?? Deno.env.get("SUPABASE_PUBLISHABLE_KEY") ?? "";
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
  if (!url || !publishableKey || !serviceKey) {
    console.error("Missing required Supabase function environment variables.");
    return json({ error: "Easter Egg delivery is not configured." }, 500);
  }

  const token = match[1];
  const authClient = createClient(url, publishableKey, {
    auth: { autoRefreshToken: false, persistSession: false },
    global: { headers: { Authorization: `Bearer ${token}` } },
  });
  const admin = createClient(url, serviceKey, {
    auth: { autoRefreshToken: false, persistSession: false },
  });

  try {
    const { data: authData, error: authError } = await authClient.auth.getUser(token);
    const user = authData.user;
    if (authError || !user) return json({ error: "Alpha Account session is invalid or expired." }, 401);

    const { data: actor, error: actorError } = await admin
      .from("profiles").select("id, username").eq("id", user.id).maybeSingle();
    if (actorError || !actor) return json({ error: "No Alpha community profile is linked to this account." }, 403);

    const body = await req.json().catch(() => null) as Record<string, unknown> | null;
    if (!body || typeof body.action !== "string") return json({ error: "Missing action." }, 400);

    if (body.action === "enqueue") {
      const { data: founder, error: founderError } = await admin.from("developer_admins")
        .select("user_id").eq("user_id", user.id).maybeSingle();
      if (founderError) throw new Error("Could not verify Founder authorization.");
      if (!founder) return json({ error: "FOUNDER-ONLY: only the authenticated Founder can remotely activate Easter Eggs." }, 403);

      if (typeof body.username !== "string" || !/^[a-z0-9_]{3,20}$/i.test(body.username.trim())) {
        return json({ error: "Provide a valid Alpha account username." }, 400);
      }
      if (!Number.isInteger(body.egg_id) || (body.egg_id as number) < 1 || (body.egg_id as number) > 25) {
        return json({ error: "Easter Egg ID must be between 1 and 25." }, 400);
      }
      const targetName = body.username.trim().toLowerCase();
      const { data: target, error: targetError } = await admin.from("profiles")
        .select("id, username").eq("username", targetName).maybeSingle();
      if (targetError) throw new Error("Could not look up the target Alpha account.");
      if (!target) return json({ error: `Alpha account not found: ${targetName}` }, 404);
      if (target.id === user.id) return json({ error: "Use the local activation command for your own account." }, 400);

      const { data: activation, error: insertError } = await admin.from("client_easter_egg_activations")
        .insert({ target_profile_id: target.id, egg_id: body.egg_id, created_by: user.id })
        .select("id, egg_id, created_at, expires_at").single();
      if (insertError || !activation) throw new Error("Could not queue the Easter Egg activation.");

      const { error: auditError } = await admin.from("audit_logs").insert({
        actor_id: user.id,
        actor_label: actor.username,
        product: "client",
        category: "easter_eggs",
        action: "remote_activation_queued",
        target_user_id: target.id,
        target_label: target.username,
        outcome: "success",
        details: { egg_id: body.egg_id, activation_id: activation.id },
      });
      if (auditError) console.error("Could not write Easter Egg audit entry:", auditError.message);
      return json({ ok: true, action: "enqueue", target_username: target.username, activation });
    }

    if (body.action === "poll") {
      const now = new Date().toISOString();
      const { data: activation, error } = await admin.from("client_easter_egg_activations")
        .select("id, egg_id, created_at")
        .eq("target_profile_id", user.id).eq("status", "pending")
        .gt("expires_at", now).order("created_at", { ascending: true }).limit(1).maybeSingle();
      if (error) throw new Error("Could not check for pending Easter Egg activations.");
      return json({ ok: true, action: "poll", activation: activation ?? null });
    }

    if (body.action === "ack") {
      if (typeof body.activation_id !== "string" ||
          !/^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(body.activation_id)) {
        return json({ error: "Invalid activation ID." }, 400);
      }
      const { data: consumed, error } = await admin.from("client_easter_egg_activations")
        .update({ status: "consumed", consumed_at: new Date().toISOString() })
        .eq("id", body.activation_id).eq("target_profile_id", user.id).eq("status", "pending")
        .select("id").maybeSingle();
      if (error) throw new Error("Could not acknowledge the Easter Egg activation.");
      return json({ ok: true, action: "ack", consumed: !!consumed });
    }

    return json({ error: "Unknown action." }, 400);
  } catch (error) {
    console.error("Easter Egg activation request failed:", error);
    return json({ error: error instanceof Error ? error.message : "Easter Egg activation failed." }, 500);
  }
});