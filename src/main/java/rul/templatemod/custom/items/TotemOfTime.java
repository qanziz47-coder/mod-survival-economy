package rul.templatemod.custom.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import rul.templatemod.custom.CustomItemBuilder;
import rul.templatemod.custom.CustomItemRegistry;
import rul.templatemod.custom.CustomItemStorage;

import java.util.List;

/**
 * Totem of Time — slow-motion mob di sekitar saat HP < 5.
 */
public class TotemOfTime {

    public static final int DEFAULT_CHARGE = 5;
    public static final int COOLDOWN_TICKS = 2400; // 2 menit
    public static final float HEALTH_THRESHOLD = 5.0f;
    public static final double RADIUS = 10.0;

    public static ItemStack findTotem(ServerPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (CustomItemBuilder.isCustomItem(main, CustomItemRegistry.ID_TOTEM_TIME)) {
            return main;
        }
        ItemStack off = player.getOffHandStack();
        if (CustomItemBuilder.isCustomItem(off, CustomItemRegistry.ID_TOTEM_TIME)) {
            return off;
        }
        return null;
    }

    public static boolean tryTrigger(ServerPlayerEntity player) {
        if (player.getHealth() > HEALTH_THRESHOLD) return false;

        ItemStack totem = findTotem(player);
        if (totem == null) return false;

        CustomItemStorage storage = CustomItemStorage.getServerState(player.getServer());
        long worldTime = player.getServer().getOverworld().getTimeOfDay();

        // Cek cooldown
        if (storage.isOnCooldown(player.getUuid(), CustomItemRegistry.ID_TOTEM_TIME, worldTime)) {
            return false;
        }

        // Cek charge
        int charge = storage.getCharge(player.getUuid(), CustomItemRegistry.ID_TOTEM_TIME, DEFAULT_CHARGE);
        if (charge <= 0) return false;

        // Cari mob di sekitar
        Box area = new Box(
                player.getX() - RADIUS, player.getY() - RADIUS, player.getZ() - RADIUS,
                player.getX() + RADIUS, player.getY() + RADIUS, player.getZ() + RADIUS
        );

        List<LivingEntity> mobs = player.getWorld().getEntitiesByClass(
                LivingEntity.class,
                area,
                e -> e != player && !(e instanceof ServerPlayerEntity)
        );

        // Apply slow-motion ke mob
        int affected = 0;
        for (LivingEntity mob : mobs) {
            mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 3)); // Slowness IV, 10 detik
            mob.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 2));  // Weakness III, 10 detik
            affected++;
        }

        // Kurangi charge
        storage.decrementCharge(player.getUuid(), CustomItemRegistry.ID_TOTEM_TIME, DEFAULT_CHARGE);

        // Set cooldown
        storage.setCooldown(player.getUuid(), CustomItemRegistry.ID_TOTEM_TIME, worldTime + COOLDOWN_TICKS);

        // Pesan
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§b§l⌛ TIME SLOW AKTIF! ⌛"), false);
        player.sendMessage(Text.literal("§7" + affected + " mob melambat 50% selama 10 detik!"), false);

        int remaining = storage.getCharge(player.getUuid(), CustomItemRegistry.ID_TOTEM_TIME, DEFAULT_CHARGE);
        player.sendMessage(Text.literal("§7Charge tersisa: §e" + remaining + "/" + DEFAULT_CHARGE), false);

        if (remaining <= 0) {
            totem.setCount(0);
            player.sendMessage(Text.literal("§cTotem hancur setelah dipakai!"), false);
        }

        return true;
    }
}