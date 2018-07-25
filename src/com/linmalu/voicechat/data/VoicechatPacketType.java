package com.linmalu.voicechat.data;

public enum VoicechatPacketType
{
	MESSAGE(1),
	ERROR_MESSAGE(2),
	PLAYER_MESSAGE(3),
	DISTANCE(100),
	PLAYER_LOCATION(101),
	OTHER_PLAYER_LOCATION(102),
	MUTE(103),
	UNMUTE(104),
	ALL_MUTE(105),
	ALL_UNMUTE(106),
	PLAY_MUSIC(110),
	STOP_MUSIC(111),
	LOC_MUSIC(112),
	JOIN_CLIENT(200),
	NONE(0);

	public static VoicechatPacketType getVoicechatType(int id)
	{
		for(VoicechatPacketType type : values())
		{
			if(type.getID() == id)
			{
				return type;
			}
		}
		return NONE;
	}

	private final int id;

	private VoicechatPacketType(int id)
	{
		this.id = id;
	}
	public int getID()
	{
		return id;
	}
}
