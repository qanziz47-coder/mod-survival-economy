package rul.templatemod.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Struktur data untuk file config JSON.
 * Bentuknya persis seperti isi file shop.json.
 */
public class ShopConfigData {

    public List<ShopConfigItem> items = new ArrayList<>();

    /**
     * Satu entry item di config.
     */
    public static class ShopConfigItem {
        public String id;            // minecraft:diamond
        public String name;          // Diamond
        public String category;      // ORES_MINERALS
        public long buyPrice = 0;
        public long sellPrice = 0;
        public int bundleSize = 1;
        public boolean dynamic = false;
        public long baseStock = 1000;
        public long minStock = 100;
        public long maxStock = 10000;
    }
}