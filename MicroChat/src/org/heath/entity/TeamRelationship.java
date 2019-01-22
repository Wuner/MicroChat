package org.heath.entity;

public class TeamRelationship {

	private String id;
	private String tid;
	private String inviter;
	private String beinviter;
	private String msg;
	private String state;
	private String delState;
	private String readState;
	private String type;
	private UserInfo userInfo;
	private Team team;

	public TeamRelationship() {
		super();
	}

	public TeamRelationship(String tid, String inviter, String beinviter, String msg, String state, String type) {
		super();
		this.tid = tid;
		this.inviter = inviter;
		this.beinviter = beinviter;
		this.msg = msg;
		this.state = state;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getInviter() {
		return inviter;
	}

	public void setInviter(String inviter) {
		this.inviter = inviter;
	}

	public String getBeinviter() {
		return beinviter;
	}

	public void setBeinviter(String beinviter) {
		this.beinviter = beinviter;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDelState() {
		return delState;
	}

	public void setDelState(String delState) {
		this.delState = delState;
	}

	public String getReadState() {
		return readState;
	}

	public void setReadState(String readState) {
		this.readState = readState;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

}
