package org.heath.mapper;

import java.util.List;
import java.util.Map;

import org.heath.entity.User;
import org.heath.entity.UserInfo;


public interface UserMapper {
	void register(User user);
	void addUserinfo(String account);
	int checkRegister(String account);
	int checkAccount(String account);
	User login(User user);
	int resetProhibition(String account);
	int modifyPassword(Map<String, Object> user);
	UserInfo queryMyInfo(String account);
	List<UserInfo> queryUsersInfo(List<String> accounts);
	int modifyMyInfo(UserInfo userInfo);
	int resetPassword(User user);
	int accountBan(User user);
	int queryUserNum();
	List<Map<String, Object>> queryEveryMonthRegisterNum(int year);
	List<Map<String, Object>> queryUserinfo(Map<String, Object> map);
}
