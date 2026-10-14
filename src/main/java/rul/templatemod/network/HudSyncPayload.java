package rul.templatemod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Payload untuk sync data HUD dari server ke client.
 * Termasuk: balance, kills, deaths, worldTime, role, tier.
 */
public record HudSyncPayload(
        long balance,
        int kills,
        int deaths,
        long worldTime,
        String role,
        int tier
) implements CustomPayload {

    public static final CustomPayload.Id<HudSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of("survival_economy", "hud_sync"));

    public static final PacketCodec<RegistryByteBuf, HudSyncPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeLong(value.balance());
                buf.writeInt(value.kills());
                buf.writeInt(value.deaths());
                buf.writeLong(value.worldTime());
                buf.writeString(value.role());
                buf.writeInt(value.tier());
            },
            buf -> new HudSyncPayload(
                    buf.readLong(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readLong(),
                    buf.readString(),
                    buf.readInt()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }
}