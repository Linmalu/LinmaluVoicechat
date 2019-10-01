package com.linmalu.voicechat;

import com.linmalu.library.api.LinmaluEvent;
import com.linmalu.library.api.LinmaluMain;
import com.linmalu.library.api.LinmaluServer;
import com.linmalu.voicechat.data.VoicechatClientManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Main_Event extends LinmaluEvent
{
	private final VoicechatClientManager vcm = Main.getInstance().getVoicechatClientManager();

	public Main_Event(LinmaluMain main)
	{
		super(main);
	}

	@EventHandler
	public void event(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if(player.isOp())
		{
			LinmaluServer.version(_main, player);
		}
		vcm.joinPlayer(player);
	}

	@EventHandler
	public void event(PlayerQuitEvent event)
	{
		vcm.quitPlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void event(PlayerTeleportEvent event)
	{
		if(vcm.isRun())
		{
			vcm.changeLocation(event.getPlayer().getUniqueId(), event.getTo());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void event(PlayerMoveEvent event)
	{
		if(vcm.isRun())
		{
			vcm.changeLocation(event.getPlayer().getUniqueId(), event.getTo());
		}
	}
}
