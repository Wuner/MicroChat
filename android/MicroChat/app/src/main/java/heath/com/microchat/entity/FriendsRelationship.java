package heath.com.microchat.entity;

import java.io.Serializable;

public class FriendsRelationship implements Serializable {

    private int id;
    private String account;
    private String fromAccount;
    private String state;
    private String content;
    private UserInfo userInfo;

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

    public String getFrom_account() {
        return fromAccount;
    }

    public void setFrom_account(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

}
