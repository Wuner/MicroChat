package heath.com.microchat.entity;

public class CommentReply {
	private String id;
	private String dynamicId;
	private String account;
	private String accountNickname;
	private String beAccount;
	private String beAccountNickname;
	private String content;
	private String type;
	private String time;
	private String state;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDynamicId() {
		return dynamicId;
	}

	public void setDynamicId(String dynamicId) {
		this.dynamicId = dynamicId;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getAccountNickname() {
		return accountNickname;
	}

	public void setAccountNickname(String accountNickname) {
		this.accountNickname = accountNickname;
	}

	public String getBeAccount() {
		return beAccount;
	}

	public void setBeAccount(String beAccount) {
		this.beAccount = beAccount;
	}

	public String getBeAccountNickname() {
		return beAccountNickname;
	}

	public void setBeAccountNickname(String beAccountNickname) {
		this.beAccountNickname = beAccountNickname;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
