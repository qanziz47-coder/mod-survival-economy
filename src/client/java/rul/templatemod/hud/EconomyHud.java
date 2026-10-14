package rul.templatemod.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Render HUD sidebar dengan indikator mode di bawah Rank.
 */
public class EconomyHud {

    private static final NumberFormat FORMATTER = NumberFormat.getInstance(new Locale("id", "ID"));

    // ==================== WARNA ====================
    private static final int COLOR_BG = 0xD0000000;
    private static final int COLOR_BORDER_OUTER = 0xFF000000;
    private static final int COLOR_BORDER_INNER = 0xFFFFAA00;
    private static final int COLOR_HEADER_STRIP = 0x40FFAA00;
    private static final int COLOR_HEADER_TEXT = 0xFFFFD700;
    private static final int COLOR_DIVIDER = 0x80FFAA00;
    private static final int COLOR_SUBHEADER = 0xFFFFAA00;
    private static final int COLOR_LABEL = 0xFFCCCCCC;
    private static final int COLOR_VALUE = 0xFFFFFFFF;
    private static final int COLOR_ACCENT = 0xFFFFD700;
    private static final int COLOR_RANK = 0xFFFF5555;

    // ==================== LAYOUT ====================
    private static final int MARGIN_RIGHT = 8;
    private static final int MARGIN_TOP = 55;
    private static final int EXTRA_WIDTH = 10;

    // ==================== ICON ====================
    private static final String ICON_PLAYER = "\u2666";
    private static final String ICON_RANK = "\u2605";
    private static final String ICON_MODE = "\u2694";
    private static final String ICON_KILL = "\u2694";
    private static final String ICON_DEATH = "\u2620";
    private static final String ICON_KD = "\u2696";
    private static final String ICON_COIN = "\u25C6";
    private static final String ICON_DAY = "\u2600";
    private static final String ICON_TIME = "\u231B";

    // ==================== STRUKTUR ====================
    private enum LineType { HEADER, DIVIDER, SUBHEADER, INFO, SPACER }

    private record Line(LineType type, String label, String value, int valueColor) {
        static Line header(String text) { return new Line(LineType.HEADER, text, "", COLOR_HEADER_TEXT); }
        static Line divider() { return new Line(LineType.DIVIDER, "", "", COLOR_DIVIDER); }
        static Line subheader(String text) { return new Line(LineType.SUBHEADER, text, "", COLOR_SUBHEADER); }
        static Line info(String label, String value, int color) { return new Line(LineType.INFO, label, value, color); }
        static Line spacer() { return new Line(LineType.SPACER, "", "", COLOR_VALUE); }
    }

    public static void register() {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> {
            layeredDrawer.attachLayerAfter(
                    IdentifiedLayer.MISC_OVERLAYS,
                    Identifier.of("survival_economy", "sidebar"),
                    (context, tickCounter) -> render(context)
            );
        });
    }

    private static void render(DrawContext context) {
        // Animasi alpha
        HudData.updateAnimation();

        // Skip kalau alpha = 0
        if (!HudData.shouldRender()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (client.getDebugHud().shouldShowDebugHud()) return;

        var textRenderer = client.textRenderer;
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        // Layout adaptif
        int padding;
        int lineHeight;
        if (screenWidth >= 800) { padding = 8; lineHeight = 10; }
        else if (screenWidth >= 500) { padding = 7; lineHeight = 10; }
        else { padding = 6; lineHeight = 9; }

        // Ambil data
        String playerName = client.player.getName().getString();
        String rank = getPlayerRank(client);
        String modeText = getGameModeText(client);
        int modeColor = getGameModeColor(client);
        long balance = HudData.getBalance();
        int kills = HudData.getKills();
        int deaths = HudData.getDeaths();
        String kd = HudData.getKDRatioFormatted();
        long day = HudData.getWorldDay();
        String time = HudData.getFormattedTime();

        // ==================== SUSUN BARIS ====================
        List<Line> lines = new ArrayList<>();
        lines.add(Line.header("Survival Economy"));
        lines.add(Line.divider());

        // Identitas + Mode (mode di bawah rank, format sama)
        lines.add(Line.info(ICON_PLAYER + " Player", playerName, COLOR_VALUE));
        lines.add(Line.info(ICON_RANK + " Rank", rank, COLOR_RANK));
        lines.add(Line.info("\u2694 Mode", getGameModeText(client), getGameModeColor(client)));
        lines.add(Line.info("\u2605 Role", HudData.getRoleDisplay(), COLOR_ACCENT));


        lines.add(Line.spacer());

        // Stats
        lines.add(Line.subheader("Stats"));
        lines.add(Line.info(ICON_KILL + " Kills", String.valueOf(kills), COLOR_VALUE));
        lines.add(Line.info(ICON_DEATH + " Deaths", String.valueOf(deaths), COLOR_VALUE));
        lines.add(Line.info(ICON_KD + " K/D", kd, COLOR_VALUE));

        lines.add(Line.spacer());

        // Ekonomi
        lines.add(Line.subheader("Economy"));
        lines.add(Line.info(ICON_COIN + " Coins", FORMATTER.format(balance), COLOR_ACCENT));
        lines.add(Line.info(ICON_DAY + " Day", String.valueOf(day), COLOR_VALUE));
        lines.add(Line.info(ICON_TIME + " Time", time, COLOR_VALUE));

        // ==================== HITUNG UKURAN ====================
        int maxWidth = 0;
        for (Line line : lines) {
            int w;
            switch (line.type) {
                case HEADER, SUBHEADER, DIVIDER -> w = textRenderer.getWidth(line.label) + 4;
                case INFO -> w = textRenderer.getWidth(line.label + ": ") + textRenderer.getWidth(line.value) + 10;
                default -> w = 0;
            }
            if (w > maxWidth) maxWidth = w;
        }
        maxWidth += EXTRA_WIDTH;

        int panelWidth = maxWidth + padding * 2;
        int panelHeight = lines.size() * lineHeight + padding * 2;
        int panelX = screenWidth - panelWidth - MARGIN_RIGHT;
        int panelY = MARGIN_TOP;

        if (panelX < 0 || panelY + panelHeight > screenHeight) return;

        // ==================== RENDER BACKGROUND ====================
        float alpha = HudData.getCurrentAlpha();
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2,
                withAlpha(COLOR_BORDER_OUTER, alpha));
        context.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, COLOR_BORDER_INNER);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, COLOR_BG);

        // ==================== RENDER BARIS ====================
        int textY = panelY + padding;
        for (Line line : lines) {
            switch (line.type) {
                case DIVIDER -> context.fill(
                        panelX + padding, textY + 5,
                        panelX + panelWidth - padding, textY + 6,
                        COLOR_DIVIDER);
                case HEADER -> {
                    context.fill(panelX + 1, textY - 1,
                            panelX + panelWidth - 1, textY + lineHeight - 1,
                            COLOR_HEADER_STRIP);
                    int headerWidth = textRenderer.getWidth(line.label);
                    int headerX = panelX + (panelWidth - headerWidth) / 2;
                    context.drawTextWithShadow(textRenderer,
                            Text.literal(line.label).formatted(Formatting.BOLD),
                            headerX, textY, line.valueColor);
                }
                case SUBHEADER -> {
                    context.fill(panelX + padding, textY + 4,
                            panelX + padding + 2, textY + 5, COLOR_SUBHEADER);
                    context.drawTextWithShadow(textRenderer,
                            Text.literal(line.label).formatted(Formatting.BOLD),
                            panelX + padding + 6, textY, line.valueColor);
                }
                case INFO -> {
                    context.drawTextWithShadow(textRenderer,
                            Text.literal(line.label + ":"),
                            panelX + padding, textY, COLOR_LABEL);
                    int valueWidth = textRenderer.getWidth(line.value);
                    int valueX = panelX + panelWidth - padding - valueWidth;
                    context.drawTextWithShadow(textRenderer,
                            Text.literal(line.value),
                            valueX, textY, line.valueColor);
                }
                case SPACER -> { }
            }
            textY += lineHeight;
        }
    }

    // ==================== MODE ====================

    /**
     * Teks mode — tanpa icon di depan (biar iconnya dari Line.info).
     */
    private static String getGameModeText(MinecraftClient client) {
        if (client.player == null) return "UNKNOWN";

        if (client.world != null && client.world.getLevelProperties().isHardcore()) {
            return "HARDCORE";
        }

        if (client.player.isCreative()) return "CREATIVE";
        if (client.player.isSpectator()) return "SPECTATOR";

        return "SURVIVAL";
    }

    /**
     * Warna mode.
     */
    private static int getGameModeColor(MinecraftClient client) {
        if (client.player == null) return 0xFFAAAAAA;

        if (client.world != null && client.world.getLevelProperties().isHardcore()) {
            return 0xFFFF5555; // merah
        }

        if (client.player.isCreative()) return 0xFF55AAFF;   // biru
        if (client.player.isSpectator()) return 0xFFAAAAAA;   // abu-abu
        return 0xFF55FF55; // hijau (survival)
    }

    // ==================== RANK ====================

    private static String getPlayerRank(MinecraftClient client) {
        if (client.player == null) return "PLAYER";
        try {
            if (client.getServer() != null) {
                var serverPlayer = client.getServer().getPlayerManager().getPlayer(client.player.getUuid());
                if (serverPlayer != null && serverPlayer.hasPermissionLevel(2)) return "OWNER";
            }
        } catch (Exception e) { }
        if (client.player.isCreative()) return "ADMIN";
        return "PLAYER";
    }

    /**
     * Konversi warna dengan alpha.
     */
    private static int withAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}