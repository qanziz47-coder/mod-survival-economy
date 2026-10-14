package rul.templatemod.shop;

import net.minecraft.server.MinecraftServer;
import rul.templatemod.config.ShopConfigData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager daftar item toko.
 */
public class ShopManager {

    private static final List<ShopItem> ALL_ITEMS = new ArrayList<>();
    private static final Map<String, ShopItem> ITEM_LOOKUP = new HashMap<>();

    // ==================== INIT ====================

    public static void init() {
        ALL_ITEMS.clear();
        ITEM_LOOKUP.clear();

        ShopConfigData configData = rul.templatemod.config.ShopConfig.load();

        if (configData != null && configData.items != null && !configData.items.isEmpty()) {
            loadFromConfig(configData);
            return;
        }

        loadDefaults();
        saveDefaultsToConfig();
    }

    // ==================== LOAD FROM CONFIG ====================

    private static void loadFromConfig(ShopConfigData data) {
        for (ShopConfigData.ShopConfigItem cfgItem : data.items) {
            ShopCategory category;
            try {
                category = ShopCategory.valueOf(cfgItem.category);
            } catch (IllegalArgumentException e) {
                continue;
            }

            ShopItem item = new ShopItem(
                    cfgItem.id,
                    cfgItem.name,
                    cfgItem.buyPrice,
                    cfgItem.sellPrice,
                    cfgItem.bundleSize,
                    category,
                    cfgItem.baseStock,
                    cfgItem.minStock,
                    cfgItem.maxStock,
                    cfgItem.dynamic
            );

            ALL_ITEMS.add(item);
            ITEM_LOOKUP.put(cfgItem.id, item);
        }
    }

    // ==================== LOAD DEFAULTS ====================

    private static void loadDefaults() {
        // ==================== BUILDING BLOCKS (20 item) ====================
        add("minecraft:oak_planks", "Oak Planks", 5, 2, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:spruce_planks", "Spruce Planks", 5, 2, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:birch_planks", "Birch Planks", 5, 2, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:jungle_planks", "Jungle Planks", 5, 2, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:acacia_planks", "Acacia Planks", 5, 2, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:dark_oak_planks", "Dark Oak Planks", 5, 2, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:stone", "Stone", 3, 1, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:cobblestone", "Cobblestone", 2, 1, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:stone_bricks", "Stone Bricks", 8, 3, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:mossy_stone_bricks", "Mossy Stone Bricks", 10, 4, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:cracked_stone_bricks", "Cracked Stone Bricks", 10, 4, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:chiseled_stone_bricks", "Chiseled Stone Bricks", 12, 5, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:bricks", "Bricks", 15, 6, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:glass", "Glass", 10, 4, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:glass_pane", "Glass Pane", 5, 2, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:sand", "Sand", 3, 1, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:gravel", "Gravel", 3, 1, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:sandstone", "Sandstone", 5, 2, 16, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:quartz_block", "Quartz Block", 30, 15, 8, ShopCategory.BUILDING_BLOCKS);
        add("minecraft:terracotta", "Terracotta", 12, 5, 16, ShopCategory.BUILDING_BLOCKS);

        // ==================== NATURAL BLOCKS (15 item) ====================
        add("minecraft:oak_log", "Oak Log", 8, 3, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:spruce_log", "Spruce Log", 8, 3, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:birch_log", "Birch Log", 8, 3, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:jungle_log", "Jungle Log", 8, 3, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:acacia_log", "Acacia Log", 8, 3, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:dark_oak_log", "Dark Oak Log", 8, 3, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:dirt", "Dirt", 2, 1, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:grass_block", "Grass Block", 5, 2, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:podzol", "Podzol", 8, 3, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:mycelium", "Mycelium", 10, 4, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:clay", "Clay", 12, 5, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:ice", "Ice", 8, 3, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:snow_block", "Snow Block", 5, 2, 16, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:obsidian", "Obsidian", 100, 40, 4, ShopCategory.NATURAL_BLOCKS);
        add("minecraft:netherrack", "Netherrack", 5, 2, 16, ShopCategory.NATURAL_BLOCKS);

        // ==================== ORES & MINERALS (15 item) ====================
        addDynamic("minecraft:coal", "Coal", 20, 10, 16, ShopCategory.ORES_MINERALS, 3000L, 300L, 30000L);
        addDynamic("minecraft:iron_ingot", "Iron Ingot", 80, 40, 8, ShopCategory.ORES_MINERALS, 2000L, 200L, 20000L);
        addDynamic("minecraft:gold_ingot", "Gold Ingot", 150, 75, 8, ShopCategory.ORES_MINERALS, 1500L, 150L, 15000L);
        addDynamic("minecraft:copper_ingot", "Copper Ingot", 40, 20, 8, ShopCategory.ORES_MINERALS, 2500L, 250L, 25000L);
        addDynamic("minecraft:diamond", "Diamond", 500, 250, 4, ShopCategory.ORES_MINERALS, 1000L, 100L, 10000L);
        addDynamic("minecraft:emerald", "Emerald", 400, 200, 4, ShopCategory.ORES_MINERALS, 800L, 80L, 8000L);
        addDynamic("minecraft:lapis_lazuli", "Lapis Lazuli", 50, 25, 8, ShopCategory.ORES_MINERALS, 1500L, 150L, 15000L);
        addDynamic("minecraft:redstone", "Redstone Dust", 30, 15, 16, ShopCategory.ORES_MINERALS, 3000L, 300L, 30000L);
        addDynamic("minecraft:quartz", "Nether Quartz", 60, 30, 8, ShopCategory.ORES_MINERALS, 1500L, 150L, 15000L);
        addDynamic("minecraft:netherite_scrap", "Netherite Scrap", 2000, 1000, 1, ShopCategory.ORES_MINERALS, 200L, 20L, 2000L);
        add("minecraft:iron_block", "Iron Block", 720, 360, 4, ShopCategory.ORES_MINERALS);
        add("minecraft:gold_block", "Gold Block", 1350, 675, 4, ShopCategory.ORES_MINERALS);
        add("minecraft:diamond_block", "Diamond Block", 4500, 2250, 1, ShopCategory.ORES_MINERALS);
        add("minecraft:emerald_block", "Emerald Block", 3600, 1800, 1, ShopCategory.ORES_MINERALS);
        add("minecraft:netherite_ingot", "Netherite Ingot", 8000, 4000, 1, ShopCategory.ORES_MINERALS);

        // ==================== FARMING (15 item) ====================
        add("minecraft:wheat", "Wheat", 15, 8, 16, ShopCategory.FARMING);
        add("minecraft:wheat_seeds", "Wheat Seeds", 5, 2, 16, ShopCategory.FARMING);
        add("minecraft:carrot", "Carrot", 12, 6, 16, ShopCategory.FARMING);
        add("minecraft:potato", "Potato", 12, 6, 16, ShopCategory.FARMING);
        add("minecraft:beetroot", "Beetroot", 12, 6, 16, ShopCategory.FARMING);
        add("minecraft:sugar_cane", "Sugar Cane", 10, 5, 16, ShopCategory.FARMING);
        add("minecraft:pumpkin", "Pumpkin", 20, 10, 8, ShopCategory.FARMING);
        add("minecraft:melon_slice", "Melon Slice", 5, 2, 16, ShopCategory.FARMING);
        add("minecraft:cactus", "Cactus", 15, 8, 16, ShopCategory.FARMING);
        add("minecraft:bamboo", "Bamboo", 8, 4, 16, ShopCategory.FARMING);
        add("minecraft:sweet_berries", "Sweet Berries", 10, 5, 16, ShopCategory.FARMING);
        add("minecraft:kelp", "Kelp", 5, 2, 16, ShopCategory.FARMING);
        add("minecraft:cocoa_beans", "Cocoa Beans", 15, 7, 16, ShopCategory.FARMING);
        add("minecraft:nether_wart", "Nether Wart", 25, 12, 16, ShopCategory.FARMING);
        add("minecraft:hay_block", "Hay Bale", 135, 67, 4, ShopCategory.FARMING);

        // ==================== FOOD (15 item) ====================
        add("minecraft:bread", "Bread", 25, 12, 8, ShopCategory.FOOD);
        add("minecraft:cooked_beef", "Steak", 40, 20, 8, ShopCategory.FOOD);
        add("minecraft:cooked_porkchop", "Cooked Porkchop", 40, 20, 8, ShopCategory.FOOD);
        add("minecraft:cooked_chicken", "Cooked Chicken", 30, 15, 8, ShopCategory.FOOD);
        add("minecraft:cooked_mutton", "Cooked Mutton", 35, 17, 8, ShopCategory.FOOD);
        add("minecraft:cooked_rabbit", "Cooked Rabbit", 30, 15, 8, ShopCategory.FOOD);
        add("minecraft:cooked_cod", "Cooked Cod", 25, 12, 8, ShopCategory.FOOD);
        add("minecraft:cooked_salmon", "Cooked Salmon", 30, 15, 8, ShopCategory.FOOD);
        add("minecraft:apple", "Apple", 20, 10, 8, ShopCategory.FOOD);
        add("minecraft:golden_apple", "Golden Apple", 500, 250, 4, ShopCategory.FOOD);
        add("minecraft:enchanted_golden_apple", "Enchanted Golden Apple", 5000, 2500, 1, ShopCategory.FOOD);
        add("minecraft:cake", "Cake", 100, 50, 1, ShopCategory.FOOD);
        add("minecraft:pumpkin_pie", "Pumpkin Pie", 60, 30, 4, ShopCategory.FOOD);
        add("minecraft:cookie", "Cookie", 15, 7, 8, ShopCategory.FOOD);
        add("minecraft:melon_slice", "Melon Slice", 8, 4, 16, ShopCategory.FOOD);

        // ==================== MOB DROPS (15 item) ====================
        add("minecraft:rotten_flesh", "Rotten Flesh", 5, 2, 16, ShopCategory.MOB_DROPS);
        add("minecraft:bone", "Bone", 10, 5, 16, ShopCategory.MOB_DROPS);
        add("minecraft:bone_meal", "Bone Meal", 5, 2, 16, ShopCategory.MOB_DROPS);
        add("minecraft:string", "String", 8, 4, 16, ShopCategory.MOB_DROPS);
        add("minecraft:spider_eye", "Spider Eye", 15, 7, 16, ShopCategory.MOB_DROPS);
        add("minecraft:gunpowder", "Gunpowder", 25, 12, 16, ShopCategory.MOB_DROPS);
        add("minecraft:ender_pearl", "Ender Pearl", 200, 100, 4, ShopCategory.MOB_DROPS);
        add("minecraft:blaze_rod", "Blaze Rod", 250, 125, 4, ShopCategory.MOB_DROPS);
        add("minecraft:blaze_powder", "Blaze Powder", 130, 65, 8, ShopCategory.MOB_DROPS);
        add("minecraft:ghast_tear", "Ghast Tear", 300, 150, 4, ShopCategory.MOB_DROPS);
        add("minecraft:magma_cream", "Magma Cream", 150, 75, 8, ShopCategory.MOB_DROPS);
        add("minecraft:slime_ball", "Slime Ball", 20, 10, 16, ShopCategory.MOB_DROPS);
        add("minecraft:leather", "Leather", 20, 10, 16, ShopCategory.MOB_DROPS);
        add("minecraft:feather", "Feather", 8, 4, 16, ShopCategory.MOB_DROPS);
        add("minecraft:ink_sac", "Ink Sac", 15, 7, 16, ShopCategory.MOB_DROPS);

        // ==================== TOOLS (15 item) ====================
        add("minecraft:wooden_pickaxe", "Wooden Pickaxe", 30, 10, 1, ShopCategory.TOOLS);
        add("minecraft:stone_pickaxe", "Stone Pickaxe", 60, 20, 1, ShopCategory.TOOLS);
        add("minecraft:iron_pickaxe", "Iron Pickaxe", 250, 80, 1, ShopCategory.TOOLS);
        add("minecraft:golden_pickaxe", "Golden Pickaxe", 400, 130, 1, ShopCategory.TOOLS);
        add("minecraft:diamond_pickaxe", "Diamond Pickaxe", 1500, 500, 1, ShopCategory.TOOLS);
        add("minecraft:netherite_pickaxe", "Netherite Pickaxe", 5000, 2000, 1, ShopCategory.TOOLS);
        add("minecraft:iron_axe", "Iron Axe", 250, 80, 1, ShopCategory.TOOLS);
        add("minecraft:diamond_axe", "Diamond Axe", 1500, 500, 1, ShopCategory.TOOLS);
        add("minecraft:iron_shovel", "Iron Shovel", 150, 50, 1, ShopCategory.TOOLS);
        add("minecraft:diamond_shovel", "Diamond Shovel", 1000, 350, 1, ShopCategory.TOOLS);
        add("minecraft:iron_hoe", "Iron Hoe", 150, 50, 1, ShopCategory.TOOLS);
        add("minecraft:diamond_hoe", "Diamond Hoe", 1000, 350, 1, ShopCategory.TOOLS);
        add("minecraft:shears", "Shears", 100, 30, 1, ShopCategory.TOOLS);
        add("minecraft:flint_and_steel", "Flint and Steel", 80, 25, 1, ShopCategory.TOOLS);
        add("minecraft:fishing_rod", "Fishing Rod", 100, 30, 1, ShopCategory.TOOLS);

        // ==================== WEAPONS (15 item) ====================
        add("minecraft:wooden_sword", "Wooden Sword", 20, 5, 1, ShopCategory.WEAPONS);
        add("minecraft:stone_sword", "Stone Sword", 40, 12, 1, ShopCategory.WEAPONS);
        add("minecraft:iron_sword", "Iron Sword", 200, 60, 1, ShopCategory.WEAPONS);
        add("minecraft:golden_sword", "Golden Sword", 350, 110, 1, ShopCategory.WEAPONS);
        add("minecraft:diamond_sword", "Diamond Sword", 1200, 400, 1, ShopCategory.WEAPONS);
        add("minecraft:netherite_sword", "Netherite Sword", 4000, 1500, 1, ShopCategory.WEAPONS);
        add("minecraft:bow", "Bow", 150, 50, 1, ShopCategory.WEAPONS);
        add("minecraft:crossbow", "Crossbow", 300, 100, 1, ShopCategory.WEAPONS);
        add("minecraft:arrow", "Arrow", 5, 2, 16, ShopCategory.WEAPONS);
        add("minecraft:spectral_arrow", "Spectral Arrow", 15, 7, 8, ShopCategory.WEAPONS);
        add("minecraft:tipped_arrow", "Tipped Arrow", 30, 15, 8, ShopCategory.WEAPONS);
        add("minecraft:shield", "Shield", 200, 70, 1, ShopCategory.WEAPONS);
        add("minecraft:trident", "Trident", 3000, 1200, 1, ShopCategory.WEAPONS);
        add("minecraft:snowball", "Snowball", 3, 1, 16, ShopCategory.WEAPONS);
        add("minecraft:egg", "Egg", 5, 2, 16, ShopCategory.WEAPONS);

        // ==================== ARMOR (15 item) ====================
        add("minecraft:leather_helmet", "Leather Helmet", 60, 20, 1, ShopCategory.ARMOR);
        add("minecraft:leather_chestplate", "Leather Chestplate", 100, 30, 1, ShopCategory.ARMOR);
        add("minecraft:leather_leggings", "Leather Leggings", 80, 25, 1, ShopCategory.ARMOR);
        add("minecraft:leather_boots", "Leather Boots", 60, 20, 1, ShopCategory.ARMOR);
        add("minecraft:iron_helmet", "Iron Helmet", 200, 70, 1, ShopCategory.ARMOR);
        add("minecraft:iron_chestplate", "Iron Chestplate", 350, 120, 1, ShopCategory.ARMOR);
        add("minecraft:iron_leggings", "Iron Leggings", 300, 100, 1, ShopCategory.ARMOR);
        add("minecraft:iron_boots", "Iron Boots", 200, 70, 1, ShopCategory.ARMOR);
        add("minecraft:diamond_helmet", "Diamond Helmet", 1200, 400, 1, ShopCategory.ARMOR);
        add("minecraft:diamond_chestplate", "Diamond Chestplate", 2000, 700, 1, ShopCategory.ARMOR);
        add("minecraft:diamond_leggings", "Diamond Leggings", 1800, 600, 1, ShopCategory.ARMOR);
        add("minecraft:diamond_boots", "Diamond Boots", 1200, 400, 1, ShopCategory.ARMOR);
        add("minecraft:netherite_helmet", "Netherite Helmet", 5000, 2000, 1, ShopCategory.ARMOR);
        add("minecraft:netherite_chestplate", "Netherite Chestplate", 8000, 3000, 1, ShopCategory.ARMOR);
        add("minecraft:netherite_boots", "Netherite Boots", 5000, 2000, 1, ShopCategory.ARMOR);

        // ==================== REDSTONE (15 item) ====================
        add("minecraft:redstone_torch", "Redstone Torch", 20, 8, 8, ShopCategory.REDSTONE);
        add("minecraft:repeater", "Redstone Repeater", 60, 20, 4, ShopCategory.REDSTONE);
        add("minecraft:comparator", "Redstone Comparator", 80, 30, 4, ShopCategory.REDSTONE);
        add("minecraft:piston", "Piston", 50, 15, 8, ShopCategory.REDSTONE);
        add("minecraft:sticky_piston", "Sticky Piston", 70, 25, 8, ShopCategory.REDSTONE);
        add("minecraft:observer", "Observer", 80, 30, 8, ShopCategory.REDSTONE);
        add("minecraft:hopper", "Hopper", 150, 50, 4, ShopCategory.REDSTONE);
        add("minecraft:dropper", "Dropper", 50, 15, 4, ShopCategory.REDSTONE);
        add("minecraft:dispenser", "Dispenser", 60, 20, 4, ShopCategory.REDSTONE);
        add("minecraft:redstone_lamp", "Redstone Lamp", 100, 35, 4, ShopCategory.REDSTONE);
        add("minecraft:note_block", "Note Block", 80, 25, 4, ShopCategory.REDSTONE);
        add("minecraft:daylight_detector", "Daylight Detector", 120, 40, 4, ShopCategory.REDSTONE);
        add("minecraft:target", "Target Block", 100, 30, 4, ShopCategory.REDSTONE);
        add("minecraft:tripwire_hook", "Tripwire Hook", 30, 10, 8, ShopCategory.REDSTONE);
        add("minecraft:lever", "Lever", 10, 3, 8, ShopCategory.REDSTONE);

        // ==================== MISCELLANEOUS (15 item) ====================
        add("minecraft:stick", "Stick", 3, 1, 16, ShopCategory.MISCELLANEOUS);
        add("minecraft:torch", "Torch", 5, 2, 16, ShopCategory.MISCELLANEOUS);
        add("minecraft:book", "Book", 30, 12, 8, ShopCategory.MISCELLANEOUS);
        add("minecraft:paper", "Paper", 10, 4, 16, ShopCategory.MISCELLANEOUS);
        add("minecraft:ender_eye", "Eye of Ender", 300, 150, 4, ShopCategory.MISCELLANEOUS);
        add("minecraft:name_tag", "Name Tag", 100, 40, 4, ShopCategory.MISCELLANEOUS);
        add("minecraft:saddle", "Saddle", 500, 200, 1, ShopCategory.MISCELLANEOUS);
        add("minecraft:lead", "Lead", 100, 35, 4, ShopCategory.MISCELLANEOUS);
        add("minecraft:map", "Empty Map", 50, 20, 1, ShopCategory.MISCELLANEOUS);
        add("minecraft:compass", "Compass", 100, 35, 1, ShopCategory.MISCELLANEOUS);
        add("minecraft:clock", "Clock", 150, 50, 1, ShopCategory.MISCELLANEOUS);
        add("minecraft:spyglass", "Spyglass", 200, 70, 1, ShopCategory.MISCELLANEOUS);
        add("minecraft:firework_rocket", "Firework Rocket", 30, 12, 8, ShopCategory.MISCELLANEOUS);
        add("minecraft:bucket", "Bucket", 200, 70, 1, ShopCategory.MISCELLANEOUS);
        add("minecraft:experience_bottle", "Bottle o' Enchanting", 100, 40, 4, ShopCategory.MISCELLANEOUS);
    }

    // ==================== SAVE DEFAULTS TO CONFIG ====================

    private static void saveDefaultsToConfig() {
        ShopConfigData data = new ShopConfigData();

        for (ShopItem item : ALL_ITEMS) {
            ShopConfigData.ShopConfigItem cfg = new ShopConfigData.ShopConfigItem();
            cfg.id = item.getItemId();
            cfg.name = item.getDisplayName();
            cfg.category = item.getCategory().name();
            cfg.buyPrice = item.getBuyPrice();
            cfg.sellPrice = item.getSellPrice();
            cfg.bundleSize = item.getBundleSize();
            cfg.dynamic = item.isDynamicPricing();
            cfg.baseStock = item.getBaseStock();
            cfg.minStock = item.getMinStock();
            cfg.maxStock = item.getMaxStock();
            data.items.add(cfg);
        }

        rul.templatemod.config.ShopConfig.save(data);
    }

    // ==================== TAMBAH ITEM ====================

    private static void add(String itemId, String displayName, long buyPrice, long sellPrice,
                            int bundleSize, ShopCategory category) {
        ShopItem item = new ShopItem(itemId, displayName, buyPrice, sellPrice, bundleSize, category);
        ALL_ITEMS.add(item);
        ITEM_LOOKUP.put(itemId, item);
    }

    private static void addDynamic(String itemId, String displayName, long buyPrice, long sellPrice,
                                   int bundleSize, ShopCategory category,
                                   long baseStock, long minStock, long maxStock) {
        ShopItem item = new ShopItem(itemId, displayName, buyPrice, sellPrice, bundleSize, category,
                baseStock, minStock, maxStock, true);
        ALL_ITEMS.add(item);
        ITEM_LOOKUP.put(itemId, item);
    }

    // ==================== LOOKUP ====================

    public static ShopItem getItem(String itemId) {
        return ITEM_LOOKUP.get(itemId);
    }

    public static List<ShopItem> getItemsByCategory(ShopCategory category) {
        List<ShopItem> result = new ArrayList<>();
        for (ShopItem item : ALL_ITEMS) {
            if (item.getCategory() == category) {
                result.add(item);
            }
        }
        return result;
    }

    public static List<ShopItem> getAllItems() {
        return new ArrayList<>(ALL_ITEMS);
    }

    public static boolean canSell(String itemId) {
        ShopItem item = getItem(itemId);
        return item != null && item.canSell();
    }

    public static boolean canBuy(String itemId) {
        ShopItem item = getItem(itemId);
        return item != null && item.canBuy();
    }

    // ==================== INFLASI ====================

    public static long getStock(MinecraftServer server, ShopItem item) {
        return ShopStorage.getServerState(server).getStock(item.getItemId(), item.getBaseStock());
    }

    public static void applyStockChange(MinecraftServer server, String itemId,
                                        boolean isBuy, int amount) {
        ShopItem item = getItem(itemId);
        if (item == null || !item.isDynamicPricing()) return;

        ShopStorage storage = ShopStorage.getServerState(server);
        long current = storage.getStock(itemId, item.getBaseStock());

        long oldPrice = isBuy
                ? item.getDynamicBuyPrice(current)
                : item.getDynamicSellPrice(current);

        long delta = isBuy ? -amount : amount;
        long newStock = current + delta;

        if (newStock < item.getMinStock()) newStock = item.getMinStock();
        if (newStock > item.getMaxStock()) newStock = item.getMaxStock();

        storage.setStock(itemId, newStock);

        long newPrice = isBuy
                ? item.getDynamicBuyPrice(newStock)
                : item.getDynamicSellPrice(newStock);

        PriceNotifier.checkAndNotify(server, item, oldPrice, newPrice);
    }

    public static void regenerateStock(MinecraftServer server, double percent) {
        ShopStorage storage = ShopStorage.getServerState(server);

        for (ShopItem item : ALL_ITEMS) {
            if (!item.isDynamicPricing()) continue;

            long current = storage.getStock(item.getItemId(), item.getBaseStock());
            long base = item.getBaseStock();

            long gap = base - current;
            if (gap <= 0) continue;

            long regenAmount = Math.max(1L, Math.round(gap * percent));
            long newStock = current + regenAmount;

            if (newStock > base) newStock = base;
            if (newStock > item.getMaxStock()) newStock = item.getMaxStock();

            storage.setStock(item.getItemId(), newStock);
        }
    }
}