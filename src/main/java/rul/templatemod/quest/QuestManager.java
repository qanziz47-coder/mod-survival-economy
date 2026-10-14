package rul.templatemod.quest;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rul.templatemod.economy.EconomyManager;
import rul.templatemod.role.PlayerRole;
import rul.templatemod.role.RoleManager;

import java.util.UUID;

/**
 * Manager logika quest.
 */
public class QuestManager {

    /**
     * Selesaikan quest — kasih reward.
     */
    public static void completeQuest(MinecraftServer server, ServerPlayerEntity player, QuestType quest) {
        QuestStorage storage = QuestStorage.getServerState(server);
        UUID uuid = player.getUuid();

        // Skip kalau sudah selesai
        if (storage.isCompleted(uuid, quest)) return;

        // Tandai selesai
        storage.completeQuest(uuid, quest);

        // Kasih reward Coins
        EconomyManager.addBalance(server, uuid, quest.getReward());

        // Notifikasi
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§6§l╔══════════════════════════╗").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("§6§l║   MISI SELESAI!          ║").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("§6§l╚══════════════════════════╝").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal(quest.getColor() + "§l✦ " + quest.getDisplayName()), false);
        player.sendMessage(Text.literal("§7" + quest.getDescription()), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§aReward: §e" + EconomyManager.format(quest.getReward()) + " Coins"), false);

        // Cek tier completion — kalau semua misi tier selesai, naik tier role
        checkTierUp(server, player, quest.getTier());

        player.sendMessage(Text.literal(""), false);

        // Sound
        player.playSoundToPlayer(
                net.minecraft.sound.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                net.minecraft.sound.SoundCategory.MASTER,
                1.0f, 1.0f
        );
    }

    /**
     * Cek kalau semua misi tier selesai → naik tier role.
     */
    private static void checkTierUp(MinecraftServer server, ServerPlayerEntity player, int tier) {
        QuestStorage storage = QuestStorage.getServerState(server);
        UUID uuid = player.getUuid();

        // Ambil semua quest di tier ini
        QuestType[] tierQuests = QuestType.getByTier(tier);
        int completed = 0;
        for (QuestType q : tierQuests) {
            if (storage.isCompleted(uuid, q)) completed++;
        }

        // Kalau semua selesai → naik tier role
        if (completed >= tierQuests.length) {
            PlayerRole role = RoleManager.getRole(server, uuid);
            if (role == null) return;

            int currentTier = RoleManager.getTier(server, uuid);
            if (currentTier < tier) {
                // Naik tier
                var roleStorage = rul.templatemod.role.RoleStorage.getServerState(server);
                roleStorage.setTier(uuid, tier);

                player.sendMessage(Text.literal(""), false);
                player.sendMessage(Text.literal("§6§l✦ TIER UP! ✦").formatted(Formatting.GOLD, Formatting.BOLD), false);
                player.sendMessage(Text.literal("§7Kamu sekarang §eTier " + tier + "§7 di role "
                        + role.getColor() + role.getDisplayName() + "§7!"), false);
                player.sendMessage(Text.literal("§7Gaji baru: §e"
                        + EconomyManager.format(role.getSalary(tier)) + " Coins"), false);
                player.sendMessage(Text.literal(""), false);
            }
        }
    }

    // ==================== INFO ====================

    public static String getQuestInfo(MinecraftServer server, UUID playerId) {
        QuestStorage storage = QuestStorage.getServerState(server);

        StringBuilder sb = new StringBuilder();
        sb.append("§6§l=== DAFTAR MISI ===\n");
        sb.append("§7Total selesai: §e").append(storage.getCompletedCount(playerId))
                .append("§7/§e").append(QuestType.values().length).append("\n");

        for (int tier = 1; tier <= 5; tier++) {
            QuestType[] tierQuests = QuestType.getByTier(tier);
            int completed = storage.getCompletedCountByTier(playerId, tier);

            sb.append("\n§6§lTier ").append(tier).append(" §7(")
                    .append(completed).append("/").append(tierQuests.length).append(")\n");

            for (QuestType q : tierQuests) {
                boolean done = storage.isCompleted(playerId, q);
                String mark = done ? "§a✔" : "§c✘";
                sb.append(mark).append(" ").append(q.getColor()).append(q.getDisplayName())
                        .append(" §7— §e").append(EconomyManager.format(q.getReward())).append(" Coins\n");
            }
        }

        return sb.toString();
    }
}