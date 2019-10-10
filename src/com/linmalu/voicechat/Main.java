package com.linmalu.voicechat;

import com.linmalu.library.api.LinmaluMain;
import com.linmalu.voicechat.data.VoicechatClientManager;
import com.linmalu.voicechat.data.VoicechatServer;

public class Main extends LinmaluMain
{
	public static Main getInstance()
	{
		return (Main)LinmaluMain.getInstance();
	}

	private VoicechatClientManager manager;
	private VoicechatServer server;

	@Override
	public void onEnable()
	{
		super.onEnable();
		manager = new VoicechatClientManager();
		new MainCommand(this);
		new MainEvent(this);
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
