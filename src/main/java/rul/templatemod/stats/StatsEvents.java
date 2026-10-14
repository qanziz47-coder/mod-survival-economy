package rul.templatemod.stats;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Menghubungkan statistik dengan event Minecraft.
 */
public class StatsEvents {

    public static void register() {
        // Event: entitas mati (untuk death pemain dan kill)
        ServerLivingEntityEvents.AFTER_DEATH.register(
                (entity, damageSource) -> {
                    // Jika yang mati adalah pemain, tambahkan death
                    if (entity instanceof ServerPlayerEntity player) {
                        PlayerStatsManager.addDeath(
                                player.getServer(),
                                player.getUuid()
                        );
                    }

                    // Cek apakah pembunuhnya adalah pemain (untuk kill)
                    if (damageSource.getAttacker() instanceof ServerPlayerEntity killer) {
                        // Pastikan yang mati BUKAN pemain itu sendiri (biar tidak dihitung suicide)
                        if (entity != killer) {
                            boolean isPlayerKill = entity instanceof PlayerEntity;
                            PlayerStatsManager.addKill(
                                    killer.getServer(),
                                    killer.getUuid(),
                                    isPlayerKill
                            );
                        }
                    }
                }
        );
    }
}