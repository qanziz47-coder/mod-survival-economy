package rul.templatemod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loader/saver untuk file config JSON.
 * Kalau file belum ada → buat default.
 * Kalau file rusak → fallback ke default.
 */
public class ShopConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("Survival Economy Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR = "survival_economy";
    private static final String CONFIG_FILE = "shop.json";

    /**
     * Ambil path file config.
     * Hasil: .minecraft/config/survival_economy/shop.json
     */
    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir()
                .resolve(CONFIG_DIR)
                .resolve(CONFIG_FILE);
    }

    /**
     * Load config dari file.
     * Kalau file tidak ada → return null (biar caller pakai default hardcoded).
     * Kalau file rusak → return null + log warning.
     */
    public static ShopConfigData load() {
        Path path = getConfigPath();

        if (!Files.exists(path)) {
            LOGGER.info("[Config] File config belum ada: " + path);
            return null;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            ShopConfigData data = GSON.fromJson(reader, ShopConfigData.class);

            if (data == null || data.items == null) {
                LOGGER.warn("[Config] File config kosong atau rusak, pakai default.");
                return null;
            }

            // Validasi setiap item
            int valid = 0;
            int invalid = 0;
            for (var it : data.items) {
                if (validateItem(it)) {
                    valid++;
                } else {
                    invalid++;
                }
            }

            LOGGER.info("[Config] Berhasil load " + valid + " item"
                    + (invalid > 0 ? " (" + invalid + " invalid, di-skip)" : ""));

            // Hapus yang invalid
            data.items.removeIf(it -> !validateItem(it));

            if (data.items.isEmpty()) {
                LOGGER.warn("[Config] Tidak ada item valid, pakai default.");
                return null;
            }

            return data;

        } catch (Exception e) {
            LOGGER.error("[Config] Gagal load config: " + e.getMessage());
            LOGGER.error("[Config] Pakai default hardcoded.");
            return null;
        }
    }

    /**
     * Simpan config ke file.
     */
    public static void save(ShopConfigData data) {
        Path path = getConfigPath();

        try {
            Files.createDirectories(path.getParent());

            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, writer);
                LOGGER.info("[Config] Config disimpan: " + path);
            }
        } catch (IOException e) {
            LOGGER.error("[Config] Gagal simpan config: " + e.getMessage());
        }
    }

    /**
     * Validasi satu item config.
     */
    private static boolean validateItem(ShopConfigData.ShopConfigItem item) {
        if (item.id == null || item.id.isEmpty()) return false;
        if (item.name == null || item.name.isEmpty()) return false;
        if (item.category == null || item.category.isEmpty()) return false;
        if (item.buyPrice < 0 || item.sellPrice < 0) return false;
        if (item.bundleSize <= 0) return false;
        if (item.dynamic) {
            if (item.baseStock <= 0) return false;
            if (item.minStock < 0 || item.minStock >= item.baseStock) return false;
            if (item.maxStock <= item.baseStock) return false;
        }
        return true;
    }
}