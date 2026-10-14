package rul.templatemod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Perintah /info — panduan mod.
 */
public class InfoCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("info")
                        .executes(ctx -> showMain(ctx.getSource()))
                        .then(CommandManager.literal("economy")
                                .executes(ctx -> showEconomy(ctx.getSource())))
                        .then(CommandManager.literal("bank")
                                .executes(ctx -> showBank(ctx.getSource())))
                        .then(CommandManager.literal("role")
                                .executes(ctx -> showRole(ctx.getSource())))
                        .then(CommandManager.literal("quest")
                                .executes(ctx -> showQuest(ctx.getSource())))
                        .then(CommandManager.literal("items")
                                .executes(ctx -> showItems(ctx.getSource())))
                        .then(CommandManager.literal("democracy")
                                .executes(ctx -> showDemocracy(ctx.getSource())))
        );
    }

    // ==================== /info ====================

    private static int showMain(ServerCommandSource source) {
        send(source,
                "§6§l╔══════════════════════════════════╗",
                "§6§l║   SURVIVAL ECONOMY — INFO        ║",
                "§6§l╚══════════════════════════════════╝",
                "",
                "§6§l✦ Fitur Mod:",
                "§7- §aEkonomi §7(saldo, toko, inflasi)",
                "§7- §aBank §7(bunga majemuk)",
                "§7- §aPajak & Demokrasi",
                "§7- §aRole System + Gaji",
                "§7- §aQuest System §7(20 misi)",
                "§7- §a8 Custom Items §7(totem, role items)",
                "§7- §aSidebar HUD",
                "",
                "§6§l✦ Perintah Utama:",
                "§e/bal §7- Cek saldo",
                "§e/shop §7- Buka toko",
                "§e/bank §7- Bank",
                "§e/role §7- Role system",
                "§e/quest §7- Daftar misi",
                "§e/politik §7- Demokrasi",
                "§e/sidebar §7- Toggle HUD",
                "§e/givecustom §7- Custom items",
                "",
                "§6§l✦ Bantuan Lanjutan:",
                "§e/info economy §7- Detail ekonomi",
                "§e/info bank §7- Detail bank",
                "§e/info role §7- Detail role",
                "§e/info quest §7- Detail quest",
                "§e/info items §7- Detail custom items",
                "§e/info democracy §7- Detail demokrasi",
                ""
        );
        return 1;
    }

    // ==================== /info economy ====================

    private static int showEconomy(ServerCommandSource source) {
        send(source,
                "§6§l═══ 💰 EKONOMI ═══",
                "",
                "§7Saldo Coins tersimpan permanen di dunia.",
                "§7Setiap pemain punya saldo awal §e1.000 Coins§7.",
                "",
                "§6§l✦ Perintah:",
                "§e/bal §7- Cek saldo",
                "§e/balance §7- Alias /bal",
                "§e/money §7- Alias /bal",
                "§e/shop §7- Buka toko GUI",
                "§e/sell hand §7- Jual item di tangan",
                "§e/sell all §7- Jual semua item",
                "§e/sell <jumlah> §7- Jual sejumlah item",
                "",
                "§6§l✦ Toko:",
                "§7- §a170 item §7dalam §a11 kategori",
                "§7- Harga §edinamis §7(sesuai stok pasar)",
                "§7- Beli tinggi → harga naik §c↑",
                "§7- Jual banyak → harga turun §a↓",
                "",
                "§6§l✦ Notifikasi Harga:",
                "§7- Broadcast otomatis saat harga berubah >20%",
                ""
        );
        return 1;
    }

    // ==================== /info bank ====================

    private static int showBank(ServerCommandSource source) {
        send(source,
                "§6§l═══ 🏦 BANK ═══",
                "",
                "§7Simpan uang di bank → dapat §abunga majemuk§7.",
                "",
                "§6§l✦ Perintah:",
                "§e/bank deposit <jumlah> §7- Setor Coins",
                "§e/bank withdraw <jumlah> §7- Ambil Coins",
                "§e/bank balance §7- Cek saldo bank",
                "§e/bank simulasi <jumlah> <hari> §7- Simulasi bunga",
                "",
                "§6§l✦ Bunga:",
                "§7- Rate: §a1% per hari §7(di-compound)",
                "§7- 1000 Coins × 30 hari = §e~1.347 Coins",
                "§7- 1000 Coins × 365 hari = §e~37.783 Coins",
                "",
                "§6§l✦ Tips:",
                "§7- Uang di bank tetap aman saat offline",
                "§7- Compound interest — uang bekerja untukmu",
                ""
        );
        return 1;
    }

    // ==================== /info role ====================

    private static int showRole(ServerCommandSource source) {
        send(source,
                "§6§l═══ 🎭 ROLE SYSTEM ═══",
                "",
                "§7Pilih role → dapat §agaji §7berkala.",
                "",
                "§6§l✦ 5 Role:",
                "§a🌾 Petani §7- Farming focus",
                "§b⛏ Penambang §7- Mining focus",
                "§c⚔ Petualang §7- Combat focus",
                "§e🏗 Pembangun §7- Building focus",
                "§6💰 Pedagang §7- Trading focus",
                "",
                "§6§l✦ Perintah:",
                "§e/role §7- Lihat role sendiri",
                "§e/role list §7- Lihat 5 role",
                "§e/role set <role> §7- Pilih role",
                "§e/role info §7- Info detail",
                "§e/role gaji §7- Lihat gaji",
                "",
                "§6§l✦ Gaji (per 7 hari MC):",
                "§7Tier 1: §e500 Coins",
                "§7Tier 2: §e2.000 Coins",
                "§7Tier 3: §e5.000 Coins",
                "§7Tier 4: §e15.000 Coins",
                "§7Tier 5: §e50.000 Coins",
                "",
                "§6§l✦ Ganti Role:",
                "§7Biaya: §e1.000 Coins",
                ""
        );
        return 1;
    }

    // ==================== /info quest ====================

    private static int showQuest(ServerCommandSource source) {
        send(source,
                "§6§l═══ 📋 QUEST SYSTEM ═══",
                "",
                "§7Selesaikan misi → dapat §eCoins + Tier Up§7.",
                "",
                "§6§l✦ Perintah:",
                "§e/quest §7- Daftar semua misi",
                "§e/quest info <nama> §7- Detail misi",
                "",
                "§6§l✦ 20 Misi (5 Tier):",
                "§7Tier 1 §7(4 misi) — Pemula",
                "§7Tier 2 §7(4 misi) — Menengah",
                "§7Tier 3 §7(4 misi) — Ahli",
                "§7Tier 4 §7(4 misi) — Master",
                "§7Tier 5 §7(4 misi) — Legenda",
                "",
                "§6§l✦ Auto-Check:",
                "§7- Misi dicek otomatis §asetiap 2 detik",
                "§7- Cukup main normal — tidak perlu klaim",
                "§7- Popup advancement muncul saat selesai",
                "",
                "§6§l✦ Reward:",
                "§7Tier 1: §e500 Coins",
                "§7Tier 2: §e2.000 Coins",
                "§7Tier 3: §e5.000 Coins",
                "§7Tier 4: §e15.000 Coins",
                "§7Tier 5: §e50.000 Coins",
                ""
        );
        return 1;
    }

    // ==================== /info items ====================

    private static int showItems(ServerCommandSource source) {
        send(source,
                "§6§l═══ 🎨 CUSTOM ITEMS ═══",
                "",
                "§6§l✦ 3 Totem:",
                "§7- §6⚡ Totem of Resurrection §7- Revive + Full HP",
                "§7- §c🔥 Totem of Vengeance §7- Balas 300% damage",
                "§7- §b⌛ Totem of Time §7- Slow-motion mob",
                "",
                "§6§l✦ 5 Role Items:",
                "§7- §a🌾 Petani Hoe §7- Auto-harvest 3×3",
                "§7- §b⛏ Miner Pickaxe §7- Auto-smelt ore",
                "§7- §c⚔ Adventurer Blade §7- Lifesteal 20%",
                "§7- §e🏗 Builder Wand §7- Place 10 blok",
                "§7- §6💰 Trader Ring §7- Diskon 50%",
                "",
                "§6§l✦ Perintah:",
                "§e/givecustom <item> §7- Dapat item",
                "",
                "§6§l✦ Item ID:",
                "§7totem_resurrection, totem_vengeance, totem_time,",
                "§7petani_hoe, miner_pickaxe, adventurer_blade,",
                "§7builder_wand, trader_ring",
                ""
        );
        return 1;
    }

    // ==================== /info democracy ====================

    private static int showDemocracy(ServerCommandSource source) {
        send(source,
                "§6§l═══ 🏛️ DEMOKRASI ═══",
                "",
                "§7Sistem protes & voting pajak.",
                "",
                "§6§l✦ Pajak:",
                "§7- §cPPh §7(Penjualan): §e5%",
                "§7- §aPPN §7(Pembelian): §e2%",
                "§7- Pajak tinggi → pemain protes",
                "§7- Pajak >12% → auto referendum",
                "",
                "§6§l✦ Perintah:",
                "§e/protes §7- Protes pajak tinggi",
                "§e/vote A §7- Turunkan pajak 1%",
                "§e/vote B §7- Naikkan pajak 1%",
                "§e/vote C §7- Tetap",
                "§e/politik §7- Info politik",
                "",
                "§6§l✦ Mekanisme:",
                "§7- Protes >50% pemain → referendum",
                "§7- Referendum berlangsung 3 menit",
                "§7- Hasil voting mengikat",
                "§7- Cooldown 1 hari MC",
                ""
        );
        return 1;
    }

    // ==================== UTIL ====================

    private static void send(ServerCommandSource source, String... lines) {
        for (String line : lines) {
            source.sendMessage(Text.literal(line));
        }
    }
}