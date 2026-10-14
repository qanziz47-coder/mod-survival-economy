package rul.templatemod.bank;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Menyimpan saldo bank setiap pemain per dunia.
 * Bunga di-compound otomatis setiap hari Minecraft.
 */
public class BankStorage extends PersistentState {

    private static final String STORAGE_ID = "survival_economy_bank";

    // UUID pemain -> saldo bank
    private final Map<UUID, Long> balances = new HashMap<>();

    // Batas maksimum saldo bank (mencegah overflow)
    public static final long MAX_BANK_BALANCE = 100_000_000L; // 100 juta

    public BankStorage() {
    }

    // ==================== AKSES SALDO ====================

    public long getBalance(UUID playerId) {
        return balances.getOrDefault(playerId, 0L);
    }

    public void setBalance(UUID playerId, long amount) {
        if (amount < 0) amount = 0;
        if (amount > MAX_BANK_BALANCE) amount = MAX_BANK_BALANCE;
        balances.put(playerId, amount);
        markDirty();
    }

    public void addBalance(UUID playerId, long amount) {
        long current = getBalance(playerId);
        long newBalance = current + amount;
        if (newBalance > MAX_BANK_BALANCE) newBalance = MAX_BANK_BALANCE;
        setBalance(playerId, newBalance);
    }

    public boolean subtractBalance(UUID playerId, long amount) {
        long current = getBalance(playerId);
        if (current < amount) return false;
        setBalance(playerId, current - amount);
        return true;
    }

    public boolean hasEnough(UUID playerId, long amount) {
        return getBalance(playerId) >= amount;
    }

    // ==================== BUNGA MAJEMUK ====================

    /**
     * Terapkan bunga ke semua pemain.
     * @param ratePercent Persentase bunga (misal 1.0 = 1%)
     * @return Total bunga yang diberikan
     */
    public long applyInterest(double ratePercent) {
        long totalInterest = 0;
        double multiplier = ratePercent / 100.0;

        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            long current = entry.getValue();
            if (current <= 0) continue;

            long interest = Math.round(current * multiplier);
            if (interest <= 0) continue;

            long newBalance = current + interest;
            if (newBalance > MAX_BANK_BALANCE) newBalance = MAX_BANK_BALANCE;

            balances.put(entry.getKey(), newBalance);
            totalInterest += interest;
        }

        if (totalInterest > 0) markDirty();
        return totalInterest;
    }

    /**
     * Ambil semua UUID pemain yang punya saldo bank.
     */
    public java.util.Set<UUID> getAllPlayers() {
        return balances.keySet();
    }

    // ==================== PENYIMPANAN ====================

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound playersNbt = new NbtCompound();
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            playersNbt.putLong(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("balances", playersNbt);
        return nbt;
    }

    public static BankStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        BankStorage storage = new BankStorage();
        NbtCompound playersNbt = nbt.getCompound("balances");
        for (String key : playersNbt.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                storage.balances.put(uuid, playersNbt.getLong(key));
            } catch (IllegalArgumentException e) {
                // skip UUID invalid
            }
        }
        return storage;
    }

    public static BankStorage getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(
                new PersistentState.Type<>(BankStorage::new, BankStorage::createFromNbt, null),
                STORAGE_ID
        );
    }
}