package rul.templatemod.stats;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * manager untuk semua operasi statisitk pemain/player
 */
public class PlayerStatsManager {
    public static PlayerStats getStats(MinecraftServer server, UUID playerId){
        return PlayerStatsStorage.getServerState(server).getStats(playerId);
    }
    public static PlayerStats getStats(MinecraftServer server, ServerPlayerEntity player){
       return getStats(server, player.getUuid());
    }
    /**
     * Tambah kill untuk player
     */
    public static void addKill(MinecraftServer server, UUID playerId, boolean isPlayerKill){
        PlayerStatsStorage storage = PlayerStatsStorage.getServerState(server);
        PlayerStats stats = storage.getStats(playerId);
        stats.addKill(isPlayerKill);
        storage.markDirty();
    }
    /**
     * tambah jumlah mati untuk player
     */
    public static void addDeath(MinecraftServer server, UUID playerId){
        PlayerStatsStorage storage = PlayerStatsStorage.getServerState(server);
        PlayerStats stats = storage.getStats(playerId);
        stats.addDeath();
        storage.markDirty();
    }
}
