package vg.civcraft.mc.civmodcore.itemHandling;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class NiceNames {
	private static Map<NameSearchObject, String> items;
	private static Map<Enchantment, String> enchants;
	private static Map<Enchantment, String> enchantAcronyms;

	private static class NameSearchObject {
		private String data;

		private NameSearchObject(Material m, short dura, List<String> lore) {
			StringBuilder sb = new StringBuilder();
			sb.append(m.toString());
			sb.append(dura);
			for (String s : lore) {
				sb.append(s);
			}
			data = sb.toString();
		}

		private NameSearchObject(ItemStack is) {
			this(is.getType(), is.getDurability(),
					is.getItemMeta().hasLore() ? is.getItemMeta().getLore()
							: new LinkedList<String>());
		}

		public int hashCode() {
			return data.hashCode();
		}

		public boolean equals(Object o) {
			return o instanceof NameSearchObject
					&& ((NameSearchObject) o).getData().equals(data);
		}

		private String getData() {
			return data;
		}
	}

	public static String getName(ItemStack is) {
		return items.get(new NameSearchObject(is));
	}

	public static String getName(Enchantment enchant) {
		return enchants.get(enchant);
	}

	public static String getAcronym(Enchantment enchant) {
		return enchantAcronyms.get(enchant);
	}

	public void loadNames() {
		// item aliases
		items = new HashMap<NiceNames.NameSearchObject, String>();
		try {
			InputStream in = getClass().getResourceAsStream(
					"/resources/materials.csv");
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String line = reader.readLine();
			while (line != null) {
				String[] content = line.split(",");
				NameSearchObject nso = new NameSearchObject(
						Material.valueOf(content[1]),
						Short.valueOf(content[3]), new LinkedList<String>());
				items.put(nso, content[0]);
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// enchantment aliases
		enchants = new HashMap<Enchantment, String>();
		enchantAcronyms = new HashMap<Enchantment, String>();
		try {
			InputStream in = getClass().getResourceAsStream(
					"/resources/enchantments.csv");
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String line = reader.readLine();
			while (line != null) {
				String[] content = line.split(",");
				Enchantment enchant = Enchantment.getByName(content[1]);
				enchants.put(enchant, content[2]);
				enchantAcronyms.put(enchant, content[0]);
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
