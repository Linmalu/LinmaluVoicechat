package com.linmalu.voicechat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.linmalu.library.api.LinmaluServer;
import com.linmalu.voicechat.data.VoicechatClientManager;

public class Main_Event implements Listener
{
	private final VoicechatClientManager vcm = Main.getMain().getVoicechatClientManager();

	@EventHandler
	public void Event(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if(player.isOp())
		{
			LinmaluServer.version(Main.getMain(), player);
		}
		vcm.joinPlayer(player);
	}
	@EventHandler
	public void Event(PlayerQuitEvent event)
	{
		vcm.quitPlayer(event.getPlayer());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void Event(PlayerTeleportEvent event)
	{
		if(vcm.isRun())
		{
			vcm.changeLocation(event.getPlayer().getUniqueId(), event.getTo());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void Event(PlayerMoveEvent event)
	{
		if(vcm.isRun())
		{
			vcm.changeLocation(event.getPlayer().getUniqueId(), event.getTo());
		}
	}
}
