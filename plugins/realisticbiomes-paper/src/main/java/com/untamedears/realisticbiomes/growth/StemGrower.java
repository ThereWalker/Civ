package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;

import com.google.common.base.Preconditions;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;

public class StemGrower extends AgeableGrower {

	private String fruitConfigName;
	private PlantGrowthConfig fruitConfig;
	
	public StemGrower(Material material, String fruitConfig) {
		super(material, 7, 1);
		Preconditions.checkNotNull(fruitConfig);
		this.fruitConfigName = fruitConfig;
	}
	
	public String getFruitConfigName() {
		return fruitConfigName;
	}
	
	public void setFruitConfig(PlantGrowthConfig fruitConfig) {
		this.fruitConfig = fruitConfig;
	}
	
	public PlantGrowthConfig getFruitConfig() {
		return fruitConfig;
	}
	
	@Override
	public void setStage(Plant plant, int stage) {
		super.setStage(plant, stage);
		if (getMaxStage() == stage) {
			plant.resetCreationTime();
			plant.setGrowthConfig(fruitConfig);
		}
	}

}
