package org.heath.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
	
	private int id;
	private String account;
	private String fromAccount;
	private String content;
	private String sendTime;
	private String state;
	private String messageType;
	private String duration;
	private String sessionType;
	private String width;
	private String height;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getFromAccount() {
		return fromAccount;
	}
	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSendTime() {
		return sendTime;
	}
	public void setSendTime(String sendTime) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = fmt.parse(sendTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.sendTime = fmt.format(date);;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getSessionType() {
		return sessionType;
	}
	public void setSessionType(String sessionType) {
		this.sessionType = sessionType;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getGeight() {
		return height;
	}
	public void setGeight(String geight) {
		this.height = geight;
	}
	@Override
	public String toString() {
		return "Message [id=" + id + ", account=" + account + ", fromAccount=" + fromAccount + ", content=" + content
				+ ", sendTime=" + sendTime + ", state=" + state + ", messageType=" + messageType + ", duration="
				+ duration + ", sessionType=" + sessionType + ", width=" + width + ", height=" + height + "]";
	}

}
