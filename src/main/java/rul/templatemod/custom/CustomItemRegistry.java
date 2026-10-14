package rul.templatemod.custom;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Daftar semua custom item di mod.
 */
public class CustomItemRegistry {

    // Custom ID — dipakai untuk identifikasi
    public static final String ID_TOTEM_RESURRECTION = "totem_resurrection";
    public static final String ID_TOTEM_VENGEANCE = "totem_vengeance";
    public static final String ID_TOTEM_TIME = "totem_time";
    public static final String ID_PETANI_HOE = "petani_hoe";
    public static final String ID_MINER_PICKAXE = "miner_pickaxe";
    public static final String ID_ADVENTURER_BLADE = "adventurer_blade";
    public static final String ID_BUILDER_WAND = "builder_wand";
    public static final String ID_TRADER_RING = "trader_ring";

    // ==================== TOTEM OF RESURRECTION ====================

    public static ItemStack createTotemOfResurrection() {
        return CustomItemBuilder.of(Items.TOTEM_OF_UNDYING)
                .name("§6§l✦ Totem of Resurrection ✦")
                .lore("§7Reward: Kill semua bos")
                .lore("")
                .lore("§7Kemampuan:")
                .lore("§7- Bangkit dengan §aFull HP")
                .lore("§7- §aRegeneration V §7(10 detik)")
                .lore("§7- §aFire Resistance §7(60 detik)")
                .lore("")
                .lore("§7Cooldown: §e5 menit")
                .lore("§7Charge: §a3x")
                .lore("")
                .lore("§7§oSoulbound — tidak bisa di-trade")
                .customId(ID_TOTEM_RESURRECTION)
                .build();
    }

    // ==================== TOTEM OF VENGEANCE ====================

    public static ItemStack createTotemOfVengeance() {
        return CustomItemBuilder.of(Items.TOTEM_OF_UNDYING)
                .name("§c§l✦ Totem of Vengeance ✦")
                .lore("§7Reward: Bunuh 5000 mob")
                .lore("")
                .lore("§7Kemampuan:")
                .lore("§7- Balas §c300% §7damage saat HP < 3")
                .lore("§7- Otomatis aktif di off-hand")
                .lore("")
                .lore("§7Cooldown: §e30 detik")
                .lore("§7Charge: §a10x")
                .lore("")
                .lore("§7§oSoulbound — tidak bisa di-trade")
                .customId(ID_TOTEM_VENGEANCE)
                .build();
    }

    // ==================== TOTEM OF TIME ====================

    public static ItemStack createTotemOfTime() {
        return CustomItemBuilder.of(Items.TOTEM_OF_UNDYING)
                .name("§b§l✦ Totem of Time ✦")
                .lore("§7Reward: Selesaikan misi dalam 3 menit")
                .lore("")
                .lore("§7Kemampuan:")
                .lore("§7- Slow-motion mob di sekitar saat HP < 5")
                .lore("§7- Efek §bSlowness IV §7untuk mob (10 detik)")
                .lore("")
                .lore("§7Cooldown: §e2 menit")
                .lore("§7Charge: §a5x")
                .lore("")
                .lore("§7§oSoulbound — tidak bisa di-trade")
                .customId(ID_TOTEM_TIME)
                .build();
    }

    // ==================== PETANI HOE ====================

    public static ItemStack createPetaniHoe() {
        return CustomItemBuilder.of(Items.DIAMOND_HOE)
                .name("§6§l✦ Petani Hoe ✦")
                .lore("§7Reward: Petani Tier 5")
                .lore("")
                .lore("§7Kemampuan:")
                .lore("§7- Auto-harvest §a3x3 §7saat klik kanan")
                .lore("§7- Auto-replant otomatis")
                .lore("§7- §bEfficiency V")
                .lore("§7- §6Fortune III")
                .lore("")
                .lore("§7§oSoulbound — tidak bisa di-trade")
                .customId(ID_PETANI_HOE)
                .build();
    }
    // ==================== MINER PICKAXE ====================

    public static ItemStack createMinerPickaxe() {
        return CustomItemBuilder.of(Items.DIAMOND_PICKAXE)
                .name("§b§l✦ Miner Pickaxe ✦")
                .lore("§7Reward: Penambang Tier 5")
                .lore("")
                .lore("§7Kemampuan:")
                .lore("§7- Auto-smelt §aore §7saat ditambang")
                .lore("§7- §bEfficiency V")
                .lore("§7- §6Fortune III")
                .lore("")
                .lore("§7§oSoulbound — tidak bisa di-trade")
                .customId(ID_MINER_PICKAXE)
                .build();
    }
    // ==================== ADVENTURER BLADE ====================

    public static ItemStack createAdventurerBlade() {
        return CustomItemBuilder.of(Items.DIAMOND_SWORD)
                .name("§c§l✦ Adventurer Blade ✦")
                .lore("§7Reward: Petualang Tier 5")
                .lore("")
                .lore("§7Kemampuan:")
                .lore("§7- §cLifesteal 20% §7per kill")
                .lore("§7- §cSharpness V")
                .lore("§7- §6Fire Aspect II")
                .lore("")
                .lore("§7§oSoulbound — tidak bisa di-trade")
                .customId(ID_ADVENTURER_BLADE)
                .build();
    }
    // ==================== BUILDER WAND ====================

    public static ItemStack createBuilderWand() {
        return CustomItemBuilder.of(Items.STICK)
                .name("§e§l✦ Builder Wand ✦")
                .lore("§7Reward: Pembangun Tier 5")
                .lore("")
                .lore("§7Kemampuan:")
                .lore("§7- Place §a10 blok §7sekaligus")
                .lore("§7- Shift + Klik Kanan untuk pakai")
                .lore("§7- Blok diambil dari off-hand")
                .lore("")
                .lore("§7§oSoulbound — tidak bisa di-trade")
                .customId(ID_BUILDER_WAND)
                .build();
    }

    // ==================== TRADER RING ====================

    public static ItemStack createTraderRing() {
        return CustomItemBuilder.of(Items.GOLD_NUGGET)
                .name("§6§l✦ Trader Ring ✦")
                .lore("§7Reward: Pedagang Tier 5")
                .lore("")
                .lore("§7Kemampuan:")
                .lore("§7- Diskon toko §a50%")
                .lore("§7- Bonus jual §a50%")
                .lore("§7- Passive (cukup di inventory)")
                .lore("")
                .lore("§7§oSoulbound — tidak bisa di-trade")
                .customId(ID_TRADER_RING)
                .build();
    }
}