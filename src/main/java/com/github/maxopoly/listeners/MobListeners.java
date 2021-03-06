package com.github.maxopoly.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import com.github.maxopoly.EnvironmentalEffects;
import com.github.maxopoly.datarepresentations.MobConfig;
import com.github.maxopoly.repeatingEffects.DispenserBuff;
import com.github.maxopoly.repeatingEffects.RandomMobSpawningHandler;

public class MobListeners implements Listener {
	private Random rng;
	private HashMap<EntityType, MobConfig> spawnerConfig;
	private boolean cancelAllOther;

	public MobListeners(HashMap<EntityType, MobConfig> spawnerConfig, boolean cancelAllOther) {
		rng = new Random();
		this.spawnerConfig = spawnerConfig;
		this.cancelAllOther = cancelAllOther;
	}

	@EventHandler
	public void monsterSpawn(CreatureSpawnEvent e) {
		if (cancelAllOther
				&& e.getSpawnReason() == SpawnReason.NATURAL
				&& e.getEntity() instanceof Monster) {
			e.setCancelled(true);
		}

	}

	@EventHandler
	public void monsterDamagePlayer(EntityDamageByEntityEvent e) {
		if (e.getEntityType() != EntityType.PLAYER) {
			return;
		}
		Monster damager = null;
		if (e.getDamager() instanceof Monster) {
			damager = (Monster) e.getDamager();
		} else {
			if (e.getDamager() instanceof Projectile) {
				ProjectileSource ps = ((Projectile) e.getDamager())
						.getShooter();
				if (ps instanceof Monster) {
					damager = (Monster) ps;
				} else {
					return;
				}
			} else {
				return;
			}
		}
		MobConfig config = RandomMobSpawningHandler.getConfig(damager);
		if (config != null) {
			HashMap<PotionEffect, Double> debuffs = config.getOnHitDebuffs();
			if (debuffs != null) {
				for (Map.Entry<PotionEffect, Double> current : debuffs
						.entrySet()) {
					if (rng.nextDouble() <= current.getValue()) {
						((Player) e.getEntity()).addPotionEffect(
								current.getKey(), true);
					}
				}
			}
		}
	}

	@EventHandler
	public void monsterDeath(EntityDeathEvent e) {
		Entity en = e.getEntity();
		MobConfig config;
		if (en instanceof Monster
				&& (config = RandomMobSpawningHandler.getConfig((Monster) en)) != null) {
			RandomMobSpawningHandler.removeMonster((Monster) en);
			if (((LivingEntity) en).getKiller() instanceof Player) {
				String deathMsg = config.getDeathMessage();
				if (deathMsg != null && !deathMsg.equals("")) {
					((LivingEntity) en).getKiller().sendMessage(deathMsg);
				}

				List<ItemStack> drops = e.getDrops();
				drops.clear();
				LinkedList<ItemStack> dropsToInsert = config.getDrops();
				if (dropsToInsert != null) {
					for (ItemStack is : dropsToInsert) {
						drops.add(is);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void spawnerSpawn(SpawnerSpawnEvent e) {
		if (spawnerConfig == null) {
			return;
		}
		MobConfig mc = spawnerConfig.get(e.getEntityType());
		if (mc != null) {
			e.setCancelled(true);
			LinkedList<Monster> spawned = mc.createMobAt(e.getLocation());
			if (spawned != null) {
				for (Monster m : spawned) {
					RandomMobSpawningHandler.addMonster(m, mc);
				}
			}
		}
	}

	@EventHandler
	public void arrowHitPlayer(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player
				&& e.getDamager() instanceof Projectile) {
			ProjectileSource shooter = ((Projectile) e.getDamager())
					.getShooter();
			if (shooter instanceof BlockProjectileSource) {
				Block disp = ((BlockProjectileSource) shooter).getBlock();
				DispenserBuff db = (DispenserBuff) EnvironmentalEffects
						.getManager().getEffect(DispenserBuff.class,
								disp.getLocation());
				if (db != null) {
					db.applyToPlayer((Player) e.getEntity());
				}

			}
		}
	}

	@EventHandler
	public void dupeArrows(ProjectileLaunchEvent e) {
		if (e.getEntity() instanceof Arrow
				&& e.getEntity().getShooter() instanceof BlockProjectileSource) {
			BlockProjectileSource sourceblock = (BlockProjectileSource) ((Arrow) e
					.getEntity()).getShooter();
			DispenserBuff db = (DispenserBuff) EnvironmentalEffects
					.getManager().getEffect(DispenserBuff.class,
							sourceblock.getBlock().getLocation());
			if (db != null && db.getInfiniteArrow()) {
				((Dispenser) (sourceblock.getBlock().getState()))
						.getInventory().addItem(new ItemStack(Material.ARROW));
			}

		}
	}

}
