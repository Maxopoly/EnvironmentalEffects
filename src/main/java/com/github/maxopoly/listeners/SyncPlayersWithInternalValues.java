package com.github.maxopoly.listeners;

import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import com.github.maxopoly.Effect;
import com.github.maxopoly.EffectManager;
import com.github.maxopoly.repeatingEffects.DaytimeModifier;
import com.github.maxopoly.repeatingEffects.PotionBuff;
import com.github.maxopoly.repeatingEffects.TitleDisplayer;
import com.github.maxopoly.repeatingEffects.WeatherMachine;

public class SyncPlayersWithInternalValues implements Listener {
	EffectManager manager;

	public SyncPlayersWithInternalValues(EffectManager manager) {
		this.manager = manager;
	}

	@EventHandler
	public void changeWeatherAndDaytimeOnLogin(PlayerJoinEvent e) {
		// Set that player hasnt been in an area to display a title
		LinkedList<Effect> titledisplayers = manager
				.getEffects(TitleDisplayer.class);
		for (Effect td : titledisplayers) {
			((TitleDisplayer) td).addPlayer(e.getPlayer(), false);
		}

		updateValues(e.getPlayer());
	}

	@EventHandler
	public void cancelAllNaturalWeather(WeatherChangeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void changeWeatherAndDaytimeAfterDeath(PlayerRespawnEvent e) {
		updateValues(e.getPlayer());
	}

	public void updateValues(Player p) {
		if (p != null) {
			WeatherMachine wm = (WeatherMachine) manager.getEffect(
					WeatherMachine.class, p.getLocation());
			if (wm != null) {
				wm.applyToPlayer(p);
			}
			DaytimeModifier dtm = (DaytimeModifier) manager.getEffect(
					DaytimeModifier.class, p.getLocation());
			if (dtm != null) {
				dtm.applyToPlayer(p);
			}
			TitleDisplayer td = (TitleDisplayer) manager.getEffect(
					TitleDisplayer.class, p.getLocation());
			if (td != null) {
				td.applyToPlayer(p);
				td.addPlayer(p, true);
			}
			PotionBuff pb = (PotionBuff) manager.getEffect(PotionBuff.class,
					p.getLocation());
			if (pb != null) {
				pb.applyToPlayer(p);
			}

		}
	}
}
