package rul.templatemod.role;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rul.templatemod.economy.EconomyManager;
import rul.templatemod.economy.TransactionResult;

import java.util.UUID;

/**
 * Manager logika role + gaji.
 */
public class RoleManager {

    // Biaya ganti role
    public static final long CHANGE_ROLE_COST = 1000L;

    // Gaji diberikan setiap X hari MC
    public static final int SALARY_INTERVAL_DAYS = 7;

    // ==================== ROLE ====================

    public static PlayerRole getRole(MinecraftServer server, UUID playerId) {
        return RoleStorage.getServerState(server).getRole(playerId);
    }

    public static boolean hasRole(MinecraftServer server, UUID playerId) {
        return RoleStorage.getServerState(server).hasRole(playerId);
    }

    public static int getTier(MinecraftServer server, UUID playerId) {
        return RoleStorage.getServerState(server).getTier(playerId);
    }

    public static long getSalary(MinecraftServer server, UUID playerId) {
        PlayerRole role = getRole(server, playerId);
        if (role == null) return 0;
        int tier = getTier(server, playerId);
        return role.getSalary(tier);
    }

    /**
     * Set role baru untuk pemain.
     * Return pesan hasil.
     */
    public static String setRole(MinecraftServer server, ServerPlayerEntity player, PlayerRole role) {
        RoleStorage storage = RoleStorage.getServerState(server);
        UUID uuid = player.getUuid();

        // Cek sudah punya role?
        if (storage.hasRole(uuid)) {
            PlayerRole current = storage.getRole(uuid);
            if (current == role) {
                return "§cKamu sudah jadi " + role.getColor() + role.getDisplayName() + "§c.";
            }

            // Ganti role — butuh Coins
            long balance = EconomyManager.getBalance(server, uuid);
            if (balance < CHANGE_ROLE_COST) {
                return "§cGanti role butuh §e" + EconomyManager.format(CHANGE_ROLE_COST)
                        + " Coins§c. Saldo kamu: §e" + EconomyManager.format(balance) + " Coins";
            }

            TransactionResult result = EconomyManager.subtractBalance(server, uuid, CHANGE_ROLE_COST);
            if (!result.isSuccess()) {
                return "§cGagal: " + result.getMessage();
            }
        }

        // Set role baru
        storage.setRole(uuid, role);
        storage.setTier(uuid, 1); // Reset tier saat ganti role

        return "§aKamu sekarang menjadi " + role.getColor() + role.getDisplayName()
                + "§a! Tier: §e1";
    }

    // ==================== GAJI ====================

    /**
     * Cek apakah pemain harus dapat gaji (setiap 7 hari).
     */
    public static void checkSalary(MinecraftServer server, ServerPlayerEntity player) {
        RoleStorage storage = RoleStorage.getServerState(server);
        UUID uuid = player.getUuid();

        // Skip kalau belum punya role
        PlayerRole role = storage.getRole(uuid);
        if (role == null) return;

        // Cek hari ini
        long currentDay = server.getOverworld().getTimeOfDay() / 24000L;
        long lastSalary = storage.getLastSalaryDay(uuid);

        if (lastSalary < 0) {
            // Pertama kali — set baseline
            storage.setLastSalaryDay(uuid, currentDay);
            return;
        }

        // Cek apakah sudah lewat 7 hari
        if (currentDay - lastSalary >= SALARY_INTERVAL_DAYS) {
            // Kasih gaji
            long salary = role.getSalary(storage.getTier(uuid));
            EconomyManager.addBalance(server, uuid, salary);

            storage.setLastSalaryDay(uuid, currentDay);

            // Pesan ke pemain
            player.sendMessage(Text.literal(""), false);
            player.sendMessage(Text.literal("§6§l✦ GAJI MINGGUAN ✦").formatted(Formatting.GOLD), false);
            player.sendMessage(Text.literal("§7Role: " + role.getColor() + role.getDisplayName()
                    + " §7(Tier §e" + storage.getTier(uuid) + "§7)"), false);
            player.sendMessage(Text.literal("§7Gaji: §e" + EconomyManager.format(salary) + " Coins"), false);
            player.sendMessage(Text.literal(""), false);
        }
    }

    /**
     * Cek semua pemain untuk gaji.
     */
    public static void checkAllSalaries(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            checkSalary(server, player);
        }
    }

    // ==================== INFO ====================

    public static String getInfo(MinecraftServer server, UUID playerId) {
        PlayerRole role = getRole(server, playerId);
        if (role == null) {
            return "§cKamu belum punya role. Ketik §e/role list §cuntuk lihat pilihan.";
        }

        int tier = getTier(server, playerId);
        long salary = role.getSalary(tier);
        int nextTierDay = SALARY_INTERVAL_DAYS;

        StringBuilder sb = new StringBuilder();
        sb.append("§6§l=== INFO ROLE ===\n");
        sb.append("§7Role: ").append(role.getColor()).append(role.getDisplayName()).append("\n");
        sb.append("§7Tier: §e").append(tier).append("§7/§e5\n");
        sb.append("§7Gaji: §e").append(EconomyManager.format(salary)).append(" Coins / ")
                .append(SALARY_INTERVAL_DAYS).append(" hari\n");
        sb.append("§7Ganti role: §e").append(EconomyManager.format(CHANGE_ROLE_COST)).append(" Coins\n");

        return sb.toString();
    }
}