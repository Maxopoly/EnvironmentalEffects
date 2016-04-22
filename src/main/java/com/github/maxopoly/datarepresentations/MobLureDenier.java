package com.github.maxopoly.datarepresentations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.github.maxopoly.EnvironmentalEffects;

public class MobLureDenier implements Runnable {
	private int lureRange;
	private LivingEntity e;
	private Location l;
	private static Map <UUID,Location> bindLoc = new HashMap<UUID, Location>();

	public MobLureDenier(int lureRange, LivingEntity e, Location l) {
		this.e = e;
		this.lureRange = lureRange;
		this.l = l;
		bindLoc.put(e.getUniqueId(),l);
	}

	public void run() {
		if (e.isDead()) {
			return;
		}
		if (e.getLocation().distance(l) > lureRange) {
			e.teleport(l);
		}
		EnvironmentalEffects
				.getPlugin()
				.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(EnvironmentalEffects.getPlugin(), this, 200);
	}
	
	public static Location getBind(UUID uuid) {
		return bindLoc.get(uuid);
	}

}
