package rul.templatemod.hud;

public class HudData {

    private static long balance = 0L;
    private static int kills = 0;
    private static int deaths = 0;
    private static long worldTime = 0L;
    private static String role = "None";
    private static int tier = 0;
    private static boolean enabled = true;

    private static float currentAlpha = 1.0f;
    private static float targetAlpha = 1.0f;

    // ==================== SALDO ====================
    public static long getBalance() { return balance; }
    public static void setBalance(long value) { balance = value; }

    // ==================== STATISTIK ====================
    public static int getKills() { return kills; }
    public static void setKills(int value) { kills = value; }

    public static int getDeaths() { return deaths; }
    public static void setDeaths(int value) { deaths = value; }

    public static String getKDRatioFormatted() {
        if (deaths == 0) {
            return String.format("%.2f", (double) kills);
        }
        return String.format("%.2f", (double) kills / deaths);
    }

    // ==================== ROLE ====================
    public static String getRole() { return role; }
    public static void setRole(String value) { role = value; }

    public static int getTier() { return tier; }
    public static void setTier(int value) { tier = value; }

    public static String getRoleDisplay() {
        if (role == null || role.equals("None")) return "§7None";
        return "§e" + role + " §7[T" + tier + "]";
    }

    // ==================== WAKTU ====================
    public static long getWorldTime() { return worldTime; }
    public static void setWorldTime(long value) { worldTime = value; }

    public static long getWorldDay() {
        return (worldTime / 24000L) + 1;
    }

    public static String getFormattedTime() {
        long timeOfDay = worldTime % 24000L;
        long hours = ((timeOfDay / 1000L) + 6) % 24;
        long minutes = (long) (((timeOfDay % 1000L) / 1000.0) * 60);
        return String.format("%02d:%02d", hours, minutes);
    }

    // ==================== ENABLED + ALPHA ====================
    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean value) {
        enabled = value;
        targetAlpha = value ? 1.0f : 0.0f;
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static float getCurrentAlpha() { return currentAlpha; }
    public static float getTargetAlpha() { return targetAlpha; }

    public static void setCurrentAlpha(float alpha) {
        currentAlpha = Math.max(0f, Math.min(1f, alpha));
    }

    public static boolean shouldRender() {
        return currentAlpha > 0.01f;
    }

    public static void updateAnimation() {
        float diff = targetAlpha - currentAlpha;

        if (Math.abs(diff) < 0.01f) {
            currentAlpha = targetAlpha;
        } else {
            currentAlpha += Math.signum(diff) * 0.01f;
        }
    }
}