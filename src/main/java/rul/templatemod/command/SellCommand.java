package rul.templatemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import rul.templatemod.economy.EconomyManager;
import rul.templatemod.economy.TransactionResult;
import rul.templatemod.shop.ShopItem;
import rul.templatemod.shop.ShopManager;

/**
 * Perintah /sell untuk menjual item.
 */
public class SellCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("sell")
                        .then(CommandManager.literal("hand")
                                .executes(ctx -> sellHand(ctx.getSource(), 1)))
                        .then(CommandManager.literal("all")
                                .executes(ctx -> sellAll(ctx.getSource())))
                        .then(CommandManager.argument("jumlah", IntegerArgumentType.integer(1))
                                .executes(ctx -> sellHand(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "jumlah"))))
        );
    }

    // ==================== /sell hand ====================

    private static int sellHand(ServerCommandSource source, int amount) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) {
            player.sendMessage(Text.literal("§cKamu tidak memegang item apapun."), false);
            return 0;
        }

        String itemId = Registries.ITEM.getId(held.getItem()).toString();
        ShopItem shopItem = ShopManager.getItem(itemId);

        if (shopItem == null || !shopItem.canSell()) {
            player.sendMessage(Text.literal("§cItem ini tidak bisa dijual: §f" + itemId), false);
            return 0;
        }

        if (amount > held.getCount()) {
            player.sendMessage(Text.literal("§cKamu hanya punya §e" + held.getCount()
                    + "x §citem ini."), false);
            return 0;
        }

        // Ambil item
        held.decrement(amount);
        if (held.isEmpty()) {
            player.getInventory().setStack(player.getInventory().selectedSlot, ItemStack.EMPTY);
        }

        // Tambah saldo
        long gain = shopItem.getSellPrice() * amount;
        TransactionResult result = EconomyManager.addBalance(source.getServer(), player.getUuid(), gain);

        if (!result.isSuccess()) {
            // Rollback
            player.getInventory().insertStack(new ItemStack(held.getItem(), amount));
            player.sendMessage(Text.literal("§cGagal: " + result.getMessage()), false);
            return 0;
        }

        player.sendMessage(
                Text.literal("§aTerjual §e" + amount + "x " + shopItem.getDisplayName()
                        + " §7seharga §e" + EconomyManager.format(gain) + " Coins"),
                false
        );
        return 1;
    }

    // ==================== /sell all ====================

    private static int sellAll(ServerCommandSource source) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        long totalGain = 0;
        int totalItems = 0;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            ShopItem shopItem = ShopManager.getItem(itemId);

            if (shopItem == null || !shopItem.canSell()) continue;

            int count = stack.getCount();
            long gain = shopItem.getSellPrice() * count;

            player.getInventory().setStack(i, ItemStack.EMPTY);
            totalGain += gain;
            totalItems += count;
        }

        if (totalItems == 0) {
            player.sendMessage(Text.literal("§cTidak ada item yang bisa dijual di inventaris."), false);
            return 0;
        }

        TransactionResult result = EconomyManager.addBalance(source.getServer(), player.getUuid(), totalGain);

        if (!result.isSuccess()) {
            player.sendMessage(Text.literal("§cGagal: " + result.getMessage()), false);
            return 0;
        }

        player.sendMessage(
                Text.literal("§aTerjual §e" + totalItems + " §aitem dengan total §e"
                        + EconomyManager.format(totalGain) + " Coins"),
                false
        );
        return 1;
    }
}