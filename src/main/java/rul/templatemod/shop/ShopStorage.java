package rul.templatemod.shop;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Menyimpan stok item per dunia untuk sistem inflasi.
 * Stok berkurang saat pemain beli, bertambah saat pemain jual.
 */
public class ShopStorage extends PersistentState {

    private static final String STORAGE_ID = "survival_economy_shop_stock";

    private final Map<String, Long> stockMap = new HashMap<>();

    public ShopStorage() {
    }

    public long getStock(String itemId, long baseStock) {
        return stockMap.computeIfAbsent(itemId, id -> baseStock);
    }

    public void setStock(String itemId, long stock) {
        stockMap.put(itemId, stock);
        markDirty();
    }
    /**
     * Ambil semua itemId yang tersimpan di storage.
     */
    public java.util.Set<String> getAllItemIds() {
        return stockMap.keySet();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound stockNbt = new NbtCompound();
        for (Map.Entry<String, Long> entry : stockMap.entrySet()) {
            stockNbt.putLong(entry.getKey(), entry.getValue());
        }
        nbt.put("stock", stockNbt);
        return nbt;
    }

    public static ShopStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        ShopStorage storage = new ShopStorage();
        NbtCompound stockNbt = nbt.getCompound("stock");
        for (String key : stockNbt.getKeys()) {
            storage.stockMap.put(key, stockNbt.getLong(key));
        }
        return storage;
    }

    public static ShopStorage getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(
                new PersistentState.Type<>(ShopStorage::new, ShopStorage::createFromNbt, null),
                STORAGE_ID
        );
    }
}