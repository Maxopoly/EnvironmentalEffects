package com.github.maxopoly.repeatingEffects;

import java.util.LinkedList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.maxopoly.datarepresentations.Area;
import com.github.maxopoly.datarepresentations.PlayerEnvironmentState;

/**
 * Not functional yet
 * 
 * @author Max
 *
 */
public class EffectGenerator extends RepeatingEffect {
	private Effect effect;
	private int amount;
	private float speed;

	public EffectGenerator(LinkedList<Area> includedAreas,
			LinkedList<Area> excludedAreas, Effect effect, int amount,
			float speed, long updateTime, PlayerEnvironmentState pes) {
		super(includedAreas, excludedAreas, updateTime, pes);
		this.effect = effect;
		this.amount = amount;
		this.speed = speed;
	}

	public void applyToPlayer(Player p) {
		if (conditionsMet(p)) {
			p.spigot().playEffect(p.getLocation(), effect, 0, 0, 0F, 0F, 0F,
					speed, amount, 1);
			// TODO fix this
		}
	}

	public void playAtLocation(Location loc, int radius) {
		loc.getWorld().playEffect(loc, effect, radius);
	}

	public float getSpeed() {
		return speed;
	}

	public Effect getEffect() {
		return effect;
	}

	public int getAmount() {
		return amount;
	}

}
