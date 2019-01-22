package org.heath.service;

import java.util.List;
import java.util.Map;

import org.heath.entity.User;
import org.heath.entity.UserInfo;

public interface IUserService {
	void register(User user);
	int checkRegister(String account);
	int checkAccount(String account);
	User login(User user);
	int resetProhibition(String account);
	int modifyPassword(Map<String, Object> user);
	void addUserinfo(String account);
	UserInfo queryMyInfo(String account);
	List<UserInfo> queryUsersInfo(List<String> accounts);
	int modifyMyInfo(UserInfo userInfo);
	int resetPassword(User user);
	int accountBan(User user);
	int queryUserNum();
	List<Map<String, Object>> queryEveryMonthRegisterNum(int year);
	List<Map<String, Object>> queryUserinfo(Map<String, Object> map);
}
