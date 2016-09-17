package com.linmalu.voicechat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.linmalu.library.api.LinmaluVersion;
import com.linmalu.voicechat.data.GameData;

public class Main_Event implements Listener
{
	private final GameData data = Main.getMain().getGameData();

	@EventHandler
	public void Event(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if(player.isOp())
		{
			LinmaluVersion.check(Main.getMain(), player);
		}
		if(data.isDistance())
		{
			data.changeLocation(player.getUniqueId(), player.getLocation());
		}
	}
	@EventHandler
	public void Event(PlayerQuitEvent event)
	{
		if(data.isDistance())
		{
			Player player = event.getPlayer();
			data.changeLocation(player.getUniqueId(), new Location(player.getWorld(), 0, -1000, 0, 0, 0));
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void Event(PlayerTeleportEvent event)
	{
		if(data.isDistance())
		{
			data.changeLocation(event.getPlayer().getUniqueId(), event.getTo());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void Event(PlayerMoveEvent event)
	{
		if(data.isDistance())
		{
			data.changeLocation(event.getPlayer().getUniqueId(), event.getTo());
		}
	}
}
