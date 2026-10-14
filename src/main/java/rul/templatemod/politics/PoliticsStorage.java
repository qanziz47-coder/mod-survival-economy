package rul.templatemod.politics;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Menyimpan data politik per dunia: protes, referendum, voting.
 */
public class PoliticsStorage extends PersistentState {

    private static final String STORAGE_ID = "survival_economy_politics";

    // ==================== KONFIGURASI ====================
    public static final int TAX_THRESHOLD_PROTEST = 8;     // Pajak minimal untuk protes manual
    public static final int TAX_WARNING_THRESHOLD = 8;     // Warning muncul kalau pajak >= 8%
    public static final int TAX_AUTO_REFERENDUM = 12;      // Auto referendum kalau pajak > 12%
    public static final double PROTEST_THRESHOLD = 0.5;    // 50% pemain online
    public static final int PROTEST_COOLDOWN_HOURS = 24;   // 1 hari Minecraft
    public static final int PROTEST_EXPIRE_DAYS = 3;
    public static final int VOTE_DURATION_TICKS = 3600;    // 3 menit
    public static final int VOTE_COOLDOWN_DAYS = 1;
    public static final int MIN_VOTERS = 2;

    // ==================== STATE ====================
    private final Map<UUID, Long> activeProtests = new HashMap<>();
    private final Set<UUID> votedPlayers = new HashSet<>();
    private final Map<UUID, Long> lastProtestTime = new HashMap<>();

    private boolean referendumActive = false;
    private long referendumStartTime = 0;
    private long lastReferendumEndTime = 0;

    private int voteDown = 0;
    private int voteUp = 0;
    private int voteStay = 0;

    private long lastAutoReferendumDay = -1;
    private long lastWarningDay =-1;

    private final List<String> history = new ArrayList<>();

    public PoliticsStorage() {
    }

    // ==================== PROTEST ====================

    public boolean hasProtested(UUID playerId) {
        return activeProtests.containsKey(playerId);
    }

    public long getLastWarningDay(){
        return  lastWarningDay;
    }
    public void setLastWarningDay(long day ){
        this.lastWarningDay = day;
        markDirty();
    }
    public void addProtest(UUID playerId, long worldTime) {
        activeProtests.put(playerId, worldTime);
        lastProtestTime.put(playerId, worldTime);
        markDirty();
    }

    public void removeProtest(UUID playerId) {
        activeProtests.remove(playerId);
        markDirty();
    }

    public int getProtestCount() {
        return activeProtests.size();
    }

    public Set<UUID> getProtesters() {
        return new HashSet<>(activeProtests.keySet());
    }

    public void clearProtests() {
        activeProtests.clear();
        markDirty();
    }

    public void cleanExpiredProtests(long currentTime) {
        long expireTicks = (long) PROTEST_EXPIRE_DAYS * 24000L;
        activeProtests.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > expireTicks);
    }

    public long getLastProtestTime(UUID playerId) {
        return lastProtestTime.getOrDefault(playerId, -1L);
    }

    public void setLastProtestTime(UUID playerId, long time) {
        lastProtestTime.put(playerId, time);
        markDirty();
    }

    // ==================== REFERENDUM ====================

    public boolean isReferendumActive() {
        return referendumActive;
    }

    public void startReferendum(long worldTime) {
        this.referendumActive = true;
        this.referendumStartTime = worldTime;
        this.votedPlayers.clear();
        this.voteDown = 0;
        this.voteUp = 0;
        this.voteStay = 0;
        markDirty();
    }

    public void endReferendum(long worldTime) {
        this.referendumActive = false;
        this.lastReferendumEndTime = worldTime;
        markDirty();
    }

    public long getReferendumStartTime() {
        return referendumStartTime;
    }

    public long getLastReferendumEndTime() {
        return lastReferendumEndTime;
    }

    public long getReferendumElapsed(long currentTime) {
        return currentTime - referendumStartTime;
    }

    public boolean isReferendumExpired(long currentTime) {
        return getReferendumElapsed(currentTime) >= VOTE_DURATION_TICKS;
    }

    // ==================== AUTO REFERENDUM ====================

    public long getLastAutoReferendumDay() {
        return lastAutoReferendumDay;
    }

    public void setLastAutoReferendumDay(long day) {
        this.lastAutoReferendumDay = day;
        markDirty();
    }

    // ==================== VOTE ====================

    public boolean hasVoted(UUID playerId) {
        return votedPlayers.contains(playerId);
    }

    public boolean addVote(UUID playerId, int choice) {
        if (votedPlayers.contains(playerId)) return false;

        votedPlayers.add(playerId);
        switch (choice) {
            case 0 -> voteDown++;
            case 1 -> voteUp++;
            case 2 -> voteStay++;
            default -> { return false; }
        }
        markDirty();
        return true;
    }

    public int getVoteDown() { return voteDown; }
    public int getVoteUp() { return voteUp; }
    public int getVoteStay() { return voteStay; }

    public int getTotalVotes() {
        return voteDown + voteUp + voteStay;
    }

    public void resetVotes() {
        votedPlayers.clear();
        voteDown = 0;
        voteUp = 0;
        voteStay = 0;
        markDirty();
    }

    // ==================== HISTORY ====================

    public void addHistory(String entry) {
        history.add(entry);
        if (history.size() > 50) history.remove(0);
        markDirty();
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    // ==================== PENYIMPANAN ====================

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound protestsNbt = new NbtCompound();
        for (var entry : activeProtests.entrySet()) {
            protestsNbt.putLong(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("activeProtests", protestsNbt);

        NbtCompound lastProtestNbt = new NbtCompound();
        for (var entry : lastProtestTime.entrySet()) {
            lastProtestNbt.putLong(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("lastProtestTime", lastProtestNbt);

        NbtCompound votedNbt = new NbtCompound();
        for (UUID uuid : votedPlayers) {
            votedNbt.putBoolean(uuid.toString(), true);
        }
        nbt.put("votedPlayers", votedNbt);

        nbt.putBoolean("referendumActive", referendumActive);
        nbt.putLong("referendumStartTime", referendumStartTime);
        nbt.putLong("lastReferendumEndTime", lastReferendumEndTime);
        nbt.putInt("voteDown", voteDown);
        nbt.putInt("voteUp", voteUp);
        nbt.putInt("voteStay", voteStay);
        nbt.putLong("lastAutoReferendumDay", lastAutoReferendumDay);
        nbt.putLong("lastwarning", lastWarningDay);

        NbtCompound historyNbt = new NbtCompound();
        for (int i = 0; i < history.size(); i++) {
            historyNbt.putString("e" + i, history.get(i));
        }
        historyNbt.putInt("size", history.size());
        nbt.put("history", historyNbt);

        return nbt;
    }

    public static PoliticsStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        PoliticsStorage storage = new PoliticsStorage();

        NbtCompound protestsNbt = nbt.getCompound("activeProtests");
        for (String key : protestsNbt.getKeys()) {
            try {
                storage.activeProtests.put(UUID.fromString(key), protestsNbt.getLong(key));
            } catch (IllegalArgumentException ignored) {}
        }

        NbtCompound lastProtestNbt = nbt.getCompound("lastProtestTime");
        for (String key : lastProtestNbt.getKeys()) {
            try {
                storage.lastProtestTime.put(UUID.fromString(key), lastProtestNbt.getLong(key));
            } catch (IllegalArgumentException ignored) {}
        }

        NbtCompound votedNbt = nbt.getCompound("votedPlayers");
        for (String key : votedNbt.getKeys()) {
            try {
                storage.votedPlayers.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {}
        }

        storage.referendumActive = nbt.getBoolean("referendumActive");
        storage.referendumStartTime = nbt.getLong("referendumStartTime");
        storage.lastReferendumEndTime = nbt.getLong("lastReferendumEndTime");
        storage.voteDown = nbt.getInt("voteDown");
        storage.voteUp = nbt.getInt("voteUp");
        storage.voteStay = nbt.getInt("voteStay");
        storage.lastAutoReferendumDay = nbt.getLong("lastAutoReferendumDay");
        storage.lastWarningDay = nbt.getLong("lastWarningday");

        NbtCompound historyNbt = nbt.getCompound("history");
        int size = historyNbt.getInt("size");
        for (int i = 0; i < size; i++) {
            storage.history.add(historyNbt.getString("e" + i));
        }

        return storage;
    }

    public static PoliticsStorage getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(
                new PersistentState.Type<>(PoliticsStorage::new, PoliticsStorage::createFromNbt, null),
                STORAGE_ID
        );
    }
}