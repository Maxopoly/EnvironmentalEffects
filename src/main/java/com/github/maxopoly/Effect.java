package com.github.maxopoly;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.maxopoly.datarepresentations.Area;
import com.github.maxopoly.datarepresentations.PlayerEnvironmentState;

/**
 * Superclass for any sort of effect created by this plugin
 * 
 * @author Max
 *
 */
public abstract class Effect {
	final protected EnvironmentalEffects plugin;
	protected LinkedList<Area> includedAreas;
	protected LinkedList<Area> excludedAreas;
	protected PlayerEnvironmentState pes;
	protected Random rng;

	public Effect(LinkedList<Area> includedAreas,
			LinkedList<Area> excludedAreas, PlayerEnvironmentState pes) {
		this.plugin = EnvironmentalEffects.getPlugin();
		this.includedAreas = includedAreas;
		this.excludedAreas = excludedAreas;
		this.rng = new Random();
		this.pes = pes;
	}

	/**
	 * @return all players currently on this server
	 */
	public Collection<? extends Player> getCurrentPlayers() {
		return plugin.getServer().getOnlinePlayers();
	}

	/**
	 * Checks whether the given location is inside any of the areas in which
	 * effects are applied by this instance and not in a restricted area
	 * 
	 * @param loc
	 *            location be compared with the areas
	 * @return true if the location is inside the area affected by this
	 *         instance, false if not
	 */
	public boolean isInArea(Location loc) {
		for (Area a : includedAreas) {
			if (a.isInArea(loc) && !isInRestrictedArea(loc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether a given location is in the area which is restricted for
	 * this effect
	 * 
	 * @param loc
	 * @return
	 */
	public boolean isInRestrictedArea(Location loc) {
		if (excludedAreas == null) {
			return false;
		}
		for (Area a : excludedAreas) {
			if (a.isInArea(loc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convenience method to check whether the player is in the right area and
	 * whether the environmental conditions are met
	 * 
	 * @param p
	 *            player to check
	 * @return true if the player fulfills all the criteria, false if not or if
	 *         the player has the permission to bypass effects
	 */
	public boolean conditionsMet(Player p) {
		if (p.hasPermission("EE.admin")) {
			return false;
			// this permission allows to ignore any effects
		}
		return environmentConditionMet(p) && isPlayerInArea(p);
	}

	/**
	 * Checks whether the environmental conditions for this instance are met for
	 * a player. If the conditions are null, it's always assumed they are true,
	 * also checks whether the player is null
	 * 
	 * @param p
	 *            player to check
	 * @return true if the player meets the conditions, false if not
	 */
	public boolean environmentConditionMet(Player p) {
		return p != null && (pes == null || pes.conditionMet(p));
	}

	/**
	 * Checks whether the given player is inside any of the areas in which
	 * effects are applied by this instance
	 * 
	 * @param p
	 *            player whos location is compared with the areas
	 * @return true if the player is inside the effects area, false if not
	 */
	public boolean isPlayerInArea(Player p) {
		return isInArea(p.getLocation());
	}

	/**
	 * @return Whether this instance applies globally
	 */
	public boolean isGlobal() {
		for (Area a : includedAreas) {
			if (a.isGlobal()) {
				return true;
			}
		}
		return false;
	}

}
