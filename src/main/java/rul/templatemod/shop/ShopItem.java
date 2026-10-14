package rul.templatemod.shop;

/**
 * Data satu item di toko.
 * Mendukung harga statis dan harga dinamis (inflasi).
 */
public class ShopItem {

    // ==================== IDENTITAS ====================
    private final String itemId;
    private final String displayName;
    private final ShopCategory category;

    // ==================== HARGA DASAR (STATIS) ====================
    private final long buyPrice;
    private final long sellPrice;

    // ==================== TRANSAKSI ====================
    private final int bundleSize;

    // ==================== STOK (UNTUK INFLASI) ====================
    private final long baseStock;   // stok normal
    private final long minStock;    // batas bawah
    private final long maxStock;    // batas atas
    private final boolean dynamicPricing; // true = ikut inflasi

    // ==================== CONSTRUCTOR LENGKAP ====================

    public ShopItem(String itemId, String displayName, long buyPrice, long sellPrice,
                    int bundleSize, ShopCategory category,
                    long baseStock, long minStock, long maxStock, boolean dynamicPricing) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.bundleSize = bundleSize;
        this.category = category;
        this.baseStock = baseStock;
        this.minStock = minStock;
        this.maxStock = maxStock;
        this.dynamicPricing = dynamicPricing;
    }

    // ==================== CONSTRUCTOR SIMPEL (TANPA INFLASI) ====================

    public ShopItem(String itemId, String displayName, long buyPrice, long sellPrice,
                    int bundleSize, ShopCategory category) {
        this(itemId, displayName, buyPrice, sellPrice, bundleSize, category,
                1000L, 100L, 10000L, false);
    }

    // ==================== GETTER DASAR ====================

    public String getItemId() { return itemId; }
    public String getDisplayName() { return displayName; }
    public long getBuyPrice() { return buyPrice; }
    public long getSellPrice() { return sellPrice; }
    public int getBundleSize() { return bundleSize; }
    public ShopCategory getCategory() { return category; }

    public boolean canBuy() { return buyPrice > 0; }
    public boolean canSell() { return sellPrice > 0; }

    // ==================== GETTER STOK ====================

    public long getBaseStock() { return baseStock; }
    public long getMinStock() { return minStock; }
    public long getMaxStock() { return maxStock; }
    public boolean isDynamicPricing() { return dynamicPricing; }

    // ==================== HARGA DINAMIS ====================

    /**
     * Hitung multiplier berdasarkan stok saat ini.
     * - Stok normal (baseStock) → multiplier = 1.0
     * - Stok menipis → multiplier > 1.0 (harga naik)
     * - Stok melimpah → multiplier < 1.0 (harga turun)
     */
    public double getMultiplier(long currentStock) {
        // Clamp stok ke batas min/max
        long clamped = Math.max(minStock, Math.min(maxStock, currentStock));

        if (clamped <= 0) {
            return 3.0; // stok habis = harga 3x lipat
        }

        return (double) baseStock / clamped;
    }

    /**
     * Harga beli dinamis berdasarkan stok saat ini.
     * Cap maksimal 5x harga dasar.
     */
    public long getDynamicBuyPrice(long currentStock) {
        long price = Math.round(buyPrice * getMultiplier(currentStock));
        return Math.min(price, buyPrice * 5);
    }

    /**
     * Harga jual dinamis berdasarkan stok saat ini.
     * Cap minimal 0.2x harga dasar (biar tidak jadi nol).
     */
    public long getDynamicSellPrice(long currentStock) {
        long price = Math.round(sellPrice * getMultiplier(currentStock));
        return Math.max(price, Math.max(1L, sellPrice / 5));
    }

    // ==================== TOTAL HARGA ====================

    /**
     * Total biaya pembelian (harga statis × jumlah).
     * Untuk harga dinamis, hitung manual pakai getDynamicBuyPrice.
     */
    public long getTotalBuyPrice(int amount) {
        return buyPrice * amount;
    }

    /**
     * Total pendapatan penjualan (harga statis × jumlah).
     */
    public long getTotalSellPrice(int amount) {
        return sellPrice * amount;
    }
}