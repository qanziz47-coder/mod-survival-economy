package rul.templatemod.quest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rul.templatemod.economy.EconomyManager;

/**
 * Perintah /quest — sistem misi.
 */
public class QuestCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("quest")
                        // /quest
                        .executes(ctx -> showQuests(ctx.getSource()))

                        // /quest list
                        .then(CommandManager.literal("list")
                                .executes(ctx -> showQuests(ctx.getSource())))

                        // /quest info <nama>
                        .then(CommandManager.literal("info")
                                .then(CommandManager.argument("nama", StringArgumentType.string())
                                        .executes(ctx -> showInfo(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "nama")))))

                        // /quest complete <nama> (admin)
                        .then(CommandManager.literal("complete")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("nama", StringArgumentType.string())
                                        .executes(ctx -> forceComplete(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "nama")))))

                        // /quest reset (admin)
                        .then(CommandManager.literal("reset")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(ctx -> resetQuests(ctx.getSource())))
        );
    }

    // ==================== /quest ====================

    private static int showQuests(ServerCommandSource source) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        String info = QuestManager.getQuestInfo(source.getServer(), player.getUuid());
        for (String line : info.split("\n")) {
            source.sendMessage(Text.literal(line));
        }
        return 1;
    }

    // ==================== /quest info <nama> ====================

    private static int showInfo(ServerCommandSource source, String questName) {
        QuestType quest = findQuest(questName);
        if (quest == null) {
            source.sendError(Text.literal("§cMisi tidak ditemukan: §e" + questName));
            return 0;
        }

        source.sendMessage(Text.literal(""));
        source.sendMessage(Text.literal("§6§l=== INFO MISI ==="));
        source.sendMessage(Text.literal("§7Nama: " + quest.getColor() + quest.getDisplayName()));
        source.sendMessage(Text.literal("§7Tier: §e" + quest.getTier()));
        source.sendMessage(Text.literal("§7Deskripsi: §f" + quest.getDescription()));
        source.sendMessage(Text.literal("§7Reward: §e" + EconomyManager.format(quest.getReward()) + " Coins"));
        source.sendMessage(Text.literal(""));
        return 1;
    }

    // ==================== /quest complete <nama> ====================

    private static int forceComplete(ServerCommandSource source, String questName) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        QuestType quest = findQuest(questName);
        if (quest == null) {
            source.sendError(Text.literal("§cMisi tidak ditemukan: §e" + questName));
            return 0;
        }

        QuestManager.completeQuest(source.getServer(), player, quest);
        return 1;
    }

    // ==================== /quest reset ====================

    private static int resetQuests(ServerCommandSource source) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        QuestStorage storage = QuestStorage.getServerState(source.getServer());
        storage.completeQuest(player.getUuid(), QuestType.TIER1_COBBLESTONE); // dummy — tidak mengubah

        // Reset manual
        var set = storage.getAllCompleted(player.getUuid());
        set.clear();

        player.sendMessage(Text.literal("§aSemua misi direset."), false);
        return 1;
    }

    // ==================== UTIL ====================

    private static QuestType findQuest(String name) {
        // Coba match langsung
        for (QuestType q : QuestType.values()) {
            if (q.name().equalsIgnoreCase(name)) return q;
            if (q.getDisplayName().equalsIgnoreCase(name)) return q;
        }
        return null;
    }
}