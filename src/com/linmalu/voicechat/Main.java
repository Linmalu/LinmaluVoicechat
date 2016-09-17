package com.linmalu.voicechat;

import com.linmalu.library.api.LinmaluMain;
import com.linmalu.voicechat.data.GameData;
import com.linmalu.voicechat.data.VoicechatServer;

public class Main extends LinmaluMain
{
	public static Main getMain()
	{
		return (Main)LinmaluMain.getMain();
	}

	private GameData gamedata;
	private VoicechatServer server;

	@Override
	public void onEnable()
	{
		super.onEnable();
		gamedata = new GameData();
		registerCommand(new Main_Command());
		registerEvents(new Main_Event());
		server = new VoicechatServer();
	}
	@Override
	public void onDisable()
	{
		super.onDisable();
		server.close();
		gamedata.closePlayers();
	}
	public GameData getGameData()
	{
		return gamedata;
	}
}
