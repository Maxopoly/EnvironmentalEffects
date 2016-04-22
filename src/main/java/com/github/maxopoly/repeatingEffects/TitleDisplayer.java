package com.github.maxopoly.repeatingEffects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PlayerConnection;

import com.github.maxopoly.datarepresentations.Area;
import com.github.maxopoly.datarepresentations.PlayerEnvironmentState;

public class TitleDisplayer extends RepeatingEffect {
	private String title;
	private String subtitle;
	private int fadeIn;
	private int stay;
	private int fadeOut;
	private HashMap<UUID, Boolean> alreadyShownToPlayer;

	public TitleDisplayer(LinkedList<Area> includedAreas, LinkedList <Area> excludedAreas,
			long updateTime, PlayerEnvironmentState pes, String title,
			String subtitle, int fadeIn, int stay, int fadeOut) {
		super(includedAreas,excludedAreas, updateTime, pes);
		this.title = title;
		this.subtitle = subtitle;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
		alreadyShownToPlayer = new HashMap<UUID, Boolean>();
	}

	public void applyToPlayer(Player p) {
		UUID uuid = p.getUniqueId();
		if (conditionsMet(p)) {
			if (!alreadyShownToPlayer.get(uuid)) {
				sendTitle(p);
				alreadyShownToPlayer.put(uuid, true);
			}
		} else {
			alreadyShownToPlayer.put(uuid, false);
		}
	}

	public void sendTitle(Player p) {
		PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		PacketPlayOutTitle packet = new PacketPlayOutTitle(
				PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay,
				fadeOut);
		connection.sendPacket(packet);
		IChatBaseComponent sub = IChatBaseComponent.ChatSerializer
				.a("{\"text\": \"" + subtitle + "\"}");
		packet = new PacketPlayOutTitle(
				PacketPlayOutTitle.EnumTitleAction.SUBTITLE, sub);
		connection.sendPacket(packet);
		IChatBaseComponent main = IChatBaseComponent.ChatSerializer
				.a("{\"text\": \"" + title + "\"}");
		packet = new PacketPlayOutTitle(
				PacketPlayOutTitle.EnumTitleAction.TITLE, main);
		connection.sendPacket(packet);
	}

	public void addPlayer(Player p, boolean initialValue) {
		alreadyShownToPlayer.put(p.getUniqueId(), initialValue);
	}

}
