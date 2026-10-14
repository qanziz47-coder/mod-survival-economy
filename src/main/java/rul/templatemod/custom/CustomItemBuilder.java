package rul.templatemod.custom;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Helper untuk membuat custom item dengan NBT khusus.
 */
public class CustomItemBuilder {

    private final Item item;
    private String name = "Custom Item";
    private final List<String> loreLines = new ArrayList<>();
    private String customId = null;

    public CustomItemBuilder(Item item) {
        this.item = item;
    }

    public static CustomItemBuilder of(Item item) {
        return new CustomItemBuilder(item);
    }

    public CustomItemBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CustomItemBuilder lore(String line) {
        this.loreLines.add(line);
        return this;
    }

    public CustomItemBuilder customId(String id) {
        this.customId = id;
        return this;
    }

    public ItemStack build() {
        ItemStack stack = new ItemStack(item);

        // Nama
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(name).formatted(Formatting.GOLD, Formatting.BOLD));

        // Lore
        if (!loreLines.isEmpty()) {
            List<Text> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(Text.literal(line));
            }
            stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        }

        // Enchant glint — efek berkilau
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        // Custom NBT (untuk identifikasi)
        if (customId != null) {
            net.minecraft.nbt.NbtCompound nbt = new net.minecraft.nbt.NbtCompound();
            nbt.putString("custom_id", customId);
            nbt.putString("owner_uuid", UUID.randomUUID().toString());
            nbt.putLong("created_at", System.currentTimeMillis());
            stack.set(DataComponentTypes.CUSTOM_DATA,
                    net.minecraft.component.type.NbtComponent.of(nbt));
        }
        // Enchant glint (efek berkilau)
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    /**
     * Cek apakah ItemStack adalah custom item dengan ID tertentu.
     */
    public static boolean isCustomItem(ItemStack stack, String customId) {
        if (stack.isEmpty()) return false;
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return false;
        var nbt = customData.copyNbt();
        return nbt.contains("custom_id") && nbt.getString("custom_id").equals(customId);
    }

    /**
     * Ambil custom ID dari ItemStack.
     */
    public static String getCustomId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return null;
        var nbt = customData.copyNbt();
        return nbt.contains("custom_id") ? nbt.getString("custom_id") : null;
    }
}