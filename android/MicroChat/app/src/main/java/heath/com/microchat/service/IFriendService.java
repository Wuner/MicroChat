package heath.com.microchat.service;

import org.json.JSONObject;

public interface IFriendService {
    String queryFriends(JSONObject parameterData) throws Exception;
    String requstAddFriends(JSONObject parameterData) throws Exception;
    String queryReqAddNums(JSONObject parameterData) throws Exception;
    String queryFriendsNotice(JSONObject parameterData) throws Exception;
    String modifyFriendRelationshipState(JSONObject parameterData) throws Exception;
    String queryAllFriends(JSONObject parameterData) throws Exception;
    String queryFriendInfoByAccount(JSONObject parameterData) throws Exception;
    String modifyFriendRemarks(JSONObject parameterData) throws Exception;
    String delFriend(JSONObject parameterData) throws Exception;
    String updateFriend(JSONObject parameterData) throws Exception;
}
