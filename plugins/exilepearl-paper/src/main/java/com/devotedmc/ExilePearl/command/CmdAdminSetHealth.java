package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminSetHealth extends PearlCommand {

	public CmdAdminSetHealth(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("sethealth");

		this.setHelpShort("Sets the health % value of a pearl.");
		
		this.commandArgs.add(requiredPlayer("player"));
		this.commandArgs.add(required("health %", autoTab("", "Enter the desired health percent")));
		
		this.permission = Permission.ADMIN.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		String name = argAsString(0);
		int percent = Math.min(100, Math.max(1, argAsInt(1)));
		
		ExilePearl pearl = plugin.getPearl(name);
		if (pearl == null) {
			msg("<i>No pearl was found with the name <c>%s", name);
			return;
		}
		
		// calculate the actual value
		int healthValue = (int)(plugin.getPearlConfig().getPearlHealthMaxValue() * ((double)percent / 100));
		pearl.setHealth(healthValue);
		msg("<g>You updated the pearl health of player %s to %d%%", pearl.getPlayerName(), percent);
	}
}
