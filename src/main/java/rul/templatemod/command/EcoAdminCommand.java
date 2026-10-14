package rul.templatemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rul.templatemod.economy.EconomyManager;
import rul.templatemod.economy.EconomyStorage;
import rul.templatemod.economy.TransactionResult;
import rul.templatemod.shop.ShopManager;
import rul.templatemod.shop.ShopStorage;



/**
 * Perintah admin /eco untuk mengelola ekonomi.
 */
public class EcoAdminCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("eco")
                        .requires(source -> source.hasPermissionLevel(2))

                        // /eco give <player> <jumlah>
                        .then(CommandManager.literal("give")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(CommandManager.argument("jumlah", IntegerArgumentType.integer(1))
                                                .executes(ctx -> give(
                                                        ctx.getSource(),
                                                        EntityArgumentType.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "jumlah")
                                                ))
                                        )
                                )
                        )

                        // /eco take <player> <jumlah>
                        .then(CommandManager.literal("take")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(CommandManager.argument("jumlah", IntegerArgumentType.integer(1))
                                                .executes(ctx -> take(
                                                        ctx.getSource(),
                                                        EntityArgumentType.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "jumlah")
                                                ))
                                        )
                                )
                        )

                        // /eco set <player> <jumlah>
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(CommandManager.argument("jumlah", IntegerArgumentType.integer(0))
                                                .executes(ctx -> set(
                                                        ctx.getSource(),
                                                        EntityArgumentType.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "jumlah")
                                                ))
                                        )
                                )
                        )

                        // /eco reset <player>
                        .then(CommandManager.literal("reset")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(ctx -> reset(
                                                ctx.getSource(),
                                                EntityArgumentType.getPlayer(ctx, "player")
                                        ))
                                )
                        )

                        // /eco resetstock
                        .then(CommandManager.literal("resetstock")
                                .executes(ctx -> resetStock(ctx.getSource()))
                        )
                        // /eco settax <jenis> <persen>
                        .then(CommandManager.literal("settax")
                                .then(CommandManager.literal("sell")
                                        .then(CommandManager.argument("persen", IntegerArgumentType.integer(0, 30))
                                                .executes(ctx -> setSellTax(
                                                        ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "persen")))))
                                .then(CommandManager.literal("buy")
                                        .then(CommandManager.argument("persen", IntegerArgumentType.integer(0, 30))
                                                .executes(ctx -> setBuyTax(
                                                        ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "persen")))))
                        )

                        // /eco showtax
                        .then(CommandManager.literal("showtax")
                                .executes(ctx -> showTax(ctx.getSource())))
                        // /eco forcevote
                        .then(CommandManager.literal("forcevote")
                                .executes(ctx -> forceVote(ctx.getSource())))

                        // /eco resetprotes
                        .then(CommandManager.literal("resetprotes")
                                .executes(ctx -> resetProtes(ctx.getSource())))
        );
    }

    private static int forceVote(ServerCommandSource source) {
        var storage = rul.templatemod.politics.PoliticsStorage.getServerState(source.getServer());
        if (storage.isReferendumActive()) {
            source.sendMessage(Text.literal("§cReferendum sudah aktif."));
            return 0;
        }
        long worldTime = source.getServer().getOverworld().getTimeOfDay();
        storage.startReferendum(worldTime);
        source.sendMessage(Text.literal("§aReferendum dipaksa terbuka."));
        for (var p : source.getServer().getPlayerManager().getPlayerList()) {
            p.sendMessage(Text.literal("§6§l=== REFERENDUM PAJAK DIBUKA (admin) ==="), false);
            p.sendMessage(Text.literal("§e/vote A §7- Turunkan"), false);
            p.sendMessage(Text.literal("§e/vote B §7- Naikkan"), false);
            p.sendMessage(Text.literal("§e/vote C §7- Tetap"), false);
        }
        return 1;
    }

    private static int resetProtes(ServerCommandSource source) {
        var storage = rul.templatemod.politics.PoliticsStorage.getServerState(source.getServer());
        storage.clearProtests();
        source.sendMessage(Text.literal("§aSemua protes direset."));
        return 1;
    }

    private static int setSellTax(ServerCommandSource source, int percent) {
        rul.templatemod.economy.TaxStorage storage =
                rul.templatemod.economy.TaxStorage.getServerState(source.getServer());
        storage.setSellTaxPercent(percent);
        source.sendMessage(Text.literal("§aPajak penjualan (PPh) diubah menjadi §e" + percent + "%"));
        return 1;
    }

    private static int setBuyTax(ServerCommandSource source, int percent) {
        rul.templatemod.economy.TaxStorage storage =
                rul.templatemod.economy.TaxStorage.getServerState(source.getServer());
        storage.setBuyTaxPercent(percent);
        source.sendMessage(Text.literal("§aPajak pembelian (PPN) diubah menjadi §e" + percent + "%"));
        return 1;
    }

    private static int showTax(ServerCommandSource source) {
        rul.templatemod.economy.TaxStorage storage =
                rul.templatemod.economy.TaxStorage.getServerState(source.getServer());
        source.sendMessage(Text.literal("§6§l=== PAJAK SAAT INI ==="));
        source.sendMessage(Text.literal("§7PPh (Jual): §c" + storage.getSellTaxPercent() + "%"));
        source.sendMessage(Text.literal("§7PPN (Beli): §c" + storage.getBuyTaxPercent() + "%"));
        return 1;
    }
    private static int give(ServerCommandSource source, ServerPlayerEntity target, int amount) {
        TransactionResult result = EconomyManager.addBalance(
                source.getServer(), target.getUuid(), amount);

        if (!result.isSuccess()) {
            source.sendError(Text.literal("§cGagal: " + result.getMessage()));
            return 0;
        }

        source.sendMessage(Text.literal("§aBerhasil memberi §e" + EconomyManager.format(amount)
                + " Coins §akepada §e" + target.getName().getString()));

        target.sendMessage(Text.literal("§aKamu menerima §e" + EconomyManager.format(amount)
                + " Coins §adari admin."), false);

        return 1;
    }

    private static int take(ServerCommandSource source, ServerPlayerEntity target, int amount) {
        TransactionResult result = EconomyManager.subtractBalance(
                source.getServer(), target.getUuid(), amount);

        if (!result.isSuccess()) {
            source.sendError(Text.literal("§cGagal: " + result.getMessage()));
            return 0;
        }

        source.sendMessage(Text.literal("§aBerhasil mengambil §e" + EconomyManager.format(amount)
                + " Coins §adari §e" + target.getName().getString()));

        target.sendMessage(Text.literal("§cAdmin mengambil §e" + EconomyManager.format(amount)
                + " Coins §cdari saldo kamu."), false);

        return 1;
    }

    private static int set(ServerCommandSource source, ServerPlayerEntity target, int amount) {
        EconomyStorage storage = EconomyStorage.getServerState(source.getServer());
        long current = storage.getBalance(target.getUuid());

        if (amount > current) {
            storage.addBalance(target.getUuid(), amount - current);
        } else if (amount < current) {
            storage.subtractBalance(target.getUuid(), current - amount);
        }

        source.sendMessage(Text.literal("§aSaldo §e" + target.getName().getString()
                + " §adiset ke §e" + EconomyManager.format(amount) + " Coins"));

        target.sendMessage(Text.literal("§aSaldo kamu diset ke §e"
                + EconomyManager.format(amount) + " Coins §aoleh admin."), false);

        return 1;
    }

    private static int reset(ServerCommandSource source, ServerPlayerEntity target) {
        EconomyStorage storage = EconomyStorage.getServerState(source.getServer());
        long current = storage.getBalance(target.getUuid());
        long targetBalance = EconomyStorage.STARTING_BALANCE;

        if (current > targetBalance) {
            storage.subtractBalance(target.getUuid(), current - targetBalance);
        } else if (current < targetBalance) {
            storage.addBalance(target.getUuid(), targetBalance - current);
        }

        source.sendMessage(Text.literal("§aSaldo §e" + target.getName().getString()
                + " §adireset ke §e" + EconomyManager.format(targetBalance) + " Coins"));

        target.sendMessage(Text.literal("§aSaldo kamu direset ke §e"
                + EconomyManager.format(targetBalance) + " Coins §aoleh admin."), false);

        return 1;
    }

    private static int resetStock(ServerCommandSource source) {
        ShopStorage storage = ShopStorage.getServerState(source.getServer());

        for (var item : ShopManager.getAllItems()) {
            if (item.isDynamicPricing()) {
                storage.setStock(item.getItemId(), item.getBaseStock());
            }
        }

        source.sendMessage(Text.literal("§aSemua stok toko berhasil direset ke nilai default."));
        return 1;
    }
}