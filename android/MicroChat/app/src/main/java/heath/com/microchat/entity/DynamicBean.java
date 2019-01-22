package heath.com.microchat.entity;

import java.util.List;

public class DynamicBean {

    private String id;
    private String account;
    private String type;
    private String content;
    private String path;
    private String releaseTime;
    private String praiseNums;
    private String state;
    private UserInfo userInfo;
    private List<Praise> praises;
    private List<Follow> follows;
    private List<CommentReply> commentReplys;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getPraiseNums() {
        return praiseNums;
    }

    public void setPraiseNums(String praiseNums) {
        this.praiseNums = praiseNums;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<Praise> getPraises() {
        return praises;
    }

    public void setPraises(List<Praise> praises) {
        this.praises = praises;
    }

    public List<Follow> getFollows() {
        return follows;
    }

    public void setFollows(List<Follow> follows) {
        this.follows = follows;
    }

    public List<CommentReply> getCommentReplys() {
        return commentReplys;
    }

    public void setCommentReplys(List<CommentReply> commentReplys) {
        this.commentReplys = commentReplys;
    }

}
