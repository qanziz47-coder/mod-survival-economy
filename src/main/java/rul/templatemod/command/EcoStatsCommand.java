package rul.templatemod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import rul.templatemod.bank.BankManager;
import rul.templatemod.bank.BankStorage;
import rul.templatemod.economy.EconomyManager;
import rul.templatemod.economy.EconomyStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Perintah /eco stats — statistik ekonomi global server.
 */
public class EcoStatsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("ecostats")
                        .executes(ctx -> showStats(ctx.getSource()))
        );
    }

    private static int showStats(ServerCommandSource source) {
        MinecraftServer server = source.getServer();

        // Ambil data dari storage
        EconomyStorage economyStorage = EconomyStorage.getServerState(server);
        BankStorage bankStorage = BankStorage.getServerState(server);

        java.util.Set<UUID> allPlayers = new java.util.HashSet<>();
        allPlayers.addAll(economyStorage.getAllPlayers());
        allPlayers.addAll(bankStorage.getAllPlayers());

        // Hitung total
        long totalCoins = 0;
        long totalBank = 0;
        long totalWealth = 0;

        // Kumpulkan wealth per pemain
        List<PlayerWealth> wealthList = new ArrayList<>();

        for (UUID uuid : allPlayers) {
            long coins = economyStorage.getBalance(uuid);
            long bank = bankStorage.getBalance(uuid);
            long wealth = coins + bank;

            totalCoins += coins;
            totalBank += bank;
            totalWealth += wealth;

            // Cari nama pemain
            var player = server.getPlayerManager().getPlayer(uuid);
            String name = (player != null) ? player.getName().getString() : uuid.toString().substring(0, 8);

            wealthList.add(new PlayerWealth(name, coins, bank, wealth));
        }

        // Sort by wealth desc
        wealthList.sort(Comparator.comparingLong((PlayerWealth w) -> w.wealth).reversed());

        long avgWealth = allPlayers.isEmpty() ? 0 : totalWealth / allPlayers.size();

        // ==================== TAMPILKAN ====================
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§6§l=== STATISTIK EKONOMI GLOBAL ==="));
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§7Pemain terdaftar: §f" + allPlayers.size()));
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§7Total Coins (dompet): §e" + EconomyManager.format(totalCoins)));
        source.sendMessage(Text.literal("§7Total Coins (bank):   §a" + EconomyManager.format(totalBank)));
        source.sendMessage(Text.literal("§7Total Kekayaan:       §6" + EconomyManager.format(totalWealth)));
        source.sendMessage(Text.literal("§7Rata-rata:            §6" + EconomyManager.format(avgWealth)));
        source.sendMessage(Text.literal(""));

        // Top 5
        if (!wealthList.isEmpty()) {
            source.sendMessage(Text.literal("§6§l=== TOP 5 TERKAYA ==="));
            int limit = Math.min(5, wealthList.size());
            for (int i = 0; i < limit; i++) {
                PlayerWealth w = wealthList.get(i);
                String medal = switch (i) {
                    case 0 -> "§6①";
                    case 1 -> "§7②";
                    case 2 -> "§c③";
                    case 3 -> "§e④";
                    default -> "§f⑤";
                };
                source.sendMessage(Text.literal(
                        medal + " §f" + w.name + " §7— §6" + EconomyManager.format(w.wealth)
                                + " §7(§e" + EconomyManager.format(w.coins) + "§7 + §a"
                                + EconomyManager.format(w.bank) + "§7)"
                ));
            }
        }

        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§7§oStats hanya menghitung pemain online."));
        source.sendMessage(Text.literal(""));
        return 1;
    }

    // ==================== HELPER ====================

    private record PlayerWealth(String name, long coins, long bank, long wealth) {}
}