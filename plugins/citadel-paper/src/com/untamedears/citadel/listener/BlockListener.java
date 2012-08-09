package com.untamedears.citadel.listener;

import static com.untamedears.citadel.Utility.createReinforcement;
import static com.untamedears.citadel.Utility.maybeReinforcementDamaged;
import static com.untamedears.citadel.Utility.reinforcementBroken;
import static com.untamedears.citadel.Utility.reinforcementDamaged;
import static com.untamedears.citadel.Utility.sendMessage;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import org.bukkit.material.PistonBaseMaterial;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.PluginConsumer;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.PlayerState;
import com.untamedears.citadel.entity.Reinforcement;
import com.untamedears.citadel.entity.ReinforcementMaterial;

public class BlockListener implements Listener {

    /**
     * This handles the BlockPlaceEvent for Fortification mode (all placed blocks are reinforced)
     *
     * @param bpe BlockPlaceEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void placeFortifiedBlock(BlockPlaceEvent bpe) {
        Player player = bpe.getPlayer();
        PlayerState state = PlayerState.get(player);

        if (state.getMode() != PlacementMode.FORTIFICATION) {
            // if we are not in fortification mode
            // cancel event if we are not in normal mode
            if (state.getMode() == PlacementMode.REINFORCEMENT || state.getMode() == PlacementMode.REINFORCEMENT_SINGLE_BLOCK)
                bpe.setCancelled(true);
            return;
        }

        Block block = bpe.getBlockPlaced();
        PlayerInventory inventory = player.getInventory();

        ReinforcementMaterial material = state.getReinforcementMaterial();
        ItemStack required = material.getRequiredMaterials();
        if (inventory.contains(material.getMaterial(), required.getAmount())) {
            if (createReinforcement(player, block) == null) {
                sendMessage(player, ChatColor.RED, "%s is not a reinforcible material", block.getType().name());
            }
            else
            	state.checkResetMode();
        } else {
            sendMessage(player, ChatColor.YELLOW, "%s depleted, left fortification mode", material.getMaterial().name());
            state.reset();
            bpe.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void blockBreak(BlockBreakEvent bbe) {
        Block block = bbe.getBlock();
        Player player = bbe.getPlayer();

        AccessDelegate delegate = AccessDelegate.getDelegate(block);
        Reinforcement reinforcement = delegate.getReinforcement();
        if (reinforcement == null) return;

        PlayerState state = PlayerState.get(player);
        if (state.isBypassMode() && reinforcement.isBypassable(player)) {
			Citadel.info(player.getDisplayName() + " bypassed reinforcement %s at " 
					+ reinforcement.getBlock().getLocation().toString());

            bbe.setCancelled(reinforcementBroken(reinforcement));
        } else {
            bbe.setCancelled(reinforcementDamaged(reinforcement));
        }
        if (bbe.isCancelled()) {
            block.getDrops().clear();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void pistonExtend(BlockPistonExtendEvent bpee) {
    	Block piston = bpee.getBlock();
	BlockState state = piston.getState();
	MaterialData data = state.getData();
	BlockFace direction = null;
	
	if (data instanceof PistonBaseMaterial) {
		direction = ((PistonBaseMaterial) data).getFacing();
	}
	
	// if no direction was found, no point in going on
	if (direction == null)
		return;
	
	// Check the affected blocks
	for (int i = 1; i < bpee.getLength() + 2; i++) {
		Block block = piston.getRelative(direction, i);
	
		if (block.getType() == Material.AIR)
			break;
	
		AccessDelegate delegate = AccessDelegate.getDelegate(block);
		Reinforcement reinforcement = delegate.getReinforcement();
	
		if (reinforcement != null){
			bpee.setCancelled(true);
			break;
		}
	}
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void pistonRetract(BlockPistonRetractEvent bpre) {
    	Block piston = bpre.getBlock();
	BlockState state = piston.getState();
	MaterialData data = state.getData();
	BlockFace direction = null;

	// Check the block it pushed directly
	if (data instanceof PistonBaseMaterial) {
		direction = ((PistonBaseMaterial) data).getFacing();
	}

	if (direction == null)
		return;

	// the block that the piston moved
	Block moved = piston.getRelative(direction, 2);

	AccessDelegate delegate = AccessDelegate.getDelegate(moved);
	Reinforcement reinforcement = delegate.getReinforcement();

	if (reinforcement != null) {
		bpre.setCancelled(true);
	}
    }
    
    private static final Material matfire = Material.FIRE;
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void blockBurn(BlockBurnEvent bbe) {
        boolean wasprotected = maybeReinforcementDamaged(bbe.getBlock());
    	if (wasprotected) {
            bbe.setCancelled(wasprotected);
	    Block block = bbe.getBlock();
            // Basic essential fire protection
            if (block.getRelative(0,1,0).getType() == matfire) {block.getRelative(0,1,0).setTypeId(0);} // Essential
            // Extended fire protection (recommend)
            if (block.getRelative(1,0,0).getType() == matfire) {block.getRelative(1,0,0).setTypeId(0);}
            if (block.getRelative(-1,0,0).getType() == matfire) {block.getRelative(-1,0,0).setTypeId(0);}
            if (block.getRelative(0,-1,0).getType() == matfire) {block.getRelative(0,-1,0).setTypeId(0);}
            if (block.getRelative(0,0,1).getType() == matfire) {block.getRelative(0,0,1).setTypeId(0);}
            if (block.getRelative(0,0,-1).getType() == matfire) {block.getRelative(0,0,-1).setTypeId(0);}
            // Aggressive fire protection (would seriously reduce effectiveness of flint down to near the "you'd have to use it 25 times" mentality)
            /*
            if (block.getRelative(1,1,0).getType() == matfire) {block.getRelative(1,1,0).setTypeId(0);}
            if (block.getRelative(1,-1,0).getType() == matfire) {block.getRelative(1,-1,0).setTypeId(0);}
            if (block.getRelative(-1,1,0).getType() == matfire) {block.getRelative(-1,1,0).setTypeId(0);}
            if (block.getRelative(-1,-1,0).getType() == matfire) {block.getRelative(-1,-1,0).setTypeId(0);}
            if (block.getRelative(0,1,1).getType() == matfire) {block.getRelative(0,1,1).setTypeId(0);}
            if (block.getRelative(0,-1,1).getType() == matfire) {block.getRelative(0,-1,1).setTypeId(0);}
            if (block.getRelative(0,1,-1).getType() == matfire) {block.getRelative(0,1,-1).setTypeId(0);}
            if (block.getRelative(0,-1,-1).getType() == matfire) {block.getRelative(0,-1,-1).setTypeId(0);}
            */
	}
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void redstonePower(BlockRedstoneEvent bre) {
        Block block = bre.getBlock();
        
        if (!(block.getState().getData() instanceof Openable)) return;
        
        Openable openable = (Openable) block.getState().getData();
        if (openable.isOpen()) return;
        
        Reinforcement reinforcement = Citadel.getReinforcementManager().getReinforcement(block);
        if (reinforcement == null || reinforcement.getSecurityLevel() == SecurityLevel.PUBLIC) return;

        Set<Player> onlinePlayers = new HashSet<Player>(Citadel.getMemberManager().getOnlinePlayers());
		boolean isAuthorizedPlayerNear = false;
		try {
			for(Player player : onlinePlayers){
				double redstoneDistance = Citadel.getConfigManager().getRedstoneDistance();
				if(reinforcement.isAccessible(player) 
						&& player.getLocation().distanceSquared(block.getLocation()) < redstoneDistance){
					isAuthorizedPlayerNear = true;
					break;
				}
			}
		} catch (ConcurrentModificationException e){
			
		}

        if (!isAuthorizedPlayerNear) {
			Citadel.info("Prevented redstone from opening reinforcement %s at " 
					+ reinforcement.getBlock().getLocation().toString());
            bre.setNewCurrent(bre.getOldCurrent());
        }
    }
}