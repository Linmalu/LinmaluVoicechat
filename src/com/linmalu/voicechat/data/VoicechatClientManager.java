package com.linmalu.voicechat.data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.linmalu.voicechat.Main;

public class VoicechatClientManager
{
	private boolean run = false;
	private float distance = 20;
	private final Set<VoicechatClient> clients = new HashSet<>();

	// 음성거리채팅 설정
	public void setRun(boolean run)
	{
		this.run = run;
		for(VoicechatClient vc : clients)
		{
			vc.changeMute(run);
			Player player = Bukkit.getPlayer(vc.getPlayer());
			if(player != null)
			{
				joinPlayer(player);
			}
		}
	}
	// 음성채팅 거리 변경
	public void changeDistance(float distance)
	{
		this.distance = distance;
		for(VoicechatClient vc : clients)
		{
			vc.setDistance(distance, 0);
		}
	}
	// 클라이언트 접속확인
	public void checkClient()
	{
		for(Iterator<VoicechatClient> it = clients.iterator(); it.hasNext();)
		{
			if(it.next().isClose())
			{
				it.remove();
			}
		}
	}
	// 팀스피크플러그인 서버연결
	public void connect(VoicechatClient vc)
	{
		checkClient();
		clients.add(vc);
		vc.setDistance(distance, 0);
		vc.changeMute(run);
		vc.changeLocation(vc.getClientID(), new Location(null, 0, 0, 0, 0, 0));
	}
	// 팀스피크플러그인 서버연결종료
	public void disconnect(VoicechatClient vc)
	{
		if(vc.isPlayer())
		{
			String name = Bukkit.getOfflinePlayer(vc.getPlayer()).getName();
			UUID uuid = cancelPlayer(vc.getPlayer());
			if(uuid != null)
			{
				Player player = Bukkit.getPlayer(uuid);
				if(player != null)
				{
					player.sendMessage(Main.getMain().getTitle() + ChatColor.GOLD + name + ChatColor.YELLOW + "님이 통화신청을 취소했습니다.");
				}
			}
			for(UUID id : vc.callPlayers)
			{
				Player player = Bukkit.getPlayer(id);
				if(player != null)
				{
					player.sendMessage(Main.getMain().getTitle() + ChatColor.GOLD + name + ChatColor.YELLOW + "님이 통화신청을 취소했습니다.");
				}
			}
			Set<UUID> set = quitClient(vc.getPlayer());
			if(set != null)
			{
				String msg;
				if(set.size() > 1)
				{
					msg = ChatColor.GREEN + "통화중 : " + ChatColor.GOLD + set.stream().map(id -> Bukkit.getOfflinePlayer(id).getName()).collect(Collectors.joining(", "));
				}
				else
				{
					msg = ChatColor.GREEN + "통화가 종료되었습니다.";
				}
				for(UUID id : set)
				{
					Player player2 = Bukkit.getPlayer(id);
					if(player2 != null)
					{
						player2.sendMessage(Main.getMain().getTitle() + ChatColor.GOLD + name + ChatColor.GREEN + "님이 통화를 종료했습니다.");
						player2.sendMessage(Main.getMain().getTitle() + msg);
					}
				}
			}
			Player player = Bukkit.getPlayer(vc.getPlayer());
			if(player != null)
			{
				player.sendMessage(Main.getMain().getTitle() + ChatColor.YELLOW + "음성채팅 클라이언트가 종료되었습니다.");
			}
		}
		checkClient();
		clients.remove(vc);
	}
	// 음성거리채팅 작동확인
	public boolean isRun()
	{
		return run;
	}
	// 비밀번호 확인
	public boolean checkPassword(Player player, String password)
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer(player.getUniqueId()))
			{
				return false;
			}
		}
		for(VoicechatClient vc : clients)
		{
			if(!vc.isPlayer() && vc.checkPassword(player, password))
			{
				vc.sendMessage("[b]" + player.getName() + "[/b]플레이어와 연결되었습니다.");
				joinPlayer(player);
				return true;
			}
		}
		return false;
	}
	// 플레이어 접속
	public void joinPlayer(Player player)
	{
		changeLocation(player.getUniqueId(), player.getLocation());
		for(VoicechatClient vc1 : clients)
		{
			if(vc1.isPlayer(player.getUniqueId()))
			{
				for(VoicechatClient vc2 : clients)
				{
					if(vc2.isPlayer())
					{
						Player p = Bukkit.getPlayer(vc2.getPlayer());
						if(p != null)
						{
							vc1.changeMute(vc2.getClientID(), false);
							vc2.changeMute(vc1.getClientID(), false);
						}
					}
				}
				break;
			}
		}
	}
	// 플레이어 종료
	public void quitPlayer(Player player)
	{
		for(VoicechatClient vc1 : clients)
		{
			if(vc1.isPlayer(player.getUniqueId()))
			{
				vc1.changeMute(run);
				for(VoicechatClient vc2 : clients)
				{
					vc2.changeMute(vc1.getClientID(), run);
				}
				break;
			}
		}
	}
	// 플레이어 위치변경
	public void changeLocation(UUID player, Location loc)
	{
		for(VoicechatClient vc1 : clients)
		{
			if(vc1.isPlayer(player))
			{
				for(VoicechatClient vc2 : clients)
				{
					vc2.changeLocation(vc1.getClientID(), !run || vc2.players.contains(player) ? new Location(null, 0, 0, 0, 0, 0) : loc);
				}
				break;
			}
		}
	}
	// 음악 재생
	public void playMusic(Player player, String msg)
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer(player.getUniqueId()))
			{
				vc.playMusic(msg);
				vc.locMusic(player.getLocation());
				break;
			}
		}
	}
	// 음악 종료
	public void stopMusic(UUID player)
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer(player))
			{
				vc.stopMusic();
				break;
			}
		}
	}
	// 플레이어 확인
	public boolean isPlayer(UUID player)
	{
		for(VoicechatClient client : clients)
		{
			if(client.isPlayer(player))
			{
				return true;
			}
		}
		return false;
	}
	// 통화 신청
	public boolean callPlayer(UUID player1, UUID player2)
	{
		for(VoicechatClient vc1 : clients)
		{
			if(vc1.isPlayer(player1))
			{
				for(VoicechatClient vc2 : clients)
				{
					if(vc2.isPlayer(player2))
					{
						if(vc1.callPlayers.contains(player2))
						{
							Set<UUID> set = new HashSet<>(vc1.players);
							set.addAll(vc2.players);
							set.add(vc1.getPlayer());
							set.add(vc2.getPlayer());
							for(VoicechatClient vc3 : clients)
							{
								if(vc3.isPlayer() && set.contains(vc3.getPlayer()))
								{
									vc3.callPlayers.removeAll(set);
									vc3.players.addAll(set);
									vc3.players.remove(vc3.getPlayer());
								}
							}
							refreshPlayer();
							return true;
						}
						else
						{
							vc2.callPlayers.add(player1);
						}
					}
				}
			}
		}
		return false;
	}
	// 통화 신청 취소
	public UUID cancelPlayer(UUID player)
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer() && vc.callPlayers.remove(player))
			{
				return vc.getPlayer();
			}
		}
		return null;
	}
	// 통화 신청 거절
	public boolean refusePlayer(UUID player1, UUID player2)
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer(player1))
			{
				return vc.callPlayers.remove(player2);
			}
		}
		return false;
	}
	// 통화 종료
	public Set<UUID> quitClient(UUID player)
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer(player) && vc.players.size() > 0)
			{
				for(VoicechatClient vc1 : clients)
				{
					if(vc1.isPlayer())
					{
						vc1.players.remove(player);
					}
				}
				Set<UUID> set = new HashSet<>(vc.players);
				vc.players.clear();
				refreshPlayer();
				return set;
			}
		}
		return null;
	}
	// 통화 신청 플레이어
	public UUID getCallPlayer(UUID player)
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer() && !vc.getPlayer().equals(player) && vc.callPlayers.contains(player))
			{
				return vc.getPlayer();
			}
		}
		return null;
	}
	// 통화 신청한 플레이어
	public Set<UUID> getCallPlayers(UUID player)
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer(player))
			{
				return new HashSet<>(vc.callPlayers);
			}
		}
		return null;
	}
	// 통화 신청 가능 플레이어
	public Set<String> getCallPlayerNames(UUID player)
	{
		Set<String> set = new HashSet<>();
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer() && !vc.getPlayer().equals(player))
			{
				Player p = Bukkit.getPlayer(vc.getPlayer());
				if(player != null)
				{
					set.add(p.getName());
				}
			}
		}
		set.removeAll(getPlayers(player).stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.toSet()));
		return set;
	}
	// 통화 거절 가능 플레이어
	public Set<String> getRefusePlayerNames(UUID player)
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer(player))
			{
				return vc.callPlayers.stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.toSet());
			}
		}
		return null;
	}
	// 통화 목록
	public Set<UUID> getPlayers(UUID player)
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer(player))
			{
				return new HashSet<UUID>(vc.players);
			}
		}
		return null;
	}
	// 통화 변경
	private void refreshPlayer()
	{
		for(VoicechatClient vc : clients)
		{
			if(vc.isPlayer())
			{
				Player player = Bukkit.getPlayer(vc.getPlayer());
				if(player != null)
				{
					changeLocation(vc.getPlayer(), player.getLocation());
				}
			}
		}
	}
	// 정리
	public void clear()
	{
		for(Iterator<VoicechatClient> it = clients.iterator(); it.hasNext();)
		{
			it.next().close();
			it.remove();
		}
	}
}
