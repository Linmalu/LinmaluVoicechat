package com.linmalu.voicechat;

import com.linmalu.library.api.LinmaluMain;
import com.linmalu.voicechat.data.VoicechatClientManager;
import com.linmalu.voicechat.data.VoicechatServer;

public class Main extends LinmaluMain
{
	public static Main getMain()
	{
		return (Main)LinmaluMain.getMain();
	}

	private VoicechatClientManager manager;
	private VoicechatServer server;

	@Override
	public void onEnable()
	{
		super.onEnable();
		manager = new VoicechatClientManager();
		registerCommand(new Main_Command());
		registerEvents(new Main_Event());
		server = new VoicechatServer();
	}
	@Override
	public void onDisable()
	{
		super.onDisable();
		manager.clear();
		server.close();
	}
	public VoicechatClientManager getVoicechatClientManager()
	{
		return manager;
	}
}
