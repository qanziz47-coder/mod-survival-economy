package rul.templatemod.custom.items;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class PetaniHoe {

    public static final int RADIUS = 1;

    public static void harvest(ServerPlayerEntity player, BlockPos centerPos) {
        ServerWorld world = player.getServerWorld();
        int harvested = 0;
        int replanted = 0;

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                BlockPos pos = centerPos.add(dx, 0, dz);
                BlockState state = world.getBlockState(pos);

                if (state.getBlock() instanceof CropBlock crop) {
                    if (crop.isMature(state)) {
                        world.breakBlock(pos, true, player);
                        world.setBlockState(pos, crop.getDefaultState(), 3);
                        replanted++;
                        harvested++;
                    }
                }
            }
        }

        if (harvested > 0) {
            player.sendMessage(
                    Text.literal("§aAuto-harvest: §e" + harvested + " §acrop dipanen! §7(Replant: " + replanted + ")"),
                    false);
        } else {
            player.sendMessage(
                    Text.literal("§cTidak ada crop mature di area 3x3."),
                    false);
        }
    }
}