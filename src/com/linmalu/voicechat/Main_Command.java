package com.linmalu.voicechat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.linmalu.library.api.LinmaluTellraw;
import com.linmalu.library.api.LinmaluVersion;
import com.linmalu.voicechat.data.GameData;

public class Main_Command implements CommandExecutor
{
	private final GameData data = Main.getMain().getGameData();

	public Main_Command()
	{
		Main.getMain().getCommand(Main.getMain().getDescription().getName()).setTabCompleter(new TabCompleter()
		{
			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
			{
				ArrayList<String> list = new ArrayList<>();
				if(args.length == 1)
				{
					list.add("시작");
					list.add("중지");
					list.add("거리");
					list.add("start");
					list.add("stop");
					list.add("distance");
				}
				return list.stream().filter(msg -> msg.startsWith(args[args.length -1])).count() == 0 ? list : list.stream().filter(msg -> msg.startsWith(args[args.length -1])).collect(Collectors.toList());
			}
		});
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String args[])
	{
		if(sender.isOp())
		{
			if(args.length == 1 && (args[0].equals("시작") || args[0].equalsIgnoreCase("start")))
			{
				if(data.isDistance())
				{
					sender.sendMessage(Main.getMain().getTitle() + ChatColor.YELLOW + "이미 거리음성채팅이 시작되었습니다.");
				}
				else
				{
					data.setDistance(true);
					Bukkit.broadcastMessage(Main.getMain().getTitle() + ChatColor.GREEN + "거리음성채팅이 시작됩니다.");
				}
				return true;
			}
			else if(args.length == 1 && (args[0].equals("중지") || args[0].equalsIgnoreCase("stop")))
			{
				if(data.isDistance())
				{
					data.setDistance(false);
					Bukkit.broadcastMessage(Main.getMain().getTitle() + ChatColor.GREEN + "거리음성채팅이 중지됩니다.");
				}
				else
				{
					sender.sendMessage(Main.getMain().getTitle() + ChatColor.YELLOW + "거리음성채팅이 시작되지 않았습니다.");
				}
				return true;
			}
			else if(args.length == 2 && (args[0].equals("거리") || args[0].equalsIgnoreCase("distance")))
			{
				try
				{
					data.changeRange(Float.parseFloat(args[1]));
					Bukkit.broadcastMessage(Main.getMain().getTitle() + ChatColor.GREEN + "거리음성채팅의 거리가 설정되었습니다." + ChatColor.RESET + " - " + ChatColor.GOLD + args[1] + "m");
				}
				catch(Exception e)
				{
					sender.sendMessage(Main.getMain().getTitle() + ChatColor.YELLOW + "숫자가 입력되지 않았습니다.");
				}
				return true;
			}
			sender.sendMessage(ChatColor.GREEN + " = = = = = [ Linmalu Voicechat ] = = = = =");
			LinmaluTellraw.sendChat(sender, "/" + label + " 시작 ", ChatColor.GOLD + "/" + label + " 시작" + ChatColor.GRAY + " : 거리음성채팅시작");
			LinmaluTellraw.sendChat(sender, "/" + label + " 중지 ", ChatColor.GOLD + "/" + label + " 중지" + ChatColor.GRAY + " : 거리음성채팅중지");
			LinmaluTellraw.sendChat(sender, "/" + label + " 거리 ", ChatColor.GOLD + "/" + label + " 거리 <범위>" + ChatColor.GRAY + " : 거리음성채팅 범위 설정");
			sender.sendMessage(ChatColor.YELLOW + "제작자 : " + ChatColor.AQUA + "린마루(Linmalu)" + ChatColor.WHITE + " - http://blog.linmalu.com");
			if(sender.isOp())
			{
				LinmaluVersion.check(Main.getMain(), sender);
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
		}
		return true;
	}
}
