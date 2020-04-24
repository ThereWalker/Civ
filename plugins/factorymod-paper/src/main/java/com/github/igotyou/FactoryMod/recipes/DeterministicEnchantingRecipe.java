package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

public class DeterministicEnchantingRecipe extends InputRecipe {
	private Enchantment enchant;
	private int level;
	private ItemMap tool;

	public DeterministicEnchantingRecipe(String identifier, String name, int productionTime,
			ItemMap input, ItemMap tool, Enchantment enchant, int level) {
		super(identifier, name, productionTime, input);
		this.enchant = enchant;
		this.tool = tool;
		this.level = level;
	}

	@Override
	public boolean enoughMaterialAvailable(Inventory i) {
		if (input.isContainedIn(i)) {
			ItemStack toolio = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : i.getContents()) {
				if (is != null
						&& toolio.getType() == is.getType()
						&& toolio.getEnchantmentLevel(enchant) == is
								.getEnchantmentLevel(enchant)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		ItemStack is = tool.getItemStackRepresentation().get(0);
		ItemMeta im = is.getItemMeta();
		im.removeEnchant(enchant);
		im.addEnchant(enchant, level, true);
		is.setItemMeta(im);
		ItemAPI.setDisplayName(is, name);
		return is;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack is = tool.getItemStackRepresentation().get(0);
		ItemMeta im = is.getItemMeta();
		im.removeEnchant(enchant);
		im.addEnchant(enchant, level, true);
		is.setItemMeta(im);
		if (i != null) {
			ItemAPI.addLore(
					is,
					ChatColor.GREEN
							+ "Enough materials for "
							+ String.valueOf(Math.min(
									tool.getMultiplesContainedIn(i),
									input.getMultiplesContainedIn(i)))
							+ " runs");
		}
		List<ItemStack> stacks = new LinkedList<>();
		stacks.add(is);
		return stacks;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			List<ItemStack> bla = input.getItemStackRepresentation();
			bla.add(tool.getItemStackRepresentation().get(0));
			return bla;
		}
		List<ItemStack> returns = createLoredStacksForInfo(i);
		ItemStack toSt = tool.getItemStackRepresentation().get(0);
		ItemAPI.addLore(toSt, ChatColor.GREEN + "Enough materials for "
				+ new ItemMap(toSt).getMultiplesContainedIn(i) + " runs");
		returns.add(toSt);
		return returns;
	}

	@Override
	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		if (input.removeSafelyFrom(i)) {
			ItemStack toolio = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : i.getContents()) {
				if (is != null
						&& toolio.getType() == is.getType()
						&& toolio.getEnchantmentLevel(enchant) == is
								.getEnchantmentLevel(enchant)) {
					ItemMeta im = is.getItemMeta();
					im.removeEnchant(enchant);
					im.addEnchant(enchant, level, true);
					is.setItemMeta(im);
					break;
				}
			}
		}
		logAfterRecipeRun(i, fccf);
	}

	@Override
	public String getTypeIdentifier() {
		return "ENCHANT";
	}

	public int getLevel() {
		return level;
	}

	public Enchantment getEnchant() {
		return enchant;
	}

	public ItemMap getTool() {
		return tool;
	}
}