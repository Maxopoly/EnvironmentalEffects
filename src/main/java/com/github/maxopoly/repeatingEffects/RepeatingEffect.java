package com.github.maxopoly.repeatingEffects;

import java.util.LinkedList;

import org.bukkit.entity.Player;

import com.github.maxopoly.Effect;
import com.github.maxopoly.datarepresentations.Area;
import com.github.maxopoly.datarepresentations.PlayerEnvironmentState;

/**
 * Pretty much every effect caused by this plugin repeats in some sort of way,
 * this provides a super class for any repeating effect with some convenience
 * methods
 * 
 * @author Max
 *
 */
public abstract class RepeatingEffect extends Effect implements Runnable {
	protected long updateTime;

	public RepeatingEffect(LinkedList<Area> includedAreas,
			LinkedList<Area> excludedAreas, long updateTime,
			PlayerEnvironmentState pes) {
		super(includedAreas, excludedAreas, pes);
		this.updateTime = updateTime;
		plugin.getServer().getScheduler()
				.scheduleSyncRepeatingTask(plugin, this, 0L, updateTime);
	}

	/**
	 * Should apply what ever effect the class gives out to the player or
	 * attempt to apply it
	 * 
	 * @param p
	 *            player to which the effect should be applied to
	 */
	public abstract void applyToPlayer(Player p);

	/**
	 * When rescheduled, this amount of ticks later the effect will applied
	 * again. 20 ticks = 1 second
	 * 
	 * @return amount of ticks between runs
	 */
	public long getUpdateTime() {
		return updateTime;
	}

	/**
	 * Changes how often the effect is updated for each player. Be aware that
	 * this will not affect the already scheduled task
	 * 
	 * @param updateTime
	 *            how often the effect should be updated (in ticks)
	 */
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * Standard run method for an effect. May be overwritten by a subclass
	 */
	public void run() {
		for (Player p : getCurrentPlayers()) {
			applyToPlayer(p);
		}
	}
}
