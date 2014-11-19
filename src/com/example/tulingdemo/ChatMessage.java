package com.example.tulingdemo;
/**
 * 聊天记录实体类
 */
public class ChatMessage {
	
	public static final int SEND = 0;
	public static final int RECEIVER = 1;
	private String content;
	private int flag;
	private String time;
	
	public ChatMessage(String content,int flag,String time) {
		this.content = content;
		setFlag(flag);
		setTime(time);
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public void setFlag(int flag) {
		this.flag = flag;
	}
	
	public int getFlag() {
		return flag;
	}
	
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}

}
