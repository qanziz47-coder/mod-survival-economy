package rul.templatemod.role;

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
 * Menyimpan data role per pemain.
 */
public class RoleStorage extends PersistentState {

    private static final String STORAGE_ID = "survival_economy_role";

    // UUID → nama role
    private final Map<UUID, String> roleMap = new HashMap<>();

    // UUID → tier (1-5)
    private final Map<UUID, Integer> tierMap = new HashMap<>();

    // UUID → hari terakhir dapat gaji
    private final Map<UUID, Long> lastSalaryDay = new HashMap<>();

    public RoleStorage() {
    }

    // ==================== ROLE ====================

    public PlayerRole getRole(UUID playerId) {
        String roleName = roleMap.get(playerId);
        if (roleName == null) return null;
        return PlayerRole.fromName(roleName);
    }

    public void setRole(UUID playerId, PlayerRole role) {
        roleMap.put(playerId, role.getDisplayName());
        if (!tierMap.containsKey(playerId)) {
            tierMap.put(playerId, 1); // Default tier 1
        }
        markDirty();
    }

    public boolean hasRole(UUID playerId) {
        return roleMap.containsKey(playerId);
    }

    // ==================== TIER ====================

    public int getTier(UUID playerId) {
        return tierMap.getOrDefault(playerId, 1);
    }

    public void setTier(UUID playerId, int tier) {
        tierMap.put(playerId, Math.max(1, Math.min(5, tier)));
        markDirty();
    }

    public void upgradeTier(UUID playerId) {
        int current = getTier(playerId);
        if (current < 5) {
            setTier(playerId, current + 1);
        }
    }

    // ==================== GAJI ====================

    public long getLastSalaryDay(UUID playerId) {
        return lastSalaryDay.getOrDefault(playerId, -1L);
    }

    public void setLastSalaryDay(UUID playerId, long day) {
        lastSalaryDay.put(playerId, day);
        markDirty();
    }

    // ==================== PENYIMPANAN ====================

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound rolesNbt = new NbtCompound();
        for (var entry : roleMap.entrySet()) {
            rolesNbt.putString(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("roles", rolesNbt);

        NbtCompound tiersNbt = new NbtCompound();
        for (var entry : tierMap.entrySet()) {
            tiersNbt.putInt(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("tiers", tiersNbt);

        NbtCompound salaryNbt = new NbtCompound();
        for (var entry : lastSalaryDay.entrySet()) {
            salaryNbt.putLong(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("lastSalary", salaryNbt);

        return nbt;
    }

    public static RoleStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        RoleStorage storage = new RoleStorage();

        NbtCompound rolesNbt = nbt.getCompound("roles");
        for (String key : rolesNbt.getKeys()) {
            try {
                storage.roleMap.put(UUID.fromString(key), rolesNbt.getString(key));
            } catch (IllegalArgumentException ignored) {}
        }

        NbtCompound tiersNbt = nbt.getCompound("tiers");
        for (String key : tiersNbt.getKeys()) {
            try {
                storage.tierMap.put(UUID.fromString(key), tiersNbt.getInt(key));
            } catch (IllegalArgumentException ignored) {}
        }

        NbtCompound salaryNbt = nbt.getCompound("lastSalary");
        for (String key : salaryNbt.getKeys()) {
            try {
                storage.lastSalaryDay.put(UUID.fromString(key), salaryNbt.getLong(key));
            } catch (IllegalArgumentException ignored) {}
        }

        return storage;
    }

    public static RoleStorage getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(
                new PersistentState.Type<>(RoleStorage::new, RoleStorage::createFromNbt, null),
                STORAGE_ID
        );
    }
}