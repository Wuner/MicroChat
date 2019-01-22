package org.heath.entity;

public class User {
	private int id;
	private String account;
	private String bindMobile;
	private String token;
	private String password;
	private String registerTime;
	private String state;
	private String prohibitionTime;
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
	public String getBindMobile() {
		return bindMobile;
	}
	public void setBindMobile(String bindMobile) {
		this.bindMobile = bindMobile;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getRegisterTime() {
		return registerTime;
	}
	public void setRegisterTime(String registerTime) {
		this.registerTime = registerTime;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getProhibitionTime() {
		return prohibitionTime;
	}
	public void setProhibitionTime(String prohibitionTime) {
		this.prohibitionTime = prohibitionTime;
	}
}
