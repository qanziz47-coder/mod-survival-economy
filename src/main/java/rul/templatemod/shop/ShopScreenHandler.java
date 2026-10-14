package rul.templatemod.shop;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import rul.templatemod.economy.EconomyManager;
import rul.templatemod.economy.TransactionResult;

import java.util.ArrayList;
import java.util.List;

public class ShopScreenHandler extends ScreenHandler {

    public enum Mode { MENU, CATEGORY }

    private static final int ROWS = 6;
    private static final int COLS = 9;
    private static final int SIZE = ROWS * COLS;

    private static final int SLOT_CLOSE = 49;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_PREV = 46;
    private static final int SLOT_NEXT = 53;

    private final Inventory inventory;
    private final PlayerEntity player;
    private Mode mode = Mode.MENU;
    private ShopCategory currentCategory = null;
    private int currentPage = 0;

    private final List<ShopItem> visibleItems = new ArrayList<>();

    public ShopScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, Mode.MENU, null, 0);
    }

    public ShopScreenHandler(int syncId, PlayerInventory playerInventory, Mode mode,
                             ShopCategory category, int page) {
        super(ScreenHandlerType.GENERIC_9X6, syncId);
        this.player = playerInventory.player;
        this.inventory = new SimpleInventory(SIZE);
        this.mode = mode;
        this.currentCategory = category;
        this.currentPage = page;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int index = row * COLS + col;
                this.addSlot(new Slot(inventory, index, 8 + col * 18, 18 + row * 18) {
                    @Override public boolean canInsert(ItemStack stack) { return false; }
                    @Override public boolean canTakeItems(PlayerEntity playerEntity) { return false; }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }

        // Sound buka toko
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.playSoundToPlayer(
                    SoundEvents.BLOCK_CHEST_OPEN,
                    SoundCategory.MASTER,
                    0.7f, 1.0f
            );
        }

        refresh();
    }

    // ==================== ISI GUI ====================

    private void refresh() {
        for (int i = 0; i < SIZE; i++) {
            inventory.setStack(i, ItemStack.EMPTY);
        }
        visibleItems.clear();

        if (mode == Mode.MENU) {
            fillMenuMode();
        } else {
            fillCategoryMode();
        }

        this.sendContentUpdates();
    }

    private void fillMenuMode() {
        ShopCategory[] categories = ShopCategory.values();
        int startSlot = 10;
        for (int i = 0; i < categories.length && i < 45; i++) {
            inventory.setStack(startSlot + i, getCategoryIcon(categories[i]));
        }
        inventory.setStack(SLOT_CLOSE, createButton(Items.BARRIER, "§c§l✖ TUTUP TOKO ✖"));
    }

    private void fillCategoryMode() {
        if (currentCategory == null) {
            mode = Mode.MENU;
            fillMenuMode();
            return;
        }

        List<ShopItem> items = ShopManager.getItemsByCategory(currentCategory);
        int itemsPerPage = 28;
        int startIndex = currentPage * itemsPerPage;

        for (int i = 0; i < itemsPerPage && startIndex + i < items.size(); i++) {
            ShopItem shopItem = items.get(startIndex + i);
            int row = i / 7;
            int col = (i % 7) + 1;
            int slot = row * 9 + col;
            inventory.setStack(slot, createItemIcon(shopItem));
            visibleItems.add(shopItem);
        }

        inventory.setStack(SLOT_BACK, createButton(Items.ARROW, "§e§l⬅ KEMBALI"));

        if (currentPage > 0) {
            inventory.setStack(SLOT_PREV, createButton(Items.SPECTRAL_ARROW, "§a§l⬅ SEBELUMNYA"));
        }
        if (startIndex + itemsPerPage < items.size()) {
            inventory.setStack(SLOT_NEXT, createButton(Items.SPECTRAL_ARROW, "§a§lBERIKUTNYA ➡"));
        }
    }

    // ==================== ICON ====================

    private ItemStack getCategoryIcon(ShopCategory category) {
        Item item = Registries.ITEM.get(Identifier.of(category.getIconItemId()));
        if (item == Items.AIR) item = Items.CHEST;
        ItemStack stack = new ItemStack(item);

        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✦ " + category.getDisplayName() + " ✦")
                        .formatted(Formatting.GOLD, Formatting.BOLD));

        var lore = new ArrayList<Text>();
        lore.add(Text.literal("§7Klik untuk buka kategori"));
        lore.add(Text.literal("§8§o" + ShopManager.getItemsByCategory(category).size() + " item tersedia"));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return stack;
    }

    private ItemStack createItemIcon(ShopItem shopItem) {
        Item item = Registries.ITEM.get(Identifier.of(shopItem.getItemId()));
        if (item == Items.AIR) item = Items.BARRIER;
        ItemStack stack = new ItemStack(item);

        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(shopItem.getDisplayName())
                        .formatted(Formatting.YELLOW, Formatting.BOLD));

        long currentStock = shopItem.getBaseStock();
        if (player instanceof ServerPlayerEntity serverPlayer && serverPlayer.getServer() != null) {
            currentStock = ShopManager.getStock(serverPlayer.getServer(), shopItem);
        }

        double multiplier = shopItem.getMultiplier(currentStock);
        String arrow = "";
        String color = "§e";

        if (shopItem.isDynamicPricing()) {
            if (multiplier > 1.1) {
                color = "§c";
                arrow = " §c↑";
            } else if (multiplier < 0.9) {
                color = "§a";
                arrow = " §a↓";
            }
        }

        var lore = new ArrayList<Text>();
        lore.add(Text.literal("§7§m                    "));
        lore.add(Text.literal(""));

        if (shopItem.canBuy()) {
            long buyPrice = shopItem.isDynamicPricing()
                    ? shopItem.getDynamicBuyPrice(currentStock)
                    : shopItem.getBuyPrice();
            lore.add(Text.literal("§a§lBELI  §f: " + color + EconomyManager.format(buyPrice) + " Coins" + arrow));
        }
        if (shopItem.canSell()) {
            long sellPrice = shopItem.isDynamicPricing()
                    ? shopItem.getDynamicSellPrice(currentStock)
                    : shopItem.getSellPrice();
            lore.add(Text.literal("§c§lJUAL  §f: " + color + EconomyManager.format(sellPrice) + " Coins" + arrow));
        }

        lore.add(Text.literal(""));
        lore.add(Text.literal("§7§m                    "));

        if (shopItem.isDynamicPricing()) {
            lore.add(Text.literal("§7Stok pasar: §f" + currentStock + " §7/ " + shopItem.getBaseStock()));
        }

        lore.add(Text.literal("§7Bundle: §f" + shopItem.getBundleSize() + " item"));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§7§oKlik kiri = Beli 1x bundle"));
        lore.add(Text.literal("§7§oKlik kanan = Jual 1x bundle"));

        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return stack;
    }

    private ItemStack createButton(Item item, String legacyName) {
        ItemStack stack = new ItemStack(item);
        Text name = legacyName.contains("§")
                ? Text.literal(legacyName)
                : Text.literal(legacyName).formatted(Formatting.YELLOW);
        stack.set(DataComponentTypes.CUSTOM_NAME, name);
        return stack;
    }

    // ==================== KLIK ====================

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex < 0 || slotIndex >= SIZE) {
            super.onSlotClick(slotIndex, button, actionType, player);
            return;
        }

        if (mode == Mode.MENU) {
            handleMenuClick(slotIndex);
            return;
        }
        handleCategoryClick(slotIndex, button);
    }

    private void handleMenuClick(int slotIndex) {
        if (slotIndex == SLOT_CLOSE) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.closeHandledScreen();
                serverPlayer.playSoundToPlayer(
                        SoundEvents.BLOCK_CHEST_CLOSE,
                        SoundCategory.MASTER,
                        0.7f, 1.0f
                );
            }
            return;
        }

        int startSlot = 10;
        int categoryIndex = slotIndex - startSlot;
        ShopCategory[] categories = ShopCategory.values();
        if (categoryIndex >= 0 && categoryIndex < categories.length) {
            this.mode = Mode.CATEGORY;
            this.currentCategory = categories[categoryIndex];
            this.currentPage = 0;
            playClick();
            refresh();
        }
    }

    private void handleCategoryClick(int slotIndex, int button) {
        if (slotIndex == SLOT_BACK) {
            this.mode = Mode.MENU;
            this.currentCategory = null;
            this.currentPage = 0;
            playClick();
            refresh();
            return;
        }
        if (slotIndex == SLOT_PREV && currentPage > 0) {
            currentPage--;
            playClick();
            refresh();
            return;
        }
        if (slotIndex == SLOT_NEXT) {
            currentPage++;
            playClick();
            refresh();
            return;
        }

        int itemIndex = getItemIndexFromSlot(slotIndex);
        if (itemIndex < 0 || itemIndex >= visibleItems.size()) return;

        ShopItem shopItem = visibleItems.get(itemIndex);

        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        if (button == 0) {
            handleBuy(serverPlayer, shopItem);
        } else if (button == 1) {
            handleSell(serverPlayer, shopItem);
        }
    }

    private int getItemIndexFromSlot(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        if (row < 0 || row > 3) return -1;
        if (col < 1 || col > 7) return -1;
        return row * 7 + (col - 1);
    }

    // ==================== BELI ====================

    private void handleBuy(ServerPlayerEntity serverPlayer, ShopItem shopItem) {
        if (!shopItem.canBuy()) {
            serverPlayer.sendMessage(Text.literal("§cItem ini tidak bisa dibeli."), false);
            playError();
            return;
        }

        int amount = shopItem.getBundleSize();

        long unitPrice = shopItem.isDynamicPricing()
                ? shopItem.getDynamicBuyPrice(ShopManager.getStock(serverPlayer.getServer(), shopItem))
                : shopItem.getBuyPrice();
        long baseCost = unitPrice * amount;

        if (rul.templatemod.custom.items.TraderRing.hasRing(serverPlayer)) {
            baseCost = rul.templatemod.custom.items.TraderRing.applyBuyDiscount(baseCost);
        }

        long totalCost = rul.templatemod.economy.TaxManager.calculateBuyCost(serverPlayer.getServer(), baseCost);
        long balance = EconomyManager.getBalance(serverPlayer.getServer(), serverPlayer.getUuid());

        if (balance < totalCost) {
            serverPlayer.sendMessage(
                    Text.literal("§cSaldo tidak cukup. Butuh §e" + EconomyManager.format(totalCost)
                            + " Coins§c, saldo kamu §e" + EconomyManager.format(balance) + " Coins"),
                    false);
            playError();
            return;
        }

        Item item = Registries.ITEM.get(Identifier.of(shopItem.getItemId()));
        if (item == Items.AIR) {
            serverPlayer.sendMessage(Text.literal("§cItem tidak valid."), false);
            return;
        }

        ItemStack giveStack = new ItemStack(item, amount);

        if (!canFitInInventory(serverPlayer, giveStack)) {
            serverPlayer.sendMessage(Text.literal("§cInventaris penuh. Transaksi dibatalkan."), false);
            playError();
            return;
        }

        TransactionResult result = EconomyManager.subtractBalance(
                serverPlayer.getServer(), serverPlayer.getUuid(), totalCost);

        if (!result.isSuccess()) {
            serverPlayer.sendMessage(Text.literal("§cTransaksi gagal: " + result.getMessage()), false);
            return;
        }

        serverPlayer.getInventory().insertStack(giveStack);
        ShopManager.applyStockChange(serverPlayer.getServer(), shopItem.getItemId(), true, amount);

        serverPlayer.sendMessage(
                Text.literal("§aPembelian berhasil! §e" + amount + "x " + shopItem.getDisplayName()),
                false);
        serverPlayer.sendMessage(
                Text.literal("§7" + rul.templatemod.economy.TaxManager.formatBuyTaxInfo(serverPlayer.getServer(), baseCost)),
                false);

        serverPlayer.playSoundToPlayer(
                SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),
                SoundCategory.MASTER,
                1.0f, 1.5f
        );
    }

    private boolean canFitInInventory(ServerPlayerEntity player, ItemStack stack) {
        int remaining = stack.getCount();
        int maxStackSize = stack.getMaxCount();

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack slot = player.getInventory().getStack(i);
            if (slot.isEmpty()) {
                remaining -= maxStackSize;
            } else if (ItemStack.areItemsAndComponentsEqual(slot, stack)) {
                remaining -= (maxStackSize - slot.getCount());
            }
            if (remaining <= 0) return true;
        }
        return remaining <= 0;
    }

    // ==================== JUAL ====================

    private void handleSell(ServerPlayerEntity serverPlayer, ShopItem shopItem) {
        if (!shopItem.canSell()) {
            serverPlayer.sendMessage(Text.literal("§cItem ini tidak bisa dijual."), false);
            playError();
            return;
        }

        int amount = shopItem.getBundleSize();
        Item item = Registries.ITEM.get(Identifier.of(shopItem.getItemId()));
        if (item == Items.AIR) {
            serverPlayer.sendMessage(Text.literal("§cItem tidak valid."), false);
            return;
        }

        int owned = countItem(serverPlayer, item);
        if (owned < amount) {
            serverPlayer.sendMessage(
                    Text.literal("§cKamu hanya punya §e" + owned + "x §c" + shopItem.getDisplayName()
                            + ". Butuh §e" + amount + "x"),
                    false);
            playError();
            return;
        }

        int removed = removeItem(serverPlayer, item, amount);
        if (removed < amount) {
            serverPlayer.sendMessage(Text.literal("§cGagal mengambil item dari inventaris."), false);
            return;
        }

        long unitPrice = shopItem.isDynamicPricing()
                ? shopItem.getDynamicSellPrice(ShopManager.getStock(serverPlayer.getServer(), shopItem))
                : shopItem.getSellPrice();
        long baseGain = unitPrice * amount;

        if (rul.templatemod.custom.items.TraderRing.hasRing(serverPlayer)) {
            baseGain = rul.templatemod.custom.items.TraderRing.applySellBonus(baseGain);
        }

        long totalGain = rul.templatemod.economy.TaxManager.calculateSellGain(serverPlayer.getServer(), baseGain);

        TransactionResult result = EconomyManager.addBalance(
                serverPlayer.getServer(), serverPlayer.getUuid(), totalGain);

        if (!result.isSuccess()) {
            serverPlayer.getInventory().insertStack(new ItemStack(item, amount));
            serverPlayer.sendMessage(Text.literal("§cTransaksi gagal: " + result.getMessage()), false);
            return;
        }

        ShopManager.applyStockChange(serverPlayer.getServer(), shopItem.getItemId(), false, amount);

        serverPlayer.sendMessage(
                Text.literal("§aPenjualan berhasil! §e" + amount + "x " + shopItem.getDisplayName()),
                false);
        serverPlayer.sendMessage(
                Text.literal("§7" + rul.templatemod.economy.TaxManager.formatSellTaxInfo(serverPlayer.getServer(), baseGain)),
                false);

        serverPlayer.playSoundToPlayer(
                SoundEvents.ENTITY_ITEM_PICKUP,
                SoundCategory.MASTER,
                0.7f, 1.2f
        );
    }

    // ==================== UTIL ====================

    private void playClick() {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.playSoundToPlayer(
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundCategory.MASTER,
                    0.5f, 1.2f
            );
        }
    }

    private void playError() {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.playSoundToPlayer(
                    SoundEvents.BLOCK_ANVIL_LAND,
                    SoundCategory.MASTER,
                    0.5f, 1.5f
            );
        }
    }

    private int countItem(ServerPlayerEntity player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) count += stack.getCount();
        }
        return count;
    }

    private int removeItem(ServerPlayerEntity player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                int take = Math.min(stack.getCount(), remaining);
                stack.decrement(take);
                remaining -= take;
                if (stack.isEmpty()) player.getInventory().setStack(i, ItemStack.EMPTY);
            }
        }
        return amount - remaining;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}