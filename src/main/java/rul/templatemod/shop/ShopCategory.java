package rul.templatemod.shop;

/**
 * 11 kategori toko.
 * Setiap kategori punya nama tampilan dan icon item.
 */
public enum ShopCategory {

    BUILDING_BLOCKS("Building Blocks", "bricks"),
    NATURAL_BLOCKS("Natural Blocks", "grass_block"),
    ORES_MINERALS("Ores & Minerals", "diamond"),
    FARMING("Farming", "wheat"),
    FOOD("Food", "bread"),
    MOB_DROPS("Mob Drops", "rotten_flesh"),
    TOOLS("Tools", "iron_pickaxe"),
    WEAPONS("Weapons", "iron_sword"),
    ARMOR("Armor", "iron_chestplate"),
    REDSTONE("Redstone", "redstone"),
    MISCELLANEOUS("Miscellaneous", "stick");

    private final String displayName;
    private final String iconItemId;

    ShopCategory(String displayName, String iconItemId) {
        this.displayName = displayName;
        this.iconItemId = iconItemId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconItemId() {
        return iconItemId;
    }
}