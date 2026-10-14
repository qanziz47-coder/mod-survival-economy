package rul.templatemod.custom.items;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Builder Wand — place 10 blok sekaligus.
 */
public class BuilderWand {

    public static final int PLACE_LIMIT = 10;

    public static void placeLine(ServerPlayerEntity player, BlockPos startPos, Direction facing) {
        ServerWorld world = player.getServerWorld();
        ItemStack held = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        // Ambil blok dari off-hand (kalau ada)
        ItemStack blockSource = null;
        if (offHand.getItem() instanceof BlockItem) {
            blockSource = offHand;
        } else if (held.getItem() instanceof BlockItem) {
            blockSource = held;
        }

        if (blockSource == null) {
            player.sendMessage(Text.literal("§cPegang blok di off-hand untuk Builder Wand."), true);
            return;
        }

        BlockItem blockItem = (BlockItem) blockSource.getItem();
        BlockState placeState = blockItem.getBlock().getDefaultState();

        int placed = 0;
        BlockPos currentPos = startPos;

        for (int i = 0; i < PLACE_LIMIT; i++) {
            currentPos = currentPos.offset(facing);

            // Cek apakah posisi kosong
            if (!world.getBlockState(currentPos).isAir()) {
                continue;
            }

            // Cek jarak dengan player (max 10 blok)
            if (currentPos.getManhattanDistance(player.getBlockPos()) > 10) {
                break;
            }

            world.setBlockState(currentPos, placeState, 3);
            placed++;
        }

        if (placed > 0) {
            player.sendMessage(Text.literal("§aBuilder Wand: §e" + placed + " §ablok terpasang!"), true);
        } else {
            player.sendMessage(Text.literal("§cTidak ada tempat kosong untuk blok."), true);
        }
    }
}