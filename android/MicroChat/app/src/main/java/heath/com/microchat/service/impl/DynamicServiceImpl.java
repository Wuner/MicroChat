package heath.com.microchat.service.impl;

import org.json.JSONObject;

import heath.com.microchat.service.IDynamicService;
import heath.com.microchat.utils.ClientUtils;
import heath.com.microchat.utils.Common;

public class DynamicServiceImpl implements IDynamicService {
    @Override
    public String release(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "dynamic/release.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryDynamicByAccount(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "dynamic/queryDynamicByAccount.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String praise(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "dynamic/praise.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String addCommentReply(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "dynamic/addCommentReply.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryDynamicFriendsByAccount(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "dynamic/queryDynamicFriendsByAccount.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryDynamicByImageTextType(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "dynamic/queryDynamicByImageTextType.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryDynamicNums(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "dynamic/queryDynamicNums.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String follow(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "dynamic/follow.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryDynamicFollowByAccount(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "dynamic/queryDynamicFollowByAccount.action";
        return ClientUtils.client(parameterData, url);
    }
}
