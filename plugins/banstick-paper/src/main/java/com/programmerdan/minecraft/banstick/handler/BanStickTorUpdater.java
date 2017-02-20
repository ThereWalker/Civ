package com.programmerdan.minecraft.banstick.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;

import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class BanStickTorUpdater {
	
	/*
	 *
tor:
 check: true
 autoban: true
 lists:
  dan.xxx:
   ban:
    length: 86400000
    message: TOR node use is prohibited. Please use residential connection.
   address: https://www.dan.me.uk/torlist/?exit
   period: 36000
   delay: 3000
   cidr: false
	 */

	private boolean banNewNodes = false;
	
	private List<TorList> torLists = new ArrayList<TorList>();
	private List<BukkitTask> torListUpdaters = new ArrayList<BukkitTask>();

	public BanStickTorUpdater(FileConfiguration config) {
		if (!configureTor(config.getConfigurationSection("tor"))) {
			throw new RuntimeException("Failed to configure Tor lists for BanStick!");
		}
		
		activateTor();
	}
	
	private boolean configureTor(ConfigurationSection config) {
		if (!config.getBoolean("check", false)) {
			BanStick.getPlugin().info("TOR exit node checks disabled.");
			return false;
		}
		
		try {
			banNewNodes = config.getBoolean("autoban", false);
			ConfigurationSection lists = config.getConfigurationSection("lists");
			for (String listName : lists.getKeys(false)) {
				ConfigurationSection list = lists.getConfigurationSection(listName);
				
				torLists.add(new TorList(
							list.getString("address"),
							list.getLong("period"),
							list.getLong("delay"),
							list.getBoolean("cidr"),
							list.getString("ban.message"),
							list.getLong("ban.length")
						));
			}
			return true;
		} catch (Exception e) {
			BanStick.getPlugin().warning("Failed to load Tor nodes.");
			return false;
		}
	}
	
	private void activateTor() {
		for (TorList tor : torLists) {
			BanStick.getPlugin().info("Preparing Tor runnable for " + tor.address);
			BukkitRunnable run = new BukkitRunnable() {
				final TorList torSave = tor;
				@Override
				public void run() {
					try {
						URL connection = new URL(torSave.address);
						InputStream readIn = connection.openStream();
						BufferedReader in = new BufferedReader(new InputStreamReader(readIn));
						String line = in.readLine();
						while (line != null) {
							try {
								IPAddressString address = new IPAddressString(line);
								address.validate();
								BSIP found = null;
								if (torSave.cidr) {
									found = BSIP.byCIDR(address.toAddress().toSupernet().toString(), address.getNetworkPrefixLength());
								} else {
									found = BSIP.byIPAddress(address.toAddress());
								}
								if (found == null) {
									if (torSave.cidr) {
										found = BSIP.create(address.toAddress().toSupernet(), address.getNetworkPrefixLength());
									} else {
										found = BSIP.create(address.toAddress());
									}
									// TODO: CREATE A VPN RECORD
								}
								
								if (banNewNodes) {
									List<BSBan> ban = BSBan.byIP(found, true);
									if (ban == null || ban.size() == 0) { 
										BSBan.create(found, torSave.banMessage, 
												torSave.endlessBan ? null : new Date(System.currentTimeMillis() + torSave.banLength), false);
									} else {
										// look for match; if unexpired, extend.
										for (int i = ban.size() - 1 ; i >= 0; i-- ) {
											BSBan pickOne = ban.get(i);
											if (pickOne.isAdminBan()) continue; // skip admin entered bans.
											if (pickOne.getBanEndTime() != null && pickOne.getBanEndTime().after(new Date())) {
												pickOne.setBanEndTime(torSave.endlessBan ? null : new Date(System.currentTimeMillis() + torSave.banLength));
												break;
											}
										}
									}
								}
								
							} catch (Exception e) {
								// quiet.
							}
							line = in.readLine();
						}
					} catch (IOException  e) {
						BanStick.getPlugin().debug("Failed reading from TOR list: " + torSave.address);
					}
				}
			};
			
			torListUpdaters.add(run.runTaskTimerAsynchronously(BanStick.getPlugin(), tor.delay, tor.period));
		}
	}

	class TorList {
		public String address;
		public long period;
		public long delay;
		public boolean cidr;
		public String banMessage;
		public long banLength;
		public boolean endlessBan;
		
		public TorList(String address, long period, long delay, boolean cidr, String banMessage, long banLength) {
			this.address = address;
			this.period = period;
			this.delay = delay;
			this.cidr = cidr;
			this.banMessage = banMessage;
			this.banLength = banLength;
			this.endlessBan = banLength == 0;
		}
	}
}
