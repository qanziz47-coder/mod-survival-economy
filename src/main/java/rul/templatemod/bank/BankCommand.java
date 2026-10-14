package rul.templatemod.bank;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rul.templatemod.economy.EconomyManager;
import rul.templatemod.economy.TransactionResult;
import rul.templatemod.util.SoundHelper;

/**
 * Perintah /bank untuk deposit, withdraw, cek saldo, dan simulasi bunga.
 */
public class BankCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("bank")
                        // /bank
                        .executes(ctx -> showInfo(ctx.getSource()))

                        // /bank balance
                        .then(CommandManager.literal("balance")
                                .executes(ctx -> showBalance(ctx.getSource())))

                        // /bank deposit <jumlah>
                        .then(CommandManager.literal("deposit")
                                .then(CommandManager.argument("jumlah", LongArgumentType.longArg(1))
                                        .executes(ctx -> deposit(
                                                ctx.getSource(),
                                                LongArgumentType.getLong(ctx, "jumlah")))))

                        // /bank withdraw <jumlah>
                        .then(CommandManager.literal("withdraw")
                                .then(CommandManager.argument("jumlah", LongArgumentType.longArg(1))
                                        .executes(ctx -> withdraw(
                                                ctx.getSource(),
                                                LongArgumentType.getLong(ctx, "jumlah")))))

                        // /bank simulasi <jumlah> <hari>
                        .then(CommandManager.literal("simulasi")
                                .then(CommandManager.argument("jumlah", LongArgumentType.longArg(1))
                                        .then(CommandManager.argument("hari", LongArgumentType.longArg(1, 3650))
                                                .executes(ctx -> simulate(
                                                        ctx.getSource(),
                                                        LongArgumentType.getLong(ctx, "jumlah"),
                                                        (int) LongArgumentType.getLong(ctx, "hari"))))))
        );
    }

    // ==================== INFO ====================

    private static int showInfo(ServerCommandSource source) {
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§6§l=== BANK SURVIVAL ECONOMY ==="));
        source.sendMessage(Text.literal("§7Bunga: §a" + BankManager.INTEREST_RATE + "% §7per hari (compound)"));
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§e/bank balance §7- Cek saldo bank"));
        source.sendMessage(Text.literal("§e/bank deposit <jumlah> §7- Setor Coins ke bank"));
        source.sendMessage(Text.literal("§e/bank withdraw <jumlah> §7- Ambil dari bank"));
        source.sendMessage(Text.literal("§e/bank simulasi <jumlah> <hari> §7- Simulasi bunga"));
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§7§oCompound interest: uang tumbuh otomatis"));
        source.sendMessage(Text.literal("§7§otiap hari tanpa melakukan apa-apa."));
        source.sendMessage(Text.literal(""));
        return 1;
    }

    // ==================== BALANCE ====================

    private static int showBalance(ServerCommandSource source) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        long bankBalance = BankManager.getBalance(source.getServer(), player.getUuid());
        long coinBalance = EconomyManager.getBalance(source.getServer(), player.getUuid());

        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§6§l=== SALDO BANK ==="));
        source.sendMessage(Text.literal("§7Bank: §a" + EconomyManager.format(bankBalance) + " Coins"));
        source.sendMessage(Text.literal("§7Coins: §e" + EconomyManager.format(coinBalance) + " Coins"));
        source.sendMessage(Text.literal("§7Total: §6"
                + EconomyManager.format(bankBalance + coinBalance) + " Coins"));

        // Simulasi 30 hari
        if (bankBalance > 0) {
            long after30 = BankManager.simulateCompound(bankBalance, 30);
            long after365 = BankManager.simulateCompound(bankBalance, 365);
            source.sendMessage(Text.literal(""));
            source.sendMessage(Text.literal("§7Proyeksi bunga:"));
            source.sendMessage(Text.literal("§730 hari: §a" + EconomyManager.format(after30)
                    + " §7(+" + EconomyManager.format(after30 - bankBalance) + ")"));
            source.sendMessage(Text.literal("§7365 hari: §a" + EconomyManager.format(after365)
                    + " §7(+" + EconomyManager.format(after365 - bankBalance) + ")"));
        }
        source.sendMessage(Text.literal(""));
        return 1;
    }

    // ==================== DEPOSIT ====================

    private static int deposit(ServerCommandSource source, long amount) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        TransactionResult result = BankManager.deposit(source.getServer(), player.getUuid(), amount);
        if (result.isSuccess()) {
            player.sendMessage(Text.literal("§a" + result.getMessage()), false);
            SoundHelper.playDeposit(player);
        } else {
            player.sendMessage(Text.literal("§c" + result.getMessage()), false);
            SoundHelper.playError(player);
        }
        return result.isSuccess() ? 1 : 0;
    }

    // ==================== WITHDRAW ====================

    private static int withdraw(ServerCommandSource source, long amount) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        TransactionResult result = BankManager.withdraw(source.getServer(), player.getUuid(), amount);
        if (result.isSuccess()) {
            player.sendMessage(Text.literal("§a" + result.getMessage()), false);
            SoundHelper.playWithdraw(player);
        } else {
            player.sendMessage(Text.literal("§c" + result.getMessage()), false);
            SoundHelper.playError(player);
        }
        return result.isSuccess() ? 1 : 0;
    }

    // ==================== SIMULASI ====================

    private static int simulate(ServerCommandSource source, long principal, int days) {
        long result = BankManager.simulateCompound(principal, days);
        long interest = result - principal;

        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§6§l=== SIMULASI BUNGA MAJEMUK ==="));
        source.sendMessage(Text.literal("§7Pokok: §e" + EconomyManager.format(principal) + " Coins"));
        source.sendMessage(Text.literal("§7Bunga: §a" + BankManager.INTEREST_RATE + "% §7per hari"));
        source.sendMessage(Text.literal("§7Durasi: §e" + days + " hari"));
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§aHasil: §6" + EconomyManager.format(result) + " Coins"));
        source.sendMessage(Text.literal("§aKeuntungan: §6+" + EconomyManager.format(interest) + " Coins"));
        source.sendMessage(Text.literal("§7Pertumbuhan: §e"
                + String.format("%.1f", (interest * 100.0 / principal)) + "%"));
        source.sendMessage(Text.literal(""));
        return 1;
    }
}