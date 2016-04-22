package com.github.maxopoly.repeatingEffects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.github.maxopoly.datarepresentations.Area;

public class DispenserBuff extends RepeatingEffect {
	private HashMap<PotionEffect, Double> onHitDebuffs;
	private int extraDamage;
	private boolean infiniteArrows;

	public DispenserBuff(LinkedList<Area> includedAreas,
			LinkedList<Area> excludedAreas, int extraDamage,
			HashMap<PotionEffect, Double> onHitDebuffs, boolean infiniteArrows) {
		super(includedAreas, excludedAreas, 24 * 60 * 60 * 30, null);
		this.onHitDebuffs = onHitDebuffs;
		this.extraDamage = extraDamage;
		this.infiniteArrows = infiniteArrows;
	}

	public void applyToPlayer(Player p) {
		p.damage((double) extraDamage);
		if (onHitDebuffs != null) {
			for (Map.Entry<PotionEffect, Double> current : onHitDebuffs
					.entrySet()) {
				if (rng.nextDouble() <= current.getValue()) {
					p.addPotionEffect(current.getKey(), true);
				}
			}
		}
	}

	public HashMap<PotionEffect, Double> getDebuffs() {
		return onHitDebuffs;
	}

	public boolean getInfiniteArrow() {
		return infiniteArrows;
	}

	public int getExtraDamage() {
		return extraDamage;
	}
}
