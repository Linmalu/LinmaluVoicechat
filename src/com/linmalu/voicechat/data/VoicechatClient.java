package com.linmalu.voicechat.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.linmalu.voicechat.Main;

public class VoicechatClient implements Runnable
{
	private static final String CLIENT = "client";
	private static final String ID = "id";
	private static final String NAME = "name";

	private final GameData data = Main.getMain().getGameData();
	private Socket client;
	private int id;
	private UUID uuid;
	private String name;

	public VoicechatClient(Socket client)
	{
		this.client = client;
		new Thread(this).start();
	}
	public void run()
	{
		try
		{
			DataInputStream in = new DataInputStream(client.getInputStream());
			byte[] data = new byte[in.readInt()];
			in.readFully(data);
			JsonObject json = new JsonParser().parse(new String(data, Charset.forName("EUC-KR"))).getAsJsonObject();
			if(json.has(CLIENT) && json.has(ID) && json.has(NAME))
			{
				id = json.get(CLIENT).getAsInt();
				uuid = UUID.fromString(json.get(ID).getAsString());
				name = json.get(NAME).getAsString();
				this.data.connect(this);
				Bukkit.broadcastMessage("클라이언트 연결 - IP : " + client.getRemoteSocketAddress() + " / Port : " + client.getPort());
			}
			else
			{
				close();
			}
		}
		catch(Exception e)
		{
			// e.printStackTrace();
			close();
			data.disconnect(uuid);
		}
	}
	public void sendMessage(String msg)
	{
		try
		{
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			byte[] data = msg.getBytes(Charset.forName("EUC-KR"));
			out.writeInt(data.length);
			out.write(data);
			out.flush();
		}
		catch(Exception e)
		{
			// e.printStackTrace();
			close();
			data.disconnect(uuid);
		}
	}
	public int getID()
	{
		return id;
	}
	public UUID getUUID()
	{
		return uuid;
	}
	public String getName()
	{
		return name;
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
