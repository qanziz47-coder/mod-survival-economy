package rul.templatemod.role;

/**
 * 5 Role untuk pemain.
 */
public enum PlayerRole {

    PETANI("Petani", "§a", 500L),
    PENAMBANG("Penambang", "§b", 500L),
    PETUALANG("Petualang", "§c", 500L),
    PEMBANGUN("Pembangun", "§e", 500L),
    PEDAGANG("Pedagang", "§6", 500L);

    private final String displayName;
    private final String color;
    private final long baseSalary;  // Gaji tier 1

    PlayerRole(String displayName, String color, long baseSalary) {
        this.displayName = displayName;
        this.color = color;
        this.baseSalary = baseSalary;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public long getBaseSalary() { return baseSalary; }

    /**
     * Hitung gaji berdasarkan tier.
     */
    public long getSalary(int tier) {
        // Tier 1: 1x, Tier 2: 4x, Tier 3: 10x, Tier 4: 30x, Tier 5: 100x
        long[] multipliers = {1L, 4L, 10L, 30L, 100L};
        int index = Math.max(0, Math.min(4, tier - 1));
        return baseSalary * multipliers[index];
    }

    /**
     * Cari role dari nama. Return null kalau tidak ada.
     */
    public static PlayerRole fromName(String name) {
        for (PlayerRole role : values()) {
            if (role.displayName.equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }
}