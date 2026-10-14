package rul.templatemod.custom.items;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rul.templatemod.custom.CustomItemBuilder;
import rul.templatemod.custom.CustomItemRegistry;
import rul.templatemod.custom.CustomItemStorage;

/**
 * Totem of Resurrection — revive + full HP + buff.
 */
public class TotemOfResurrection {

    public static final int DEFAULT_CHARGE = 3;
    public static final int COOLDOWN_TICKS = 6000; // 5 menit (20 tick/detik * 60 * 5)

    /**
     * Cek apakah player punya Totem of Resurrection di inventory (main atau off-hand).
     */
    public static ItemStack findTotem(ServerPlayerEntity player) {
        // Cek main hand
        ItemStack main = player.getMainHandStack();
        if (CustomItemBuilder.isCustomItem(main, CustomItemRegistry.ID_TOTEM_RESURRECTION)) {
            return main;
        }
        // Cek off hand
        ItemStack off = player.getOffHandStack();
        if (CustomItemBuilder.isCustomItem(off, CustomItemRegistry.ID_TOTEM_RESURRECTION)) {
            return off;
        }
        return null;
    }

    /**
     * Trigger saat player hampir mati. Return true kalau totem dipakai.
     */
    public static boolean tryUse(ServerPlayerEntity player) {
        ItemStack totem = findTotem(player);
        if (totem == null) return false;

        CustomItemStorage storage = CustomItemStorage.getServerState(player.getServer());
        long worldTime = player.getServer().getOverworld().getTimeOfDay();

        // Cek cooldown
        if (storage.isOnCooldown(player.getUuid(), CustomItemRegistry.ID_TOTEM_RESURRECTION, worldTime)) {
            long remaining = storage.getCooldown(player.getUuid(), CustomItemRegistry.ID_TOTEM_RESURRECTION) - worldTime;
            player.sendMessage(Text.literal("§cTotem masih cooldown! Tunggu " + (remaining / 20) + " detik."), false);
            return false;
        }

        // Cek charge
        int charge = storage.getCharge(player.getUuid(), CustomItemRegistry.ID_TOTEM_RESURRECTION, DEFAULT_CHARGE);
        if (charge <= 0) {
            player.sendMessage(Text.literal("§cTotem sudah habis charge! Dapat lagi dari misi."), false);
            return false;
        }

        // Terapkan efek
        player.setHealth(player.getMaxHealth());
        player.getHungerManager().setFoodLevel(20);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 4)); // Regen V, 10 detik
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1200, 0)); // Fire Res, 60 detik
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 2)); // Resistance III, 5 detik

        // Kurangi charge
        storage.decrementCharge(player.getUuid(), CustomItemRegistry.ID_TOTEM_RESURRECTION, DEFAULT_CHARGE);

        // Set cooldown
        storage.setCooldown(player.getUuid(), CustomItemRegistry.ID_TOTEM_RESURRECTION, worldTime + COOLDOWN_TICKS);

        // Pesan
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§6§l⚡ TOTEM OF RESURRECTION AKTIF! ⚡").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("§7Kamu bangkit kembali dari kematian!"), false);

        int remaining = storage.getCharge(player.getUuid(), CustomItemRegistry.ID_TOTEM_RESURRECTION, DEFAULT_CHARGE);
        player.sendMessage(Text.literal("§7Charge tersisa: §e" + remaining + "/" + DEFAULT_CHARGE), false);

        // Kalau charge habis, hapus totem
        if (remaining <= 0) {
            totem.setCount(0);
            player.sendMessage(Text.literal("§cTotem hancur setelah dipakai! Dapat lagi dari misi."), false);
        }

        return true;
    }
}