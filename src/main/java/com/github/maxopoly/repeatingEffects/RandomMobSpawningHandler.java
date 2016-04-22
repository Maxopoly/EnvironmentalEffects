package com.github.maxopoly.repeatingEffects;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.github.maxopoly.Effect;
import com.github.maxopoly.EffectManager;
import com.github.maxopoly.EnvironmentalEffects;
import com.github.maxopoly.datarepresentations.Area;
import com.github.maxopoly.datarepresentations.MobConfig;
import com.github.maxopoly.datarepresentations.MobLureDenier;
import com.github.maxopoly.datarepresentations.PlayerEnvironmentState;
import com.github.maxopoly.listeners.effects.SpawnerSpawnModifier;

/**
 * Manages the spawning of completly configurable mobs. All of the mobs are kept
 * track of in an internal hashmap, currently a server restart will stop any
 * special effects of the mobs from working and they also wont drop special loot
 * afterwards, because they are being kept track of anymore
 * 
 * @author Max
 *
 */
public class RandomMobSpawningHandler extends RepeatingEffect {

	private LinkedList<MobConfig> mobConfigs;
	private static HashMap<UUID, MobConfig> currentMobs = new HashMap<UUID, MobConfig>();;

	public RandomMobSpawningHandler(LinkedList<Area> includedAreas,
			LinkedList<Area> excludedAreas, LinkedList<MobConfig> mobConfigs,
			long updateTime, PlayerEnvironmentState pes) {
		super(includedAreas, excludedAreas, updateTime, pes);
		this.mobConfigs = mobConfigs;
	}

	/**
	 * Attempt to spawn monsters near the player. Cycles through all the
	 * available mobconfigs for this area and tries to create each of them
	 * according to it's config. This can still fail because there is no free
	 * space for it or because of the spawn chance which is not taken into
	 * account before calling this method
	 */
	public void applyToPlayer(Player p) {
		if (conditionsMet(p)) {
			for (MobConfig mc : mobConfigs) {
				LinkedList<Entity> resultedMobs = mc.createMob(p.getLocation());
				if (resultedMobs != null) {
					for (Entity mob : resultedMobs) {
						if (mob != null) {
							addEntity(mob, mc);
						}
					}
				}
			}
		}
	}

	/**
	 * Kills all mobs currently handled by any MobSpawningHandler. This is run
	 * every time the this plugin (or in most cases the server) is disabled,
	 * because this plugin is currently not hooked up to a database to make
	 * drops and effects consistent over restarts.
	 */
	public static void killAll() {
		for (World w : Bukkit.getWorlds()) {
			for (Entity e : w.getEntities()) {
				if (currentMobs.containsKey(e.getUniqueId())) {
					e.remove();
					currentMobs.remove(e.getUniqueId());
				}
			}
		}

	}

	/**
	 * Gets the stored mob config for a given entity
	 * 
	 * @param e
	 *            Entity whose config is wanted
	 * @return The config of the given entity if one exists or null if no config
	 *         for this entity exists
	 */
	public static MobConfig getConfig(Entity e) {
		MobConfig mb = currentMobs.get(e.getUniqueId());
		return mb;
	}

	/**
	 * Adds an entity together with a config to the internal tracking
	 * 
	 * @param e
	 *            Entity to add
	 * @param config
	 *            Config to add
	 */
	public static void addEntity(Entity e, MobConfig config) {
		currentMobs.put(e.getUniqueId(), config);
	}

	/**
	 * Removes an entity and it's config from the internal tracking
	 * 
	 * @param e
	 *            Entity to remove
	 */
	public static void removeEntity(Entity e) {
		currentMobs.remove(e.getUniqueId());
	}

	/**
	 * @return All mob configs contained in this instance
	 */
	public List<MobConfig> getAllConfigs() {
		return mobConfigs;
	}

	public static void saveMobs() {
		EnvironmentalEffects ee = EnvironmentalEffects.getPlugin();
		File save = new File(ee.getDataFolder().getAbsolutePath()
				+ File.separator + "mobSaves.txt");
		if (save.exists()) {
			save.delete();
		}
		try {
			save.createNewFile();
			FileWriter fw = new FileWriter(save);
			BufferedWriter buff = new BufferedWriter(fw);
			for (Entry<UUID, MobConfig> entry : currentMobs.entrySet()) {
				StringBuilder sb = new StringBuilder();
				sb.append(entry.getKey().toString() + "#"
						+ entry.getValue().getIdentifier());
				buff.write(sb.toString());
				buff.newLine();
			}
			buff.flush();
			buff.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void loadMobs() {
		EnvironmentalEffects ee = EnvironmentalEffects.getPlugin();
		File save = new File(ee.getDataFolder().getAbsolutePath()
				+ File.separator + "mobSaves.txt");
		if (!save.exists()) {
			return;
		}
		Map<String, MobConfig> configs = new HashMap<String, MobConfig>();
		EffectManager manager = EnvironmentalEffects.getManager();
		for (Effect e : manager.getEffects(RandomMobSpawningHandler.class)) {
			RandomMobSpawningHandler rmsh = (RandomMobSpawningHandler) e;
			for (MobConfig mc : rmsh.getAllConfigs()) {
				configs.put(mc.getIdentifier(), mc);
			}
		}
		for(Effect e : manager.getEffects(SpawnerSpawnModifier.class)) {
			SpawnerSpawnModifier ssm = (SpawnerSpawnModifier) e;
			for(MobConfig mc: ssm.getConfigs()) {
				configs.put(mc.getIdentifier(), mc);
			}
		}
		try {
			FileReader fr = new FileReader(save);
			BufferedReader reader = new BufferedReader(fr);
			String line = reader.readLine();
			while (line != null && !line.equals("")) {
				String[] content = line.split("#");
				UUID uuid = UUID.fromString(content[0]);
				MobConfig conf = configs.get(content[1]);
				if (conf != null) {
					currentMobs.put(uuid, conf);
				} else {
					ee.getLogger().log(
							Level.SEVERE,
							"Could not find config " + content[1]
									+ ", failed to load it's mobs");
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		save.delete();
	}
}
