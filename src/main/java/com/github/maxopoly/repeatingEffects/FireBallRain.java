package com.github.maxopoly.repeatingEffects;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.github.maxopoly.datarepresentations.Area;
import com.github.maxopoly.datarepresentations.PlayerEnvironmentState;

/**
 * Spawns fireballs randomly around players. Be careful when using this without
 * an additional listener to prevent the fireballs from dealing terrain damage,
 * they might slowly destroy the whole surrounding
 * 
 * @author Max
 *
 */
public class FireBallRain extends RepeatingEffect {
	private int range;

	public FireBallRain(LinkedList<Area> includedAreas,
			LinkedList<Area> excludedAreas, long frequency, int range,
			PlayerEnvironmentState pes) {
		super(includedAreas, excludedAreas, frequency, pes);
		this.range = range;
	}

	/**
	 * Spawns a fireball somewhere around the player inside the spawning range
	 * at y=255. It will fly straight down.
	 */
	public void applyToPlayer(Player p) {
		if (conditionsMet(p)) {
			int x = rng.nextInt(range * 2) - range;
			int z = rng.nextInt(range * 2) - range;
			Location pLoc = p.getLocation();
			Location spawnLoc = new Location(p.getWorld(), pLoc.getX() + x,
					255, pLoc.getZ() + z);
			// this will spawn them at build limit, doesnt work in caves
			Fireball fireball = p.getWorld().spawn(spawnLoc, Fireball.class);
			fireball.setDirection(new Vector(0, -1, 0));
		}
	}

	/**
	 * The spawned fireballs are randomly distributed inside a square, which's
	 * sides are each 2* range long and which is centred on the player
	 * 
	 * @return current range used for finding spawning locations for fireballs
	 */
	public int getRange() {
		return range;
	}

	/**
	 * The spawned fireballs are randomly distributed inside a square, which's
	 * sides are each 2* range long and which is centred on the player. This
	 * will change the range used for this calculation for any future spawns by
	 * this specific instance
	 * 
	 * @param range
	 *            new range to be used
	 */
	public void setRange(int range) {
		this.range = range;
	}

}
