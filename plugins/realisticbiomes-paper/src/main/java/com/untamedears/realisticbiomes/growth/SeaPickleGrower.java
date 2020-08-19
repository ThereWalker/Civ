package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.SeaPickle;

import com.untamedears.realisticbiomes.model.Plant;

public class SeaPickleGrower extends IArtificialGrower {

	@Override
	public int getIncrementPerStage() {
		return 1;
	}

	@Override
	public int getMaxStage() {
		return 3;
	}

	@Override
	public int getStage(Plant plant) {
		Block block = plant.getLocation().getBlock();
		if (block.getType() != Material.SEA_PICKLE) {
			return -1;
		}
		SeaPickle data = (SeaPickle) block.getBlockData();
		return data.getPickles() - 1;
	}

	@Override
	public void setStage(Plant plant, int stage) {
		Block block = plant.getLocation().getBlock();
		if (block.getType() != Material.SEA_PICKLE) {
			return;
		}
		SeaPickle data = (SeaPickle) block.getBlockData();
		data.setPickles(stage + 1);
		block.setBlockData(data);
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return true;
	}

}
