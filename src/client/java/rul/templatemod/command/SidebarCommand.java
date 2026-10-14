package rul.templatemod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rul.templatemod.hud.HudData;

/**
 * Perintah /sidebar — toggle tampilkan/sembunyikan HUD sidebar.
 * Client-side, tidak perlu OP.
 */
public class SidebarCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("sidebar")
                        .executes(ctx -> toggle(ctx.getSource()))
                        .then(ClientCommandManager.literal("on")
                                .executes(ctx -> setEnabled(ctx.getSource(), true)))
                        .then(ClientCommandManager.literal("off")
                                .executes(ctx -> setEnabled(ctx.getSource(), false)))
                        .then(ClientCommandManager.literal("toggle")
                                .executes(ctx -> toggle(ctx.getSource())))
        );
    }

    private static int toggle(FabricClientCommandSource source) {
        HudData.setEnabled(!HudData.isEnabled());
        sendStatus(source);
        return 1;
    }

    private static int setEnabled(FabricClientCommandSource source, boolean enabled) {
        HudData.setEnabled(enabled);
        sendStatus(source);
        return 1;
    }

    private static void sendStatus(FabricClientCommandSource source) {
        String status = HudData.isEnabled() ? "§aAKTIF" : "§cNONAKTIF";
        source.sendFeedback(Text.literal("§eSidebar: " + status));

        // Sound client-side
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player != null) {
            if (HudData.isEnabled()) {
                client.player.playSound(
                        net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(),
                        0.5f, 1.5f
                );
            } else {
                client.player.playSound(
                        net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(),
                        0.5f, 0.8f
                );
            }
        }
    }
}