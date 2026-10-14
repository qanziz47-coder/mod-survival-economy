package rul.templatemod.custom.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Miner Pickaxe — auto-smelt ore saat ditambang.
 * Tambang iron ore → langsung iron ingot.
 */
public class MinerPickaxe {

    /**
     * Cek apakah block yang ditambang adalah ore, dan return item smelted-nya.
     */
    public static ItemStack getSmeltedResult(Block block) {
        // Ore → Ingot mapping
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return new ItemStack(Items.IRON_INGOT);
        }
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            return new ItemStack(Items.GOLD_INGOT);
        }
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return new ItemStack(Items.COPPER_INGOT);
        }
        if (block == Blocks.ANCIENT_DEBRIS) {
            return new ItemStack(Items.NETHERITE_SCRAP);
        }
        return null;
    }

    /**
     * Handle saat ore ditambang dengan Miner Pickaxe.
     * Drop item yang sudah di-smelt, bukan ore mentah.
     */
    public static void handleBreak(ServerPlayerEntity player, BlockPos pos, BlockState state, ServerWorld world) {
        ItemStack smelted = getSmeltedResult(state.getBlock());
        if (smelted == null) return;

        // Hapus block asli (drop ore mentah)
        world.breakBlock(pos, false, player);

        // Drop hasil smelted
        Block.dropStack(world, pos, smelted);

        player.sendMessage(
                Text.literal("§aAuto-smelt: §e" + smelted.getCount() + "x " + smelted.getName().getString()),
                true);
    }
}