package com.github.maxopoly;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.maxopoly.datarepresentations.Area;
import com.github.maxopoly.datarepresentations.ArmourState;
import com.github.maxopoly.datarepresentations.ArmourState.ArmourType;
import com.github.maxopoly.datarepresentations.MobConfig;
import com.github.maxopoly.datarepresentations.PlayerEnvironmentState;
import com.github.maxopoly.datarepresentations.Area.Shape;
import com.github.maxopoly.exceptions.ConfigParseException;
import com.github.maxopoly.listeners.effects.SpawnerSpawnModifier;
import com.github.maxopoly.listeners.effects.TerrainRestriction;
import com.github.maxopoly.repeatingEffects.ArmourBasedDamage;
import com.github.maxopoly.repeatingEffects.DaytimeModifier;
import com.github.maxopoly.repeatingEffects.DispenserBuff;
import com.github.maxopoly.repeatingEffects.FireBallRain;
import com.github.maxopoly.repeatingEffects.LightningControl;
import com.github.maxopoly.repeatingEffects.RandomMobSpawningHandler;
import com.github.maxopoly.repeatingEffects.PotionBuff;
import com.github.maxopoly.repeatingEffects.ReinforcementDecay;
import com.github.maxopoly.repeatingEffects.TitleDisplayer;
import com.github.maxopoly.repeatingEffects.WeatherMachine;

import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parseTime;

public class ConfigParser {
	JavaPlugin plugin;
	private EffectManager manager;
	boolean fireballTerrainDamage;
	boolean fireballTerrainIgnition;
	boolean disableFirespread;
	boolean cancelAllOtherSpawns;

	ConfigParser(JavaPlugin plugin) {
		this.plugin = plugin;
		this.manager = new EffectManager();
	}

	/**
	 * Parses the config, creates everything needed and adds it to the manager
	 */
	public EffectManager parseConfig() throws ConfigParseException {
		sendConsoleMessage("Initializing config");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		long rainUpdate = config.getLong("rainupdate", 200L);
		sendConsoleMessage("Rain for players will be updated every "
				+ rainUpdate + " ticks");
		long timeUpdate = config.getLong("timeupdate", 200L);
		sendConsoleMessage("Daytime for players will be updated every "
				+ timeUpdate + " ticks");
		fireballTerrainDamage = config.getBoolean(
				"disable_fireball_terraindamage", false);
		sendConsoleMessage("fireball terrain damage: " + fireballTerrainDamage);
		fireballTerrainIgnition = config.getBoolean(
				"disable_fireball_terrainignition", false);
		sendConsoleMessage("fireball terrain ignition: "
				+ fireballTerrainIgnition);
		disableFirespread = config.getBoolean("firespread_disabled", false);
		sendConsoleMessage("Disabling fire spread: "
				+ String.valueOf(disableFirespread));
		String worldname = config.getString("worldname", "world");
		sendConsoleMessage("Worldname is:" + worldname);
		cancelAllOtherSpawns = config
				.getBoolean("cancel_natural_spawns", false);
		sendConsoleMessage("Cancel natural spawns: " + cancelAllOtherSpawns);

		// Intialize weather machines
		ConfigurationSection weatherSection = config
				.getConfigurationSection("weathermachines");
		if (weatherSection != null) {
			for (String key : weatherSection.getKeys(false)) {
				ConfigurationSection currentWeatherSection = weatherSection
						.getConfigurationSection(key);
				double rainChance = currentWeatherSection
						.getDouble("rain_chance");
				long minRainDuration = parseTime(currentWeatherSection
						.getString("minimum_rain_duration"));
				LinkedList<Area> areas = parseAreas(
						currentWeatherSection.getConfigurationSection("areas"),
						worldname);
				LinkedList<Area> excludedAreas = parseAreas(
						currentWeatherSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				WeatherMachine wm = new WeatherMachine(areas, excludedAreas,
						rainChance, minRainDuration, rainUpdate);
				manager.add(wm);
				sendConsoleMessage("Initialized weather machine for " + key
						+ ", rainchance: " + rainChance
						+ " ,minimum rain duration: " + minRainDuration);
			}
		} else {
			sendConsoleMessage("No weather config found weather will be vanilla!");
		}

		// Initialize daytime modifier
		ConfigurationSection dayTimeSection = config
				.getConfigurationSection("daytime_modifier");
		if (dayTimeSection != null) {
			for (String key : dayTimeSection.getKeys(false)) {
				ConfigurationSection currentSection = dayTimeSection
						.getConfigurationSection(key);
				DaytimeModifier dtm;
				Double daySpeed = currentSection.getDouble("dayspeed");
				Double nightSpeed = currentSection.getDouble("nightspeed");
				long startingTime = parseTime(currentSection
						.getString("starting_time"));
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				LinkedList<Area> excludedAreas = parseAreas(
						currentSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				if (currentSection.contains("nightspeed")) {
					dtm = new DaytimeModifier(areas, excludedAreas,
							startingTime, daySpeed, nightSpeed, timeUpdate);
				} else {
					dtm = new DaytimeModifier(areas, excludedAreas,
							startingTime, daySpeed, daySpeed, timeUpdate);
				}
				manager.add(dtm);
				sendConsoleMessage("Initialized daytime modifier "
						+ key
						+ " starting time:"
						+ startingTime
						+ ", day speed:"
						+ daySpeed
						+ " night speed:"
						+ (currentSection.contains("nightspeed") ? nightSpeed
								: daySpeed));
			}
		} else {
			sendConsoleMessage("No daytime config found for biome daytime will be vanilla!");
		}

		// Initialize effects
		/*
		 * ConfigurationSection effectSection = config
		 * .getConfigurationSection("effects"); if (effectSection != null) { for
		 * (String currentEffect : effectSection.getKeys(false)) {
		 * ConfigurationSection detailsCurrentEffect = effectSection
		 * .getConfigurationSection(currentEffect); Effect effectType =
		 * Effect.valueOf(detailsCurrentEffect .getString("effect_type"));
		 * double speed = detailsCurrentEffect.getDouble("speed"); int amount =
		 * detailsCurrentEffect.getInt("amount"); long delay =
		 * parseTime(detailsCurrentEffect.getString("delay")); LinkedList<Area>
		 * areas = parseAreas(
		 * detailsCurrentEffect.getConfigurationSection("areas"), worldname);
		 * PlayerEnvironmentState pes =
		 * parsePlayerEnvironmentState(detailsCurrentEffect
		 * .getConfigurationSection("player_environment_state"));
		 * EffectGenerator eg = new EffectGenerator(plugin, areas, effectType,
		 * amount, (float) speed, delay, pes); manager.add(eg);
		 * sendConsoleMessage("Initialized effect handler for " + currentEffect
		 * + "type: " + effectType.getName() + " ,speed: " + speed +
		 * ", amount: " + amount + ", delay: " + delay); } }
		 */

		// Intialize cool fireballs
		ConfigurationSection fireballSection = config
				.getConfigurationSection("fireball");
		if (fireballSection != null) {
			for (String key : fireballSection.getKeys(false)) {
				ConfigurationSection currentSection = fireballSection
						.getConfigurationSection(key);
				long frequency = parseTime(currentSection
						.getString("frequency"));
				int range = currentSection.getInt("range", 32);
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				LinkedList<Area> excludedAreas = parseAreas(
						currentSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				PlayerEnvironmentState pes = parsePlayerEnvironmentState(currentSection
						.getConfigurationSection("player_environment_state"));
				FireBallRain fbr = new FireBallRain(areas, excludedAreas,
						frequency, range, pes);
				manager.add(fbr);
				sendConsoleMessage("Loaded fireball rain " + key
						+ "; frequency:" + frequency + ", range:" + range);
			}
		}

		// Initialize potion buffs
		ConfigurationSection potionSection = config
				.getConfigurationSection("potion_effects");
		if (potionSection != null) {
			for (String key : potionSection.getKeys(false)) {
				ConfigurationSection currentSection = potionSection
						.getConfigurationSection(key);
				PotionEffectType pet = PotionEffectType
						.getByName(currentSection.getString("type"));
				long duration = parseTime(currentSection.getString("duration"));
				int level = currentSection.getInt("level");
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				LinkedList<Area> excludedAreas = parseAreas(
						currentSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				PlayerEnvironmentState pes = parsePlayerEnvironmentState(currentSection
						.getConfigurationSection("player_environment_state"));
				PotionBuff pb = new PotionBuff(areas, excludedAreas, pet,
						level, duration, pes);
				manager.add(pb);
				sendConsoleMessage("Loaded potion buff " + key + " type:"
						+ pet.toString() + ",level:" + level + " duration:"
						+ duration);
			}

		}

		// Initialize lightning
		ConfigurationSection lightningSection = config
				.getConfigurationSection("lightning");
		if (lightningSection != null) {
			for (String key : lightningSection.getKeys(false)) {
				ConfigurationSection currentSection = lightningSection
						.getConfigurationSection(key);
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				LinkedList<Area> excludedAreas = parseAreas(
						currentSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				long frequency = parseTime(currentSection
						.getString("frequency"));
				PlayerEnvironmentState pes = parsePlayerEnvironmentState(currentSection
						.getConfigurationSection("player_environment_state"));
				boolean dealDamage = currentSection.getBoolean("deal_damage",
						true);
				int range = currentSection.getInt("range", 32);
				LightningControl lc = new LightningControl(areas,
						excludedAreas, frequency, pes, dealDamage, range);
				sendConsoleMessage("Loaded lightning effect " + key
						+ ", frequency:" + frequency + ",dealDamage:"
						+ dealDamage + ",range:" + range);
				manager.add(lc);
			}
		}

		// Initialize armourbased damage
		ConfigurationSection armourDamage = config
				.getConfigurationSection("armour_based_damage");
		if (armourDamage != null) {
			for (String key : armourDamage.getKeys(false)) {
				ConfigurationSection currentSection = armourDamage
						.getConfigurationSection(key);
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				LinkedList<Area> excludedAreas = parseAreas(
						currentSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				long frequency = parseTime(currentSection
						.getString("frequency"));
				PlayerEnvironmentState pes = parsePlayerEnvironmentState(currentSection
						.getConfigurationSection("player_environment_state"));
				String dmgMsg = currentSection
						.getString("damage_message", null);
				int dmg = currentSection.getInt("damage_amount");
				ConfigurationSection as = currentSection
						.getConfigurationSection("armour");
				LinkedList<ArmourType> head = parseArmourTypeList(as, "helmet");
				LinkedList<ArmourType> chest = parseArmourTypeList(as, "chest");
				LinkedList<ArmourType> pants = parseArmourTypeList(as,
						"leggings");
				LinkedList<ArmourType> boots = parseArmourTypeList(as, "boots");
				ArmourState armourState = new ArmourState(head, chest, pants,
						boots);
				ArmourBasedDamage abd = new ArmourBasedDamage(areas,
						excludedAreas, frequency, pes, armourState, dmgMsg, dmg);
				sendConsoleMessage("Loaded armour based damage " + key
						+ ";frequency:" + frequency + ",damageMessage:"
						+ dmgMsg + ",damage:" + dmg);
				manager.add(abd);
			}
		}

		// Initialize random mob spawning
		ConfigurationSection mobSection = config
				.getConfigurationSection("monster");
		if (mobSection != null) {
			for (String key : mobSection.getKeys(false)) {
				ConfigurationSection currentSection = mobSection
						.getConfigurationSection(key);
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				long updateTime = parseTime(currentSection
						.getString("updatetime"));
				PlayerEnvironmentState pes = parsePlayerEnvironmentState(currentSection
						.getConfigurationSection("player_environment_state"));
				LinkedList<Area> excludedAreas = parseAreas(
						currentSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				LinkedList<MobConfig> mobconfigs = new LinkedList<MobConfig>();
				ConfigurationSection mobconfigsection = currentSection
						.getConfigurationSection("mobconfig");
				if (mobconfigsection != null) {
					for (String mobkey : mobconfigsection.getKeys(false)) {
						ConfigurationSection currentMobConfig = mobconfigsection
								.getConfigurationSection(mobkey);
						MobConfig mobconfig = parseMobConfig(currentMobConfig);
						mobconfigs.add(mobconfig);
					}
				} else {
					throw new ConfigParseException("No mobconfigs for" + key);
				}
				RandomMobSpawningHandler msh = new RandomMobSpawningHandler(
						areas, excludedAreas, mobconfigs, updateTime, pes);
				sendConsoleMessage("Created mob spawning " + key
						+ ";frequency:" + updateTime);
				manager.add(msh);
			}
		}

		// Intialize spawner based mob spawning

		ConfigurationSection spawnerSection = config
				.getConfigurationSection("spawner");
		if (spawnerSection != null) {
			for (String key : spawnerSection.getKeys(false)) {
				HashMap<EntityType, MobConfig> spawnerConfig = new HashMap<EntityType, MobConfig>();
				ConfigurationSection currentSection = spawnerSection
						.getConfigurationSection(key);
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				LinkedList<Area> excludedAreas = parseAreas(
						currentSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				for (String mappingKey : currentSection
						.getConfigurationSection("mobs").getKeys(false)) {
					ConfigurationSection currentSubSection = currentSection
							.getConfigurationSection("mobs")
							.getConfigurationSection(mappingKey);
					EntityType spawn = EntityType.valueOf(currentSubSection
							.getString("spawn"));
					MobConfig mobconfig = parseMobConfig(currentSubSection);
					spawnerConfig.put(spawn, mobconfig);
				}
				SpawnerSpawnModifier ssm = new SpawnerSpawnModifier(areas,
						excludedAreas, spawnerConfig);
				sendConsoleMessage("Successfully parsed mob spawner config "
						+ key);
			}
		}

		// Intitialize title displaying

		ConfigurationSection titleSection = config
				.getConfigurationSection("title");
		if (titleSection != null) {
			for (String key : titleSection.getKeys(false)) {
				ConfigurationSection currentSection = titleSection
						.getConfigurationSection(key);
				String title = currentSection.getString("title");
				String subtitle = currentSection.getString("subtitle", "");
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				LinkedList<Area> excludedAreas = parseAreas(
						currentSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				long fadeIn = parseTime(currentSection
						.getString("fadein", "1s"));
				long fadeOut = parseTime(currentSection.getString("fadeout",
						"1s"));
				long stay = parseTime(currentSection.getString("stay", "1s"));
				long updateTime = parseTime(currentSection
						.getString("updatetime"));
				PlayerEnvironmentState pes = parsePlayerEnvironmentState(currentSection
						.getConfigurationSection("player_environment_state"));
				TitleDisplayer td = new TitleDisplayer(areas, excludedAreas,
						updateTime, pes, title, subtitle, (int) fadeIn,
						(int) stay, (int) fadeOut);
				sendConsoleMessage("Loaded title displayer " + key + ";title:"
						+ title + ",subtitle:" + subtitle + ",fadein:" + fadeIn
						+ ",stay:" + stay + ",fadeout:" + fadeOut
						+ ",updatetime:" + updateTime);
				manager.add(td);
			}
		}

		// Initialize dispenser buffs
		ConfigurationSection dispenserSection = config
				.getConfigurationSection("dispenser");
		if (dispenserSection != null) {
			for (String key : dispenserSection.getKeys(false)) {
				ConfigurationSection currentSection = dispenserSection
						.getConfigurationSection(key);
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				LinkedList<Area> excludedAreas = parseAreas(
						currentSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				int dmg = currentSection.getInt("extradamage", 0);
				ConfigurationSection onHitDebuffSection = currentSection
						.getConfigurationSection("on_hit_debuffs");
				boolean infiniteArrows = currentSection.getBoolean(
						"infinite_arrows", false);
				HashMap<PotionEffect, Double> onHitDebuffs = new HashMap<PotionEffect, Double>();
				if (onHitDebuffSection != null) {
					for (String debuffkey : onHitDebuffSection.getKeys(false)) {
						ConfigurationSection currentDebuffSection = onHitDebuffSection
								.getConfigurationSection(debuffkey);
						PotionEffectType pet = PotionEffectType
								.getByName(currentDebuffSection
										.getString("type"));
						int level = currentDebuffSection.getInt("level", 1);
						long duration = parseTime(currentDebuffSection
								.getString("duration", "5s"));
						double chance = currentDebuffSection.getDouble(
								"chance", 1.0);
						PotionEffect pe = new PotionEffect(pet, (int) duration,
								level - 1); // -1 because its an amplifier
											// internally
						onHitDebuffs.put(pe, chance);
					}
				}
				DispenserBuff db = new DispenserBuff(areas, excludedAreas, dmg,
						onHitDebuffs, infiniteArrows);
				sendConsoleMessage("Loaded dispenser buff " + key + ";damage:"
						+ dmg + ",infinitearrows:" + infiniteArrows);
				manager.add(db);
			}
		}

		// Initialize reinforcement decay
		ConfigurationSection reinforcementSection = config
				.getConfigurationSection("reinforcement_decay");
		if (reinforcementSection != null) {
			for (String key : reinforcementSection.getKeys(false)) {
				ConfigurationSection currentSection = reinforcementSection
						.getConfigurationSection(key);
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				int amount = currentSection.getInt("amount");
				long updateTime = currentSection.getInt("updatetime");
				ReinforcementDecay rd = new ReinforcementDecay(areas,
						updateTime, amount);
				manager.add(rd);
				sendConsoleMessage("Loaded reinforcement decayer " + key
						+ ";amount:" + amount + ",frequency:" + updateTime);
			}
		}

		// Initialize block prevention
		ConfigurationSection blockPreventionSection = config
				.getConfigurationSection("block_prevention");
		if (blockPreventionSection != null) {
			for (String key : blockPreventionSection.getKeys(false)) {
				ConfigurationSection currentSection = blockPreventionSection
						.getConfigurationSection(key);
				LinkedList<Area> areas = parseAreas(
						currentSection.getConfigurationSection("areas"),
						worldname);
				LinkedList<Area> excludedAreas = parseAreas(
						currentSection
								.getConfigurationSection("excluded_areas"),
						worldname);
				PlayerEnvironmentState pes = parsePlayerEnvironmentState(currentSection
						.getConfigurationSection("player_environment_state"));
				boolean preventPlacing = currentSection.getBoolean(
						"preventPlacing", false);
				boolean preventBreaking = currentSection.getBoolean(
						"preventBreaking", false);
				boolean preventBuckets = currentSection.getBoolean(
						"preventBuckets", false);
				boolean preventIgnitions = currentSection.getBoolean(
						"preventIgnitions", false);
				boolean preventPearls = currentSection.getBoolean(
						"preventPearls", false);
				TerrainRestriction trl = new TerrainRestriction(areas,
						excludedAreas, pes, preventPlacing, preventBreaking,
						preventIgnitions, preventBuckets, preventPearls);
				manager.add(trl);
			}
		}

		sendConsoleMessage("Successfully parsed EE config");
		return manager;
	}

	private MobConfig parseMobConfig(ConfigurationSection currentMobConfig)
			throws ConfigParseException {
		EntityType type = EntityType
				.valueOf(currentMobConfig.getString("type"));
		String name = currentMobConfig.getString("name", null);
		int range = currentMobConfig.getInt("range", 32);
		int amount = currentMobConfig.getInt("amount", 1);
		int maximumTries = currentMobConfig.getInt("maximum_spawn_attempts", 5);
		String deathmsg = currentMobConfig.getString("deathmessage", null);
		String identifier = currentMobConfig.getString("identifier");
		if (identifier == null) {
			identifier = currentMobConfig.getName();
		}
		double spawnChance = currentMobConfig.getDouble("spawn_chance");
		String onHitMessage = currentMobConfig.getString("on_hit_message");
		ConfigurationSection dropsSection = currentMobConfig
				.getConfigurationSection("drops");
		LinkedList<ItemStack> drops = null;
		if (dropsSection != null) {
			drops = getItemStacks(dropsSection);
		}
		ConfigurationSection armourSection = currentMobConfig
				.getConfigurationSection("equipment");
		LinkedList<ItemStack> armour = null;
		if (armourSection != null) {
			armour = getItemStacks(armourSection);
		}
		HashMap<PotionEffectType, Integer> buffs = new HashMap<PotionEffectType, Integer>();
		ConfigurationSection buffSection = currentMobConfig
				.getConfigurationSection("buffs");
		if (buffSection != null) {
			for (String buffkey : buffSection.getKeys(false)) {
				ConfigurationSection currentBuffSection = buffSection
						.getConfigurationSection(buffkey);
				PotionEffectType pet = PotionEffectType
						.getByName(currentBuffSection.getString("type"));
				int level = currentBuffSection.getInt("level");
				buffs.put(pet, level);
			}
		}
		ConfigurationSection onHitDebuffSection = currentMobConfig
				.getConfigurationSection("on_hit_debuffs");
		HashMap<PotionEffect, Double> onHitDebuffs = new HashMap<PotionEffect, Double>();
		if (onHitDebuffSection != null) {
			for (String debuffkey : onHitDebuffSection.getKeys(false)) {
				ConfigurationSection currentDebuffSection = onHitDebuffSection
						.getConfigurationSection(debuffkey);
				PotionEffectType pet = PotionEffectType
						.getByName(currentDebuffSection.getString("type"));
				int level = currentDebuffSection.getInt("level");
				long duration = parseTime(currentDebuffSection.getString(
						"duration", "5s"));
				double chance = currentDebuffSection.getDouble("chance", 1.0);
				PotionEffect pe = new PotionEffect(pet, (int) duration, level);
				onHitDebuffs.put(pe, chance);
			}
		}
		LinkedList<Material> blocksToSpawnOn = convertMaterialList(currentMobConfig
				.getStringList("blocks_to_spawn_on"));
		LinkedList<Material> blocksNotToSpawnOn = convertMaterialList(currentMobConfig
				.getStringList("blocks_not_to_spawn_on"));
		LinkedList<Material> blocksToSpawnIn = convertMaterialList(currentMobConfig
				.getStringList("blocks_to_spawn_in"));
		int minimumLightLevel = currentMobConfig.getInt("minimum_light_level",
				0);
		int maximumLightLevel = currentMobConfig.getInt("maximum_light_level",
				15);
		boolean alternativeVersion = currentMobConfig.getBoolean(
				"alternative_version", false);
		int lureRange = currentMobConfig.getInt("lurerange",-1);
		double helmetDropChance = currentMobConfig.getDouble("helmet_dropchance", 0.0);
		double chestDropChance = currentMobConfig.getDouble("chestplate_dropchance", 0.0);
		double pantsDropChance = currentMobConfig.getDouble("leggings_dropchance", 0.0); 
		double bootsDropChance = currentMobConfig.getDouble("boots_dropchance", 0.0);
		double inHandDropChance = currentMobConfig.getDouble("item_in_hand_dropchance", 0.0);
		boolean despawnOnChunkUnload =  currentMobConfig.getBoolean("despawn_on_chunk_unload", true);
		boolean canPickUpItems = currentMobConfig.getBoolean("can_pickup_items", false);
		int health = currentMobConfig.getInt("health", -1);
		int ySpawnRange = currentMobConfig.getInt("y_spawn_range", 32);
		sendConsoleMessage("Successfully parsed mobconfig, type:"
				+ type.toString() + ",name:" + name + ",spawnChance:"
				+ spawnChance + ",amount:" + amount + ",minimumLightLevel:"
				+ minimumLightLevel + ",maximumLightLevel:" + maximumLightLevel
				+ ",alternativeVersion:" + alternativeVersion);
		return new MobConfig(identifier, type, name, buffs, armour, drops, onHitDebuffs,
				deathmsg, spawnChance, amount, range, maximumTries,
				onHitMessage, blocksToSpawnOn, blocksNotToSpawnOn,
				blocksToSpawnIn, minimumLightLevel, maximumLightLevel,
				alternativeVersion,lureRange, helmetDropChance, chestDropChance,
				pantsDropChance, bootsDropChance,
				 inHandDropChance, despawnOnChunkUnload,
				canPickUpItems, health, ySpawnRange);
	}

	public LinkedList<Material> convertMaterialList(List<String> input) {
		if (input == null || input.size() == 0) {
			return null;
		}
		LinkedList<Material> output = new LinkedList<Material>();
		for (String s : input) {
			output.add(Material.valueOf(s));
		}
		return output;

	}

	private LinkedList<Area> parseAreas(ConfigurationSection cs,
			String worldname) throws ConfigParseException {
		if (cs == null) {
			return null;
		}
		LinkedList<Area> areas = new LinkedList<Area>();
		List<String> biomes = cs.getStringList("biomes");
		ConfigurationSection locs = cs.getConfigurationSection("locations");
		if (biomes != null) {
			for (String current : biomes) {
				if (current.toLowerCase().equals("global")) {
					Area global = new Area(Shape.GLOBAL);
					areas.add(global);
					return areas;
				}
				Biome b = Biome.valueOf(current);
				if (b != null) {
					Area temp = new Area(Shape.BIOME, b);
					areas.add(temp);
				} else {
					throw new ConfigParseException(current + " is not a biome");
				}
			}
		}
		if (locs != null) {
			for (String current : locs.getKeys(false)) {
				ConfigurationSection currentSection = locs
						.getConfigurationSection(current);
				int minimumY = currentSection.getInt("minimum_y", 0);
				int maximumY = currentSection.getInt("maximum_y", 255);
				Shape shape = Shape.valueOf(currentSection.getString("shape"));
				Area temp = null;
				Location center;
				switch (shape) {
				case CIRCLE:
				case RECTANGLE:
					int xSize = currentSection.getInt("xsize");
					int zSize = currentSection.getInt("zsize");
					center = parseLocation(
							currentSection.getConfigurationSection("center"),
							worldname);
					temp = new Area(shape, center, xSize, zSize, minimumY,
							maximumY);
					break;
				case RING:
					int innerRadius = currentSection.getInt("inner_limit");
					int outerRadius = currentSection.getInt("outer_limit");
					center = parseLocation(
							currentSection.getConfigurationSection("center"),
							worldname);
					temp = new Area(shape, innerRadius, outerRadius, center,
							minimumY, maximumY);
					break;
				default:
					throw new ConfigParseException();
				}
				areas.add(temp);
			}
		}
		return areas;
	}

	private Location parseLocation(ConfigurationSection c, String worldname) {
		long x = c.getLong("x");
		long y = c.getLong("y", 0L);
		long z = c.getLong("z");
		return new Location(plugin.getServer().getWorld(worldname), x, y, z);
	}

	private LinkedList<ItemStack> getItemStacks(ConfigurationSection cs) {
		LinkedList<ItemStack> result = new LinkedList<ItemStack>();
		for (String key : cs.getKeys(false)) {
			ConfigurationSection currentSection = cs
					.getConfigurationSection(key);
			Material material = Material.getMaterial(currentSection
					.getString("material"));
			if (material == null) {
				sendConsoleMessage("Material was null for "
						+ currentSection.toString());
			}
			int amount = currentSection.getInt("amount", 1);
			ItemStack item = new ItemStack(material, amount);
			ItemMeta meta = item.getItemMeta();
			String displayName = currentSection.getString("display_name");
			if (displayName != null) {
				meta.setDisplayName(displayName);
			}
			String lore = currentSection.getString("lore");
			if (lore != null) {
				List<String> lorelist = new LinkedList<String>();
				lorelist.add(lore);
				meta.setLore(lorelist);
			}
			ConfigurationSection enchants = currentSection
					.getConfigurationSection("enchants");
			if (enchants != null) {
				for (String enchantKey : enchants.getKeys(false)) {
					ConfigurationSection enchantSection = enchants
							.getConfigurationSection(enchantKey);
					Enchantment enchant = Enchantment.getByName(enchantSection
							.getString("enchant"));
					int level = enchantSection.getInt("level");
					meta.addEnchant(enchant, level, true);
				}
			}
			item.setItemMeta(meta);
			result.add(item);
		}
		return result;
	}

	private PlayerEnvironmentState parsePlayerEnvironmentState(
			ConfigurationSection cs) {
		if (cs == null) {
			return null;
		}
		Boolean rain = booleanNullCheck(cs, "rain");
		Boolean night = booleanNullCheck(cs, "night");
		return new PlayerEnvironmentState(rain, night);

	}

	private Boolean booleanNullCheck(ConfigurationSection cs, String s) {
		if (cs.contains(s)) {
			return cs.getBoolean(s);
		} else {
			return null;
		}

	}

	private LinkedList<ArmourType> parseArmourTypeList(ConfigurationSection cs,
			String type) {
		if (!cs.contains(type)) {
			sendConsoleMessage(type + " was null");
			return null;
		}
		LinkedList<ArmourType> result = new LinkedList<ArmourType>();
		for (String s : cs.getStringList(type)) {
			sendConsoleMessage("adding " + s);
			result.add(ArmourType.valueOf(s.toUpperCase()));
		}
		if (result.size() == 0) {
			sendConsoleMessage("size was 0");
			return null;
		}
		return result;
	}

	public void sendConsoleMessage(String a) {
		EnvironmentalEffects.sendConsoleMessage(a);
	}
}
