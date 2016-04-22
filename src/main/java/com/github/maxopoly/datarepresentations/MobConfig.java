package com.github.maxopoly.datarepresentations;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * A complete configuration how a specific monster (here often referred to as
 * "mob") should be spawned. Each mob spawned will be stored in memory with a
 * reference to it's config to determine it's drops when it's killed and for
 * other functionality
 * 
 * @author Max
 *
 */
public class MobConfig {
	// see getter and setter javadoc for explaination what each of those do
	private EntityType type;
	private LinkedList<ItemStack> drops;
	private LinkedList<ItemStack> armour;
	private HashMap<PotionEffectType, Integer> buffs;
	private HashMap<PotionEffect, Double> onHitDebuffs;
	private String name;
	private int range;
	private Random rng;
	private int maximumTries;
	private double spawnChance;
	private int amount;
	private String deathMessage;
	private String onHitMessage;
	private LinkedList<Material> spawnOnBlocks;
	private LinkedList<Material> doNotSpawnOnBlocks;
	private LinkedList<Material> spawnInBlocks;
	private int lureRange;
	private int minimumLightLevel;
	private int maximumLightLevel;
	private boolean alternateVersion;
	private String identifer;
	private double helmetDropChance, chestDropChance, pantsDropChance,
			bootsDropChance, inHandDropChance;
	private boolean despawnOnChunkUnload;
	private boolean canPickupItems;
	private int health;
	private int ySpawnRange;

	public MobConfig(String identifier, EntityType type, String name,
			HashMap<PotionEffectType, Integer> buffs,
			LinkedList<ItemStack> armour, LinkedList<ItemStack> drops,
			HashMap<PotionEffect, Double> onHitDebuffs, String deathMessage,
			double spawnChance, int amount, int range, int maxiumumTries,
			String onHitMessage, LinkedList<Material> spawnOnBlocks,
			LinkedList<Material> doNotSpawnOnBlocks,
			LinkedList<Material> spawnInBlocks, int minimumLightLevel,
			int maximumLightLevel, boolean alternativeVersion, int lureRange,
			double helmetDropChance, double chestDropChance,
			double pantsDropChance, double bootsDropChance,
			double inHandDropChance, boolean despawnOnChunkOnLoad,
			boolean canPickUpItems, int health, int ySpawnRange) {
		this.name = name;
		this.identifer = identifier;
		this.type = type;
		this.buffs = buffs;
		this.armour = armour;
		this.drops = drops;
		this.range = range;
		this.rng = new Random();
		this.maximumTries = maxiumumTries;
		this.spawnChance = spawnChance;
		this.amount = amount;
		this.deathMessage = deathMessage;
		this.onHitDebuffs = onHitDebuffs;
		this.onHitMessage = onHitMessage;
		this.spawnOnBlocks = spawnOnBlocks;
		this.spawnInBlocks = spawnInBlocks;
		this.doNotSpawnOnBlocks = doNotSpawnOnBlocks;
		this.minimumLightLevel = minimumLightLevel;
		this.maximumLightLevel = maximumLightLevel;
		this.alternateVersion = alternativeVersion;
		this.lureRange = lureRange;
		this.helmetDropChance = helmetDropChance;
		this.chestDropChance = chestDropChance;
		this.pantsDropChance = pantsDropChance;
		this.bootsDropChance = bootsDropChance;
		this.inHandDropChance = inHandDropChance;
		this.despawnOnChunkUnload = despawnOnChunkOnLoad;
		this.canPickupItems = canPickUpItems;
		this.health = health;
		this.ySpawnRange = ySpawnRange;
	}

	/**
	 * Attempts to spawn a mob according to this config around the given
	 * location. This can fail for multiple reasons: The set spawnchance could
	 * prevent the spawn, because of randomized checks, no free space for the
	 * mob could be found or another plugin or an external restriction could
	 * cancel the spawn
	 * 
	 * @param loc
	 *            center of the square in which the mob will be spawned
	 * @return list of all mobs successfully spawned by this method
	 */
	public LinkedList<Entity> createMob(Location loc) {
		if (rng.nextDouble() > spawnChance) {
			return null;
		}
		Location spawnLoc = findSpawningLocation(loc);
		if (spawnLoc == null) {
			return null;
		}
		LinkedList<Entity> resultMobs = new LinkedList<Entity>();
		for (int i = 0; i < amount; i++) {
			LivingEntity mob = (LivingEntity) createMobAt(spawnLoc);
			if (mob != null) {
				resultMobs.add(mob);
			}
		}

		return resultMobs;
	}

	/**
	 * The possible states while searching for feasible spawning locations
	 *
	 */
	private enum BlockCountState {
		NOTHING, FOUNDBASEBLOCK, ONEAIR;
	}

	public Location findSpawningLocation(Location loc) {
		for (int i = 0; i < maximumTries; i++) {
			int x = loc.getBlockX() + rng.nextInt(range * 2) - range;
			int z = loc.getBlockZ() + rng.nextInt(range * 2) - range;
			BlockCountState bcs = BlockCountState.NOTHING;
			LinkedList<Integer> yLevels = new LinkedList<Integer>();
			for (int y = Math.max(0, loc.getBlockY() - ySpawnRange); y <= Math.min(255,
					loc.getBlockY() + ySpawnRange); y++) {
				Material m = loc.getWorld().getBlockAt(x, y, z).getType();
				switch (bcs) {
				case NOTHING:
					if ((spawnOnBlocks == null && m.isSolid() && (doNotSpawnOnBlocks == null || !doNotSpawnOnBlocks
							.contains(m)))
							|| (spawnOnBlocks != null && spawnOnBlocks
									.contains(m))) {
						bcs = BlockCountState.FOUNDBASEBLOCK;
					}
					break;
				case FOUNDBASEBLOCK:
					if ((spawnInBlocks == null && m == Material.AIR)
							|| (spawnInBlocks != null && spawnInBlocks
									.contains(m))) {
						int light = loc.getWorld().getBlockAt(x, y, z)
								.getLightLevel();

						if (light >= minimumLightLevel
								&& light <= maximumLightLevel) {
							bcs = BlockCountState.ONEAIR;
						}
						break;
					}
					if ((spawnOnBlocks == null && m.isSolid() && (doNotSpawnOnBlocks == null || !doNotSpawnOnBlocks
							.contains(m)))
							|| (spawnOnBlocks != null && spawnOnBlocks
									.contains(m))) {
						// another good base block, just leave the counter
						// as it is
						break;
					} else {
						bcs = BlockCountState.NOTHING;
					}
				case ONEAIR:
					if ((spawnInBlocks == null && m == Material.AIR)
							|| (spawnInBlocks != null && spawnInBlocks
									.contains(m))) {
						yLevels.add(y);
						bcs = BlockCountState.NOTHING;
						break;
					}
					if ((spawnOnBlocks == null && m.isSolid() && (doNotSpawnOnBlocks == null || !doNotSpawnOnBlocks
							.contains(m)))
							|| (spawnOnBlocks != null && spawnOnBlocks
									.contains(m))) {
						// base block
						bcs = BlockCountState.FOUNDBASEBLOCK;
						break;
					} else {
						bcs = BlockCountState.NOTHING;
					}
				}
			}
			if (yLevels.size() > 0) {
				return new Location(loc.getWorld(), x, yLevels.get(rng
						.nextInt(yLevels.size())), z);
			}
		}
		return null;
	}

	public Entity createMobAt(Location loc) {
		if (rng.nextDouble() > spawnChance) {
			return null;
		}
		LivingEntity mob = (LivingEntity) loc.getWorld().spawnEntity(loc, type);
		if (mob != null) { // event wasn't cancelled
			if (name != null && name != "") {
				mob.setCustomName(name);
				mob.setCustomNameVisible(true);
			}
			EntityEquipment eq = mob.getEquipment();
			if (armour != null) {
				for (ItemStack is : armour) {
					setSlot(eq, is);
				}
			}
			if (health != -1) {
				mob.setMaxHealth(health);
				mob.setHealth(health);
			}
			eq.setBootsDropChance((float) bootsDropChance);
			eq.setLeggingsDropChance((float) pantsDropChance);
			eq.setChestplateDropChance((float) chestDropChance);
			eq.setHelmetDropChance((float) helmetDropChance);
			eq.setItemInHandDropChance((float) inHandDropChance);
			mob.setCanPickupItems(canPickupItems);
			mob.setRemoveWhenFarAway(despawnOnChunkUnload);

			switch (type) {
			case SKELETON:
				if (alternateVersion) {
					((Skeleton) mob).setSkeletonType(SkeletonType.WITHER);
				} else {
					((Skeleton) mob).setSkeletonType(SkeletonType.NORMAL);
				}
				break;
			case CREEPER:
				if (alternateVersion) {
					((Creeper) mob).setPowered(true);
				} else {
					((Creeper) mob).setPowered(false);
				}
			}

			for (Map.Entry<PotionEffectType, Integer> current : buffs
					.entrySet()) {
				mob.addPotionEffect(new PotionEffect(current.getKey(),
						Integer.MAX_VALUE, current.getValue(), false, false));
				// That buff lasts for 68 years, that should be long enough
			}
			/*
			 * if (lureRange != -1) { EnvironmentalEffects .getPlugin()
			 * .getServer() .getScheduler() .scheduleSyncDelayedTask(
			 * EnvironmentalEffects.getPlugin(), new MobLureDenier(lureRange,
			 * mob, loc)); }
			 */
		}
		return mob;
	}

	/**
	 * Equips the given itemstack to the right slot. This is not safe, if an
	 * item is given here that can not be held in hand, exceptions might be
	 * thrown. Will override any previous item in the slot.
	 * 
	 * @param eq
	 *            EntityEquipment of any entity
	 * @param is
	 *            item to be put into an equipment slot
	 */
	public void setSlot(EntityEquipment eq, ItemStack is) {
		switch (is.getType()) {
		case DIAMOND_BOOTS:
		case LEATHER_BOOTS:
		case GOLD_BOOTS:
		case IRON_BOOTS:
		case CHAINMAIL_BOOTS:
			eq.setBoots(is);
			return;
		case DIAMOND_HELMET:
		case GOLD_HELMET:
		case IRON_HELMET:
		case LEATHER_HELMET:
		case CHAINMAIL_HELMET:
		case PUMPKIN:
			eq.setHelmet(is);
			return;
		case DIAMOND_CHESTPLATE:
		case GOLD_CHESTPLATE:
		case IRON_CHESTPLATE:
		case LEATHER_CHESTPLATE:
		case CHAINMAIL_CHESTPLATE:
			eq.setChestplate(is);
			return;
		case DIAMOND_LEGGINGS:
		case GOLD_LEGGINGS:
		case IRON_LEGGINGS:
		case LEATHER_LEGGINGS:
		case CHAINMAIL_LEGGINGS:
			eq.setLeggings(is);
			return;
		default:
			// assume it's a weapon
			eq.setItemInHand(is);
		}
	}

	/**
	 * @return what should be dropped by mobs spawned by this config upon
	 *         killing them
	 */
	public LinkedList<ItemStack> getDrops() {
		return drops;
	}

	/**
	 * Alters what should be dropped by mobs spawned by this config when they
	 * get killed. This also applies to mobs spawned in the past by this config
	 * 
	 * @param drops
	 *            new list of drops
	 */
	public void setDrops(LinkedList<ItemStack> drops) {
		this.drops = drops;
	}

	/**
	 * @return the name given to every mob spawned by this config
	 */
	public String getName() {
		return name;
	}

	/**
	 * Changes the name given to mobs spawned by this config. Will only apply to
	 * mobs spawned after the change
	 * 
	 * @param name
	 *            new name to be given
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return how often this config tries to find a suitable empty spawning
	 *         spot for a mob before cancelling
	 */
	public int getMaximumTries() {
		return maximumTries;
	}

	/**
	 * @return The distance a mob can move away from it's spawn before it gets
	 *         teleported back or -1 if no such behavior is wanted
	 */
	public int getLureRange() {
		return lureRange;
	}

	/**
	 * Sets how often this config tries to find a suitable location to spawn a
	 * mob (no blocks in the way)
	 * 
	 * @param maximumTries
	 *            new amount of tries
	 */
	public void setMaximumTries(int maximumTries) {
		this.maximumTries = maximumTries;
	}

	/**
	 * The spawning range is a square, which's sides each are 2* range long and
	 * which is centred on the player
	 * 
	 * @return the current spawning range set
	 */
	public int getRange() {
		return range;
	}

	/**
	 * To persist mobs and their special effects past restarts, the uuid of each
	 * mob is stored together with the identifier of it's mobconfig
	 * 
	 * @return Identifier of this config
	 */
	public String getIdentifier() {
		return identifer;
	}

	/**
	 * The spawning range is a square, which's sides each are 2* range long and
	 * which is centred on the player
	 * 
	 * @param range
	 *            new range to be used
	 */
	public void setRange(int range) {
		this.range = range;
	}

	/**
	 * MobConfigs can spawn multiple mobs at once, this number decides how many
	 * that is
	 * 
	 * @return current amount of mobs which are spawned at once
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Sets the amount of mobs which are attempted to be spawned at once if all
	 * spawning conditions are met. The actual amount might be lower because it
	 * might be restricted by external spawning conditions or other plugins
	 * 
	 * @param amount
	 *            new amount of mobs to spawn
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * Upon spawning, all the buffs listed in this config are applied for
	 * MAX_INT seconds to the mob (68 years)
	 * 
	 * @return buffs applied to every spawned mob
	 */
	public HashMap<PotionEffectType, Integer> getBuffs() {
		return buffs;
	}

	/**
	 * Overwrites the currently selected buffs. This will only be applied for
	 * mobs spawned after the change
	 * 
	 * @param buffs
	 *            Hashmap with the new buffs
	 */
	public void setBuffs(HashMap<PotionEffectType, Integer> buffs) {
		this.buffs = buffs;
	}

	/**
	 * This string is sent to a player if he kills a mob spawned by this config.
	 * If the string is null or empty, no message will be sent
	 * 
	 * @return text sent to each player upon killing a mob of this config
	 */
	public String getDeathMessage() {
		return deathMessage;
	}

	/**
	 * Changes which string is send to a player if he kills a mob spawned by
	 * this config. Changing this to null or an empty string will prevent the
	 * sending of any messages. Changes here will also be applied for all mobs
	 * spawned previously
	 * 
	 * @param deathMessage
	 *            new message to be sent
	 */
	public void setDeathMessage(String deathMessage) {
		this.deathMessage = deathMessage;
	}

	/**
	 * @return type of the mobs spawned by this config
	 */
	public EntityType getType() {
		return type;
	}

	/**
	 * Mobs spawned by this plugin can apply debuffs with a set probability, if
	 * wanted. They are stored in a HashMap, where the debuff to apply is the
	 * key and the chance to apply it on each hit is the value
	 * 
	 * @return debuffs applied on hit with their chances
	 */
	public HashMap<PotionEffect, Double> getOnHitDebuffs() {
		return onHitDebuffs;
	}

	/**
	 * Mobs spawned by this plugin can apply debuffs with a set probability, if
	 * wanted. They are stored in a HashMap, where the debuff to apply is the
	 * key and the chance to apply it on each hit is the value
	 * 
	 * @param onHitDebuffs
	 *            new map of debuffs and their chances to be applied
	 */
	public void setOnHitDebuffs(HashMap<PotionEffect, Double> onHitDebuffs) {
		this.onHitDebuffs = onHitDebuffs;
	}

	/**
	 * @return The message sent to the player whenever he gets hit or null if
	 *         nothing is sent
	 */
	public String onHitMessage() {
		return onHitMessage;
	}

	/**
	 * @return Which blocks were specified as material to spawn on, null if
	 *         nothing was specified and every block except the for the
	 *         forbidden ones are valid
	 */
	public LinkedList<Material> getBlocksToSpawnOn() {
		return spawnOnBlocks;
	}

	/**
	 * @return Which blocks are explicitly forbidden for this mob to spawn on or
	 *         null if nothing was specified
	 */
	public LinkedList<Material> getBlockNotToSpawnOn() {
		return doNotSpawnOnBlocks;
	}

	/**
	 * @return Which blocks are considered "air" or space to spawn in for this
	 *         mobconfig, null if nothing was specified and only air is accepted
	 */
	public LinkedList<Material> getBlockToSpawnIn() {
		return spawnInBlocks;
	}

	/**
	 * @return The maximum light level at which this mob can spawn
	 */
	public int getMaximumLightLevel() {
		return maximumLightLevel;
	}

	/**
	 * @return The minimum light level required for this mob to spawn
	 */
	public int getMinimumLightLevel() {
		return minimumLightLevel;
	}

}
