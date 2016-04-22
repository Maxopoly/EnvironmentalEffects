package com.github.maxopoly.repeatingEffects;

import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.maxopoly.datarepresentations.Area;
import com.github.maxopoly.datarepresentations.PlayerEnvironmentState;

public class PotionBuff extends RepeatingEffect {
	private PotionEffectType pet;
	private long duration; // in ticks
	private int level;

	public PotionBuff(LinkedList<Area> includedAreas,
			LinkedList<Area> excludedAreas, PotionEffectType pet, int level,
			long duration, PlayerEnvironmentState pes) {
		super(includedAreas, excludedAreas, (duration / 2), pes);
		this.pet = pet;
		this.level = level - 1;
		// for some reason applying a potion effect with level 1, will result
		// in an ingame level 2 effect, because it is interpreted as an
		// amplifier
		// this just ensures the expected behavior from the config
		this.duration = duration;
	}

	public void applyToPlayer(Player p) {
		if (conditionsMet(p)) {
			PotionEffect pe = new PotionEffect(pet, (int) duration, level);
			p.addPotionEffect(pe, true);
		}
	}

	/**
	 * @return length of the potion effect applied in ticks
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Changes the duration of the potion effect, which is applied by this
	 * instance. If a permanent effect is wanted, the updateTime will also need
	 * to be adjusted after changing this
	 * 
	 * @param duration
	 *            new duration for the potion effect
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * @return the level of the potion effect applied by this instance
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Change the level of the potion effect applied by this instance
	 * 
	 * @param level
	 *            new level of the effect
	 */
	public void setLevel(int level) {
		this.level = level;
	}

}
