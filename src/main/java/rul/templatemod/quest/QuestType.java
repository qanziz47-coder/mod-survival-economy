package rul.templatemod.quest;

/**
 * 20 misi dari pemula sampai legenda.
 */
public enum QuestType {

    // ==================== TIER 1 — PEMULA ====================
    TIER1_COBBLESTONE("Dapatkan Cobblestone", "§7", 1, 500L, "Dapatkan 64 cobblestone"),
    TIER1_WHEAT("Petani Pertama", "§a", 1, 500L, "Panen 1 wheat"),
    TIER1_CRAFTING_TABLE("Tukang Kayu", "§6", 1, 500L, "Buat crafting table"),
    TIER1_SLEEP("Tidur Pertama", "§9", 1, 500L, "Tidur di kasur"),

    // ==================== TIER 2 — MENENGAH ====================
    TIER2_IRON("Zaman Besi", "§f", 2, 2000L, "Dapatkan iron ingot"),
    TIER2_ZOMBIE("Pemburu Zombie", "§2", 2, 2000L, "Bunuh 10 zombie"),
    TIER2_IRON_PICKAXE("Alat Baru", "§7", 2, 2000L, "Buat iron pickaxe"),
    TIER2_BREED("Peternak", "§a", 2, 2000L, "Ternak hewan"),

    // ==================== TIER 3 — AHLI ====================
    TIER3_DIAMOND("Berlian Pertama", "§b", 3, 5000L, "Dapatkan diamond"),
    TIER3_SKELETON("Pemburu Skeleton", "§f", 3, 5000L, "Bunuh 20 skeleton"),
    TIER3_DIAMOND_SWORD("Pedang Berlian", "§b", 3, 5000L, "Buat diamond sword"),
    TIER3_COAL("Tambang Batubara", "§8", 3, 5000L, "Dapatkan 64 coal"),

    // ==================== TIER 4 — MASTER ====================
    TIER4_NETHERITE("Netherite Master", "§4", 4, 15000L, "Dapatkan netherite ingot"),
    TIER4_ENDERMAN("Pemburu Enderman", "§5", 4, 15000L, "Bunuh 10 enderman"),
    TIER4_BEACON("Beacon Builder", "§b", 4, 15000L, "Buat beacon"),
    TIER4_DIAMOND_ARMOR("Full Armor", "§b", 4, 15000L, "Pakai full diamond armor"),

    // ==================== TIER 5 — LEGENDA ====================
    TIER5_DRAGON("Pembunuh Naga", "§5", 5, 50000L, "Bunuh Ender Dragon"),
    TIER5_WITHER("Pembunuh Wither", "§8", 5, 50000L, "Bunuh Wither"),
    TIER5_DRAGON_EGG("Dragon Egg", "§5", 5, 50000L, "Dapatkan dragon egg"),
    TIER5_ALL_ROLE_ITEMS("Kolektor Legenda", "§6", 5, 50000L, "Kumpulkan semua role items");

    private final String displayName;
    private final String color;
    private final int tier;
    private final long reward;
    private final String description;

    QuestType(String displayName, String color, int tier, long reward, String description) {
        this.displayName = displayName;
        this.color = color;
        this.tier = tier;
        this.reward = reward;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public int getTier() { return tier; }
    public long getReward() { return reward; }
    public String getDescription() { return description; }

    /**
     * Ambil semua quest di tier tertentu.
     */
    public static QuestType[] getByTier(int tier) {
        java.util.List<QuestType> result = new java.util.ArrayList<>();
        for (QuestType q : values()) {
            if (q.tier == tier) result.add(q);
        }
        return result.toArray(new QuestType[0]);
    }
}