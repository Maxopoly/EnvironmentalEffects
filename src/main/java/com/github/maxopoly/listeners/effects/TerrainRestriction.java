package com.github.maxopoly.listeners.effects;

import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.github.maxopoly.Effect;
import com.github.maxopoly.datarepresentations.Area;
import com.github.maxopoly.datarepresentations.PlayerEnvironmentState;

public class TerrainRestriction extends Effect implements Listener {
	private boolean preventPlacement;
	private boolean preventBreaking;
	private boolean preventFire;
	private boolean preventBuckets;
	private boolean preventPearls;
	private final String preventionMessage = "You are not allowed to do this here";

	public TerrainRestriction(LinkedList<Area> includedAreas,
			LinkedList<Area> excludedAreas, PlayerEnvironmentState pes,
			boolean preventPlacement, boolean preventBreaking,
			boolean preventFire, boolean preventBuckets, boolean preventPearls) {
		super(includedAreas, excludedAreas, pes);
		this.preventFire = preventFire;
		this.preventBreaking = preventBreaking;
		this.preventBuckets = preventBuckets;
		this.preventPlacement = preventPlacement;
		this.preventPearls = preventPearls;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent e) {
		if (preventPlacement && conditionsMet(e.getPlayer())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(preventionMessage);
		}
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent e) {
		if (preventBreaking && conditionsMet(e.getPlayer())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(preventionMessage);
		}
	}

	@EventHandler
	public void bucketPlace(PlayerBucketEmptyEvent e) {
		if (preventBuckets && conditionsMet(e.getPlayer())) {
			e.getPlayer().sendMessage(preventionMessage);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void bucketFill(PlayerBucketFillEvent e) {
		if (preventBuckets && conditionsMet(e.getPlayer())) {
			e.getPlayer().sendMessage(preventionMessage);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void lighterUse(BlockIgniteEvent e) {
		if (preventFire && e.getIgnitingEntity() instanceof Player) {
			Player p = (Player) e.getIgnitingEntity();
			if (conditionsMet(p)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void blockPearls(PlayerTeleportEvent e) {
		if (preventPearls
				&& e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
				&& (conditionsMet(e.getPlayer()) || (environmentConditionMet(e
						.getPlayer()) && isInArea(e.getTo())))) {
			e.setCancelled(true);
		}
	}
}
