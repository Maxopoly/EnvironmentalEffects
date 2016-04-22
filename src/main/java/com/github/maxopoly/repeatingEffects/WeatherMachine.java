package com.github.maxopoly.repeatingEffects;

import java.util.LinkedList;

import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.maxopoly.datarepresentations.Area;

/**
 * Allows to set randomize weather in specific areas to configured values. The
 * weather will only be changed client side, because this supports multiple
 * areas on the same map and server side rain is global. Use this classes
 * methods to determine whether it's raining for players right now, they might
 * be in async with the server's weather. Players might walk into an area at any
 * time, so this runnable constantly schedules itself to ensure the weather is
 * right for every player. This doesn't mean it's rerolled everytime run() is
 * run
 * 
 * @author Max
 *
 */
public class WeatherMachine extends RepeatingEffect {

	double rainChance; // between 0 and 1, where 1 is 100%
	long minRainDuration; // in ticks
	boolean rain;
	WeatherType currentWeather;
	long rainUpdate;
	int i = 0;

	public WeatherMachine(LinkedList<Area> includedAreas,
			LinkedList<Area> excludedAreas, double rainChance,
			long minRainDuration, long rainUpdate) {
		super(includedAreas, excludedAreas, rainUpdate, null);
		this.rainChance = rainChance;
		this.minRainDuration = minRainDuration;
		this.rainUpdate = rainUpdate;
		willItRain();
	}

	public void run() {
		i += rainUpdate;
		if (i >= minRainDuration) {
			i = 0;
			willItRain();
		}
		setRainForPlayers();
	}

	/**
	 * Sets the weather for all players in this classes biome to the currently
	 * selected weather type
	 */
	public void setRainForPlayers() {
		for (Player p : getCurrentPlayers()) {
			if (p.getPlayerWeather() != getCurrentWeatherType()) {
				applyToPlayer(p);
			}
		}
	}

	/**
	 * Recalculates whether it should rain based on probabilities
	 */
	public void willItRain() {
		rain = rng.nextDouble() <= rainChance;
		if (rain) {
			currentWeather = WeatherType.DOWNFALL;
		} else {
			currentWeather = WeatherType.CLEAR;
		}
	}

	/**
	 * Getter for the rain bool
	 * 
	 * @return true if it's raining, false if not
	 */
	public boolean isItRaining() {
		return rain;
	}

	/**
	 * Getter for the WeatherType
	 * 
	 * @return current WeatherType
	 */
	public WeatherType getCurrentWeatherType() {
		if (isItRaining()) {
			return WeatherType.DOWNFALL;
		}
		return WeatherType.CLEAR;
	}

	public void applyToPlayer(Player p) {
		if (conditionsMet(p)) {
			p.setPlayerWeather(getCurrentWeatherType());
		}
	}

}
