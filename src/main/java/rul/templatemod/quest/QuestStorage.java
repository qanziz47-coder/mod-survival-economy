package rul.templatemod.quest;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Menyimpan progress quest per pemain.
 */
public class QuestStorage extends PersistentState {

    private static final String STORAGE_ID = "survival_economy_quest";

    // UUID → set of completed quests
    private final Map<UUID, Set<String>> completedQuests = new HashMap<>();

    public QuestStorage() {
    }

    // ==================== QUEST ====================

    public boolean isCompleted(UUID playerId, QuestType quest) {
        Set<String> set = completedQuests.get(playerId);
        return set != null && set.contains(quest.name());
    }

    public void completeQuest(UUID playerId, QuestType quest) {
        completedQuests.computeIfAbsent(playerId, k -> new HashSet<>()).add(quest.name());
        markDirty();
    }

    public int getCompletedCount(UUID playerId) {
        Set<String> set = completedQuests.get(playerId);
        return set != null ? set.size() : 0;
    }

    public int getCompletedCountByTier(UUID playerId, int tier) {
        Set<String> set = completedQuests.get(playerId);
        if (set == null) return 0;

        int count = 0;
        for (String name : set) {
            try {
                QuestType q = QuestType.valueOf(name);
                if (q.getTier() == tier) count++;
            } catch (IllegalArgumentException ignored) {}
        }
        return count;
    }

    public Set<String> getAllCompleted(UUID playerId) {
        return new HashSet<>(completedQuests.getOrDefault(playerId, new HashSet<>()));
    }

    // ==================== PENYIMPANAN ====================

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound questsNbt = new NbtCompound();
        for (var entry : completedQuests.entrySet()) {
            NbtCompound playerQuests = new NbtCompound();
            for (String quest : entry.getValue()) {
                playerQuests.putBoolean(quest, true);
            }
            questsNbt.put(entry.getKey().toString(), playerQuests);
        }
        nbt.put("quests", questsNbt);
        return nbt;
    }

    public static QuestStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        QuestStorage storage = new QuestStorage();
        NbtCompound questsNbt = nbt.getCompound("quests");

        for (String uuidKey : questsNbt.getKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidKey);
                NbtCompound playerQuests = questsNbt.getCompound(uuidKey);
                Set<String> set = new HashSet<>();
                for (String questName : playerQuests.getKeys()) {
                    set.add(questName);
                }
                storage.completedQuests.put(uuid, set);
            } catch (IllegalArgumentException ignored) {}
        }

        return storage;
    }

    public static QuestStorage getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(
                new PersistentState.Type<>(QuestStorage::new, QuestStorage::createFromNbt, null),
                STORAGE_ID
        );
    }
}