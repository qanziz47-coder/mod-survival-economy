package rul.templatemod.custom.items;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import rul.templatemod.custom.CustomItemBuilder;
import rul.templatemod.custom.CustomItemRegistry;

/**
 * Trader Ring — diskon toko 50%, bonus jual 50%.
 * Passive — cukup ada di inventory.
 */
public class TraderRing {

    public static final double BUY_DISCOUNT = 0.50;  // Diskon 50%
    public static final double SELL_BONUS = 0.50;     // Bonus jual 50%

    /**
     * Cek apakah player punya Trader Ring di inventory.
     */
    public static boolean hasRing(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (CustomItemBuilder.isCustomItem(stack, CustomItemRegistry.ID_TRADER_RING)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Hitung harga beli setelah diskon.
     */
    public static long applyBuyDiscount(long originalPrice) {
        long discounted = Math.round(originalPrice * (1.0 - BUY_DISCOUNT));
        return Math.max(discounted, 1L); // minimal 1
    }

    /**
     * Hitung harga jual setelah bonus.
     */
    public static long applySellBonus(long originalPrice) {
        return Math.round(originalPrice * (1.0 + SELL_BONUS));
    }
}