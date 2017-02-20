package com.programmerdan.minecraft.banstick.handler;

import org.bukkit.configuration.file.FileConfiguration;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.commands.BanSaveCommand;
import com.programmerdan.minecraft.banstick.commands.BanStickCommand;
import com.programmerdan.minecraft.banstick.commands.ForgiveCommand;

/**
 * Handles Commands for this plugin. Check plugin.yml for details!
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class BanStickCommandHandler {
	
	public BanStickCommandHandler(FileConfiguration config) {
		registerCommands();
	}
	
	private void registerCommands() {
		BanStick.getPlugin().getCommand(BanStickCommand.name).setExecutor(new BanStickCommand());
		BanStick.getPlugin().getCommand(ForgiveCommand.name).setExecutor(new ForgiveCommand());
		BanStick.getPlugin().getCommand(BanSaveCommand.name).setExecutor(new BanSaveCommand());
	}

}
