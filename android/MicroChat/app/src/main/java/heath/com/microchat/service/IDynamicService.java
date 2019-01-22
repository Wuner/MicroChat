package heath.com.microchat.service;

import org.json.JSONObject;

public interface IDynamicService {
    String release(JSONObject parameterData) throws Exception;
    String queryDynamicByAccount(JSONObject parameterData) throws Exception;
    String praise(JSONObject parameterData) throws Exception;
    String addCommentReply(JSONObject parameterData) throws Exception;
    String queryDynamicFriendsByAccount(JSONObject parameterData) throws Exception;
    String queryDynamicByImageTextType(JSONObject parameterData) throws Exception;
    String queryDynamicNums(JSONObject parameterData) throws Exception;
    String follow(JSONObject parameterData) throws Exception;
    String queryDynamicFollowByAccount(JSONObject parameterData) throws Exception;
}
