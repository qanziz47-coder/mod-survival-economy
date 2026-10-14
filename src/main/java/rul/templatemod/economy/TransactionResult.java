package rul.templatemod.economy;

/**
 * Hasil dari sebuah transaksi ekonomi.
 * Digunakan untuk memberi tahu apakah transaksi berhasil atau gagal,
 * beserta pesan yang bisa ditampilkan ke pemain.
 */
public class TransactionResult {

    private final boolean success;
    private final String message;
    private final long amount;

    private TransactionResult(boolean success, String message, long amount) {
        this.success = success;
        this.message = message;
        this.amount = amount;
    }

    public static TransactionResult success(String message, long amount) {
        return new TransactionResult(true, message, amount);
    }

    public static TransactionResult failure(String message) {
        return new TransactionResult(false, message, 0L);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public long getAmount() {
        return amount;
    }
}