package rul.templatemod.politics;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Perintah /protes — pemain protes pajak tinggi.
 */
public class ProtestCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("protes")
                        .executes(ctx -> {
                            ServerPlayerEntity player;
                            try {
                                player = ctx.getSource().getPlayerOrThrow();
                            } catch (Exception e) {
                                ctx.getSource().sendError(Text.literal("Perintah ini hanya untuk pemain."));
                                return 0;
                            }

                            String result = PoliticsManager.protest(ctx.getSource().getServer(), player);
                            player.sendMessage(Text.literal(result), false);
                            return 1;
                        })
        );
    }
}