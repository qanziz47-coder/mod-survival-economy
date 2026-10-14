package rul.templatemod.role;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rul.templatemod.economy.EconomyManager;

/**
 * Perintah /role — sistem role & gaji.
 */
public class RoleCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("role")
                        // /role
                        .executes(ctx -> showInfo(ctx.getSource()))

                        // /role info
                        .then(CommandManager.literal("info")
                                .executes(ctx -> showInfo(ctx.getSource())))

                        // /role list
                        .then(CommandManager.literal("list")
                                .executes(ctx -> showList(ctx.getSource())))

                        // /role gaji
                        .then(CommandManager.literal("gaji")
                                .executes(ctx -> showSalary(ctx.getSource())))

                        // /role set <role>
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("role", StringArgumentType.string())
                                        .executes(ctx -> setRole(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "role")))))
        );
    }

    // ==================== /role ====================

    private static int showInfo(ServerCommandSource source) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        String info = RoleManager.getInfo(source.getServer(), player.getUuid());
        for (String line : info.split("\n")) {
            source.sendMessage(Text.literal(line));
        }
        return 1;
    }

    // ==================== /role list ====================

    private static int showList(ServerCommandSource source) {
        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§6§l=== DAFTAR ROLE ==="));
        source.sendMessage(Text.literal(""));

        for (PlayerRole role : PlayerRole.values()) {
            source.sendMessage(Text.literal(
                    role.getColor() + "✦ " + role.getDisplayName()
                            + " §7— Gaji Tier 1: §e" + EconomyManager.format(role.getBaseSalary())
                            + " Coins/§7" + RoleManager.SALARY_INTERVAL_DAYS + " hari"
            ));
        }

        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§7Ketik §e/role set <nama> §7untuk pilih role"));
        source.sendMessage(Text.literal("§7Ganti role: §e" + EconomyManager.format(RoleManager.CHANGE_ROLE_COST) + " Coins"));
        source.sendMessage(Text.literal(""));

        return 1;
    }

    // ==================== /role gaji ====================

    private static int showSalary(ServerCommandSource source) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        PlayerRole role = RoleManager.getRole(source.getServer(), player.getUuid());
        if (role == null) {
            source.sendMessage(Text.literal("§cKamu belum punya role. Ketik §e/role list"));
            return 0;
        }

        int tier = RoleManager.getTier(source.getServer(), player.getUuid());
        long salary = role.getSalary(tier);

        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§6§l=== GAJI KAMU ==="));
        source.sendMessage(Text.literal("§7Role: " + role.getColor() + role.getDisplayName()));
        source.sendMessage(Text.literal("§7Tier: §e" + tier + "§7/§e5"));
        source.sendMessage(Text.literal("§7Gaji: §e" + EconomyManager.format(salary) + " Coins"));
        source.sendMessage(Text.literal("§7Interval: §e" + RoleManager.SALARY_INTERVAL_DAYS + " hari Minecraft"));
        source.sendMessage(Text.literal(""));

        // Gaji tier berikutnya
        if (tier < 5) {
            long nextSalary = role.getSalary(tier + 1);
            source.sendMessage(Text.literal("§7Tier berikutnya: §e" + EconomyManager.format(nextSalary) + " Coins"));
        } else {
            source.sendMessage(Text.literal("§6§l✦ Kamu sudah Tier MAX! ✦"));
        }
        source.sendMessage(Text.literal(""));

        return 1;
    }

    // ==================== /role set <role> ====================

    private static int setRole(ServerCommandSource source, String roleName) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        PlayerRole role = PlayerRole.fromName(roleName);
        if (role == null) {
            player.sendMessage(Text.literal("§cRole tidak valid. Pilihan: §ePetani, Penambang, Petualang, Pembangun, Pedagang"), false);
            return 0;
        }

        String result = RoleManager.setRole(source.getServer(), player, role);
        player.sendMessage(Text.literal(result), false);
        return 1;
    }
}