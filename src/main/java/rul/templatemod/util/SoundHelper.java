package rul.templatemod.util;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/**
 * Helper untuk main sound ke player.
 * Support 2 tipe: RegistryEntry<SoundEvent> dan SoundEvent langsung.
 */
public class SoundHelper {

    // ==================== UI ====================

    public static void playShopOpen(ServerPlayerEntity player) {
        play(player, SoundEvents.BLOCK_CHEST_OPEN, 0.7f, 1.0f);
    }

    public static void playShopClose(ServerPlayerEntity player) {
        play(player, SoundEvents.BLOCK_CHEST_CLOSE, 0.7f, 1.0f);
    }

    public static void playClick(ServerPlayerEntity player) {
        play(player, SoundEvents.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    public static void playToggleOn(ServerPlayerEntity player) {
        play(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.8f);
    }

    public static void playToggleOff(ServerPlayerEntity player) {
        play(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING, 0.6f, 0.8f);
    }

    // ==================== TRANSAKSI ====================

    public static void playBuySuccess(ServerPlayerEntity player) {
        play(player, SoundEvents.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);
    }

    public static void playSellSuccess(ServerPlayerEntity player) {
        play(player, SoundEvents.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
    }

    public static void playError(ServerPlayerEntity player) {
        play(player, SoundEvents.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
    }

    // ==================== BANK ====================

    public static void playDeposit(ServerPlayerEntity player) {
        play(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
    }

    public static void playWithdraw(ServerPlayerEntity player) {
        play(player, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.8f, 1.2f);
    }

    // ==================== UTIL — 2 OVERLOAD ====================

    /**
     * Versi 1 — untuk RegistryEntry<SoundEvent>.
     */
    public static void play(ServerPlayerEntity player,
                            RegistryEntry<SoundEvent> sound,
                            float volume, float pitch) {
        player.playSoundToPlayer(
                sound.value(),
                SoundCategory.MASTER,
                volume,
                pitch
        );
    }

    /**
     * Versi 2 — untuk SoundEvent langsung.
     */
    public static void play(ServerPlayerEntity player,
                            SoundEvent sound,
                            float volume, float pitch) {
        player.playSoundToPlayer(
                sound,
                SoundCategory.MASTER,
                volume,
                pitch
        );
    }
}