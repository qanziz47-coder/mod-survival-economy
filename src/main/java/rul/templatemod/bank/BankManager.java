package rul.templatemod.bank;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import rul.templatemod.economy.EconomyManager;
import rul.templatemod.economy.TransactionResult;

import java.util.UUID;

/**
 * Manager logika bank: deposit, withdraw, transfer.
 * Semua transaksi harus lewat class ini agar konsisten.
 */
public class BankManager {

    // Bunga per hari (%)
    public static final double INTEREST_RATE = 1.0;

    // Minimum deposit / withdraw
    public static final long MIN_TRANSACTION = 1L;

    // ==================== SALDO ====================

    public static long getBalance(MinecraftServer server, UUID playerId) {
        return BankStorage.getServerState(server).getBalance(playerId);
    }

    public static long getBalance(MinecraftServer server, ServerPlayerEntity player) {
        return getBalance(server, player.getUuid());
    }

    // ==================== DEPOSIT ====================

    /**
     * Setor uang dari Coins (saldo utama) ke bank.
     * Validasi: uang cukup di Coins, saldo bank tidak overflow.
     */
    public static TransactionResult deposit(MinecraftServer server, UUID playerId, long amount) {
        if (amount < MIN_TRANSACTION) {
            return TransactionResult.failure("Minimum deposit adalah " + MIN_TRANSACTION + " Coins.");
        }

        // Cek saldo Coins utama
        long coinBalance = EconomyManager.getBalance(server, playerId);
        if (coinBalance < amount) {
            return TransactionResult.failure(
                    "Saldo Coins tidak cukup. Kamu punya " + EconomyManager.format(coinBalance) + " Coins.");
        }

        // Cek bank overflow
        BankStorage storage = BankStorage.getServerState(server);
        long currentBank = storage.getBalance(playerId);
        if (currentBank + amount > BankStorage.MAX_BANK_BALANCE) {
            return TransactionResult.failure(
                    "Saldo bank akan melebihi batas maksimum ("
                            + EconomyManager.format(BankStorage.MAX_BANK_BALANCE) + " Coins).");
        }

        // Kurangi Coins
        TransactionResult coinResult = EconomyManager.subtractBalance(server, playerId, amount);
        if (!coinResult.isSuccess()) {
            return TransactionResult.failure("Gagal kurangi Coins: " + coinResult.getMessage());
        }

        // Tambah ke bank
        storage.addBalance(playerId, amount);

        long newBankBalance = storage.getBalance(playerId);
        return TransactionResult.success(
                "Berhasil deposit " + EconomyManager.format(amount) + " Coins. "
                        + "Saldo bank: " + EconomyManager.format(newBankBalance) + " Coins",
                amount);
    }

    // ==================== WITHDRAW ====================

    /**
     * Ambil uang dari bank ke Coins.
     */
    public static TransactionResult withdraw(MinecraftServer server, UUID playerId, long amount) {
        if (amount < MIN_TRANSACTION) {
            return TransactionResult.failure("Minimum withdraw adalah " + MIN_TRANSACTION + " Coins.");
        }

        BankStorage storage = BankStorage.getServerState(server);
        long currentBank = storage.getBalance(playerId);
        if (currentBank < amount) {
            return TransactionResult.failure(
                    "Saldo bank tidak cukup. Saldo bank: " + EconomyManager.format(currentBank) + " Coins.");
        }

        // Kurangi bank
        if (!storage.subtractBalance(playerId, amount)) {
            return TransactionResult.failure("Gagal kurangi saldo bank.");
        }

        // Tambah ke Coins
        TransactionResult coinResult = EconomyManager.addBalance(server, playerId, amount);
        if (!coinResult.isSuccess()) {
            // Rollback: kembalikan ke bank
            storage.addBalance(playerId, amount);
            return TransactionResult.failure("Gagal tambah Coins: " + coinResult.getMessage());
        }

        long newBankBalance = storage.getBalance(playerId);
        long newCoinBalance = EconomyManager.getBalance(server, playerId);
        return TransactionResult.success(
                "Berhasil withdraw " + EconomyManager.format(amount) + " Coins. "
                        + "Saldo bank: " + EconomyManager.format(newBankBalance)
                        + " | Coins: " + EconomyManager.format(newCoinBalance),
                amount);
    }

    // ==================== BUNGA ====================

    /**
     * Terapkan bunga ke semua pemain. Dipanggil setiap hari Minecraft.
     */
    public static long applyInterest(MinecraftServer server) {
        BankStorage storage = BankStorage.getServerState(server);
        return storage.applyInterest(INTEREST_RATE);
    }

    /**
     * Simulasi: hitung saldo setelah N hari.
     * Rumus: balance * (1 + rate/100)^days
     */
    public static long simulateCompound(long principal, int days) {
        double rate = INTEREST_RATE / 100.0;
        double result = principal * Math.pow(1 + rate, days);
        return Math.round(result);
    }
}