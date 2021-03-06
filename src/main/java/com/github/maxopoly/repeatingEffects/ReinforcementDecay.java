package com.github.maxopoly.repeatingEffects;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.maxopoly.datarepresentations.Area;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

/**
 * Allows to decay reinforcements created by the citadel plugin
 * (https://github.com/civcraft/citadel)
 * 
 * @author Max
 *
 */
public class ReinforcementDecay extends RepeatingEffect {
	int reinforcementBreakPerRun;

	public ReinforcementDecay(JavaPlugin plugin, LinkedList<Area> areas,
			long updateTime, int reinforcementBreakPerRun) {
		super(plugin, areas, updateTime, null);
		this.reinforcementBreakPerRun = reinforcementBreakPerRun;
	}

	/**
	 * No implementation because this effect will be tied to locations
	 */
	public void applyToPlayer(Player p) {
	}

	/**
	 * Reduces every reinforcement in the areas of this instance by
	 * reinforcementBreakPerRun
	 */
	public void run() {
		ReinforcementManager rm = Citadel.getReinforcementManager();
		for (Area a : areas) {
			LinkedList<Chunk> chunks = a.getChunks();
			for (Chunk c : chunks) {
				List<Reinforcement> reins = rm.getReinforcementsByChunk(c);
				for (Reinforcement re : reins) {
					re.setDurability(re.getDurability()
							- reinforcementBreakPerRun);
				}
			}
		}

	}
}
