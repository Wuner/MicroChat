package heath.com.microchat.service;

import org.json.JSONObject;

public interface IMessageService {
    String addMessageInfo(JSONObject parameterData) throws Exception;
    String queryAllUnreadMessageCount(JSONObject parameterData) throws Exception;
}
