package rul.templatemod.hud;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import rul.templatemod.network.HudSyncPayload;

public class HudSyncHandler {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(HudSyncPayload.ID, (payload, context) -> {
            HudData.setBalance(payload.balance());
            HudData.setKills(payload.kills());
            HudData.setDeaths(payload.deaths());
            HudData.setWorldTime(payload.worldTime());
            HudData.setRole(payload.role());
            HudData.setTier(payload.tier());
        });
    }
}