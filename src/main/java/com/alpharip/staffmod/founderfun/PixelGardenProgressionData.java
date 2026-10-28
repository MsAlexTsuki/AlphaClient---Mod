// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent foundation for Pixel Garden progression and quests.
 *
 * XP earning, prestige ownership, reward granting, NPC spawning and inventory
 * transactions are intentionally handled elsewhere and are not implemented here.
 */
public final class PixelGardenProgressionData {
    public static final int SAVE_VERSION = 1;
    public static final int MAX_ACTIVE_QUESTS = 5;

    public enum QuestStatus { AVAILABLE, ACTIVE, COMPLETED, ABANDONED }
    public enum GoalType { MAIN, SIDE, DAILY }

    private int level = 1;
    private long experience;
    private int storylineChapter = 1;
    private int tutorialStep;
    private long nextDailyResetAt;
    private final Map<String, QuestDefinition> definitions = new LinkedHashMap<String, QuestDefinition>();
    private final Map<String, QuestInstance> instances = new LinkedHashMap<String, QuestInstance>();
    private final Map<String, GoalProgress> goals = new LinkedHashMap<String, GoalProgress>();
    private final List<HistoryEntry> history = new ArrayList<HistoryEntry>();
    private final List<String> recentRotationIds = new ArrayList<String>();

    public synchronized int getLevel() { return level; }
    public synchronized long getExperience() { return experience; }
    public synchronized int getStorylineChapter() { return storylineChapter; }
    public synchronized int getTutorialStep() { return tutorialStep; }
    public synchronized long getNextDailyResetAt() { return nextDailyResetAt; }

    /** Stores progression values without defining how XP is earned. */
    public synchronized void setProgression(int levelValue, long experienceValue) {
        level = Math.max(1, levelValue);
        experience = Math.max(0L, experienceValue);
    }

    public synchronized void setStorylineProgress(int chapter, int step) {
        storylineChapter = Math.max(1, chapter);
        tutorialStep = Math.max(0, step);
    }

    public synchronized void setNextDailyResetAt(long timestamp) {
        nextDailyResetAt = Math.max(0L, timestamp);
    }

    public synchronized boolean addDefinition(QuestDefinition definition) {
        if (definition == null || !definition.isValid() || definitions.containsKey(definition.id)) return false;
        definitions.put(definition.id, definition.copy());
        return true;
    }

    public synchronized boolean replaceDefinition(QuestDefinition definition) {
        if (definition == null || !definition.isValid()) return false;
        if (!definitions.containsKey(definition.id)) return false;
        definitions.put(definition.id, definition.copy());
        return true;
    }

    public synchronized QuestDefinition getDefinition(String id) {
        QuestDefinition value = definitions.get(id);
        return value == null ? null : value.copy();
    }

    public synchronized List<QuestDefinition> getDefinitions() {
        List<QuestDefinition> result = new ArrayList<QuestDefinition>();
        for (QuestDefinition value : definitions.values()) result.add(value.copy());
        return Collections.unmodifiableList(result);
    }

    /** Assigns a definition to this player's save as an AVAILABLE quest instance. */
    public synchronized String assignQuest(String definitionId) {
        if (definitionId == null || !definitions.containsKey(definitionId)) return null;
        String instanceId = UUID.randomUUID().toString();
        QuestInstance instance = new QuestInstance(instanceId, definitionId, QuestStatus.AVAILABLE,
                System.currentTimeMillis());
        QuestDefinition definition = definitions.get(definitionId);
        for (Objective objective : definition.objectives) {
            instance.objectiveProgress.put(objective.id, Integer.valueOf(0));
        }
        instances.put(instanceId, instance);
        return instanceId;
    }

    public synchronized QuestInstance getInstance(String instanceId) {
        QuestInstance value = instances.get(instanceId);
        return value == null ? null : value.copy();
    }

    public synchronized List<QuestInstance> getInstances() {
        List<QuestInstance> result = new ArrayList<QuestInstance>();
        for (QuestInstance value : instances.values()) result.add(value.copy());
        return Collections.unmodifiableList(result);
    }

    /**
     * Records progress after the caller has validated the underlying game action.
     * Delivery callers must consume inventory successfully before recording progress.
     */
    public synchronized boolean setObjectiveProgress(String instanceId, String objectiveId, int amount) {
        QuestInstance instance = instances.get(instanceId);
        if (instance == null || instance.status != QuestStatus.ACTIVE) return false;
        QuestDefinition definition = definitions.get(instance.definitionId);
        if (definition == null) return false;
        for (Objective objective : definition.objectives) {
            if (objective.id.equals(objectiveId)) {
                instance.objectiveProgress.put(objectiveId,
                        Integer.valueOf(Math.max(0, Math.min(objective.requiredAmount, amount))));
                return true;
            }
        }
        return false;
    }

    /** Completes an active quest only after every configured objective is complete. */
    public synchronized boolean completeQuest(String instanceId) {
        QuestInstance instance = instances.get(instanceId);
        if (instance == null || instance.status != QuestStatus.ACTIVE) return false;
        QuestDefinition definition = definitions.get(instance.definitionId);
        if (definition == null || definition.objectives.isEmpty()) return false;
        for (Objective objective : definition.objectives) {
            if (instance.getObjectiveProgress(objective.id) < objective.requiredAmount) return false;
        }
        instance.status = QuestStatus.COMPLETED;
        instance.finishedAt = System.currentTimeMillis();
        history.add(new HistoryEntry(instance.instanceId, instance.definitionId,
                QuestStatus.COMPLETED, instance.finishedAt));
        return true;
    }

    /**
     * Marks rewards as claimed only after the reward transaction succeeds.
     * Actual reward delivery and durable transaction guarantees are implemented separately.
     */
    public synchronized boolean markRewardsClaimed(String instanceId) {
        QuestInstance instance = instances.get(instanceId);
        if (instance == null || instance.status != QuestStatus.COMPLETED || instance.rewardsClaimed) return false;
        instance.rewardsClaimed = true;
        return true;
    }

    public synchronized int getActiveQuestCount() {
        int count = 0;
        for (QuestInstance instance : instances.values()) {
            if (instance.status == QuestStatus.ACTIVE) count++;
        }
        return count;
    }

    /** Checks both level and prestige; prestige is supplied by the existing prestige system. */
    public synchronized boolean isEligible(String instanceId, int playerLevel, int playerPrestige) {
        QuestInstance instance = instances.get(instanceId);
        if (instance == null) return false;
        QuestDefinition definition = definitions.get(instance.definitionId);
        return definition != null && playerLevel >= definition.requiredLevel
                && playerPrestige >= definition.requiredPrestige;
    }

    public synchronized boolean acceptQuest(String instanceId, int playerLevel, int playerPrestige) {
        QuestInstance instance = instances.get(instanceId);
        if (instance == null || instance.status != QuestStatus.AVAILABLE
                || getActiveQuestCount() >= MAX_ACTIVE_QUESTS
                || !isEligible(instanceId, playerLevel, playerPrestige)) return false;
        instance.status = QuestStatus.ACTIVE;
        instance.acceptedAt = System.currentTimeMillis();
        return true;
    }

    public synchronized boolean abandonQuest(String instanceId) {
        QuestInstance instance = instances.get(instanceId);
        if (instance == null || instance.status != QuestStatus.ACTIVE) return false;
        instance.status = QuestStatus.ABANDONED;
        instance.finishedAt = System.currentTimeMillis();
        history.add(new HistoryEntry(instance.instanceId, instance.definitionId,
                QuestStatus.ABANDONED, instance.finishedAt));
        return true;
    }

    public synchronized void putGoal(GoalProgress goal) {
        if (goal != null && goal.id != null && !goal.id.trim().isEmpty()) goals.put(goal.id, goal.copy());
    }

    public synchronized List<GoalProgress> getGoals() {
        List<GoalProgress> result = new ArrayList<GoalProgress>();
        for (GoalProgress value : goals.values()) result.add(value.copy());
        return Collections.unmodifiableList(result);
    }

    public synchronized void recordRotation(String definitionId, int maxHistory) {
        if (definitionId == null || definitionId.trim().isEmpty()) return;
        recentRotationIds.remove(definitionId);
        recentRotationIds.add(definitionId);
        while (recentRotationIds.size() > Math.max(1, maxHistory)) {
            recentRotationIds.remove(0);
        }
    }

    public synchronized List<String> getRecentRotationIds() {
        return Collections.unmodifiableList(new ArrayList<String>(recentRotationIds));
    }

    public synchronized List<HistoryEntry> getHistory() {
        List<HistoryEntry> result = new ArrayList<HistoryEntry>();
        for (HistoryEntry entry : history) result.add(entry.copy());
        return Collections.unmodifiableList(result);
    }

    public synchronized void reset() {
        level = 1;
        experience = 0L;
        storylineChapter = 1;
        tutorialStep = 0;
        nextDailyResetAt = 0L;
        definitions.clear();
        instances.clear();
        goals.clear();
        history.clear();
        recentRotationIds.clear();
    }

    public synchronized JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("version", SAVE_VERSION);
        root.addProperty("level", level);
        root.addProperty("experience", experience);
        root.addProperty("storyline_chapter", storylineChapter);
        root.addProperty("tutorial_step", tutorialStep);
        root.addProperty("next_daily_reset_at", nextDailyResetAt);

        JsonArray definitionArray = new JsonArray();
        for (QuestDefinition definition : definitions.values()) definitionArray.add(definition.toJson());
        root.add("quest_definitions", definitionArray);

        JsonArray instanceArray = new JsonArray();
        for (QuestInstance instance : instances.values()) instanceArray.add(instance.toJson());
        root.add("quest_instances", instanceArray);

        JsonArray goalArray = new JsonArray();
        for (GoalProgress goal : goals.values()) goalArray.add(goal.toJson());
        root.add("goals", goalArray);

        JsonArray historyArray = new JsonArray();
        for (HistoryEntry entry : history) historyArray.add(entry.toJson());
        root.add("quest_history", historyArray);

        JsonArray rotationArray = new JsonArray();
        for (String id : recentRotationIds) rotationArray.add(new JsonPrimitive(id));
        root.add("recent_rotation_ids", rotationArray);
        return root;
    }

    public synchronized void loadJson(JsonElement element) {
        reset();
        if (element == null || !element.isJsonObject()) return;
        JsonObject root = element.getAsJsonObject();
        level = Math.max(1, intValue(root, "level", 1));
        experience = Math.max(0L, longValue(root, "experience", 0L));
        storylineChapter = Math.max(1, intValue(root, "storyline_chapter", 1));
        tutorialStep = Math.max(0, intValue(root, "tutorial_step", 0));
        nextDailyResetAt = Math.max(0L, longValue(root, "next_daily_reset_at", 0L));

        JsonArray definitionArray = arrayValue(root, "quest_definitions");
        for (JsonElement item : definitionArray) {
            QuestDefinition definition = QuestDefinition.fromJson(item);
            if (definition != null && definition.isValid() && !definitions.containsKey(definition.id)) {
                definitions.put(definition.id, definition);
            }
        }

        JsonArray instanceArray = arrayValue(root, "quest_instances");
        int restoredActiveCount = 0;
        for (JsonElement item : instanceArray) {
            QuestInstance instance = QuestInstance.fromJson(item);
            if (instance == null || !definitions.containsKey(instance.definitionId)
                    || instances.containsKey(instance.instanceId)) continue;

            QuestDefinition definition = definitions.get(instance.definitionId);
            List<String> validObjectiveIds = new ArrayList<String>();
            for (Objective objective : definition.objectives) {
                validObjectiveIds.add(objective.id);
                int progress = instance.getObjectiveProgress(objective.id);
                instance.objectiveProgress.put(objective.id,
                        Integer.valueOf(Math.max(0, Math.min(objective.requiredAmount, progress))));
            }
            instance.objectiveProgress.keySet().retainAll(validObjectiveIds);

            // Malformed or legacy saves must not restore more than five active quests.
            if (instance.status == QuestStatus.ACTIVE) {
                if (restoredActiveCount >= MAX_ACTIVE_QUESTS) {
                    instance.status = QuestStatus.AVAILABLE;
                    instance.acceptedAt = 0L;
                } else {
                    restoredActiveCount++;
                }
            }
            instances.put(instance.instanceId, instance);
        }

        JsonArray goalArray = arrayValue(root, "goals");
        for (JsonElement item : goalArray) {
            GoalProgress goal = GoalProgress.fromJson(item);
            if (goal != null && goal.id != null) goals.put(goal.id, goal);
        }

        JsonArray historyArray = arrayValue(root, "quest_history");
        for (JsonElement item : historyArray) {
            HistoryEntry entry = HistoryEntry.fromJson(item);
            if (entry != null) history.add(entry);
        }

        JsonArray rotationArray = arrayValue(root, "recent_rotation_ids");
        for (JsonElement item : rotationArray) {
            if (item != null && item.isJsonPrimitive()) recentRotationIds.add(item.getAsString());
        }
    }

    public static final class QuestDefinition {
        public final String id;
        public final String name;
        public final String description;
        public final int requiredLevel;
        public final int requiredPrestige;
        public final String tier;
        public final List<Objective> objectives;
        public final Map<String, Integer> rewards;

        public QuestDefinition(String id, String name, String description, int requiredLevel,
                int requiredPrestige, String tier, List<Objective> objectives, Map<String, Integer> rewards) {
            this.id = clean(id);
            this.name = clean(name);
            this.description = description == null ? "" : description;
            this.requiredLevel = Math.max(1, requiredLevel);
            this.requiredPrestige = Math.max(0, requiredPrestige);
            this.tier = tier == null ? "" : tier;
            this.objectives = new ArrayList<Objective>();
            if (objectives != null) for (Objective objective : objectives) {
                if (objective != null && objective.isValid()) this.objectives.add(objective.copy());
            }
            this.rewards = new LinkedHashMap<String, Integer>();
            if (rewards != null) for (Map.Entry<String, Integer> reward : rewards.entrySet()) {
                if (reward.getKey() != null && reward.getValue() != null && reward.getValue().intValue() > 0) {
                    this.rewards.put(reward.getKey(), reward.getValue());
                }
            }
        }

        public boolean isValid() {
            return !id.isEmpty() && !name.isEmpty() && requiredLevel >= 1 && requiredPrestige >= 0;
        }

        private QuestDefinition copy() {
            return new QuestDefinition(id, name, description, requiredLevel, requiredPrestige, tier, objectives, rewards);
        }

        private JsonObject toJson() {
            JsonObject root = new JsonObject();
            root.addProperty("id", id);
            root.addProperty("name", name);
            root.addProperty("description", description);
            root.addProperty("required_level", requiredLevel);
            root.addProperty("required_prestige", requiredPrestige);
            root.addProperty("tier", tier);
            JsonArray objectiveArray = new JsonArray();
            for (Objective objective : objectives) objectiveArray.add(objective.toJson());
            root.add("objectives", objectiveArray);
            JsonObject rewardObject = new JsonObject();
            for (Map.Entry<String, Integer> reward : rewards.entrySet()) rewardObject.addProperty(reward.getKey(), reward.getValue());
            root.add("rewards", rewardObject);
            return root;
        }

        private static QuestDefinition fromJson(JsonElement element) {
            if (element == null || !element.isJsonObject()) return null;
            JsonObject root = element.getAsJsonObject();
            List<Objective> objectives = new ArrayList<Objective>();
            for (JsonElement item : arrayValue(root, "objectives")) {
                Objective objective = Objective.fromJson(item);
                if (objective != null) objectives.add(objective);
            }
            Map<String, Integer> rewards = new LinkedHashMap<String, Integer>();
            JsonElement rewardElement = root.get("rewards");
            if (rewardElement != null && rewardElement.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : rewardElement.getAsJsonObject().entrySet()) {
                    try {
                        int amount = entry.getValue().getAsInt();
                        if (amount > 0) rewards.put(entry.getKey(), Integer.valueOf(amount));
                    } catch (RuntimeException ignored) { }
                }
            }
            QuestDefinition definition = new QuestDefinition(stringValue(root, "id", ""),
                    stringValue(root, "name", ""), stringValue(root, "description", ""),
                    intValue(root, "required_level", 1), intValue(root, "required_prestige", 0),
                    stringValue(root, "tier", ""), objectives, rewards);
            return definition.isValid() ? definition : null;
        }
    }

    public static final class Objective {
        public final String id;
        public final String type;
        public final String target;
        public final int requiredAmount;

        public Objective(String id, String type, String target, int requiredAmount) {
            this.id = clean(id);
            this.type = clean(type);
            this.target = clean(target);
            this.requiredAmount = requiredAmount;
        }

        public boolean isValid() {
            return !id.isEmpty() && !type.isEmpty() && requiredAmount > 0;
        }

        private Objective copy() { return new Objective(id, type, target, requiredAmount); }

        private JsonObject toJson() {
            JsonObject root = new JsonObject();
            root.addProperty("id", id);
            root.addProperty("type", type);
            root.addProperty("target", target);
            root.addProperty("required_amount", requiredAmount);
            return root;
        }

        private static Objective fromJson(JsonElement element) {
            if (element == null || !element.isJsonObject()) return null;
            JsonObject root = element.getAsJsonObject();
            Objective value = new Objective(stringValue(root, "id", ""), stringValue(root, "type", ""),
                    stringValue(root, "target", ""), intValue(root, "required_amount", 0));
            return value.isValid() ? value : null;
        }
    }

    public static final class QuestInstance {
        public final String instanceId;
        public final String definitionId;
        private QuestStatus status;
        private final long assignedAt;
        private long acceptedAt;
        private long finishedAt;
        private boolean rewardsClaimed;
        private final Map<String, Integer> objectiveProgress = new LinkedHashMap<String, Integer>();

        private QuestInstance(String instanceId, String definitionId, QuestStatus status, long assignedAt) {
            this.instanceId = instanceId;
            this.definitionId = definitionId;
            this.status = status;
            this.assignedAt = Math.max(0L, assignedAt);
        }

        public QuestStatus getStatus() { return status; }
        public long getAssignedAt() { return assignedAt; }
        public long getAcceptedAt() { return acceptedAt; }
        public long getFinishedAt() { return finishedAt; }
        public boolean areRewardsClaimed() { return rewardsClaimed; }
        public int getObjectiveProgress(String objectiveId) {
            Integer value = objectiveProgress.get(objectiveId);
            return value == null ? 0 : value.intValue();
        }

        private QuestInstance copy() {
            QuestInstance result = new QuestInstance(instanceId, definitionId, status, assignedAt);
            result.acceptedAt = acceptedAt;
            result.finishedAt = finishedAt;
            result.rewardsClaimed = rewardsClaimed;
            result.objectiveProgress.putAll(objectiveProgress);
            return result;
        }

        private JsonObject toJson() {
            JsonObject root = new JsonObject();
            root.addProperty("instance_id", instanceId);
            root.addProperty("definition_id", definitionId);
            root.addProperty("status", status.name());
            root.addProperty("assigned_at", assignedAt);
            root.addProperty("accepted_at", acceptedAt);
            root.addProperty("finished_at", finishedAt);
            root.addProperty("rewards_claimed", rewardsClaimed);
            JsonObject progress = new JsonObject();
            for (Map.Entry<String, Integer> entry : objectiveProgress.entrySet()) {
                progress.addProperty(entry.getKey(), Math.max(0, entry.getValue().intValue()));
            }
            root.add("objective_progress", progress);
            return root;
        }

        private static QuestInstance fromJson(JsonElement element) {
            if (element == null || !element.isJsonObject()) return null;
            JsonObject root = element.getAsJsonObject();
            QuestStatus status;
            try { status = QuestStatus.valueOf(stringValue(root, "status", "AVAILABLE")); }
            catch (RuntimeException ignored) { status = QuestStatus.AVAILABLE; }
            String instanceId = stringValue(root, "instance_id", "");
            String definitionId = stringValue(root, "definition_id", "");
            if (instanceId.isEmpty() || definitionId.isEmpty()) return null;
            QuestInstance instance = new QuestInstance(instanceId, definitionId, status,
                    longValue(root, "assigned_at", 0L));
            instance.acceptedAt = Math.max(0L, longValue(root, "accepted_at", 0L));
            instance.finishedAt = Math.max(0L, longValue(root, "finished_at", 0L));
            instance.rewardsClaimed = booleanValue(root, "rewards_claimed", false);
            JsonElement progressElement = root.get("objective_progress");
            if (progressElement != null && progressElement.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : progressElement.getAsJsonObject().entrySet()) {
                    try { instance.objectiveProgress.put(entry.getKey(), Integer.valueOf(Math.max(0, entry.getValue().getAsInt()))); }
                    catch (RuntimeException ignored) { instance.objectiveProgress.put(entry.getKey(), Integer.valueOf(0)); }
                }
            }
            return instance;
        }
    }

    public static final class GoalProgress {
        public final String id;
        public final GoalType type;
        public final String title;
        private int progress;
        private final int target;
        private long resetAt;
        private boolean completed;

        public GoalProgress(String id, GoalType type, String title, int progress, int target,
                long resetAt, boolean completed) {
            this.id = clean(id);
            this.type = type == null ? GoalType.SIDE : type;
            this.title = title == null ? "" : title;
            this.target = Math.max(1, target);
            this.progress = Math.max(0, Math.min(this.target, progress));
            this.resetAt = Math.max(0L, resetAt);
            this.completed = completed || this.progress >= this.target;
        }

        public int getProgress() { return progress; }
        public int getTarget() { return target; }
        public long getResetAt() { return resetAt; }
        public boolean isCompleted() { return completed; }

        private GoalProgress copy() { return new GoalProgress(id, type, title, progress, target, resetAt, completed); }

        private JsonObject toJson() {
            JsonObject root = new JsonObject();
            root.addProperty("id", id);
            root.addProperty("type", type.name());
            root.addProperty("title", title);
            root.addProperty("progress", progress);
            root.addProperty("target", target);
            root.addProperty("reset_at", resetAt);
            root.addProperty("completed", completed);
            return root;
        }

        private static GoalProgress fromJson(JsonElement element) {
            if (element == null || !element.isJsonObject()) return null;
            JsonObject root = element.getAsJsonObject();
            GoalType type;
            try { type = GoalType.valueOf(stringValue(root, "type", "SIDE")); }
            catch (RuntimeException ignored) { type = GoalType.SIDE; }
            String id = stringValue(root, "id", "");
            if (id.isEmpty()) return null;
            return new GoalProgress(id, type, stringValue(root, "title", ""),
                    intValue(root, "progress", 0), intValue(root, "target", 1),
                    longValue(root, "reset_at", 0L), booleanValue(root, "completed", false));
        }
    }

    public static final class HistoryEntry {
        public final String instanceId;
        public final String definitionId;
        public final QuestStatus outcome;
        public final long timestamp;

        private HistoryEntry(String instanceId, String definitionId, QuestStatus outcome, long timestamp) {
            this.instanceId = instanceId;
            this.definitionId = definitionId;
            this.outcome = outcome;
            this.timestamp = Math.max(0L, timestamp);
        }

        private HistoryEntry copy() { return new HistoryEntry(instanceId, definitionId, outcome, timestamp); }

        private JsonObject toJson() {
            JsonObject root = new JsonObject();
            root.addProperty("instance_id", instanceId);
            root.addProperty("definition_id", definitionId);
            root.addProperty("outcome", outcome.name());
            root.addProperty("timestamp", timestamp);
            return root;
        }

        private static HistoryEntry fromJson(JsonElement element) {
            if (element == null || !element.isJsonObject()) return null;
            JsonObject root = element.getAsJsonObject();
            QuestStatus outcome;
            try { outcome = QuestStatus.valueOf(stringValue(root, "outcome", "ABANDONED")); }
            catch (RuntimeException ignored) { outcome = QuestStatus.ABANDONED; }
            if (outcome != QuestStatus.ABANDONED && outcome != QuestStatus.COMPLETED) return null;
            String instanceId = stringValue(root, "instance_id", "");
            String definitionId = stringValue(root, "definition_id", "");
            if (instanceId.isEmpty() || definitionId.isEmpty()) return null;
            return new HistoryEntry(instanceId, definitionId, outcome, longValue(root, "timestamp", 0L));
        }
    }

    private static String clean(String value) { return value == null ? "" : value.trim(); }
    private static JsonArray arrayValue(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonArray() ? value.getAsJsonArray() : new JsonArray();
    }
    private static String stringValue(JsonObject object, String key, String fallback) {
        try { JsonElement value = object.get(key); return value == null || value.isJsonNull() ? fallback : value.getAsString(); }
        catch (RuntimeException ignored) { return fallback; }
    }
    private static int intValue(JsonObject object, String key, int fallback) {
        try { JsonElement value = object.get(key); return value == null || value.isJsonNull() ? fallback : value.getAsInt(); }
        catch (RuntimeException ignored) { return fallback; }
    }
    private static long longValue(JsonObject object, String key, long fallback) {
        try { JsonElement value = object.get(key); return value == null || value.isJsonNull() ? fallback : value.getAsLong(); }
        catch (RuntimeException ignored) { return fallback; }
    }
    private static boolean booleanValue(JsonObject object, String key, boolean fallback) {
        try { JsonElement value = object.get(key); return value == null || value.isJsonNull() ? fallback : value.getAsBoolean(); }
        catch (RuntimeException ignored) { return fallback; }
    }
}
