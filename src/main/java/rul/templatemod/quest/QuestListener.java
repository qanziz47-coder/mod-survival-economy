package rul.templatemod.quest;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Auto-check quest berdasarkan event pemain.
 */
public class QuestListener {

    private static int tickCounter = 0;

    public static void register() {

        // 1. KILL MOB — pakai AFTER_DEATH (sudah works)
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // Cek apakah yang bunuh adalah pemain
            if (!(damageSource.getAttacker() instanceof ServerPlayerEntity player)) return;

            // Cek tipe mob
            EntityType<?> type = entity.getType();

            if (type == EntityType.ZOMBIE) {
                QuestManager.completeQuest(player.getServer(), player, QuestType.TIER2_ZOMBIE);
            } else if (type == EntityType.SKELETON) {
                QuestManager.completeQuest(player.getServer(), player, QuestType.TIER3_SKELETON);
            } else if (type == EntityType.ENDERMAN) {
                QuestManager.completeQuest(player.getServer(), player, QuestType.TIER4_ENDERMAN);
            } else if (type == EntityType.ENDER_DRAGON) {
                QuestManager.completeQuest(player.getServer(), player, QuestType.TIER5_DRAGON);
            } else if (type == EntityType.WITHER) {
                QuestManager.completeQuest(player.getServer(), player, QuestType.TIER5_WITHER);
            }
        });

        // 2. TICK — CEK INVENTORY BERKALA
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter < 40) return; // setiap 2 detik
            tickCounter = 0;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                checkInventory(player);
            }
        });
    }

    // ==================== CHECK INVENTORY ====================

    private static void checkInventory(ServerPlayerEntity player) {
        var inv = player.getInventory();

        // Tier 1
        if (countItem(inv, Items.COBBLESTONE) >= 64)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER1_COBBLESTONE);
        if (countItem(inv, Items.WHEAT) >= 1)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER1_WHEAT);
        if (countItem(inv, Items.CRAFTING_TABLE) >= 1)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER1_CRAFTING_TABLE);

        // Tier 2
        if (countItem(inv, Items.IRON_INGOT) >= 1)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER2_IRON);
        if (countItem(inv, Items.IRON_PICKAXE) >= 1)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER2_IRON_PICKAXE);

        // Tier 3
        if (countItem(inv, Items.DIAMOND) >= 1)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER3_DIAMOND);
        if (countItem(inv, Items.DIAMOND_SWORD) >= 1)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER3_DIAMOND_SWORD);
        if (countItem(inv, Items.COAL) >= 64)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER3_COAL);

        // Tier 4
        if (countItem(inv, Items.NETHERITE_INGOT) >= 1)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER4_NETHERITE);
        if (countItem(inv, Items.BEACON) >= 1)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER4_BEACON);

        // Full diamond armor
        boolean fullArmor = countItem(inv, Items.DIAMOND_HELMET) >= 1
                && countItem(inv, Items.DIAMOND_CHESTPLATE) >= 1
                && countItem(inv, Items.DIAMOND_LEGGINGS) >= 1
                && countItem(inv, Items.DIAMOND_BOOTS) >= 1;
        if (fullArmor)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER4_DIAMOND_ARMOR);

        // Tier 5
        if (countItem(inv, Items.DRAGON_EGG) >= 1)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER5_DRAGON_EGG);
        if (countItem(inv, Items.DIAMOND_HOE) >= 1
                && countItem(inv, Items.DIAMOND_PICKAXE) >= 1
                && countItem(inv, Items.DIAMOND_SWORD) >= 1)
            QuestManager.completeQuest(player.getServer(), player, QuestType.TIER5_ALL_ROLE_ITEMS);
    }

    // ==================== UTIL ====================

    private static int countItem(net.minecraft.entity.player.PlayerInventory inv,
                                 net.minecraft.item.Item item) {
        int count = 0;
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).isOf(item)) count += inv.getStack(i).getCount();
        }
        return count;
    }
}