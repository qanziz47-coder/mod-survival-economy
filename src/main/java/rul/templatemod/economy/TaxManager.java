package rul.templatemod.economy;

import net.minecraft.server.MinecraftServer;

/**
 * Logika perhitungan pajak.
 * Semua perhitungan pajak harus lewat class ini agar konsisten.
 */
public class TaxManager {

    // ==================== HARGA SETELAH PAJAK ====================

    /**
     * Hitung total biaya beli setelah PPN.
     * Rumus: harga * (1 + buyTax/100)
     */
    public static long calculateBuyCost(MinecraftServer server, long baseCost) {
        int tax = TaxStorage.getServerState(server).getBuyTaxPercent();
        if (tax <= 0) return baseCost;
        return baseCost + Math.round(baseCost * tax / 100.0);
    }

    /**
     * Hitung uang yang diterima setelah PPh.
     * Rumus: harga * (1 - sellTax/100)
     */
    public static long calculateSellGain(MinecraftServer server, long baseGain) {
        int tax = TaxStorage.getServerState(server).getSellTaxPercent();
        if (tax <= 0) return baseGain;
        long taxAmount = Math.round(baseGain * tax / 100.0);
        return baseGain - taxAmount;
    }

    // ==================== DETAIL PAJAK ====================

    /**
     * Ambil jumlah pajak yang dibayar untuk pembelian.
     */
    public static long getBuyTaxAmount(MinecraftServer server, long baseCost) {
        long total = calculateBuyCost(server, baseCost);
        return total - baseCost;
    }

    /**
     * Ambil jumlah pajak yang dipotong dari penjualan.
     */
    public static long getSellTaxAmount(MinecraftServer server, long baseGain) {
        long net = calculateSellGain(server, baseGain);
        return baseGain - net;
    }

    // ==================== FORMAT PESAN ====================

    /**
     * Format pesan pajak untuk pembelian.
     * Contoh: "Harga dasar 500, PPN 2% (10), Total 510"
     */
    public static String formatBuyTaxInfo(MinecraftServer server, long baseCost) {
        int taxPercent = TaxStorage.getServerState(server).getBuyTaxPercent();
        if (taxPercent <= 0) {
            return "§7Total: §e" + EconomyManager.format(baseCost) + " Coins";
        }

        long taxAmount = getBuyTaxAmount(server, baseCost);
        long total = calculateBuyCost(server, baseCost);

        return "§7Harga: §e" + EconomyManager.format(baseCost)
                + " §7+ PPN §c" + taxPercent + "% §7(§c" + EconomyManager.format(taxAmount) + "§7)"
                + " = §6" + EconomyManager.format(total) + " Coins";
    }

    /**
     * Format pesan pajak untuk penjualan.
     */
    public static String formatSellTaxInfo(MinecraftServer server, long baseGain) {
        int taxPercent = TaxStorage.getServerState(server).getSellTaxPercent();
        if (taxPercent <= 0) {
            return "§7Diterima: §a" + EconomyManager.format(baseGain) + " Coins";
        }

        long taxAmount = getSellTaxAmount(server, baseGain);
        long net = calculateSellGain(server, baseGain);

        return "§7Harga: §e" + EconomyManager.format(baseGain)
                + " §7- PPh §c" + taxPercent + "% §7(§c" + EconomyManager.format(taxAmount) + "§7)"
                + " = §a" + EconomyManager.format(net) + " Coins";
    }
}