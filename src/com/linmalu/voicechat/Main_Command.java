package com.linmalu.voicechat;

import com.linmalu.library.api.LinmaluCommand;
import com.linmalu.library.api.LinmaluMain;
import com.linmalu.library.api.LinmaluServer;
import com.linmalu.library.api.LinmaluTellraw;
import com.linmalu.voicechat.data.VoicechatClientManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Main_Command extends LinmaluCommand
{
	private final VoicechatClientManager vcm = Main.getInstance().getVoicechatClientManager();

	public Main_Command(LinmaluMain main)
	{
		super(main);
	}

	@Override
	protected List<String> TabCompleter(CommandSender sender, Command command, String alias, String[] args)
	{
		ArrayList<String> list = new ArrayList<>();
		if(args.length == 1)
		{
			if(sender.isOp())
			{
				list.add("시작");
				list.add("종료");
				list.add("거리");
				list.add("start");
				list.add("stop");
				list.add("distance");
			}
			if(sender instanceof Player)
			{
				list.add("비밀번호");
				list.add("통화신청");
				list.add("통화신청취소");
				list.add("통화신청거절");
				list.add("통화종료");
				list.add("통화신청목록");
				list.add("통화목록");
				list.add("음악재생");
				list.add("음악종료");
			}
		}
		else if(args.length == 2 && sender instanceof Player)
		{
			UUID player = ((Player)sender).getUniqueId();
			if(args[0].equals("통화신청"))
			{
				list.addAll(vcm.getCallPlayerNames(player));
			}
			else if(args[0].equals("통화신청거절"))
			{
				list.addAll(vcm.getRefusePlayerNames(player));
			}
		}
		return list;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(sender.isOp())
		{
			if(args.length == 1 && (args[0].equals("시작") || args[0].equalsIgnoreCase("start")))
			{
				if(vcm.isRun())
				{
					sender.sendMessage(_main.getTitle() + ChatColor.YELLOW + "이미 거리음성채팅이 시작되었습니다.");
				}
				else
				{
					vcm.setRun(true);
					Bukkit.broadcastMessage(_main.getTitle() + ChatColor.GREEN + "거리음성채팅이 시작됩니다.");
				}
				return true;
			}
			else if(args.length == 1 && (args[0].equals("종료") || args[0].equalsIgnoreCase("stop")))
			{
				if(vcm.isRun())
				{
					vcm.setRun(false);
					Bukkit.broadcastMessage(_main.getTitle() + ChatColor.GREEN + "거리음성채팅이 종료됩니다.");
				}
				else
				{
					sender.sendMessage(_main.getTitle() + ChatColor.YELLOW + "거리음성채팅이 시작되지 않았습니다.");
				}
				return true;
			}
			else if(args.length == 2 && (args[0].equals("거리") || args[0].equalsIgnoreCase("distance")))
			{
				try
				{
					vcm.changeDistance(Float.parseFloat(args[1]));
					Bukkit.broadcastMessage(_main.getTitle() + ChatColor.GREEN + "거리음성채팅의 거리가 설정되었습니다." + ChatColor.RESET + " - " + ChatColor.GOLD + args[1] + "m");
				}
				catch(Exception e)
				{
					sender.sendMessage(_main.getTitle() + ChatColor.YELLOW + "숫자가 입력되지 않았습니다.");
				}
				return true;
			}
		}
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(args.length == 2 && args[0].equals("비밀번호"))
			{
				if(vcm.checkPassword(player, args[1]))
				{
					player.sendMessage(_main.getTitle() + ChatColor.GREEN + "비밀번호가 확인되었습니다.");
					Bukkit.getConsoleSender().sendMessage(_main.getTitle() + ChatColor.GREEN + "비밀번호가 확인되었습니다.");
				}
				else
				{
					player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "비밀번호가 존재하지 않습니다.");
				}
				return true;
			}
			else if(args.length == 2 && args[0].equals("통화신청"))
			{
				Player player1 = Bukkit.getPlayer(args[1]);
				if(player1 == null)
				{
					player.sendMessage(_main.getTitle() + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "님이 접속중이 아닙니다.");
				}
				else if(player.getUniqueId().equals(player1.getUniqueId()))
				{
					player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "자신에게 통화신청을 보낼 수 없습니다.");
				}
				else if(!vcm.isPlayer(player.getUniqueId()))
				{
					player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "음성채팅을 사용하고 있지 않습니다.");
				}
				else if(!vcm.isPlayer(player1.getUniqueId()))
				{
					player.sendMessage(_main.getTitle() + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "님이 음성채팅을 사용하고 있지 않습니다.");
				}
				else
				{
					Set<UUID> set = vcm.getPlayers(player.getUniqueId());
					if(set != null && set.contains(player1.getUniqueId()))
					{
						player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "이미 통화에 참여중입니다.");
					}
					else
					{
						UUID uuid = vcm.cancelPlayer(player.getUniqueId());
						if(uuid != null)
						{
							player.sendMessage(_main.getTitle() + ChatColor.GOLD + Bukkit.getOfflinePlayer(uuid).getName() + ChatColor.GREEN + "님에게 통화신청을 취소했습니다.");
							Player player2 = Bukkit.getPlayer(uuid);
							if(player2 != null)
							{
								player.sendMessage(_main.getTitle() + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "님이 통화신청을 취소했습니다.");
							}
						}
						if(vcm.callPlayer(player.getUniqueId(), player1.getUniqueId()))
						{
							set = vcm.getPlayers(player.getUniqueId());
							if(set != null)
							{
								set.add(player.getUniqueId());
								String msg = set.stream().map(id -> Bukkit.getOfflinePlayer(id).getName()).collect(Collectors.joining(", "));
								for(UUID id : set)
								{
									Player player2 = Bukkit.getPlayer(id);
									if(player2 != null)
									{
										player2.sendMessage(_main.getTitle() + ChatColor.GREEN + "통화인원이 변경되었습니다.");
										player2.sendMessage(_main.getTitle() + ChatColor.GREEN + "통화중 : " + ChatColor.GOLD + msg);
									}
								}
							}
							else
							{
								player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "통화인원을 불러오지 못했습니다.");
							}
						}
						else
						{
							player.sendMessage(_main.getTitle() + ChatColor.GOLD + args[1] + ChatColor.GREEN + "님에게 통화신청을 했습니다.");
							player1.sendMessage(_main.getTitle() + ChatColor.GOLD + player.getName() + ChatColor.GREEN + "님에게 통화신청이 왔습니다.");
						}
					}
				}
				return true;
			}
			else if(args.length == 1 && args[0].equals("통화신청취소"))
			{
				if(vcm.isPlayer(player.getUniqueId()))
				{
					UUID uuid = vcm.cancelPlayer(player.getUniqueId());
					if(uuid != null)
					{
						player.sendMessage(_main.getTitle() + ChatColor.GOLD + Bukkit.getOfflinePlayer(uuid).getName() + ChatColor.GREEN + "님에게 통화신청을 취소했습니다.");
						Player player1 = Bukkit.getPlayer(uuid);
						if(player1 != null)
						{
							player1.sendMessage(_main.getTitle() + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + "님이 통화신청을 취소했습니다.");
						}
					}
					else
					{
						player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "통화신청이 없습니다.");
					}
				}
				else
				{
					player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "음성채팅을 사용하고 있지 않습니다.");
				}
				return true;
			}
			else if(args.length == 2 && args[0].equals("통화신청거절"))
			{
				Player player1 = Bukkit.getPlayer(args[1]);
				if(player1 == null)
				{
					player.sendMessage(_main.getTitle() + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "님이 접속중이 아닙니다.");
				}
				else if(player.getUniqueId().equals(player1.getUniqueId()))
				{
					player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "자신에게 통화신청을 보낼 수 없습니다.");
				}
				else if(!vcm.isPlayer(player.getUniqueId()))
				{
					player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "음성채팅을 사용하고 있지 않습니다.");
				}
				else if(!vcm.isPlayer(player1.getUniqueId()))
				{
					player.sendMessage(_main.getTitle() + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "님이 음성채팅을 사용하고 있지 않습니다.");
				}
				else if(vcm.refusePlayer(player.getUniqueId(), player1.getUniqueId()))
				{
					player.sendMessage(_main.getTitle() + ChatColor.GOLD + args[1] + ChatColor.GREEN + "님의 통화신청을 거절했습니다.");
					player1.sendMessage(_main.getTitle() + ChatColor.GOLD + player.getName() + ChatColor.GREEN + "님의 통화신청을 거절했습니다.");
				}
				else
				{
					player.sendMessage(_main.getTitle() + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "님이 통화신청을 하지 않았습니다.");
				}
				return true;
			}
			else if(args.length == 1 && args[0].equals("통화종료"))
			{
				if(vcm.isPlayer(player.getUniqueId()))
				{
					Set<UUID> set = vcm.quitClient(player.getUniqueId());
					if(set != null)
					{
						player.sendMessage(_main.getTitle() + ChatColor.GREEN + "통화를 종료했습니다.");
						String msg;
						if(set.size() > 1)
						{
							msg = ChatColor.GREEN + "통화중 : " + ChatColor.GOLD + set.stream().map(id -> Bukkit.getOfflinePlayer(id).getName()).collect(Collectors.joining(", "));
						}
						else
						{
							msg = ChatColor.GREEN + "통화가 종료되었습니다.";
						}
						for(UUID uuid : set)
						{
							Player player2 = Bukkit.getPlayer(uuid);
							if(player2 != null)
							{
								player2.sendMessage(_main.getTitle() + ChatColor.GOLD + player.getName() + ChatColor.GREEN + "님이 통화를 종료했습니다.");
								player2.sendMessage(_main.getTitle() + msg);
							}
						}
					}
					else
					{
						player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "통화중이 아닙니다.");
					}
				}
				else
				{
					player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "음성채팅을 사용하고 있지 않습니다.");
				}
				return true;
			}
			else if(args.length == 1 && args[0].equals("통화신청목록"))
			{
				if(vcm.isPlayer(player.getUniqueId()))
				{
					UUID uuid = vcm.getCallPlayer(player.getUniqueId());
					player.sendMessage(_main.getTitle() + ChatColor.GREEN + "통화신청 : " + ChatColor.GOLD + (uuid == null ? "없음" : Bukkit.getOfflinePlayer(uuid).getName()));
					Set<UUID> set = vcm.getCallPlayers(player.getUniqueId());
					player.sendMessage(_main.getTitle() + ChatColor.GREEN + "통화신청목록 : " + ChatColor.GOLD + (set != null && set.size() > 0 ? set.stream().map(id -> Bukkit.getOfflinePlayer(id).getName()).collect(Collectors.joining(", ")) : "없음"));
				}
				else
				{
					player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "음성채팅을 사용하고 있지 않습니다.");
				}
				return true;
			}
			else if(args.length == 1 && args[0].equals("통화목록"))
			{
				if(vcm.isPlayer(player.getUniqueId()))
				{
					Set<UUID> set = vcm.getPlayers(player.getUniqueId());
					if(set != null && set.size() > 0)
					{
						set.add(player.getUniqueId());
						player.sendMessage(_main.getTitle() + ChatColor.GREEN + "통화중 : " + ChatColor.GOLD + set.stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.joining(", ")));
					}
					else
					{
						player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "통화중이 아닙니다.");
					}
				}
				else
				{
					player.sendMessage(_main.getTitle() + ChatColor.YELLOW + "음성채팅을 사용하고 있지 않습니다.");
				}
				return true;
			}
			else if(args.length == 2 && args[0].equals("음악재생"))
			{
				vcm.playMusic(player, args[1]);
				return true;
			}
			else if(args.length == 1 && args[0].equals("음악종료"))
			{
				vcm.stopMusic(player.getUniqueId());
				return true;
			}
		}
		sender.sendMessage(ChatColor.GREEN + " = = = = = [ Linmalu Voicechat ] = = = = =");
		if(sender.isOp())
		{
			LinmaluTellraw.sendChat(sender, "/" + label + " 시작 ", ChatColor.GOLD + "/" + label + " 시작" + ChatColor.GRAY + " : 거리음성채팅시작");
			LinmaluTellraw.sendChat(sender, "/" + label + " 중지 ", ChatColor.GOLD + "/" + label + " 중지" + ChatColor.GRAY + " : 거리음성채팅중지");
			LinmaluTellraw.sendChat(sender, "/" + label + " 거리 ", ChatColor.GOLD + "/" + label + " 거리 <범위>" + ChatColor.GRAY + " : 거리음성채팅 범위 설정");
		}
		if(sender instanceof Player)
		{
			LinmaluTellraw.sendChat(sender, "/" + label + " 비밀번호 ", ChatColor.GOLD + "/" + label + " 비밀번호 <비밀번호>" + ChatColor.GRAY + " : 비밀번호입력");
			LinmaluTellraw.sendChat(sender, "/" + label + " 통화신청 ", ChatColor.GOLD + "/" + label + " 통화신청 <플레이어>" + ChatColor.GRAY + " : 통화신청");
			LinmaluTellraw.sendChat(sender, "/" + label + " 통화신청취소 ", ChatColor.GOLD + "/" + label + " 통화신청취소" + ChatColor.GRAY + " : 통화신청취소");
			LinmaluTellraw.sendChat(sender, "/" + label + " 통화신청거절 ", ChatColor.GOLD + "/" + label + " 통화신청거절 <플레이어>" + ChatColor.GRAY + " : 통화신청거절");
			LinmaluTellraw.sendChat(sender, "/" + label + " 통화종료 ", ChatColor.GOLD + "/" + label + " 통화종료" + ChatColor.GRAY + " : 통화종료");
			LinmaluTellraw.sendChat(sender, "/" + label + " 통화신청목록 ", ChatColor.GOLD + "/" + label + " 통화신청목록" + ChatColor.GRAY + " : 통화신청목록");
			LinmaluTellraw.sendChat(sender, "/" + label + " 통화목록 ", ChatColor.GOLD + "/" + label + " 통화목록" + ChatColor.GRAY + " : 통화목록");
			LinmaluTellraw.sendChat(sender, "/" + label + " 음악재생 ", ChatColor.GOLD + "/" + label + " 음악재생 <경로>" + ChatColor.GRAY + " : 컴퓨터음악재생");
			LinmaluTellraw.sendChat(sender, "/" + label + " 음악종료 ", ChatColor.GOLD + "/" + label + " 음악종료" + ChatColor.GRAY + " : 컴퓨터음악종료");
		}
		sender.sendMessage(ChatColor.YELLOW + "제작자 : " + ChatColor.AQUA + "린마루(Linmalu)" + ChatColor.WHITE + " - http://blog.linmalu.com");
		if(sender.isOp())
		{
			LinmaluServer.version(_main, sender);
		}
		return true;
	}


}
