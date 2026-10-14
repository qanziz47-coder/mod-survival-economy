package rul.templatemod.politics;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rul.templatemod.economy.TaxStorage;

import java.util.UUID;

/**
 * Manager logika politik: protes, referendum, voting.
 */
public class PoliticsManager {

    // ==================== PROTEST ====================

    public static String protest(MinecraftServer server, ServerPlayerEntity player) {
        PoliticsStorage storage = PoliticsStorage.getServerState(server);
        TaxStorage taxStorage = TaxStorage.getServerState(server);

        int currentTax = taxStorage.getSellTaxPercent();

        if (currentTax < PoliticsStorage.TAX_THRESHOLD_PROTEST) {
            return "§cPajak saat ini (" + currentTax + "%) masih wajar. "
                    + "Protes hanya bisa dilakukan kalau pajak ≥ "
                    + PoliticsStorage.TAX_THRESHOLD_PROTEST + "%.";
        }

        long worldTime = server.getOverworld().getTimeOfDay();
        UUID uuid = player.getUuid();

        long lastProtest = storage.getLastProtestTime(uuid);
        if (lastProtest > 0) {
            long cooldownTicks = (long) PoliticsStorage.PROTEST_COOLDOWN_HOURS * 1000L;
            long elapsed = worldTime - lastProtest;
            if (elapsed < cooldownTicks) {
                long remaining = cooldownTicks - elapsed;
                return "§cKamu sudah protes hari ini. Tunggu " + (remaining / 1000) + " tick lagi.";
            }
        }

        if (storage.hasProtested(uuid)) {
            return "§cKamu sudah tercatat sebagai pemprotes aktif.";
        }

        storage.addProtest(uuid, worldTime);

        int onlinePlayers = server.getPlayerManager().getPlayerList().size();
        int protestCount = storage.getProtestCount();
        double percentage = onlinePlayers > 0 ? (double) protestCount / onlinePlayers : 0;

        String baseMsg = "§c" + player.getName().getString() + " §fprotes! Pajak §e"
                + currentTax + "% §fterlalu tinggi.\n"
                + "§7Total protes: §e" + protestCount + "§7/§e" + onlinePlayers
                + " §7pemain (§e" + String.format("%.0f", percentage * 100) + "%§7)";

        if (percentage >= PoliticsStorage.PROTEST_THRESHOLD
                && !storage.isReferendumActive()) {

            long lastEnd = storage.getLastReferendumEndTime();
            if (lastEnd > 0) {
                long cooldownTicks = (long) PoliticsStorage.VOTE_COOLDOWN_DAYS * 24000L;
                long elapsed = worldTime - lastEnd;
                if (elapsed < cooldownTicks) {
                    long remainingHours = (cooldownTicks - elapsed) / 1000;
                    return baseMsg + "\n§cReferendum masih cooldown. "
                            + "Tunggu " + remainingHours + " hari Minecraft lagi.";
                }
            }

            storage.startReferendum(worldTime);
            storage.addHistory("[Referendum] Dibuka karena protes > 50%");

            broadcastToAll(server, "§6§l=== REFERENDUM PAJAK DIBUKA ===");
            broadcastToAll(server, "§7Pajak saat ini: §e" + currentTax + "%");
            broadcastToAll(server, "§7Durasi: §e3 menit");
            broadcastToAll(server, "§7Ketik:");
            broadcastToAll(server, "§e/vote A §7- Turunkan pajak 1%");
            broadcastToAll(server, "§e/vote B §7- Naikkan pajak 1%");
            broadcastToAll(server, "§e/vote C §7- Tetap");

            return baseMsg + "\n§aAmbang batas tercapai! Referendum dibuka.";
        }

        return baseMsg;
    }

    // ==================== VOTE ====================

    public static String vote(MinecraftServer server, ServerPlayerEntity player, int choice) {
        PoliticsStorage storage = PoliticsStorage.getServerState(server);

        if (!storage.isReferendumActive()) {
            return "§cTidak ada referendum aktif saat ini.";
        }

        UUID uuid = player.getUuid();

        if (storage.hasVoted(uuid)) {
            return "§cKamu sudah vote di referendum ini.";
        }

        if (!storage.addVote(uuid, choice)) {
            return "§cGagal vote. Coba lagi.";
        }

        String choiceName = switch (choice) {
            case 0 -> "§cTurunkan";
            case 1 -> "§aNaikkan";
            case 2 -> "§eTetap";
            default -> "Unknown";
        };

        broadcastToAll(server, "§7" + player.getName().getString()
                + " §7vote: " + choiceName);

        return "§aVote kamu tercatat: " + choiceName;
    }

    // ==================== SELESAIKAN REFERENDUM ====================

    public static void checkReferendumCompletion(MinecraftServer server) {
        PoliticsStorage storage = PoliticsStorage.getServerState(server);

        if (!storage.isReferendumActive()) return;

        long worldTime = server.getOverworld().getTimeOfDay();

        // Expired?
        if (storage.isReferendumExpired(worldTime)) {
            finishReferendum(server, storage, worldTime, "§7Durasi voting habis.");
            return;
        }

        // Semua pemain vote?
        int totalVoters = storage.getTotalVotes();
        int onlinePlayers = server.getPlayerManager().getPlayerList().size();

        if (totalVoters >= onlinePlayers && totalVoters >= PoliticsStorage.MIN_VOTERS) {
            finishReferendum(server, storage, worldTime, "§7Semua pemain sudah vote.");
        }
    }

    private static void finishReferendum(MinecraftServer server, PoliticsStorage storage,
                                         long worldTime, String reason) {
        int down = storage.getVoteDown();
        int up = storage.getVoteUp();
        int stay = storage.getVoteStay();
        int total = down + up + stay;

        TaxStorage taxStorage = TaxStorage.getServerState(server);
        int oldTax = taxStorage.getSellTaxPercent();
        int newTax = oldTax;

        // ==================== KASUS TIDAK ADA VOTE ====================
        if (total == 0) {
            newTax = Math.max(TaxStorage.MIN_TAX, oldTax - 1);
            taxStorage.setSellTaxPercent(newTax);

            storage.addHistory("[Auto-Referendum] Tidak ada vote → pajak turun otomatis: "
                    + oldTax + "% → " + newTax + "%");

            broadcastToAll(server, "§6§l=== HASIL REFERENDUM ===");
            broadcastToAll(server, "§cTidak ada pemain yang vote.");
            broadcastToAll(server, "§aKeputusan otomatis: Pajak turun dari §e"
                    + oldTax + "% §ake §e" + newTax + "%");
            broadcastToAll(server, "§7Sistem melindungi pemain dari pajak ekstrem.");

            storage.endReferendum(worldTime);
            return;
        }

        // ==================== TENTUKAN PEMENANG ====================
        int winner = 2;
        int maxVotes = stay;
        if (down > maxVotes) { winner = 0; maxVotes = down; }
        if (up > maxVotes) { winner = 1; maxVotes = up; }

        switch (winner) {
            case 0 -> newTax = Math.max(TaxStorage.MIN_TAX, oldTax - 1);
            case 1 -> newTax = Math.min(TaxStorage.MAX_TAX, oldTax + 1);
            case 2 -> newTax = oldTax;
        }

        taxStorage.setSellTaxPercent(newTax);

        String winnerName = switch (winner) {
            case 0 -> "TURUN";
            case 1 -> "NAIK";
            default -> "TETAP";
        };

        storage.addHistory("[Referendum] Hasil: " + winnerName
                + " | Votes: down=" + down + " up=" + up + " stay=" + stay
                + " | Pajak: " + oldTax + "% → " + newTax + "%");

        broadcastToAll(server, "§6§l=== HASIL REFERENDUM ===");
        broadcastToAll(server, reason);
        broadcastToAll(server, "§7Opsi A (Turun): §e" + down + " suara §7("
                + percent(down, total) + "%)");
        broadcastToAll(server, "§7Opsi B (Naik): §e" + up + " suara §7("
                + percent(up, total) + "%)");
        broadcastToAll(server, "§7Opsi C (Tetap): §e" + stay + " suara §7("
                + percent(stay, total) + "%)");
        broadcastToAll(server, "");

        if (newTax != oldTax) {
            broadcastToAll(server, "§aKeputusan: Pajak " + winnerName
                    + " dari §e" + oldTax + "% §ake §e" + newTax + "%");
        } else {
            broadcastToAll(server, "§ePajak tetap di §e" + oldTax + "%");
        }

        storage.endReferendum(worldTime);
    }

    // ==================== AUTO REFERENDUM ====================

    public static void checkAutoReferendum(MinecraftServer server) {
        PoliticsStorage storage = PoliticsStorage.getServerState(server);
        TaxStorage taxStorage = TaxStorage.getServerState(server);

        int currentTax = taxStorage.getSellTaxPercent();
        long worldTime = server.getOverworld().getTimeOfDay();
        long currentDay = worldTime / 24000L;

        // 1. Warning pajak tinggi (>=8%, tapi belum referendum)
        if (currentTax >= PoliticsStorage.TAX_WARNING_THRESHOLD
                && currentTax < PoliticsStorage.TAX_AUTO_REFERENDUM
                && !storage.isReferendumActive()) {
            // Kirim warning sekali per hari
            if (storage.getLastWarningDay() != currentDay) {
                broadcastToAll(server, "§e[Peringatan] §fPajak saat ini §c" + currentTax
                        + "% §fterlalu tinggi. Ketik §e/protes §funtuk protes!");
                storage.setLastWarningDay(currentDay);   // ← BARU: track warning
            }
        }

        // 2. Auto referendum kalau pajak > 12%
        if (currentTax > PoliticsStorage.TAX_AUTO_REFERENDUM
                && !storage.isReferendumActive()) {

            if (storage.getLastAutoReferendumDay() == currentDay) {
                return;
            }

            storage.startReferendum(worldTime);
            storage.setLastAutoReferendumDay(currentDay);
            storage.addHistory("[Auto-Referendum] Dibuka karena pajak " + currentTax + "% > "
                    + PoliticsStorage.TAX_AUTO_REFERENDUM + "%");

            broadcastToAll(server, "§c§l=== PAJAK TERLALU TINGGI! ===");
            broadcastToAll(server, "§7Pajak saat ini: §c" + currentTax + "%");
            broadcastToAll(server, "§7Sistem otomatis membuka referendum.");
            broadcastToAll(server, "");
            broadcastToAll(server, "§7Ketik:");
            broadcastToAll(server, "§e/vote A §7- Turunkan pajak 1%");
            broadcastToAll(server, "§e/vote B §7- Naikkan pajak 1%");
            broadcastToAll(server, "§e/vote C §7- Tetap");
        }
    }

    // ==================== UTIL ====================

    private static void broadcastToAll(MinecraftServer server, String message) {
        Text text = Text.literal(message);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            p.sendMessage(text, false);
        }
    }

    private static int percent(int value, int total) {
        if (total == 0) return 0;
        return Math.round(value * 100f / total);
    }

    // ==================== INFO ====================

    public static String getInfo(MinecraftServer server) {
        PoliticsStorage storage = PoliticsStorage.getServerState(server);
        TaxStorage taxStorage = TaxStorage.getServerState(server);

        StringBuilder sb = new StringBuilder();
        sb.append("§6§l=== POLITIK EKONOMI ===\n");
        sb.append("§7Pajak jual: §e").append(taxStorage.getSellTaxPercent()).append("%\n");
        sb.append("§7Pajak beli: §e").append(taxStorage.getBuyTaxPercent()).append("%\n");
        sb.append("§7Protes aktif: §e").append(storage.getProtestCount()).append("\n");
        sb.append("§7Referendum: ")
                .append(storage.isReferendumActive() ? "§aAKTIF" : "§cTIDAK AKTIF").append("\n");

        if (storage.isReferendumActive()) {
            sb.append("§7Total vote: §e").append(storage.getTotalVotes()).append("\n");
        }

        sb.append("\n§7Perintah:");
        sb.append("\n§e/protes §7- Protes pajak tinggi");
        sb.append("\n§e/vote A/B/C §7- Vote di referendum");
        sb.append("\n§e/politik §7- Info politik");

        return sb.toString();
    }
}