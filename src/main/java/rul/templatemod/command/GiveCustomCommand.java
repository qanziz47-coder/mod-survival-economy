package rul.templatemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rul.templatemod.custom.CustomItemRegistry;

/**
 * Perintah /givecustom <item> — untuk testing custom item.
 * Butuh OP level 2.
 */
public class GiveCustomCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("givecustom")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("item", StringArgumentType.string())
                                .executes(ctx -> give(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "item"))))
        );
    }

    private static int give(ServerCommandSource source, String itemId) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        ItemStack item = switch (itemId) {
            case "totem_resurrection" -> CustomItemRegistry.createTotemOfResurrection();
            case "totem_vengeance" -> CustomItemRegistry.createTotemOfVengeance();
            case "totem_time" -> CustomItemRegistry.createTotemOfTime();
            case "petani_hoe" -> CustomItemRegistry.createPetaniHoe();
            case "miner_pickaxe" -> CustomItemRegistry.createMinerPickaxe();
            case "adventurer_blade" -> CustomItemRegistry.createAdventurerBlade();
            case "builder_wand" -> CustomItemRegistry.createBuilderWand();
            case "trader_ring" -> CustomItemRegistry.createTraderRing();
            default -> null;
        };

        if (item == null) {
            source.sendError(Text.literal("Item tidak valid. Pilih: totem_resurrection, totem_vengeance, totem_time, petani_hoe, miner_pickaxe, adventurer_blade"));
            return 0;
        }

        player.getInventory().insertStack(item);
        source.sendMessage(Text.literal("§aKamu mendapat §e" + itemId));
        return 1;
    }
}