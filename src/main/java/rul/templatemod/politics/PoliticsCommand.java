package rul.templatemod.politics;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Perintah /politik — info politik ekonomi.
 */
public class PoliticsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("politik")
                        .executes(ctx -> {
                            String info = PoliticsManager.getInfo(ctx.getSource().getServer());
                            ctx.getSource().sendMessage(Text.literal(info));
                            return 1;
                        })
        );
    }
}