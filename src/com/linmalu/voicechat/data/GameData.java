package com.linmalu.voicechat.data;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.google.gson.JsonObject;
import com.linmalu.voicechat.Main;

public class GameData
{
	private final HashMap<UUID, VoicechatClient> players = new HashMap<>();
	private boolean distance = false;
	private float range = 20;

	public void connect(VoicechatClient vc)
	{
		Bukkit.broadcastMessage(Main.getMain().getTitle() + ChatColor.GOLD + vc.getName() + ChatColor.GREEN + "님이 연결되었습니다.");
		synchronized(players)
		{
			players.put(vc.getUUID(), vc);
		}
		if(distance)
		{
			Bukkit.getOnlinePlayers().forEach(player ->
			{
				changeRange(range);
				changeLocation(player.getUniqueId(), player.getLocation());
			});
		}
	}
	public void disconnect(UUID uuid)
	{
		synchronized(players)
		{
			players.remove(uuid);
		}
	}
	public void changeLocation(UUID uuid, Location loc)
	{
		synchronized(players)
		{
			if(players.containsKey(uuid))
			{
				loc = loc.clone();
				JsonObject json = new JsonObject();
				json.addProperty("id", players.get(uuid).getID());
				json.addProperty("x1", loc.getX());
				json.addProperty("y1", loc.getY());
				json.addProperty("z1", loc.getZ());
				loc.setPitch(0);
				Vector v = loc.getDirection();
				json.addProperty("x2", -v.getX());
				json.addProperty("y2", -v.getY());
				json.addProperty("z2", -v.getZ());
				players.entrySet().forEach(data ->
				{
					if(uuid.equals(data.getKey()))
					{
						json.addProperty("type", 1);
					}
					else
					{
						json.addProperty("type", 2);
					}
					data.getValue().sendMessage(json.toString());
				});
			}
		}
	}
	public boolean isDistance()
	{
		return distance;
	}
	public void setDistance(boolean distance)
	{
		this.distance = distance;
		Bukkit.getOnlinePlayers().forEach(player ->
		{
			Location loc;
			if(distance)
			{
				loc = player.getLocation();
				changeRange(range);
			}
			else
			{
				loc = new Location(player.getWorld(), 0, 0, 0, 0, 0);
			}
			changeLocation(player.getUniqueId(), loc);
		});
	}
	public void closePlayers()
	{
		synchronized(players)
		{
			players.entrySet().forEach(data ->
			{
				data.getValue().close();
			});
			players.clear();
		}
	}
	public void changeRange(float range)
	{
		this.range = range;
		synchronized(players)
		{
			JsonObject json = new JsonObject();
			json.addProperty("type", 0);
			json.addProperty("distanceFactor", 20 / range);
			json.addProperty("rolloffScale", 0);
			players.entrySet().forEach(data ->
			{
				data.getValue().sendMessage(json.toString());
			});
		}
	}
}
