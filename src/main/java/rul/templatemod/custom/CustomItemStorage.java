package rul.templatemod.custom;

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
 * Menyimpan charge totem per pemain + cooldown.
 */
public class CustomItemStorage extends PersistentState {

    private static final String STORAGE_ID = "survival_economy_custom_items";

    // Key: "playerUUID:itemId" → charge
    private final Map<String, Integer> charges = new HashMap<>();

    // Key: "playerUUID:itemId" → cooldown sampai (world time)
    private final Map<String, Long> cooldowns = new HashMap<>();

    public CustomItemStorage() {
    }

    // ==================== CHARGE ====================

    public int getCharge(UUID playerId, String itemId, int defaultCharge) {
        String key = playerId + ":" + itemId;
        return charges.getOrDefault(key, defaultCharge);
    }

    public void setCharge(UUID playerId, String itemId, int charge) {
        String key = playerId + ":" + itemId;
        if (charge <= 0) {
            charges.remove(key);
        } else {
            charges.put(key, charge);
        }
        markDirty();
    }

    public void decrementCharge(UUID playerId, String itemId, int defaultCharge) {
        int current = getCharge(playerId, itemId, defaultCharge);
        setCharge(playerId, itemId, current - 1);
    }

    // ==================== COOLDOWN ====================

    public long getCooldown(UUID playerId, String itemId) {
        String key = playerId + ":" + itemId;
        return cooldowns.getOrDefault(key, 0L);
    }

    public void setCooldown(UUID playerId, String itemId, long untilWorldTime) {
        String key = playerId + ":" + itemId;
        cooldowns.put(key, untilWorldTime);
        markDirty();
    }

    public boolean isOnCooldown(UUID playerId, String itemId, long currentWorldTime) {
        return getCooldown(playerId, itemId) > currentWorldTime;
    }

    // ==================== PENYIMPANAN ====================

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound chargesNbt = new NbtCompound();
        for (var entry : charges.entrySet()) {
            chargesNbt.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("charges", chargesNbt);

        NbtCompound cooldownsNbt = new NbtCompound();
        for (var entry : cooldowns.entrySet()) {
            cooldownsNbt.putLong(entry.getKey(), entry.getValue());
        }
        nbt.put("cooldowns", cooldownsNbt);

        return nbt;
    }

    public static CustomItemStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        CustomItemStorage storage = new CustomItemStorage();

        NbtCompound chargesNbt = nbt.getCompound("charges");
        for (String key : chargesNbt.getKeys()) {
            storage.charges.put(key, chargesNbt.getInt(key));
        }

        NbtCompound cooldownsNbt = nbt.getCompound("cooldowns");
        for (String key : cooldownsNbt.getKeys()) {
            storage.cooldowns.put(key, cooldownsNbt.getLong(key));
        }

        return storage;
    }

    public static CustomItemStorage getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(
                new PersistentState.Type<>(CustomItemStorage::new, CustomItemStorage::createFromNbt, null),
                STORAGE_ID
        );
    }
}