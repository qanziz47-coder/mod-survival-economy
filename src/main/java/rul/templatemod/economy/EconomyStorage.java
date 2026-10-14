package rul.templatemod.economy;

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
 * Menyimpan saldo semua pemain ke file dunia.
 * Data otomatis dimuat saat dunia dibuka dan disimpan saat dunia ditutup.
 */
public class EconomyStorage extends PersistentState {

    // Nama file penyimpanan di folder dunia
    private static final String STORAGE_ID = "survival_economy_data";

    // Map: UUID pemain -> saldo
    private final Map<UUID, Long> balances = new HashMap<>();

    // Saldo awal untuk pemain baru
    public static final long STARTING_BALANCE = 1000L;

    // Batas maksimum saldo (mencegah overflow)
    public static final long MAX_BALANCE = 1_000_000_000_000L; // 1 triliun

    public EconomyStorage() {
        // Konstruktor kosong
    }

    // ==================== AKSES SALDO ====================

    /**
     * Ambil saldo pemain. Kalau belum ada, inisialisasi dengan saldo awal.
     */
    public long getBalance(UUID playerId) {
        return balances.computeIfAbsent(playerId, id -> STARTING_BALANCE);
    }

    /**
     * Set saldo pemain secara langsung. Hanya dipakai internal.
     */
    private void setBalance(UUID playerId, long amount) {
        balances.put(playerId, amount);
        markDirty(); // beri tahu Minecraft bahwa data berubah & perlu disimpan
    }

    // ==================== TRANSAKSI ====================

    /**
     * Tambah saldo. Return saldo baru.
     * Validasi overflow.
     */
    public long addBalance(UUID playerId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Jumlah harus positif");
        }
        long current = getBalance(playerId);
        long newBalance = current + amount;

        // Cek overflow
        if (newBalance < current || newBalance > MAX_BALANCE) {
            newBalance = MAX_BALANCE;
        }

        setBalance(playerId, newBalance);
        return newBalance;
    }

    /**
     * Kurangi saldo. Return true kalau berhasil, false kalau saldo tidak cukup.
     */
    public boolean subtractBalance(UUID playerId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Jumlah harus positif");
        }
        long current = getBalance(playerId);
        if (current < amount) {
            return false; // saldo tidak cukup
        }
        setBalance(playerId, current - amount);
        return true;
    }

    /**
     * Cek apakah pemain punya saldo cukup.
     */
    public boolean hasEnough(UUID playerId, long amount) {
        return getBalance(playerId) >= amount;
    }

    // ==================== PENYIMPANAN ====================

    /**
     * Simpan data ke NBT (format file Minecraft).
     */
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound playersNbt = new NbtCompound();
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            playersNbt.putLong(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("balances", playersNbt);
        return nbt;
    }

    /**
     * Muat data dari NBT.
     */
    public static EconomyStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        EconomyStorage storage = new EconomyStorage();
        NbtCompound playersNbt = nbt.getCompound("balances");
        for (String key : playersNbt.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                long balance = playersNbt.getLong(key);
                storage.balances.put(uuid, balance);
            } catch (IllegalArgumentException e) {
                // UUID tidak valid, skip
            }
        }
        return storage;
    }

    /**
     * Ambil instance EconomyStorage dari server.
     * Kalau belum ada, Minecraft akan membuat baru.
     */
    public static EconomyStorage getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(
                new PersistentState.Type<>(
                        EconomyStorage::new,
                        EconomyStorage::createFromNbt,
                        null
                ),
                STORAGE_ID
        );
    }
    /**
     * Ambil semua UUID pemain yang punya saldo.
     */
    public java.util.Set<UUID> getAllPlayers() {
        return balances.keySet();
    }
}