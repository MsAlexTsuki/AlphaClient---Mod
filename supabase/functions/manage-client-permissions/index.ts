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

const normalizeRank = (value: string) => {
  const key = value.trim().toLowerCase().replace(/[\s-]+/g, "_");
  const aliases: Record<string, string> = {
    tmod: "trial_moderator", trial_mod: "trial_moderator",
    mod: "moderator", srmod: "senior_moderator", sr_mod: "senior_moderator",
    sradmin: "senior_admin", sr_admin: "senior_admin",
    platform_admin: "plat_admin", platformadmin: "plat_admin",
  };
  return aliases[key] ?? key;
};

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
    return json({ error: "Permission service is not configured." }, 500);
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
      .from("profiles").select("id, username, is_founder, rank_key").eq("id", user.id).maybeSingle();
    if (actorError || !actor) return json({ error: "No Alpha community profile is linked to this account." }, 403);

    const { data: actorRanks } = await admin.from("profile_ranks")
      .select("rank_key").eq("profile_id", user.id);
    const isFounder = actor.is_founder === true || actor.rank_key === "founder"
      || (actorRanks ?? []).some((row: { rank_key: string }) => row.rank_key === "founder");

    const body = await req.json().catch(() => null) as Record<string, unknown> | null;
    if (!body || typeof body.action !== "string") return json({ error: "Missing action." }, 400);
    const action = body.action;

    const { data: catalog, error: catalogError } = await admin.from("client_command_permission_catalog")
      .select("permission_key, display_name, category, description").order("category").order("permission_key");
    if (catalogError || !catalog) throw new Error("Could not load the command permission catalog.");

    const permissionKeys = new Set(catalog.map((item: { permission_key: string }) => item.permission_key));
    const getTargetProfile = async (username: unknown) => {
      if (typeof username !== "string" || !username.trim()) throw new Error("Provide an Alpha account name.");
      const { data, error } = await admin.from("profiles").select("id, username, is_founder")
        .eq("username", username.trim().toLowerCase()).maybeSingle();
      if (error) throw new Error("Could not look up the Alpha account.");
      if (!data) throw new Error(`Alpha account not found: ${username}`);
      return data;
    };
    const getRank = async (value: unknown) => {
      if (typeof value !== "string" || !value.trim()) throw new Error("Provide a rank key.");
      const rankKey = normalizeRank(value);
      const { data, error } = await admin.from("community_ranks")
        .select("rank_key, rank_name").eq("rank_key", rankKey).maybeSingle();
      if (error || !data) throw new Error(`Unknown rank: ${value}`);
      return data;
    };
    const getPermission = (value: unknown) => {
      if (typeof value !== "string" || !permissionKeys.has(value)) {
        throw new Error("Unknown permission. Use /alphaperms list to see valid permission IDs.");
      }
      return value;
    };
    const writeAudit = async (actionName: string, targetId: string | null, targetLabel: string | null, details: Record<string, unknown>) => {
      const { error } = await admin.from("audit_logs").insert({
        actor_id: user.id,
        actor_label: actor.username,
        product: "client",
        category: "command_permissions",
        action: actionName,
        target_user_id: targetId,
        target_label: targetLabel,
        outcome: "success",
        details,
      });
      if (error) console.error("Could not write permission audit entry:", error.message);
    };

    if (action === "my_permissions") {
      const { data: profile, error: profileError } = await admin.from("profiles")
        .select("id, username, rank_key").eq("id", user.id).single();
      if (profileError || !profile) throw new Error("Could not load your Alpha profile.");
      const { data: assignedRows } = await admin.from("profile_ranks").select("rank_key").eq("profile_id", user.id);
      const ranks = new Set<string>([profile.rank_key, ...(assignedRows ?? []).map((row: { rank_key: string }) => row.rank_key)].filter(Boolean));
      const [{ data: overrides, error: overrideError }, { data: rankRows, error: rankError }] = await Promise.all([
        admin.from("client_command_user_overrides").select("permission_key, effect").eq("profile_id", user.id),
        admin.from("client_command_rank_permissions").select("rank_key, permission_key, enabled").in("rank_key", [...ranks]),
      ]);
      if (overrideError || rankError) throw new Error("Could not load command permissions.");
      const overrideMap = new Map((overrides ?? []).map((row: { permission_key: string; effect: string }) => [row.permission_key, row.effect]));
      const grouped = new Map<string, { enabled: boolean; rankKeys: string[] }>();
      for (const row of rankRows ?? []) {
        const entry = grouped.get(row.permission_key) ?? { enabled: false, rankKeys: [] };
        entry.enabled = entry.enabled || row.enabled === true;
        if (row.enabled === true) entry.rankKeys.push(row.rank_key);
        grouped.set(row.permission_key, entry);
      }
      const { data: moduleRows, error: moduleError } = await authClient.rpc("get_my_client_mod_permissions");
      if (moduleError) throw new Error("Could not load module entitlements.");
      const modulesById = new Map((moduleRows ?? []).map((row: { mod_id: string; allowed: boolean; access_source: string }) => [row.mod_id, row]));
      const permissions = catalog.map((item: { permission_key: string; display_name: string; category: string; description: string }) => {
        if (item.permission_key.startsWith("mod.")) {
          const modId = item.permission_key.substring(4);
          const moduleRow = modulesById.get(modId) as { allowed: boolean; access_source: string } | undefined;
          return { ...item, configured: true, allowed: moduleRow?.allowed === true, source: moduleRow?.access_source ?? "default" };
        }
        const override = overrideMap.get(item.permission_key);
        const rankEntry = grouped.get(item.permission_key);
        if (override !== undefined) return { ...item, configured: true, allowed: override === "allow", source: "account" };
        if (rankEntry) return { ...item, configured: true, allowed: rankEntry.enabled, source: "rank", rank_keys: rankEntry.rankKeys };
        return { ...item, configured: false, allowed: false, source: "default" };
      });
      return json({ ok: true, username: profile.username, permissions });
    }

    if (!isFounder) return json({ error: "FOUNDER-ONLY: only the authenticated Founder can manage command permissions." }, 403);

    if (action === "list") {
      if (body.target === "catalog") {
        return json({ ok: true, target: "catalog", permissions: catalog.map((item: Record<string, unknown>) => ({
          ...item, configured: false, allowed: false, source: "catalog",
        })) });
      }
      if (body.target === "rank") {
        const rank = await getRank(body.rank);
        const { data: rows, error } = await admin.from("client_command_rank_permissions")
          .select("permission_key, enabled, updated_at").eq("rank_key", rank.rank_key);
        if (error) throw new Error("Could not load rank permissions.");
        const byKey = new Map((rows ?? []).map((row: { permission_key: string; enabled: boolean; updated_at: string }) => [row.permission_key, row]));
        const { data: moduleRows, error: moduleError } = await admin.from("client_mod_rank_permissions")
          .select("mod_id, enabled, updated_at").eq("rank_key", rank.rank_key);
        if (moduleError) throw new Error("Could not load rank module entitlements.");
        const modulesById = new Map((moduleRows ?? []).map((row: { mod_id: string; enabled: boolean; updated_at: string }) => [row.mod_id, row]));
        return json({ ok: true, target: "rank", rank, permissions: catalog.map((item: Record<string, unknown>) => {
          const key = item.permission_key as string;
          if (key.startsWith("mod.")) {
            const row = modulesById.get(key.substring(4)) as { enabled: boolean; updated_at: string } | undefined;
            const founderOrOwnerDefault = rank.rank_key === "founder" || rank.rank_key === "owner";
            return { ...item, configured: !!row || founderOrOwnerDefault, allowed: founderOrOwnerDefault || (row?.enabled ?? false), source: "rank", updated_at: row?.updated_at ?? null };
          }
          const row = byKey.get(key) as { enabled: boolean; updated_at: string } | undefined;
          return { ...item, configured: !!row, allowed: row?.enabled ?? false, updated_at: row?.updated_at ?? null };
        }) });
      }
      const target = await getTargetProfile(body.username);
      const [{ data: overrides, error: overrideError }, { data: assignedRows }, { data: legacyRank }] = await Promise.all([
        admin.from("client_command_user_overrides").select("permission_key, effect, updated_at").eq("profile_id", target.id),
        admin.from("profile_ranks").select("rank_key").eq("profile_id", target.id),
        admin.from("profiles").select("rank_key, is_founder").eq("id", target.id).single(),
      ]);
      if (overrideError || !legacyRank) throw new Error("Could not load account permissions.");
      const ranks = new Set<string>([legacyRank.rank_key, ...(assignedRows ?? []).map((row: { rank_key: string }) => row.rank_key)].filter(Boolean));
      const { data: rankRows, error: rankError } = await admin.from("client_command_rank_permissions")
        .select("rank_key, permission_key, enabled").in("rank_key", [...ranks]);
      if (rankError) throw new Error("Could not load rank defaults.");
      const overrideMap = new Map((overrides ?? []).map((row: { permission_key: string; effect: string; updated_at: string }) => [row.permission_key, row]));
      const rankMap = new Map<string, { enabled: boolean; ranks: string[] }>();
      for (const row of rankRows ?? []) {
        const entry = rankMap.get(row.permission_key) ?? { enabled: false, ranks: [] };
        entry.enabled = entry.enabled || row.enabled === true;
        if (row.enabled) entry.ranks.push(row.rank_key);
        rankMap.set(row.permission_key, entry);
      }
      const [{ data: moduleRows, error: moduleError }, { data: userModuleRows, error: userModuleError }, { data: categoryRows, error: categoryError }, { data: allModules, error: allModulesError }] = await Promise.all([
        admin.from("client_mod_rank_permissions").select("rank_key, mod_id, enabled").in("rank_key", [...ranks]),
        admin.from("client_mod_user_overrides").select("mod_id, effect, expires_at, revoked_at, updated_at:created_at").eq("user_id", target.id).is("revoked_at", null),
        admin.from("client_mod_category_grants").select("category_key, expires_at, revoked_at").eq("user_id", target.id).is("revoked_at", null),
        admin.from("client_mods").select("mod_id, category_key, enabled"),
      ]);
      if (moduleError || userModuleError || categoryError || allModulesError) throw new Error("Could not load account module permissions.");
      const moduleRankMap = new Map<string, { enabled: boolean; ranks: string[] }>();
      for (const row of moduleRows ?? []) {
        const entry = moduleRankMap.get(row.mod_id) ?? { enabled: false, ranks: [] };
        entry.enabled = entry.enabled || row.enabled === true;
        if (row.enabled) entry.ranks.push(row.rank_key);
        moduleRankMap.set(row.mod_id, entry);
      }
      const moduleOverrideMap = new Map<string, { effect: string; expires_at: string | null; updated_at: string }>();
      for (const row of userModuleRows ?? []) {
        if (row.expires_at && Date.parse(row.expires_at) <= Date.now()) continue;
        moduleOverrideMap.set(row.mod_id, row);
      }
      const activeCategories = new Set((categoryRows ?? []).filter((row: { expires_at: string | null }) =>
        !row.expires_at || Date.parse(row.expires_at) > Date.now()).map((row: { category_key: string }) => row.category_key));
      const moduleCatalog = new Map((allModules ?? []).map((row: { mod_id: string; category_key: string; enabled: boolean }) => [row.mod_id, row]));
      const targetIsFounderOrOwner = legacyRank.is_founder === true || ranks.has("founder") || ranks.has("owner") || ranks.has("founder");
      const permissions = catalog.map((item: Record<string, unknown>) => {
        const key = item.permission_key as string;
        if (key.startsWith("mod.")) {
          const modId = key.substring(4);
          const mod = moduleCatalog.get(modId) as { category_key: string; enabled: boolean } | undefined;
          const override = moduleOverrideMap.get(modId);
          const rankEntry = moduleRankMap.get(modId);
          let allowed = false;
          let configured = false;
          let source = "default";
          if (!mod || mod.enabled !== true) {
            configured = true;
            source = "disabled";
          } else if (targetIsFounderOrOwner) {
            allowed = true;
            configured = true;
            source = "rank";
          } else if (override) {
            allowed = override.effect === "allow";
            configured = true;
            source = "account";
          } else if (activeCategories.has(mod.category_key)) {
            allowed = true;
            configured = true;
            source = "category";
          } else if (rankEntry) {
            allowed = rankEntry.enabled;
            configured = true;
            source = "rank";
          }
          return { ...item, configured, allowed, source, rank_keys: rankEntry?.ranks };
        }
        const override = overrideMap.get(key) as { effect: string; updated_at: string } | undefined;
        const rankEntry = rankMap.get(key);
        if (override) return { ...item, configured: true, allowed: override.effect === "allow", source: "account", updated_at: override.updated_at };
        if (rankEntry) return { ...item, configured: true, allowed: rankEntry.enabled, source: "rank", rank_keys: rankEntry.ranks };
        return { ...item, configured: false, allowed: false, source: "default" };
      });
      return json({ ok: true, target: "account", username: target.username, permissions });
    }

    if (action === "grant_user" || action === "revoke_user" || action === "clear_user") {
      const target = await getTargetProfile(body.username);
      const permission = getPermission(body.permission);
      if (permission === "staffmods.founder" && target.id !== user.id) {
        return json({ error: "Founder-folder permission is private to the Founder account and cannot be granted to other accounts." }, 403);
      }
      if (target.id === user.id && permission === "staffmods.founder" && action === "revoke_user") {
        return json({ error: "The Founder account cannot revoke its own Founder-folder permission here." }, 400);
      }
      if (permission.startsWith("mod.")) {
        const modId = permission.substring(4);
        const { data: module, error: moduleError } = await admin.from("client_mods").select("mod_id").eq("mod_id", modId).maybeSingle();
        if (moduleError || !module) throw new Error("Unknown module entitlement.");
        if (action === "clear_user") {
          const { error } = await admin.from("client_mod_user_overrides").update({
            revoked_at: new Date().toISOString(), revoked_by: user.id,
          }).eq("user_id", target.id).eq("mod_id", modId).is("revoked_at", null);
          if (error) throw new Error("Could not clear module override.");
        } else {
          const effect = action === "grant_user" ? "allow" : "deny";
          const { data: activeRows, error: activeError } = await admin.from("client_mod_user_overrides")
            .select("id").eq("user_id", target.id).eq("mod_id", modId).is("revoked_at", null);
          if (activeError) throw new Error("Could not inspect existing module overrides.");
          const values = { effect, expires_at: null, reason: "Managed via /alphaperms", granted_by: user.id, revoked_at: null, revoked_by: null };
          if (activeRows && activeRows.length > 0) {
            const { error } = await admin.from("client_mod_user_overrides").update(values)
              .eq("user_id", target.id).eq("mod_id", modId).is("revoked_at", null);
            if (error) throw new Error("Could not update module override.");
          } else {
            const { error } = await admin.from("client_mod_user_overrides").insert({ user_id: target.id, mod_id: modId, ...values });
            if (error) throw new Error("Could not create module override.");
          }
        }
      } else if (action === "clear_user") {
        const { error } = await admin.from("client_command_user_overrides").delete()
          .eq("profile_id", target.id).eq("permission_key", permission);
        if (error) throw new Error("Could not clear account override.");
      } else {
        const effect = action === "grant_user" ? "allow" : "deny";
        const { error } = await admin.from("client_command_user_overrides").upsert({
          profile_id: target.id, permission_key: permission, effect, updated_by: user.id, updated_at: new Date().toISOString(),
        }, { onConflict: "profile_id,permission_key" });
        if (error) throw new Error("Could not save account permission.");
      }
      await writeAudit(action, target.id, target.username, { permission_key: permission });
      return json({ ok: true, action, username: target.username, permission });
    }

    if (action === "grant_rank" || action === "revoke_rank") {
      const rank = await getRank(body.rank);
      const permission = getPermission(body.permission);
      if (permission === "staffmods.founder" && rank.rank_key !== "founder") {
        return json({ error: "Founder-folder permission cannot be assigned to any non-Founder rank." }, 403);
      }
      if (rank.rank_key === "founder" && action === "revoke_rank" && permission === "staffmods.founder") {
        return json({ error: "The Founder rank must retain its Founder-folder permission." }, 400);
      }
      if (permission.startsWith("mod.")) {
        const modId = permission.substring(4);
        const { data: module, error: moduleError } = await admin.from("client_mods").select("mod_id").eq("mod_id", modId).maybeSingle();
        if (moduleError || !module) throw new Error("Unknown module entitlement.");
        const { error } = await admin.from("client_mod_rank_permissions").upsert({
          rank_key: rank.rank_key, mod_id: modId, enabled: action === "grant_rank",
          updated_by: user.id, updated_at: new Date().toISOString(),
        }, { onConflict: "rank_key,mod_id" });
        if (error) throw new Error("Could not save rank module entitlement.");
      } else {
        const { error } = await admin.from("client_command_rank_permissions").upsert({
          rank_key: rank.rank_key, permission_key: permission, enabled: action === "grant_rank",
          updated_by: user.id, updated_at: new Date().toISOString(),
        }, { onConflict: "rank_key,permission_key" });
        if (error) throw new Error("Could not save rank permission.");
      }
      await writeAudit(action, null, rank.rank_name, { rank_key: rank.rank_key, permission_key: permission });
      return json({ ok: true, action, rank: rank.rank_name, permission });
    }

    return json({ error: "Unknown action. Use my_permissions, list, grant_user, revoke_user, clear_user, grant_rank, or revoke_rank." }, 400);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Permission request failed.";
    return json({ error: message }, 400);
  }
});
