package rul.templatemod.custom;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import rul.templatemod.custom.items.PetaniHoe;
import rul.templatemod.custom.items.TotemOfResurrection;
import rul.templatemod.custom.items.TotemOfTime;
import rul.templatemod.custom.items.TotemOfVengeance;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import rul.templatemod.custom.items.MinerPickaxe;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import rul.templatemod.custom.items.AdventurerBlade;
import rul.templatemod.custom.items.BuilderWand;
import net.minecraft.util.Hand;

/**
 * Event listener untuk custom item.
 */
public class CustomItemEvents {

    private static int tickCounter = 0;

    public static void register() {

        // 1. RESURRECTION — cek saat player hampir mati
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return true;
            boolean used = TotemOfResurrection.tryUse(player);
            return !used;
        });

        // 2. VENGEANCE — cek saat player menerima damage
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return true;
            TotemOfVengeance.tryTrigger(player, source, amount);
            return true;
        });

        // 3. TIME — cek setiap tick (HP rendah)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter < 20) return;
            tickCounter = 0;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                TotemOfTime.tryTrigger(player);
            }
        });

        // 4. PETANI HOE — klik kanan crop
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack held = serverPlayer.getStackInHand(hand);
            if (CustomItemBuilder.isCustomItem(held, CustomItemRegistry.ID_PETANI_HOE)) {
                PetaniHoe.harvest(serverPlayer, hitResult.getBlockPos());
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
        // 5. MINER PICKAXE — auto-smelt saat tambang ore
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) return true;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return true;

            ItemStack held = serverPlayer.getMainHandStack();
            if (CustomItemBuilder.isCustomItem(held, CustomItemRegistry.ID_MINER_PICKAXE)) {
                ItemStack smelted = MinerPickaxe.getSmeltedResult(state.getBlock());
                if (smelted != null) {
                    // Ganti drop ore mentah dengan smelted
                    if (world instanceof net.minecraft.server.world.ServerWorld sw) {
                        MinerPickaxe.handleBreak(serverPlayer, pos, state, sw);
                    }
                    return false; // batalkan break normal
                }
            }
            return true;
        });
        // Alternatif: pakai ServerLivingEntityEvents.AFTER_DEATH
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (damageSource.getAttacker() instanceof ServerPlayerEntity player) {
                if (entity != player) {
                    AdventurerBlade.onKill(player, entity);
                }
            }
        });
        // 7. BUILDER WAND — place 10 blok sekaligus
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack held = serverPlayer.getMainHandStack();
            if (CustomItemBuilder.isCustomItem(held, CustomItemRegistry.ID_BUILDER_WAND)) {
                // Hanya trigger kalau sneak
                if (serverPlayer.isSneaking()) {
                    BuilderWand.placeLine(serverPlayer, hitResult.getBlockPos(),
                            serverPlayer.getHorizontalFacing());
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
    }

}