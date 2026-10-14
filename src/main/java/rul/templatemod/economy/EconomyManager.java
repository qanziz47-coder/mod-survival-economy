package rul.templatemod.economy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Manager utama untuk semua transaksi ekonomi.
 * Semua perubahan saldo WAJIB lewat class ini agar konsisten.
 */
public class EconomyManager {

    // Format angka dengan pemisah ribuan titik (contoh: 12.500)
    private static final NumberFormat FORMATTER = NumberFormat.getInstance(new Locale("id", "ID"));

    /**
     * Format angka menjadi string cantik.
     */
    public static String format(long amount) {
        return FORMATTER.format(amount);
    }

    /**
     * Ambil saldo pemain.
     */
    public static long getBalance(MinecraftServer server, UUID playerId) {
        return EconomyStorage.getServerState(server).getBalance(playerId);
    }

    /**
     * Ambil saldo pemain dari ServerPlayerEntity.
     */
    public static long getBalance(MinecraftServer server, ServerPlayerEntity player) {
        return getBalance(server, player.getUuid());
    }

    /**
     * Tambah saldo pemain.
     */
    public static TransactionResult addBalance(MinecraftServer server, UUID playerId, long amount) {
        if (amount <= 0) {
            return TransactionResult.failure("Jumlah harus positif.");
        }
        EconomyStorage storage = EconomyStorage.getServerState(server);
        long newBalance = storage.addBalance(playerId, amount);
        return TransactionResult.success(
                "Saldo bertambah " + format(amount) + " Coins. Saldo sekarang: " + format(newBalance) + " Coins",
                amount
        );
    }

    /**
     * Kurangi saldo pemain. Gagal kalau tidak cukup.
     */
    public static TransactionResult subtractBalance(MinecraftServer server, UUID playerId, long amount) {
        if (amount <= 0) {
            return TransactionResult.failure("Jumlah harus positif.");
        }
        EconomyStorage storage = EconomyStorage.getServerState(server);
        if (!storage.hasEnough(playerId, amount)) {
            return TransactionResult.failure(
                    "Saldo tidak cukup. Saldo kamu: " + format(storage.getBalance(playerId)) + " Coins"
            );
        }
        boolean ok = storage.subtractBalance(playerId, amount);
        if (!ok) {
            return TransactionResult.failure("Transaksi gagal.");
        }
        return TransactionResult.success(
                "Saldo berkurang " + format(amount) + " Coins. Saldo sekarang: " + format(storage.getBalance(playerId)) + " Coins",
                amount
        );
    }

    /**
     * Cek apakah pemain punya saldo cukup.
     */
    public static boolean hasEnough(MinecraftServer server, UUID playerId, long amount) {
        return EconomyStorage.getServerState(server).hasEnough(playerId, amount);
    }
}