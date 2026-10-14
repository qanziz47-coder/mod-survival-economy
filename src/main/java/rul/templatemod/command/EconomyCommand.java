package rul.templatemod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Perintah /economy — menampilkan bantuan semua perintah ekonomi.
 */
public class EconomyCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("economy")
                        .executes(ctx -> showHelp(ctx.getSource()))
        );
    }

    private static int showHelp(ServerCommandSource source) {
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§6§l=== SURVIVAL ECONOMY ==="));
        source.sendMessage(Text.literal("§7Perintah yang tersedia:"));
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§e/bal §7- Lihat saldo kamu"));
        source.sendMessage(Text.literal("§e/balance §7- Alias /bal"));
        source.sendMessage(Text.literal("§e/money §7- Lihat saldo"));
        source.sendMessage(Text.literal("§e/shop §7- Buka toko"));
        source.sendMessage(Text.literal("§e/sell hand §7- Jual item di tangan"));
        source.sendMessage(Text.literal("§e/sell all §7- Jual semua item"));
        source.sendMessage(Text.literal("§e/sell <jumlah> §7- Jual sejumlah item"));
        source.sendMessage(Text.literal("§e/economy §7- Tampilkan bantuan ini"));
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§7§oTip: Klik kiri item di toko untuk beli,"));
        source.sendMessage(Text.literal("§7§oklik kanan untuk jual."));
        source.sendMessage(Text.literal(""));
        return 1;
    }
}