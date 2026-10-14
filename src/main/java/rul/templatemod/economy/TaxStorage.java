package rul.templatemod.economy;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

/**
 * Menyimpan pengaturan pajak per dunia.
 * Pajak disimpan dalam bentuk persen (misal 5 = 5%).
 */
public class TaxStorage extends PersistentState {

    private static final String STORAGE_ID = "survival_economy_tax";

    // Pajak penjualan (PPh) — dipotong dari uang yang diterima pemain saat jual
    private int sellTaxPercent = 5;

    // Pajak pembelian (PPN) — ditambahkan ke biaya beli pemain
    private int buyTaxPercent = 2;

    // Range pajak (biar tidak ekstrem)
    public static final int MIN_TAX = 0;
    public static final int MAX_TAX = 30;

    public TaxStorage() {
    }

    // ==================== GETTER ====================

    public int getSellTaxPercent() {
        return sellTaxPercent;
    }

    public int getBuyTaxPercent() {
        return buyTaxPercent;
    }

    // ==================== SETTER ====================

    public void setSellTaxPercent(int percent) {
        this.sellTaxPercent = clamp(percent);
        markDirty();
    }

    public void setBuyTaxPercent(int percent) {
        this.buyTaxPercent = clamp(percent);
        markDirty();
    }

    private int clamp(int percent) {
        if (percent < MIN_TAX) return MIN_TAX;
        if (percent > MAX_TAX) return MAX_TAX;
        return percent;
    }

    // ==================== PENYIMPANAN ====================

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putInt("sellTaxPercent", sellTaxPercent);
        nbt.putInt("buyTaxPercent", buyTaxPercent);
        return nbt;
    }

    public static TaxStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        TaxStorage storage = new TaxStorage();
        storage.sellTaxPercent = nbt.getInt("sellTaxPercent");
        storage.buyTaxPercent = nbt.getInt("buyTaxPercent");

        // Validasi (kalau config rusak)
        if (storage.sellTaxPercent < MIN_TAX) storage.sellTaxPercent = 5;
        if (storage.sellTaxPercent > MAX_TAX) storage.sellTaxPercent = MAX_TAX;
        if (storage.buyTaxPercent < MIN_TAX) storage.buyTaxPercent = 2;
        if (storage.buyTaxPercent > MAX_TAX) storage.buyTaxPercent = MAX_TAX;

        return storage;
    }

    public static TaxStorage getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(
                new PersistentState.Type<>(TaxStorage::new, TaxStorage::createFromNbt, null),
                STORAGE_ID
        );
    }
}