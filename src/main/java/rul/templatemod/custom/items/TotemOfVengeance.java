package rul.templatemod.custom.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rul.templatemod.custom.CustomItemBuilder;
import rul.templatemod.custom.CustomItemRegistry;
import rul.templatemod.custom.CustomItemStorage;

/**
 * Totem of Vengeance — balas damage 300% saat HP < 3.
 */
public class TotemOfVengeance {

    public static final int DEFAULT_CHARGE = 10;
    public static final int COOLDOWN_TICKS = 600; // 30 detik
    public static final float HEALTH_THRESHOLD = 3.0f;

    public static ItemStack findTotem(ServerPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (CustomItemBuilder.isCustomItem(main, CustomItemRegistry.ID_TOTEM_VENGEANCE)) {
            return main;
        }
        ItemStack off = player.getOffHandStack();
        if (CustomItemBuilder.isCustomItem(off, CustomItemRegistry.ID_TOTEM_VENGEANCE)) {
            return off;
        }
        return null;
    }

    public static boolean tryTrigger(ServerPlayerEntity player, DamageSource source, float damage) {
        // Cek HP threshold
        if (player.getHealth() > HEALTH_THRESHOLD) return false;

        ItemStack totem = findTotem(player);
        if (totem == null) return false;

        CustomItemStorage storage = CustomItemStorage.getServerState(player.getServer());
        long worldTime = player.getServer().getOverworld().getTimeOfDay();

        // Cek cooldown
        if (storage.isOnCooldown(player.getUuid(), CustomItemRegistry.ID_TOTEM_VENGEANCE, worldTime)) {
            return false;
        }

        // Cek charge
        int charge = storage.getCharge(player.getUuid(), CustomItemRegistry.ID_TOTEM_VENGEANCE, DEFAULT_CHARGE);
        if (charge <= 0) return false;

        // Balas damage ke attacker — kurangi HP langsung (tanpa damage())
        var attacker = source.getAttacker();
        if (attacker instanceof LivingEntity livingAttacker) {
            float reflectDamage = damage * 3.0f;
            float newHealth = livingAttacker.getHealth() - reflectDamage;

            if (newHealth <= 0) {
                // Attacker mati
                livingAttacker.setHealth(0);
                livingAttacker.onDeath(player.getDamageSources().playerAttack(player));
            } else {
                livingAttacker.setHealth(newHealth);
            }
        }

        // Kurangi charge
        storage.decrementCharge(player.getUuid(), CustomItemRegistry.ID_TOTEM_VENGEANCE, DEFAULT_CHARGE);

        // Set cooldown
        storage.setCooldown(player.getUuid(), CustomItemRegistry.ID_TOTEM_VENGEANCE, worldTime + COOLDOWN_TICKS);

        // Pesan
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§c§l🔥 VENGEANCE AKTIF! 🔥"), false);
        player.sendMessage(Text.literal("§7Kamu membalas §c300% §7damage ke attacker!"), false);

        int remaining = storage.getCharge(player.getUuid(), CustomItemRegistry.ID_TOTEM_VENGEANCE, DEFAULT_CHARGE);
        player.sendMessage(Text.literal("§7Charge tersisa: §e" + remaining + "/" + DEFAULT_CHARGE), false);

        if (remaining <= 0) {
            totem.setCount(0);
            player.sendMessage(Text.literal("§cTotem hancur setelah dipakai!"), false);
        }

        return true;
    }
}