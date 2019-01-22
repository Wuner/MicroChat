package heath.com.microchat.entity;

public class Follow {

	private String id;
	private String account;
	private String followAccount;
	private String followTime;
	private String state;

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


	public String getFollowAccount() {
		return followAccount;
	}

	public void setFollowAccount(String followAccount) {
		this.followAccount = followAccount;
	}

	public String getFollowTime() {
		return followTime;
	}

	public void setFollowTime(String followTime) {
		this.followTime = followTime;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

}
