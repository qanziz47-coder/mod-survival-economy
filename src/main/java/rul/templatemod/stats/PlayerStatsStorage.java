package rul.templatemod.stats;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Menyimpan statistik semua pemain ke file dunia.
 */
public class PlayerStatsStorage extends PersistentState {

    private static final String STORAGE_ID = "survival_economy_stats";

    private final Map<UUID, PlayerStats> statsMap = new HashMap<>();

    public PlayerStatsStorage() {
    }

    /**
     * Ambil statistik pemain. Kalau belum ada, buat baru dengan nilai nol.
     */
    public PlayerStats getStats(UUID playerId) {
        return statsMap.computeIfAbsent(playerId, id -> new PlayerStats());
    }

    /**
     * Tandai data berubah agar disimpan.
     */
    public void markDirty() {
        super.markDirty();
    }

    // ==================== PENYIMPANAN ====================

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound playersNbt = new NbtCompound();
        for (Map.Entry<UUID, PlayerStats> entry : statsMap.entrySet()) {
            PlayerStats stats = entry.getValue();
            NbtCompound statNbt = new NbtCompound();
            statNbt.putInt("kills", stats.getKills());
            statNbt.putInt("deaths", stats.getDeaths());
            statNbt.putInt("mobKills", stats.getMobKills());
            statNbt.putInt("playerKills", stats.getPlayerKills());
            playersNbt.put(entry.getKey().toString(), statNbt);
        }
        nbt.put("players", playersNbt);
        return nbt;
    }

    public static PlayerStatsStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        PlayerStatsStorage storage = new PlayerStatsStorage();
        NbtCompound playersNbt = nbt.getCompound("players");
        for (String key : playersNbt.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                NbtCompound statNbt = playersNbt.getCompound(key);
                PlayerStats stats = new PlayerStats();
                stats.setKills(statNbt.getInt("kills"));
                stats.setDeaths(statNbt.getInt("deaths"));
                stats.setMobKills(statNbt.getInt("mobKills"));
                stats.setPlayerKills(statNbt.getInt("playerKills"));
                storage.statsMap.put(uuid, stats);
            } catch (IllegalArgumentException e) {
                // skip UUID tidak valid
            }
        }
        return storage;
    }

    public static PlayerStatsStorage getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(
                new PersistentState.Type<>(
                        PlayerStatsStorage::new,
                        PlayerStatsStorage::createFromNbt,
                        null
                ),
                STORAGE_ID
        );
    }
}