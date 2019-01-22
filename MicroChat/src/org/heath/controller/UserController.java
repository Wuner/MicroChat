package org.heath.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.ibatis.annotations.Param;
import org.heath.entity.Friend;
import org.heath.entity.User;
import org.heath.entity.UserInfo;
import org.heath.service.IFriendService;
import org.heath.service.IUserService;
import org.heath.utils.Common;
import org.heath.utils.HttpClient;
import org.heath.utils.RandomCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@ResponseBody
@RequestMapping("user")
public class UserController {

	@Autowired
	@Qualifier("userServiceImpl")
	IUserService iUserService;

	public void setiUserService(IUserService iUserService) {
		this.iUserService = iUserService;
	}

	@Autowired
	@Qualifier("friendServiceImpl")
	IFriendService iFriendService;

	public void setiFriendService(IFriendService iFriendService) {
		this.iFriendService = iFriendService;
	}

	// 登录
	@RequestMapping("login.action")
	private Map<String, Object> login(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = (User) JSONObject.toBean(JSONObject.fromObject(parameterData), User.class);
			User user1 = iUserService.login(user);
			if (user1 != null) {
				if (user1.getState().equals("0")) {
					long cTime = System.currentTimeMillis();
					if (Long.parseLong(user1.getProhibitionTime()) > cTime) {
						returnMap.put("code", "808");
						returnMap.put("msg", "你的账户被封禁了，封禁到" + Common.stampToDate(user1.getProhibitionTime()));
					} else {
						int num = iUserService.resetProhibition(user1.getAccount());
						if (num > 0) {
							UserInfo userInfo = iUserService.queryMyInfo(user1.getAccount());
							List<Friend> friends = iFriendService.queryAllFriends(user1.getAccount());
							returnMap.put("code", "200");
							returnMap.put("userInfo", userInfo);
							returnMap.put("account", user1.getAccount());
							returnMap.put("token", user1.getToken());
							returnMap.put("friends", JSONArray.fromObject(friends));
							returnMap.put("msg", "登录成功");
						} else {
							returnMap.put("code", "404");
							returnMap.put("msg", "发生异常");
						}
					}
				} else {
					UserInfo userInfo = iUserService.queryMyInfo(user1.getAccount());
					List<Friend> friends = iFriendService.queryAllFriends(user1.getAccount());
					returnMap.put("code", "200");
					returnMap.put("userInfo", userInfo);
					returnMap.put("account", user1.getAccount());
					returnMap.put("token", user1.getToken());
					returnMap.put("friends", JSONArray.fromObject(friends));
					returnMap.put("msg", "登录成功");
				}
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "用户名或密码错误");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}

	// 创建账户
	@RequestMapping("create.action")
	private Map<String, Object> register(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = (User) JSONObject.toBean(JSONObject.fromObject(parameterData), User.class);
			int count = iUserService.checkRegister(user.getBindMobile());
			int count1 = 1;
			if (count >= 1) {
				returnMap.put("code", "414");
				returnMap.put("msg", "该手机号已被注册");
			} else {
				while (count1 == 1) {
					String account = Common.getRandomCode();
					user.setAccount(account);
					count1 = iUserService.checkAccount(user.getAccount());
					if (count1 == 0) {
						break;
					}
				}
				String token = RandomCode.getRandomCode();
				JSONObject httpParameter = new JSONObject();
				JSONObject parameter = new JSONObject();
				parameter.put("accid", user.getAccount());
				parameter.put("name", user.getAccount());
				parameter.put("token", token);
				httpParameter.put("url", "https://api.netease.im/nimserver/user/create.action");
				httpParameter.put("parameter", parameter);
				JSONObject returnObj = HttpClient.httpClient(httpParameter);
				if (returnObj.get("code").equals(200)) {
					user.setToken(token);
					iUserService.register(user);
					iUserService.addUserinfo(user.getAccount());
					returnMap.put("code", "200");
					returnMap.put("msg", "注册成功");
				} else if (returnObj.get("code").equals(414)) {
					returnMap.put("code", "414");
					returnMap.put("msg", "该手机号已被注册");
				} else {
					returnMap.put("code", "1000");
					returnMap.put("msg", "注册失败，请重新注册");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	@RequestMapping("updateMyInfo.action")
	private Map<String, Object> updateMyInfo(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			UserInfo userInfo = (UserInfo) JSONObject.toBean(JSONObject.fromObject(parameterData), UserInfo.class);
			JSONObject httpParameter = new JSONObject();
			JSONObject parameter = new JSONObject();
			System.out.println(userInfo);
			parameter.put("accid", userInfo.getAccount());
			if (userInfo.getNickname() != null) {
				parameter.put("name", userInfo.getNickname());
			}
			if (userInfo.getIcon() != null) {
				parameter.put("icon", userInfo.getIcon());
			}
			if (userInfo.getSign() != null) {
				parameter.put("sign", userInfo.getSign());
			}
			if (userInfo.getEmail() != null) {
				parameter.put("email", userInfo.getEmail());
			}
			if (userInfo.getBirth() != null) {
				parameter.put("birth", userInfo.getBirth());
			}
			if (userInfo.getMobile() != null) {
				parameter.put("mobile", userInfo.getMobile());
			}
			if (userInfo.getGender() != null) {
				parameter.put("gender", userInfo.getGender());
			}
			if (userInfo.getEx() != null) {
				parameter.put("ex", userInfo.getEx());
			}
			httpParameter.put("url", "https://api.netease.im/nimserver/user/update.action");
			httpParameter.put("parameter", parameter);
			JSONObject returnObj = HttpClient.httpClient(httpParameter);
			if (returnObj.get("code").equals(200)) {
				int result = iUserService.modifyMyInfo(userInfo);
				if (result > 0) {
					returnMap.put("code", "200");
					returnMap.put("msg", "修改资料成功");
				} else {
					returnMap.put("code", "414");
					returnMap.put("msg", "修改资料失败，请重新修改");
				}
			} else if (returnObj.get("code").equals(414)) {
				returnMap.put("code", "414");
				returnMap.put("msg", "修改资料失败，请重新修改");
			} else {
				returnMap.put("code", "1000");
				returnMap.put("msg", "修改资料失败，请重新修改");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// 修改密码
	@RequestMapping("modifyPassword.action")
	private Map<String, Object> modifyPassword(@Param("parameterData") String parameterData) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			JSONObject userObj = JSONObject.fromObject(parameterData);
			Map<String, Object> user = new HashMap<String, Object>();
			user.put("account", userObj.getString("account"));
			user.put("password", userObj.getString("password"));
			user.put("newPassword", userObj.getString("newPassword"));
			int num = iUserService.modifyPassword(user);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "修改成功");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "用户名或密码错误，请重新修改");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}

	@RequestMapping("getUinfos.action")
	private Map<String, Object> getUinfos(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		JSONObject accountsObject = JSONObject.fromObject(parameterData);
		JSONObject httpParameter = new JSONObject();
		JSONObject parameter = new JSONObject();
		parameter.put("accids", accountsObject.getJSONArray("accounts"));
		httpParameter.put("url", "https://api.netease.im/nimserver/user/getUinfos.action");
		httpParameter.put("parameter", parameter);
		JSONObject returnObj = HttpClient.httpClient(httpParameter);
		if (returnObj.get("code").equals(200)) {
			returnMap.put("code", "200");
			returnMap.put("uinfos", returnObj.getJSONArray("uinfos"));
		} else if (returnObj.get("code").equals(414)) {
			returnMap.put("code", "414");
			returnMap.put("msg", "该账户不存在");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	// 根据账号查询用户信息
	@RequestMapping("queryUsersInfo.action")
	private Map<String, Object> queryUsersInfo(@Param("parameterData") String parameterData) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			JSONObject accountsObject = JSONObject.fromObject(parameterData);
			JSONArray arrayAccounts = accountsObject.getJSONArray("accounts");
			List<String> accounts = new ArrayList<String>();
			if (arrayAccounts.size() == 0) {
				returnMap.put("code", "400");
				returnMap.put("msg", "无数据");
				return returnMap;
			}
			for (Object account : arrayAccounts) {
				accounts.add(account.toString());
			}
			List<UserInfo> userInfos = iUserService.queryUsersInfo(accounts);
			returnMap.put("code", "200");
			returnMap.put("userInfos", userInfos);
			returnMap.put("msg", "请求成功");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}

	// 根据账号查询用户信息
	@RequestMapping("queryMyInfo.action")
	private Map<String, Object> queryMyInfo(@Param("parameterData") String parameterData) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = (User) JSONObject.toBean(JSONObject.fromObject(parameterData), User.class);
			UserInfo userInfo = iUserService.queryMyInfo(user.getAccount());
			returnMap.put("code", "200");
			returnMap.put("userInfo", userInfo);
			returnMap.put("msg", "请求成功");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}

	// 重置密码
	@RequestMapping("resetPassword.action")
	private Map<String, Object> resetPassword(@Param("parameterData") String parameterData) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = (User) JSONObject.toBean(JSONObject.fromObject(parameterData), User.class);
			int num = iUserService.resetPassword(user);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "重置成功");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "重置失败，请重新修改");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}

	// 账号封禁
	@RequestMapping("accountBan.action")
	private Map<String, Object> accountBan(@Param("account") String account, @Param("hour") int hour,
			@Param("day") int day) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			if (day <= 0) {
				day = 0;
			}
			if (hour <= 0) {
				hour = 0;
			} else if (hour >= 24) {
				hour = 24;
			}
			long time = System.currentTimeMillis();
			time = time + day * 24 * 60 * 60 * 1000 + hour * 60 * 60 * 1000;
			User user = new User();
			user.setAccount(account);
			user.setState("0");
			user.setProhibitionTime(time + "");
			int num = iUserService.accountBan(user);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "封禁成功");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "封禁失败，请确认封禁账号");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}
	// 账号解封
	@RequestMapping("accountDecapsulation.action")
	private Map<String, Object> accountDecapsulation(@Param("account") String account) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = new User();
			user.setAccount(account);
			user.setState("1");
			user.setProhibitionTime("");
			int num = iUserService.accountBan(user);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "解封成功");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "解封失败，请确认解封账号");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}
	// 账号重置密码
	@RequestMapping("accountResetPassword.action")
	private Map<String, Object> accountResetPassword(@Param("account") String account) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			User user = new User();
			String password = Common.getRandomCode();
			user.setBindMobile(account);
			user.setPassword(password);
			int num = iUserService.resetPassword(user);
			if (num > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "重置成功");
				returnMap.put("password", password);
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "重置失败，请重新重置");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}
	// 查询当前年每一个月份
	@RequestMapping("queryEveryMonthRegisterNum.action")
	private Map<String, Object> queryEveryMonthRegisterNum() {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Calendar cale = null;  
	        cale = Calendar.getInstance();  
	        int year = cale.get(Calendar.YEAR);
			List<Map<String, Object>> list = iUserService.queryEveryMonthRegisterNum(year);
			returnMap.put("code", "200");
			returnMap.put("msg", "查询成功");
			returnMap.put("list", list);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}
	// 查询用户信息
	@RequestMapping("queryUserinfo.action")
	private Map<String, Object> queryUserinfo(@Param("pageNum") int pageNum,@Param("pageSize") int pageSize) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("start", (pageNum - 1) * pageSize);
			map.put("pageSize", pageSize);
			List<Map<String, Object>> list = iUserService.queryUserinfo(map);
			int count = iUserService.queryUserNum();
			returnMap.put("code", "200");
			returnMap.put("msg", "查询成功");
			returnMap.put("list", list);
			returnMap.put("count", count);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}
	// 管理员登录
	@RequestMapping("adminLogin.action")
	private Map<String, Object> adminLogin(@Param("account") String account,@Param("password") String password) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			if(account.equals("admin") && password.equals("admin")){
				returnMap.put("code", "200");
				returnMap.put("msg", "登录成功");
			}else{
				returnMap.put("code", "404");
				returnMap.put("msg", "账号或密码错误");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}

}
