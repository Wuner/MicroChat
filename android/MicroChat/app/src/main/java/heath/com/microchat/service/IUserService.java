package heath.com.microchat.service;

import org.json.JSONObject;

public interface IUserService {
    String Login(JSONObject loginData) throws Exception;
    String Register(JSONObject registerData) throws Exception;
    String updateMyInfo(JSONObject parameterData) throws Exception;
    String queryUsersInfo(JSONObject parameterData) throws Exception;
    String queryUserInfo(JSONObject parameterData) throws Exception;
    String resetPassword(JSONObject parameterData) throws Exception;
    String modifyPassword(JSONObject parameterData) throws Exception;
}
