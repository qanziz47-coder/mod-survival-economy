package rul.templatemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import rul.templatemod.economy.EconomyManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Perintah /bal dan /balance untuk menampilkan saldo pemain.
 */
public class BalanceCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // /bal
        dispatcher.register(
                CommandManager.literal("bal")
                        .executes(BalanceCommand::execute)
        );

        // /balance (alias)
        dispatcher.register(
                CommandManager.literal("balance")
                        .executes(BalanceCommand::execute)
        );

        // /money
        dispatcher.register(
                CommandManager.literal("money")
                        .executes(BalanceCommand::execute)
        );
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // Cek pengirim harus pemain (bukan console)
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya bisa dipakai oleh pemain."));
            return 0;
        }

        long balance = EconomyManager.getBalance(source.getServer(), player);

        Text message = Text.literal("Saldo kamu: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(EconomyManager.format(balance) + " Coins")
                        .formatted(Formatting.GOLD));

        player.sendMessage(message, false);
        return 1;
    }
}