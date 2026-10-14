package rul.templatemod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import rul.templatemod.command.SidebarCommand;
import rul.templatemod.hud.EconomyHud;
import rul.templatemod.hud.HudSyncHandler;

public class TemplateModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // HUD
        EconomyHud.register();
        HudSyncHandler.register();

        // Perintah client-side
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            SidebarCommand.register(dispatcher);
        });

        // Tidak ada custom screen — pakai chest vanilla
    }
}