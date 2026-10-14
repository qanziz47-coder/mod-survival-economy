package rul.templatemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rul.templatemod.bank.BankCommand;
import rul.templatemod.command.BalanceCommand;
import rul.templatemod.command.EcoAdminCommand;
import rul.templatemod.command.EcoStatsCommand;
import rul.templatemod.command.EconomyCommand;
import rul.templatemod.command.GiveCustomCommand;
import rul.templatemod.command.SellCommand;
import rul.templatemod.custom.CustomItemEvents;
import rul.templatemod.economy.EconomyManager;
import rul.templatemod.network.HudSyncPayload;
import rul.templatemod.politics.PoliticsCommand;
import rul.templatemod.politics.PoliticsManager;
import rul.templatemod.politics.ProtestCommand;
import rul.templatemod.politics.VoteCommand;
import rul.templatemod.shop.ShopManager;
import rul.templatemod.shop.ShopScreenHandlerFactory;
import rul.templatemod.stats.PlayerStats;
import rul.templatemod.stats.PlayerStatsManager;
import rul.templatemod.stats.StatsEvents;
import rul.templatemod.role.RoleCommand;
import rul.templatemod.role.RoleManager;
import rul.templatemod.role.PlayerRole;
import rul.templatemod.quest.QuestCommand;
import rul.templatemod.quest.QuestListener;
import rul.templatemod.command.InfoCommand;

public class SurvivalEconomyMod implements ModInitializer {
	public static final String MOD_ID = "survival_economy";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static int tickCounter = 0;
	private static long lastDay = -1;

	@Override
	public void onInitialize() {
		LOGGER.info("[Survival Economy] Mod berhasil dimuat!");

		// Inisialisasi toko
		ShopManager.init();
		LOGGER.info("[Survival Economy] Daftar toko dimuat: " + ShopManager.getAllItems().size() + " item.");

		// Registrasi payload HUD
		HudSyncPayload.register();

		// Registrasi perintah
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			BalanceCommand.register(dispatcher);
			SellCommand.register(dispatcher);
			EconomyCommand.register(dispatcher);
			EcoAdminCommand.register(dispatcher);
			BankCommand.register(dispatcher);
			EcoStatsCommand.register(dispatcher);
			ProtestCommand.register(dispatcher);
			VoteCommand.register(dispatcher);
			PoliticsCommand.register(dispatcher);
			GiveCustomCommand.register(dispatcher);
			RoleCommand.register(dispatcher);
			QuestCommand.register(dispatcher);
			InfoCommand.register(dispatcher);

			dispatcher.register(
					CommandManager.literal("shop")
							.executes(ctx -> {
								var player = ctx.getSource().getPlayerOrThrow();
								player.openHandledScreen(new ShopScreenHandlerFactory());
								return 1;
							})
			);

			LOGGER.info("[Survival Economy] Perintah berhasil didaftarkan.");
		});

		// Registrasi statistik
		StatsEvents.register();
		LOGGER.info("[Survival Economy] Event statistik berhasil didaftarkan.");

		// Registrasi custom item events
		CustomItemEvents.register();
		LOGGER.info("[Survival Economy] Custom item events berhasil didaftarkan.");

		// Registrasi quest listener
		QuestListener.register();
		LOGGER.info("[Survival Economy] Quest listener berhasil didaftarkan.");

		// Deteksi hari awal
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			lastDay = server.getOverworld().getTimeOfDay() / 24000L;
			LOGGER.info("[Survival Economy] Hari awal terdeteksi: " + lastDay);

			var taxStorage = rul.templatemod.economy.TaxStorage.getServerState(server);
			LOGGER.info("[Survival Economy] Pajak — PPh: " + taxStorage.getSellTaxPercent()
					+ "%, PPN: " + taxStorage.getBuyTaxPercent() + "%");
		});

		// Tick handler
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
	}

	private void onServerTick(MinecraftServer server) {

		tickCounter++;

		// Deteksi hari berganti
		long currentDay = server.getOverworld().getTimeOfDay() / 24000L;
		if (lastDay >= 0 && currentDay != lastDay) {
			lastDay = currentDay;
			ShopManager.regenerateStock(server, 0.05);

			long totalInterest = rul.templatemod.bank.BankManager.applyInterest(server);

			LOGGER.info("[Survival Economy] Hari baru: " + currentDay
					+ " — stok diregenerasi 5%, bunga bank "
					+ rul.templatemod.bank.BankManager.INTEREST_RATE + "% = "
					+ EconomyManager.format(totalInterest) + " Coins.");
		}
		// Cek semua gaji role
		RoleManager.checkAllSalaries(server);
		LOGGER.info("[Survival Economy] Gaji role dicek untuk semua pemain.");

		// Cek referendum
		if (tickCounter % 20 == 0) {
			PoliticsManager.checkReferendumCompletion(server);
			PoliticsManager.checkAutoReferendum(server);
		}

		// Sync HUD
		if (tickCounter >= 20) {
			tickCounter = 0;
			for (var player : server.getPlayerManager().getPlayerList()) {
				long balance = EconomyManager.getBalance(server, player);
				PlayerStats stats = PlayerStatsManager.getStats(server, player);
				long worldTime = server.getOverworld().getTimeOfDay();

				// Role + tier
				PlayerRole role = RoleManager.getRole(server, player.getUuid());
				String roleName = (role != null) ? role.getDisplayName() : "None";
				int tier = RoleManager.getTier(server, player.getUuid());

				ServerPlayNetworking.send(player, new HudSyncPayload(
						balance,
						stats.getKills(),
						stats.getDeaths(),
						worldTime,
						roleName,
						tier
				));

				// Gaji otomatis — cek setiap 1 detik
				RoleManager.checkSalary(server, player);
			}
		}
	}
}