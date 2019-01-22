package heath.com.microchat.service.impl;

import org.json.JSONObject;

import heath.com.microchat.service.IFriendService;
import heath.com.microchat.utils.ClientUtils;
import heath.com.microchat.utils.Common;

public class FriendServiceImpl implements IFriendService {
    @Override
    public String queryFriends(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "friend/queryFriends.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String requstAddFriends(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "friend/addFriend.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryReqAddNums(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "friend/queryReqAddNums.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryFriendsNotice(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "friend/queryFriendsNotice.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String modifyFriendRelationshipState(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "friend/modifyFriendRelationshipState.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryAllFriends(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "friend/queryAllFriends.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryFriendInfoByAccount(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "friend/queryFriendInfoByAccount.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String modifyFriendRemarks(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "friend/modifyFriendRemarks.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String delFriend(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "friend/delFriend.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String updateFriend(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "friend/updateFriend.action";
        return ClientUtils.client(parameterData, url);
    }
}
