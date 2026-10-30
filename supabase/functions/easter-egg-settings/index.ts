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

function normalizeSettings(row: Record<string, unknown>) {
  const source = row.master_enabled && typeof row.master_enabled === "object"
    ? row.master_enabled as Record<string, unknown>
    : {};
  const masterEnabled: Record<string, boolean> = {};
  for (let id = 1; id <= 25; id++) {
    const value = source[String(id)];
    masterEnabled[String(id)] = typeof value === "boolean" ? value : true;
  }
  return {
    globally_enabled: row.globally_enabled === true,
    master_enabled: masterEnabled,
    updated_at: row.updated_at ?? null,
  };
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json({ error: "POST required." }, 405);

  const url = Deno.env.get("SUPABASE_URL") ?? "";
  const publishableKey = Deno.env.get("SUPABASE_ANON_KEY")
    ?? Deno.env.get("SUPABASE_PUBLISHABLE_KEY") ?? "";
  const serviceKey = Deno.env.get(["SUPABASE", "SERVICE", "ROLE", "KEY"].join("_")) ?? "";
  if (!url || !publishableKey || !serviceKey) {
    console.error("Missing required Supabase function environment variables.");
    return json({ error: "Easter Egg settings are not configured." }, 500);
  }

  try {
    const body = await req.json().catch(() => null) as Record<string, unknown> | null;
    if (!body || typeof body.action !== "string") return json({ error: "Missing action." }, 400);

    const admin = createClient(url, serviceKey, {
      auth: { autoRefreshToken: false, persistSession: false },
    });

    if (body.action === "config_get") {
      const { data, error } = await admin.from("client_easter_egg_settings")
        .select("globally_enabled, master_enabled, updated_at")
        .eq("singleton", true).single();
      if (error || !data) throw new Error("Could not load the global Easter Egg settings.");
      return json({ ok: true, action: "config_get", settings: normalizeSettings(data) });
    }

    if (body.action !== "config_set_global" && body.action !== "config_set_id") {
      return json({ error: "Unknown action." }, 400);
    }

    const authHeader = req.headers.get("Authorization") ?? "";
    const match = authHeader.match(/^Bearer\s+(.+)$/i);
    if (!match) return json({ error: "A valid Founder Alpha Account session is required." }, 401);

    const authClient = createClient(url, publishableKey, {
      auth: { autoRefreshToken: false, persistSession: false },
      global: { headers: { Authorization: `Bearer ${match[1]}` } },
    });
    const { data: authData, error: authError } = await authClient.auth.getUser(match[1]);
    const user = authData.user;
    if (authError || !user) return json({ error: "Alpha Account session is invalid or expired." }, 401);

    const { data: actor, error: actorError } = await admin.from("profiles")
      .select("id, username").eq("id", user.id).maybeSingle();
    if (actorError || !actor) return json({ error: "No Alpha community profile is linked to this account." }, 403);

    const { data: founder, error: founderError } = await admin.from("developer_admins")
      .select("user_id").eq("user_id", user.id).maybeSingle();
    if (founderError) throw new Error("Could not verify Founder authorization.");
    if (!founder) return json({ error: "FOUNDER-ONLY: only the authenticated Founder can change global Easter Egg settings." }, 403);

    const { data: current, error: currentError } = await admin.from("client_easter_egg_settings")
      .select("globally_enabled, master_enabled").eq("singleton", true).single();
    if (currentError || !current) throw new Error("Could not load the current global Easter Egg settings.");

    let globallyEnabled = current.globally_enabled === true;
    const masterEnabled = current.master_enabled && typeof current.master_enabled === "object"
      ? { ...(current.master_enabled as Record<string, boolean>) }
      : {};

    let auditAction = "";
    let auditDetails: Record<string, unknown> = {};
    if (body.action === "config_set_global") {
      if (typeof body.enabled !== "boolean") return json({ error: "enabled must be a boolean." }, 400);
      globallyEnabled = body.enabled;
      auditAction = "global_toggle_changed";
      auditDetails = { globally_enabled: globallyEnabled };
    } else {
      if (!Number.isInteger(body.egg_id) || (body.egg_id as number) < 1 || (body.egg_id as number) > 25) {
        return json({ error: "Easter Egg ID must be between 1 and 25." }, 400);
      }
      if (typeof body.enabled !== "boolean") return json({ error: "enabled must be a boolean." }, 400);
      const id = body.egg_id as number;
      masterEnabled[String(id)] = body.enabled;
      auditAction = "global_id_permission_changed";
      auditDetails = { egg_id: id, enabled: body.enabled };
    }

    const { data: updated, error: updateError } = await admin.from("client_easter_egg_settings")
      .update({
        globally_enabled: globallyEnabled,
        master_enabled: masterEnabled,
        updated_at: new Date().toISOString(),
        updated_by: user.id,
      })
      .eq("singleton", true)
      .select("globally_enabled, master_enabled, updated_at")
      .single();
    if (updateError || !updated) throw new Error("Could not save the global Easter Egg settings.");

    const { error: auditError } = await admin.from("audit_logs").insert({
      actor_id: user.id,
      actor_label: actor.username,
      product: "client",
      category: "easter_eggs",
      action: auditAction,
      outcome: "success",
      details: auditDetails,
    });
    if (auditError) console.error("Could not write Easter Egg settings audit entry:", auditError.message);

    return json({
      ok: true,
      action: body.action,
      settings: normalizeSettings(updated),
    });
  } catch (error) {
    console.error("Easter Egg settings request failed:", error);
    return json({ error: error instanceof Error ? error.message : "Easter Egg settings request failed." }, 500);
  }
});