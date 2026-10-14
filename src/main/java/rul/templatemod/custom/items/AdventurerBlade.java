package rul.templatemod.custom.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rul.templatemod.custom.CustomItemBuilder;
import rul.templatemod.custom.CustomItemRegistry;

/**
 * Adventurer Blade — lifesteal 20% saat kill mob.
 */
public class AdventurerBlade {

    public static final float LIFESTEAL_PERCENT = 0.20f; // 20%

    /**
     * Cek apakah player pegang Adventurer Blade.
     */
    public static boolean hasBlade(ServerPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        return CustomItemBuilder.isCustomItem(main, CustomItemRegistry.ID_ADVENTURER_BLADE);
    }

    /**
     * Trigger saat player kill mob. Restore HP.
     */
    public static void onKill(ServerPlayerEntity player, LivingEntity killed) {
        if (!hasBlade(player)) return;

        float maxHealth = player.getMaxHealth();
        float healAmount = maxHealth * LIFESTEAL_PERCENT;
        float currentHealth = player.getHealth();

        if (currentHealth >= maxHealth) return; // HP sudah penuh

        float newHealth = Math.min(currentHealth + healAmount, maxHealth);
        player.setHealth(newHealth);

        player.sendMessage(
                Text.literal("§c❤ Lifesteal: §e+" + String.format("%.1f", healAmount) + " HP"),
                true); // action bar
    }
}