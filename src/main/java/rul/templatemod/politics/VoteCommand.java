package rul.templatemod.politics;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Perintah /vote A/B/C — vote di referendum pajak.
 */
public class VoteCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("vote")
                        // /vote A
                        .then(CommandManager.literal("A")
                                .executes(ctx -> execute(ctx.getSource(), 0)))
                        // /vote B
                        .then(CommandManager.literal("B")
                                .executes(ctx -> execute(ctx.getSource(), 1)))
                        // /vote C
                        .then(CommandManager.literal("C")
                                .executes(ctx -> execute(ctx.getSource(), 2)))
        );
    }

    private static int execute(ServerCommandSource source, int choice) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Perintah ini hanya untuk pemain."));
            return 0;
        }

        String result = PoliticsManager.vote(source.getServer(), player, choice);
        player.sendMessage(Text.literal(result), false);
        return 1;
    }
}