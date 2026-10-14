package rul.templatemod.stats;

/**
 * Data statistik untuk satu pemain.
 * Mencakup jumlah kill, death, dan statistik tambahan.
 */
public class PlayerStats {

    private int kills;
    private int deaths;
    private int mobKills;
    private int playerKills;

    public PlayerStats() {
        this.kills = 0;
        this.deaths = 0;
        this.mobKills = 0;
        this.playerKills = 0;
    }

    // ==================== GETTER ====================

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getMobKills() {
        return mobKills;
    }

    public int getPlayerKills() {
        return playerKills;
    }

    /**
     * Hitung rasio K/D. Aman untuk deaths = 0.
     */
    public double getKDRatio() {
        if (deaths == 0) {
            return kills; // kalau tidak ada death, K/D = kills
        }
        return (double) kills / deaths;
    }

    /**
     * Format K/D dengan 2 angka di belakang koma.
     */
    public String getKDRatioFormatted() {
        return String.format("%.2f", getKDRatio());
    }

    // ==================== PENAMBAHAN ====================

    public void addKill(boolean isPlayerKill) {
        this.kills++;
        if (isPlayerKill) {
            this.playerKills++;
        } else {
            this.mobKills++;
        }
    }

    public void addDeath() {
        this.deaths++;
    }

    // ==================== SERIALISASI ====================

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void setMobKills(int mobKills) {
        this.mobKills = mobKills;
    }

    public void setPlayerKills(int playerKills) {
        this.playerKills = playerKills;
    }
}