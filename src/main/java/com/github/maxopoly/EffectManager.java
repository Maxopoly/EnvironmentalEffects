package com.github.maxopoly;

import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.Location;

public class EffectManager {
	private HashSet<Effect> set;

	public EffectManager() {
		set = new HashSet<Effect>();
	}

	public void add(Effect re) {
		set.add(re);
	}

	public LinkedList<Effect> getEffects(
			Class<? extends Effect> c) {
		LinkedList<Effect> result = new LinkedList<Effect>();
		for (Effect re : set) {
			if (re.getClass() == c) {
				result.add(re);
			}
		}
		return result;
	}
	
	public LinkedList <Effect> getEffectsForLocation(Location loc) {
		LinkedList<Effect> result = new LinkedList<Effect>();
		for (Effect re : set) {
			if (re.isInArea(loc)) {
				result.add(re);
			}
		}
		return result;
	}
	
	public Effect getEffect(Class<? extends Effect> c,Location loc) {
		for(Effect re:set) {
			if(re.getClass()==c && re.isInArea(loc)) {
				return re;
			}
		}
		return null;
	}
}
