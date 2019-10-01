package com.linmalu.voicechat.data;

import com.linmalu.voicechat.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class VoicechatClient implements Runnable
{
	static final char[] passwords = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
	private Socket client;
	private DataOutputStream out;
	private int clientID;
	private byte[] key;
	private String version;
	private String password;
	private UUID player;
	public Set<UUID> callPlayers = new HashSet<>();
	public Set<UUID> players = new HashSet<>();

	public VoicechatClient(Socket client)
	{
		this.client = client;
		new Thread(this).start();
	}

	// 팀스피크 플러그인 연결
	public void run()
	{
		try
		{
			// 가짜 클라이언트 확인
			new Thread(() ->
			{
				try
				{
					Thread.sleep(1000);
					if(key == null)
					{
						client.close();
					}
				}
				catch(Exception ignored)
				{
				}
			}).start();

			out = new DataOutputStream(client.getOutputStream());
			DataInputStream in = new DataInputStream(client.getInputStream());
			ByteBuffer buffer = ByteBuffer.allocate(in.readInt());
			in.readFully(buffer.array());
			clientID = buffer.getInt();
			key = new byte[4];
			for(int i = 0; i < key.length; ++i)
			{
				key[i] = buffer.get();
			}
			buffer = ByteBuffer.allocate(in.readInt());
			in.readFully(buffer.array());
			if(buffer.getInt() == VoicechatPacketType.MESSAGE.getID())
			{
				byte[] message = new byte[buffer.limit() - buffer.position()];
				buffer.get(message);
				version = new String(message, Charset.forName("EUC-KR"));
			}
			password = "";
			Random r = new Random();
			for(int i = 0; i < 10; i++)
			{
				password += passwords[r.nextInt(passwords.length)];
			}
			sendMessage("/LinmaluVoicechat 비밀번호 " + password);
			Main.getInstance().getVoicechatClientManager().connect(this);
			Bukkit.getConsoleSender().sendMessage(Main.getInstance().getTitle() + ChatColor.GOLD + client.getInetAddress().getHostAddress() + ChatColor.YELLOW + " - 연결");
			while(true)
			{
				buffer = ByteBuffer.allocate(in.readInt());
				in.readFully(buffer.array());
				converterData(buffer.array());
				int type = buffer.getInt();
				if(type == VoicechatPacketType.ERROR_MESSAGE.getID())
				{
					byte[] message = new byte[buffer.limit() - buffer.position()];
					buffer.get(message);
					Bukkit.getConsoleSender().sendMessage(Main.getInstance().getTitle() + ChatColor.GOLD + player + ChatColor.YELLOW + new String(message, Charset.forName("EUC-KR")));
				}
				else if(type == VoicechatPacketType.PLAYER_MESSAGE.getID())
				{
					if(player != null)
					{
						Player p = Bukkit.getPlayer(player);
						if(p != null)
						{
							byte[] message = new byte[buffer.limit() - buffer.position()];
							buffer.get(message);
							p.sendMessage(Main.getInstance().getTitle() + ChatColor.YELLOW + new String(message, Charset.forName("EUC-KR")));
						}
					}
				}
				else if(type == VoicechatPacketType.JOIN_CLIENT.getID())
				{
					changeMute(buffer.getInt(), Main.getInstance().getVoicechatClientManager().isRun());
				}
			}
		}
		catch(Exception e)
		{
			// e.printStackTrace();
			Bukkit.getConsoleSender().sendMessage(Main.getInstance().getTitle() + ChatColor.GOLD + client.getInetAddress().getHostAddress() + ChatColor.YELLOW + " - 연결종료");
			Main.getInstance().getVoicechatClientManager().disconnect(this);
			close();
		}
	}

	// 메세지 보내기
	public void sendMessage(String msg)
	{
		byte[] msgs = msg.getBytes(Charset.forName("EUC-KR"));
		ByteBuffer buffer = ByteBuffer.allocate(msgs.length + 4);
		buffer.putInt(VoicechatPacketType.MESSAGE.getID());
		buffer.put(msgs);
		sendPacket(buffer.array());
	}

	// 들리는 거리 보내기
	public void setDistance(float distanceFactor, float rolloffScale)
	{
		ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(VoicechatPacketType.DISTANCE.getID());
		buffer.putFloat(distanceFactor);
		buffer.putFloat(rolloffScale);
		sendPacket(buffer.array());
	}

	// 플레이어 위치 보내기
	public void changeLocation(int clientID, Location loc)
	{
		ByteBuffer buffer;
		if(this.clientID == clientID)
		{
			buffer = ByteBuffer.allocate(28);
			buffer.putInt(VoicechatPacketType.PLAYER_LOCATION.getID());
		}
		else
		{
			buffer = ByteBuffer.allocate(20);
			buffer.putInt(VoicechatPacketType.OTHER_PLAYER_LOCATION.getID());
			buffer.putInt(clientID);
		}
		buffer.putFloat((float)loc.getX());
		buffer.putFloat((float)loc.getY());
		buffer.putFloat((float)loc.getZ());
		if(this.clientID == clientID)
		{
			Vector v = loc.getDirection();
			buffer.putFloat((float)-v.getX());
			buffer.putFloat((float)-v.getY());
			buffer.putFloat((float)-v.getZ());
		}
		sendPacket(buffer.array());
	}

	// 플레이어 음소거
	public void changeMute(int clientID, boolean mute)
	{
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putInt(mute ? VoicechatPacketType.MUTE.getID() : VoicechatPacketType.UNMUTE.getID());
		buffer.putInt(clientID);
		sendPacket(buffer.array());
	}

	// 전체 음소거
	public void changeMute(boolean mute)
	{
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(mute ? VoicechatPacketType.ALL_MUTE.getID() : VoicechatPacketType.ALL_UNMUTE.getID());
		sendPacket(buffer.array());
	}

	// 음악 재생
	public void playMusic(String msg)
	{
		byte[] msgs = msg.getBytes(Charset.forName("EUC-KR"));
		ByteBuffer buffer = ByteBuffer.allocate(msgs.length + 4);
		buffer.putInt(VoicechatPacketType.PLAY_MUSIC.getID());
		buffer.put(msgs);
		sendPacket(buffer.array());
	}

	// 음악 종료
	public void stopMusic()
	{
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(VoicechatPacketType.STOP_MUSIC.getID());
		sendPacket(buffer.array());
	}

	// 음악 위치
	public void locMusic(Location loc)
	{
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putInt(VoicechatPacketType.LOC_MUSIC.getID());
		buffer.putFloat((float)loc.getX());
		buffer.putFloat((float)loc.getY());
		buffer.putFloat((float)loc.getZ());
		sendPacket(buffer.array());
	}

	// 패킷 보내기
	private void sendPacket(byte[] data)
	{
		converterData(data);
		try
		{
			out.writeInt(data.length);
			out.write(data);
			out.flush();
		}
		catch(Exception e)
		{
			Main.getInstance().getVoicechatClientManager().disconnect(this);
			close();
		}
	}

	// 패킷 암호화 및 복호화
	private void converterData(byte[] data)
	{
		int count = 0;
		for(int i = 0; i < data.length; ++i)
		{
			data[i] = (byte)(data[i] ^ key[count]);
			count = ++count % 4;
		}
	}

	public int getClientID()
	{
		return clientID;
	}

	public String getVersion()
	{
		return version;
	}

	public boolean checkPassword(Player player, String password)
	{
		if(client.getInetAddress().getHostAddress().equals(player.getAddress().getAddress().getHostAddress()) && this.password.equals(password))
		{
			this.player = player.getUniqueId();
			return true;
		}
		return false;
	}

	public boolean isPlayer()
	{
		return player != null;
	}

	public boolean isPlayer(UUID player)
	{
		return this.player != null && this.player.equals(player);
	}

	public UUID getPlayer()
	{
		return player;
	}

	public boolean isClose()
	{
		return client.isClosed();
	}

	public void close()
	{
		try
		{
			client.close();
		}
		catch(Exception e)
		{
			// e.printStackTrace();
		}
	}
}
