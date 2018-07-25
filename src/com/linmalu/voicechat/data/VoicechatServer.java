package com.linmalu.voicechat.data;

import java.net.ServerSocket;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.linmalu.voicechat.Main;

public class VoicechatServer implements Runnable
{
	private ServerSocket server;

	public VoicechatServer()
	{
		new Thread(this).start();
	}
	public void run()
	{
		try(ServerSocket server = new ServerSocket(23456))
		{
			this.server = server;
			while(true)
			{
				new VoicechatClient(server.accept());
			}
		}
		catch(Exception e)
		{
			Bukkit.getConsoleSender().sendMessage(Main.getMain().getTitle() + ChatColor.YELLOW + "에러가 발생되어 종료됩니다.");
		}
	}
	public void close()
	{
		try
		{
			server.close();
		}
		catch(Exception e)
		{
		}
	}
}
