package rul.templatemod.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ShopScreenHandlerFactory implements NamedScreenHandlerFactory {

    @Override
    public Text getDisplayName() {
        return Text.literal("✦ SURVIVAL ECONOMY SHOP ✦")
                .formatted(Formatting.GOLD, Formatting.BOLD);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ShopScreenHandler(syncId, playerInventory);
    }
}