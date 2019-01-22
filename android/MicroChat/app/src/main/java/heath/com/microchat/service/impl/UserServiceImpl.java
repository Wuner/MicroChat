package heath.com.microchat.service.impl;

import org.json.JSONObject;

import heath.com.microchat.service.IUserService;
import heath.com.microchat.utils.ClientUtils;
import heath.com.microchat.utils.Common;

public class UserServiceImpl implements IUserService {
    @Override
    public String Login(JSONObject loginData) throws Exception {
        String url = Common.HTTP_ADDRESS + "user/login.action";
        return ClientUtils.client(loginData, url);
    }

    @Override
    public String Register(JSONObject registerData) throws Exception {
        String url = Common.HTTP_ADDRESS + "user/create.action";
        return ClientUtils.client(registerData, url);
    }

    @Override
    public String updateMyInfo(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "user/updateMyInfo.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryUsersInfo(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "user/queryUsersInfo.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryUserInfo(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "user/queryMyInfo.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String resetPassword(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "user/resetPassword.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String modifyPassword(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "user/modifyPassword.action";
        return ClientUtils.client(parameterData, url);
    }

}