package heath.com.microchat.service.impl;

import org.json.JSONObject;

import heath.com.microchat.service.IMessageService;
import heath.com.microchat.utils.ClientUtils;
import heath.com.microchat.utils.Common;

public class MessageServiceImpl implements IMessageService {
    @Override
    public String addMessageInfo(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS+"message/addMessageInfo.action";
        return ClientUtils.client(parameterData,url);
    }

    @Override
    public String queryAllUnreadMessageCount(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS+"message/queryAllUnreadMessageCount.action";
        return ClientUtils.client(parameterData,url);
    }

}
