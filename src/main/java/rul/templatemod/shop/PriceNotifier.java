package rul.templatemod.shop;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Notifikasi harga — broadcast saat harga naik/turun drastis.
 */
public class PriceNotifier {

    // Threshold untuk broadcast (20%)
    public static final double THRESHOLD = 0.20;

    /**
     * Cek harga sebelum vs sesudah, broadcast kalau perubahan > threshold.
     *
     * @param oldPrice Harga dasar sebelum transaksi
     * @param newPrice Harga setelah transaksi
     */
    public static void checkAndNotify(MinecraftServer server, ShopItem item,
                                      long oldPrice, long newPrice) {
        if (oldPrice <= 0) return;

        // Hitung persentase perubahan
        double changePercent = ((double) (newPrice - oldPrice) / oldPrice);

        // Cek threshold
        if (Math.abs(changePercent) < THRESHOLD) return;

        // Format pesan
        String direction;
        String color;
        String arrow;

        if (changePercent > 0) {
            direction = "naik";
            color = "§c";
            arrow = "§c↑";
        } else {
            direction = "turun";
            color = "§a";
            arrow = "§a↓";
        }

        String percentStr = String.format("%.0f", Math.abs(changePercent) * 100);
        String message = "§e[Pasar] §fHarga §b" + item.getDisplayName() + " §f" + direction
                + " " + color + "+" + percentStr + "% §f("
                + oldPrice + " → " + newPrice + ") " + arrow;

        // Broadcast ke semua pemain
        broadcastToAll(server, message);
    }

    /**
     * Broadcast pesan ke semua pemain online.
     */
    private static void broadcastToAll(MinecraftServer server, String message) {
        Text text = Text.literal(message);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(text, false);
        }
    }

    /**
     * Alternatif: cek berdasarkan multiplier.
     * Dipakai kalau tidak tahu old price — dari stok.
     */
    public static void checkByMultiplier(MinecraftServer server, ShopItem item,
                                         double oldMultiplier, double newMultiplier) {
        if (oldMultiplier <= 0) return;

        double changePercent = (newMultiplier - oldMultiplier) / oldMultiplier;

        if (Math.abs(changePercent) < THRESHOLD) return;

        String direction;
        String color;
        String arrow;

        if (changePercent > 0) {
            direction = "naik";
            color = "§c";
            arrow = "§c↑";
        } else {
            direction = "turun";
            color = "§a";
            arrow = "§a↓";
        }

        String percentStr = String.format("%.0f", Math.abs(changePercent) * 100);
        String message = "§e[Pasar] §fHarga §b" + item.getDisplayName() + " §f" + direction
                + " " + color + "+" + percentStr + "% " + arrow;

        broadcastToAll(server, message);
    }
}