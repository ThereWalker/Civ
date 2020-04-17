package vg.civcraft.mc.civmodcore;

import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.HumanEntity;
import vg.civcraft.mc.civmodcore.api.EnchantNames;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.chatDialog.ChatListener;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventoryListener;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkDAO;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.ConfigCommand;
import vg.civcraft.mc.civmodcore.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.scoreboard.side.ScoreBoardListener;
import vg.civcraft.mc.civmodcore.serialization.NBTSerialization;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

public final class CivModCorePlugin extends ACivMod {

	private static CivModCorePlugin instance;

	private GlobalChunkMetaManager chunkMetaManager;

	private ManagedDatasource database;

	private AikarCommandManager manager;

	@Override
	public void onEnable() {
		this.useNewCommandHandler = false;
		super.onEnable();
		instance = this;
		// Save default resources
		saveDefaultResource("enchantments.csv");
		saveDefaultResource("materials.csv");
		// Register listeners
		registerListener(new ClickableInventoryListener());
		registerListener(new ChatListener());
		registerListener(new ScoreBoardListener());
		// Register commands
		this.manager = new AikarCommandManager(this) {
			@Override
			public void registerCommands() {
				registerCommand(new ConfigCommand());
			}
		};
		ConfigurationSerialization.registerClass(ManagedDatasource.class);
		// Load Database
		try {
			this.database = (ManagedDatasource) getConfig().get("database");
			ChunkDAO dao = new ChunkDAO(this.database, this);
			if (dao.updateDatabase()) {
				this.chunkMetaManager = new GlobalChunkMetaManager(dao);
				info("Setup database successfully");
			}
			else {
				warning("Could not setup database");
			}
		}
		catch (Exception error) {
			warning("Cannot get database from config.", error);
			database = null;
		}
		// Load APIs
		ItemNames.loadItemNames();
		EnchantNames.loadEnchantmentNames();
		BottomLineAPI.init();
	}

	@Override
	public void onDisable() {
		Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);
		ItemNames.resetItemNames();
		EnchantNames.resetEnchantmentNames();
		ChunkMetaAPI.saveAll();
		chunkMetaManager = null;
		// Disconnect database
		if (database != null) {
			try {
				database.close();
			}
			catch (SQLException error) {
				warning("Was unable to close the database.", error);
			}
			database = null;
		}
		PlayerSettingAPI.saveAll();
		ConfigurationSerialization.unregisterClass(ManagedDatasource.class);
		NBTSerialization.clearAllRegistrations();
		NullCoalescing.exists(this.manager, AikarCommandManager::reset);
		super.onDisable();
		instance = null;
	}

	public static CivModCorePlugin getInstance() {
		return instance;
	}
	
	public GlobalChunkMetaManager getChunkMetaManager() {
		return chunkMetaManager;
	}

}
