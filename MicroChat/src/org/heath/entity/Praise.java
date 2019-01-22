package org.heath.entity;

public class Praise {

	String id;
	String account;
	String dynamicId;
	String praiseTime;
	String state;

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

	public String getDynamicId() {
		return dynamicId;
	}

	public void setDynamicId(String dynamicId) {
		this.dynamicId = dynamicId;
	}

	public String getPraiseTime() {
		return praiseTime;
	}

	public void setPraiseTime(String praiseTime) {
		this.praiseTime = praiseTime;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "Praise [id=" + id + ", account=" + account + ", dynamicId=" + dynamicId + ", praiseTime=" + praiseTime
				+ ", state=" + state + "]";
	}

}
