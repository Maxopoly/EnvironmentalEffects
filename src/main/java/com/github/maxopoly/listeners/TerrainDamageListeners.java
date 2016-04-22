package com.github.maxopoly.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class TerrainDamageListeners implements Listener {
	boolean preventFireballDamage;
	boolean preventFireballFire;
	boolean disableFirespread;

	public TerrainDamageListeners(boolean preventFireballDamage,
			boolean preventFireballFire, boolean disableFirespead) {
		this.preventFireballDamage = preventFireballDamage;
		this.preventFireballFire = preventFireballFire;
		this.disableFirespread = disableFirespead;
	}

	@EventHandler
	public void fireballExplosion(EntityExplodeEvent e) {
		if (preventFireballDamage) {
			if (e != null && e.getEntityType() == EntityType.FIREBALL) {
				e.blockList().clear();
			}
		}
	}

	@EventHandler
	public void fireballIgnition(BlockIgniteEvent e) {
		if (preventFireballFire) {
			if (e.getCause() == BlockIgniteEvent.IgniteCause.FIREBALL
					|| e.getCause() == BlockIgniteEvent.IgniteCause.EXPLOSION) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void firespreadNerf(BlockIgniteEvent e) {
		if (e.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
			e.setCancelled(disableFirespread);
		}
	}

	@EventHandler
	public void fireSpreadNerf2(BlockSpreadEvent e) {
		if (disableFirespread) {
			if (e.getBlock() != null && e.getBlock().getType() == Material.FIRE) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void fireSpreadNerf3(BlockBurnEvent e) {
		if (disableFirespread) {
			e.setCancelled(true);
		}
	}

}
