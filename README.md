# 💰 Survival Economy Mod

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4-green.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.17.2-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> Mod ekonomi lengkap untuk Minecraft Java Edition 1.21.4 dengan Fabric Loader. 
> Menghadirkan sistem ekonomi, toko, bank, pajak, demokrasi, role, dan quest — semua dalam satu mod!

---

## ✨ Fitur Utama

### 💰 Ekonomi
- **Sistem saldo** dengan penyimpanan permanen di dunia
- **Toko GUI** dengan 170 item dalam 11 kategori
- **Beli & jual** item dengan mudah
- **Inflasi harga dinamis** — harga naik saat stok menipis
- **Notifikasi harga** otomatis saat perubahan >20%

### 🏦 Bank
- **Setor & ambil** uang dari bank
- **Bunga majemuk** 1% per hari
- **Simulasi bunga** untuk perencanaan

### 💸 Pajak & Demokrasi
- **Pajak penjualan** (PPh) 5%
- **Pajak pembelian** (PPN) 2%
- **Protes pajak** — pemain bisa protes
- **Referendum voting** — ubah pajak lewat suara
- **Auto-referendum** saat pajak >12%

### 🎭 Role System
- **5 role**: Petani, Penambang, Petualang, Pembangun, Pedagang
- **Gaji otomatis** setiap 7 hari Minecraft
- **Tier system** (1-5) — naik tier, gaji naik

### 📋 Quest System
- **20 misi** dari pemula hingga legenda
- **Auto-check** via advancement
- **Reward** Coins + Tier up
- **Advancement popup** in-game

### 🎨 Custom Items
- **3 Totem** — Resurrection, Vengeance, Time
- **5 Role Items** — Petani Hoe, Miner Pickaxe, Adventurer Blade, Builder Wand, Trader Ring

### 🖥️ UI & Polish
- **Sidebar HUD** dengan fade animation
- **Sound effects** untuk setiap aksi
- **In-game documentation** via `/info`

---

## 📸 Screenshot

<img width="1920" height="1080" alt="Screenshot 2026-10-14 210005" src="https://github.com/user-attachments/assets/e57f8ff6-ac6c-44f5-a717-74f5715c6009" />
<img width="1920" height="1080" alt="Screenshot 2026-10-14 210051" src="https://github.com/user-attachments/assets/3e68077e-9f09-4299-894d-bd08d2e0fb5e" />



---

## 🔧 Cara Install

### Prasyarat
- **Minecraft Java Edition 1.21.4**
- **Fabric Loader 0.17.2+**
- **Fabric API 0.119.4+1.21.4**
- **Java 21**

### Langkah Instalasi

1. **Install Fabric Loader** dari [fabricmc.net](https://fabricmc.net/use/)
2. **Download Fabric API** dari [Modrinth](https://modrinth.com/mod/fabric-api/versions?g=1.21.4)
3. **Download** file `survival-economy-1.0.0.jar` dari [Releases](../../releases)
4. **Copy** kedua file `.jar` ke folder `mods/`:
5. **Jalankan Minecraft** dengan profil Fabric 1.21.4
6. **Selesai!** Mod akan otomatis dimuat

---

## 🎮 Perintah

### Ekonomi
| Perintah | Fungsi |
|----------|--------|
| `/bal` | Cek saldo |
| `/balance` | Alias `/bal` |
| `/money` | Alias `/bal` |
| `/shop` | Buka toko GUI |
| `/sell hand` | Jual item di tangan |
| `/sell all` | Jual semua item |
| `/sell <jumlah>` | Jual sejumlah item |

### Bank
| Perintah | Fungsi |
|----------|--------|
| `/bank deposit <jumlah>` | Setor uang |
| `/bank withdraw <jumlah>` | Ambil uang |
| `/bank balance` | Cek saldo bank |
| `/bank simulasi <jumlah> <hari>` | Simulasi bunga |

### Role
| Perintah | Fungsi |
|----------|--------|
| `/role` | Info role sendiri |
| `/role list` | Lihat 5 role |
| `/role set <role>` | Pilih role |
| `/role info` | Info detail |
| `/role gaji` | Lihat gaji |

### Quest
| Perintah | Fungsi |
|----------|--------|
| `/quest` | Daftar misi |
| `/quest info <nama>` | Detail misi |

### Demokrasi
| Perintah | Fungsi |
|----------|--------|
| `/protes` | Protes pajak |
| `/vote A/B/C` | Vote referendum |
| `/politik` | Info politik |

### UI
| Perintah | Fungsi |
|----------|--------|
| `/sidebar on/off/toggle` | Toggle HUD |
| `/info` | Panduan mod |

### Custom Items
| Perintah | Fungsi |
|----------|--------|
| `/givecustom petani_hoe` | Petani Hoe |
| `/givecustom miner_pickaxe` | Miner Pickaxe |
| `/givecustom adventurer_blade` | Adventurer Blade |
| `/givecustom builder_wand` | Builder Wand |
| `/givecustom trader_ring` | Trader Ring |
| `/givecustom totem_resurrection` | Totem of Resurrection |
| `/givecustom totem_vengeance` | Totem of Vengeance |
| `/givecustom totem_time` | Totem of Time |

### Admin
| Perintah | Fungsi |
|----------|--------|
| `/eco give <player> <jumlah>` | Kasih Coins |
| `/eco take <player> <jumlah>` | Ambil Coins |
| `/eco set <player> <jumlah>` | Set Coins |
| `/eco settax sell/buy <persen>` | Set pajak |
| `/eco showtax` | Lihat pajak |
| `/eco forcevote` | Paksa referendum |
| `/eco reloadshop` | Reload config toko |
| `/quest complete <nama>` | Force complete misi |

---

## ⚙️ Konfigurasi

### File Config
- **`.minecraft/config/survival_economy/shop.json`** — Harga item toko

### Data Dunia
Tersimpan di folder dunia:
- **`.minecraft/saves/<dunia>/data/survival_economy_*.dat`**
- `economy_data` — Saldo pemain
- `bank` — Saldo bank
- `shop_stock` — Stok toko
- `stats` — Statistik kill/death
- `politics` — Data politik
- `role` — Data role
- `quest` — Progress misi

---

## 🎓 Edukasi

Mod ini dirancang **edukatif** untuk mengajarkan:
- 💰 **Compound Interest** — via bank
- 📈 **Supply & Demand** — via inflasi harga
- 🏛️ **Demokrasi** — via protes & voting
- 💼 **Karier & Gaji** — via role system
- 🎯 **Tujuan & Reward** — via quest system

---

## 🤝 Kontribusi

Kontribusi **sangat diterima**! Cara:
1. Fork repo
2. Buat branch baru (`git checkout -b feature/FiturBaru`)
3. Commit perubahan (`git commit -m 'Tambah fitur X'`)
4. Push ke branch (`git push origin feature/FiturBaru`)
5. Buat Pull Request

---

## 🐛 Bug Report

Kalau nemu bug:
1. Buka [Issues](../../issues)
2. Klik **New Issue**
3. Deskripsikan bug + screenshot
4. Sertakan log Minecraft

---

## 📜 License

Mod ini dilisensikan di bawah **MIT License** — lihat file [LICENSE](LICENSE) untuk detail.

---

## 💻 Author

**Qanziz47-coder**
- GitHub: [@qanziz47-coder](https://github.com/qanziz47-coder)

---

## 🙏 Terima Kasih

- **Fabric Team** — untuk mod loader
- **Mojang** — untuk Minecraft
- **Komunitas Minecraft Indonesia** — untuk dukungan

---

<div align="center">

**⭐ Kalau mod ini bermanfaat, kasih star di GitHub! ⭐**

Made with Fajri Hafz

</div>
