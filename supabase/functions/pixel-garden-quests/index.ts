import { createClient } from "npm:@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

function reply(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

function cleanText(value: unknown, max: number): string {
  return typeof value === "string" ? value.trim().slice(0, max) : "";
}

function validAccountName(value: string): boolean {
  return /^[A-Za-z0-9_]{1,16}$/.test(value);
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return reply({ error: "POST required." }, 405);

  try {
    const authHeader = req.headers.get("Authorization") ?? "";
    const token = authHeader.replace(/^Bearer\s+/i, "");
    if (!token) return reply({ error: "Sign in to your Alpha Account first." }, 401);

    const url = Deno.env.get("SUPABASE_URL") ?? "";
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    if (!url || !anonKey || !serviceKey) throw new Error("Server auth configuration is incomplete.");

    const authClient = createClient(url, anonKey, {
      global: { headers: { Authorization: `Bearer ${token}` } },
      auth: { persistSession: false, autoRefreshToken: false },
    });
    const { data: authData, error: authError } = await authClient.auth.getUser(token);
    if (authError || !authData.user) return reply({ error: "Your Alpha Account session is invalid." }, 401);
    const userId = authData.user.id;
    const admin = createClient(url, serviceKey, {
      auth: { persistSession: false, autoRefreshToken: false },
    });

    const { data: profile, error: profileError } = await admin
      .from("profiles").select("id,username,is_founder").eq("id", userId).maybeSingle();
    if (profileError || !profile) return reply({ error: "Alpha community profile not found." }, 403);

    const input = await req.json();
    const action = cleanText(input.action, 32).toLowerCase();
    const { data: founderRecord, error: founderError } = await admin.from("developer_admins")
      .select("user_id").eq("user_id", userId).maybeSingle();
    if (founderError) throw new Error("Could not verify Founder authorization.");
    const isFounder = !!founderRecord;
    const founderActions = new Set(["create", "give", "take", "complete", "reward_add", "reward_remove"]);
    if (founderActions.has(action) && !isFounder) {
      return reply({ error: "Access denied: this action is restricted to Founder." }, 403);
    }

    if (action === "list") {
      const { data, error } = await admin.from("pixel_garden_quest_catalog")
        .select("quest_id,definition").order("created_at", { ascending: true });
      if (error) throw error;
      return reply({ definitions: (data ?? []).map((row) => row.definition) });
    }

    if (action === "create") {
      const id = cleanText(input.quest_id, 64).toLowerCase();
      const description = cleanText(input.description, 240);
      const npc = cleanText(input.npc, 64);
      const amount = Number(input.amount);
      const rewardType = cleanText(input.reward_type, 32).toLowerCase();
      if (!/^[a-z][a-z0-9_]{2,63}$/.test(id)) return reply({ error: "Quest ID must use 3-64 lowercase letters, numbers or underscores." }, 400);
      if (!description || !npc || !Number.isInteger(amount) || amount < 1 || amount > 1000000) {
        return reply({ error: "Description, NPC and a positive whole-number amount are required." }, 400);
      }
      if (!/^(coins|seeds_(wheat|carrot|potato|pumpkin)|crop_(wheat|carrot|potato|pumpkin))$/.test(rewardType)) {
        return reply({ error: "Reward type must be coins, seeds_<crop>, or crop_<crop>." }, 400);
      }
      const definition = {
        id, name: id.replace(/_/g, " ").replace(/\b\w/g, (s: string) => s.toUpperCase()),
        description: description + " (NPC: " + npc + ")", npc, required_level: 1, required_prestige: 0, tier: "NPC",
        objectives: [{ id: id + "_objective", type: "DELIVER_CROP", target: "*", required_amount: amount }],
        rewards: { [rewardType]: amount },
      };
      const { error } = await admin.from("pixel_garden_quest_catalog")
        .insert({ quest_id: id, definition, created_by: userId });
      if (error) {
        if (error.code === "23505") return reply({ error: "A quest with that ID already exists." }, 409);
        throw error;
      }
      return reply({ ok: true, definition });
    }

    const questId = cleanText(input.quest_id, 64).toLowerCase();
    if (!/^[a-z][a-z0-9_]{2,63}$/.test(questId)) return reply({ error: "Invalid quest ID." }, 400);
    const { data: questRow, error: questError } = await admin.from("pixel_garden_quest_catalog")
      .select("definition").eq("quest_id", questId).maybeSingle();
    if (questError) throw questError;
    if (!questRow) return reply({ error: "Quest not found." }, 404);

    if (action === "reward_add" || action === "reward_remove") {
      const rewardType = cleanText(input.reward_type, 32).toLowerCase();
      const amount = Number(input.amount);
      if (!/^(coins|seeds_(wheat|carrot|potato|pumpkin)|crop_(wheat|carrot|potato|pumpkin))$/.test(rewardType)) {
        return reply({ error: "Unsupported reward type." }, 400);
      }
      const definition = structuredClone(questRow.definition) as Record<string, unknown>;
      const rewards = (definition.rewards && typeof definition.rewards === "object" ? definition.rewards : {}) as Record<string, number>;
      if (action === "reward_add") {
        if (!Number.isInteger(amount) || amount < 1 || amount > 1000000) return reply({ error: "Reward amount must be a positive whole number." }, 400);
        rewards[rewardType] = Math.min(1000000, (Number(rewards[rewardType]) || 0) + amount);
      } else {
        if (input.all === true) delete rewards[rewardType];
        else {
          if (!Number.isInteger(amount) || amount < 1) return reply({ error: "Reward amount must be positive, or use all=true." }, 400);
          const remaining = (Number(rewards[rewardType]) || 0) - amount;
          if (remaining > 0) rewards[rewardType] = remaining; else delete rewards[rewardType];
        }
      }
      definition.rewards = rewards;
      const { error } = await admin.from("pixel_garden_quest_catalog")
        .update({ definition, updated_at: new Date().toISOString() }).eq("quest_id", questId);
      if (error) throw error;
      return reply({ ok: true, definition });
    }

    const targetName = cleanText(input.target_alpha_name, 16);
    if (!validAccountName(targetName)) return reply({ error: "Enter a valid Alpha Account name." }, 400);
    const { data: target, error: targetError } = await admin.from("profiles")
      .select("id,username").eq("username", targetName.toLowerCase()).maybeSingle();
    if (targetError) throw targetError;
    if (!target) return reply({ error: "Target Alpha Account was not found." }, 404);
    const { data: saveRow, error: saveError } = await admin.from("pixel_garden_saves")
      .select("save_data").eq("user_id", target.id).maybeSingle();
    if (saveError) throw saveError;
    const save = saveRow?.save_data && typeof saveRow.save_data === "object"
      ? structuredClone(saveRow.save_data) as Record<string, unknown>
      : {
          version: 2, pixelGardenUpgradeCoins: 100, auto_harvest: false,
          growth_levels: 0, more_coins_level: 0, more_crops_level: 0,
          reset_tokens: 0, purchased_upgrade_steps: 0, unlocked_plots: 1,
          last_processed_at: Date.now(), seeds: [3, 0, 0, 0], inventory: [0, 0, 0, 0],
          plots: [], progression: {},
        } as Record<string, unknown>;
    const progression = (save.progression && typeof save.progression === "object" ? save.progression : {}) as Record<string, unknown>;
    const definitions = Array.isArray(progression.quest_definitions) ? progression.quest_definitions as Record<string, unknown>[] : [];
    const instances = Array.isArray(progression.quest_instances) ? progression.quest_instances as Record<string, unknown>[] : [];
    const history = Array.isArray(progression.quest_history) ? progression.quest_history as Record<string, unknown>[] : [];
    const existingDefinition = definitions.some((item) => item.id === questId);
    if (!existingDefinition) definitions.push(questRow.definition);

    if (action === "give") {
      if (instances.some((item) => item.definition_id === questId && ["AVAILABLE", "ACTIVE"].includes(String(item.status)))) {
        return reply({ error: "That account already has this quest assigned and unfinished." }, 409);
      }
      const definition = questRow.definition as Record<string, unknown>;
      const objectives = Array.isArray(definition.objectives) ? definition.objectives as Record<string, unknown>[] : [];
      const objectiveProgress: Record<string, number> = {};
      for (const objective of objectives) objectiveProgress[String(objective.id)] = 0;
      instances.push({
        instance_id: crypto.randomUUID(), definition_id: questId, status: "AVAILABLE",
        assigned_at: Date.now(), accepted_at: 0, finished_at: 0, rewards_claimed: false,
        objective_progress: objectiveProgress,
      });
    } else if (action === "take") {
      const before = instances.length;
      const kept = instances.filter((item) => !(item.definition_id === questId && ["AVAILABLE", "ACTIVE"].includes(String(item.status))));
      if (kept.length === before) return reply({ error: "No active or available instance of that quest was found for this account." }, 404);
      instances.splice(0, instances.length, ...kept);
    } else if (action === "complete") {
      const instance = [...instances].reverse().find((item) => item.definition_id === questId && item.status === "ACTIVE");
      if (!instance) return reply({ error: "No active instance of that quest was found for this account." }, 404);
      const definition = definitions.find((item) => item.id === questId) as Record<string, unknown> | undefined;
      const objectives = Array.isArray(definition?.objectives) ? definition!.objectives as Record<string, unknown>[] : [];
      const progress = (instance.objective_progress && typeof instance.objective_progress === "object" ? instance.objective_progress : {}) as Record<string, number>;
      if (objectives.some((objective) => (Number(progress[String(objective.id)]) || 0) < Number(objective.required_amount))) {
        return reply({ error: "Quest objectives are not complete yet." }, 409);
      }
      instance.status = "COMPLETED";
      instance.finished_at = Date.now();
      history.push({ instance_id: instance.instance_id, definition_id: questId, outcome: "COMPLETED", timestamp: Date.now() });
    } else {
      return reply({ error: "Unknown quest action." }, 400);
    }

    progression.quest_definitions = definitions;
    progression.quest_instances = instances;
    progression.quest_history = history;
    save.progression = progression;
    const { error: updateError } = await admin.from("pixel_garden_saves")
      .upsert({ user_id: target.id, save_data: save, saved_at: new Date().toISOString() }, { onConflict: "user_id" });
    if (updateError) throw updateError;
    return reply({ ok: true, target_alpha_name: target.username, quest_id: questId, action });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown server error.";
    return reply({ error: message }, 400);
  }
});