package org.heath.entity;

public class TeamMember {
	private String id;
	private String account;
	private String extension;
	private String joinTime;
	private String teamNick;
	private String tid;
	private String type;
	private String isInTeam;
	private String isMute;
	public TeamMember() {
		super();
	}
	public TeamMember(String account, String extension, String joinTime, String teamNick, String tid, String type,
			String isInTeam, String isMute) {
		super();
		this.account = account;
		this.extension = extension;
		this.joinTime = joinTime;
		this.teamNick = teamNick;
		this.tid = tid;
		this.type = type;
		this.isInTeam = isInTeam;
		this.isMute = isMute;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getJoinTime() {
		return joinTime;
	}
	public void setJoinTime(String joinTime) {
		this.joinTime = joinTime;
	}
	public String getTeamNick() {
		return teamNick;
	}
	public void setTeamNick(String teamNick) {
		this.teamNick = teamNick;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getIsInTeam() {
		return isInTeam;
	}
	public void setIsInTeam(String isInTeam) {
		this.isInTeam = isInTeam;
	}
	public String getIsMute() {
		return isMute;
	}
	public void setIsMute(String isMute) {
		this.isMute = isMute;
	}
}
