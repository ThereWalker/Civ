package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.api.EnchantNames;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

public class RandomEnchantingRecipe extends InputRecipe {
	private List<RandomEnchant> enchants;
	private Material tool;
	private static Random rng;

	public class RandomEnchant {
		private Enchantment enchant;
		private int level;
		private double chance;

		public RandomEnchant(Enchantment enchant, int level, double chance) {
			this.enchant = enchant;
			this.level = level;
			this.chance = chance;
		}
	}

	public RandomEnchantingRecipe(String identifier, String name, int productionTime,
			ItemMap input, Material tool, List<RandomEnchant> enchants) {
		super(identifier, name, productionTime, input);
		this.enchants = enchants;
		this.tool = tool;
		if (rng == null) {
			rng = new Random();
		}
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		ItemStack is = new ItemStack(tool);
		for (RandomEnchant re : enchants) {
			is.addEnchantment(re.enchant, re.level);
		}
		ItemAPI.setDisplayName(is, name);
		return is;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			List<ItemStack> bla = input.getItemStackRepresentation();
			bla.add(new ItemStack(tool));
			return bla;
		}
		List<ItemStack> returns = createLoredStacksForInfo(i);
		ItemStack toSt = new ItemStack(tool);
		ItemAPI.addLore(toSt, ChatColor.GREEN + "Enough materials for "
				+ new ItemMap(toSt).getMultiplesContainedIn(i) + " runs");
		returns.add(toSt);
		return returns;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack is = new ItemStack(tool);
		for (RandomEnchant re : enchants) {
			is.addEnchantment(re.enchant, re.level);
		}
		if (i != null) {
			ItemAPI.addLore(
					is,
					ChatColor.GREEN
							+ "Enough materials for "
							+ String.valueOf(Math.max(new ItemMap(
									new ItemStack(tool))
									.getMultiplesContainedIn(i), input
									.getMultiplesContainedIn(i))) + " runs");
		}
		for (RandomEnchant re : enchants) {
			ItemAPI.addLore(is,
					ChatColor.YELLOW + String.valueOf(re.chance * 100)
							+ " % chance for " + EnchantNames.findByEnchantment(re.enchant).getDisplayName()
							+ " " + re.level);
		}
		ItemAPI.addLore(is, ChatColor.LIGHT_PURPLE
				+ "At least one guaranteed");
		List<ItemStack> stacks = new LinkedList<>();
		stacks.add(is);
		return stacks;
	}

	@Override
	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		for (ItemStack is : input.getItemStackRepresentation()) {
			i.removeItem(is);
		}
		for (ItemStack is : i.getContents()) {
			if (is != null && is.getType() == tool
					&& !is.getItemMeta().hasEnchants()) {
				boolean applied = false;
				while (!applied) {
					for (RandomEnchant re : enchants) {
						if (rng.nextDouble() <= re.chance) {
							is.getItemMeta().addEnchant(re.enchant, re.level,
									true);
							applied = true;
						}
					}
				}
				break;
			}
		}
		logAfterRecipeRun(i, fccf);
	}

	@Override
	public String getTypeIdentifier() {
		return "RANDOMENCHANT";
	}

}