package org.heath.service.impl;

import java.util.List;
import java.util.Map;

import org.heath.entity.User;
import org.heath.entity.UserInfo;
import org.heath.mapper.UserMapper;
import org.heath.service.IUserService;

public class UserServiceImpl implements IUserService{

	private UserMapper userMapper;
	
	public void setUserMapper(UserMapper userMapper) {
		this.userMapper = userMapper;
	}


	@Override
	public void register(User user) {
		userMapper.register(user);
	}


	@Override
	public int checkRegister(String account) {
		int count = userMapper.checkRegister(account);
		return count;
	}


	@Override
	public User login(User user) {
		return userMapper.login(user);
	}


	@Override
	public int modifyPassword(Map<String, Object> user) {
		return userMapper.modifyPassword(user);
	}


	@Override
	public void addUserinfo(String account) {
		userMapper.addUserinfo(account);
		
	}


	@Override
	public UserInfo queryMyInfo(String account) {
		return userMapper.queryMyInfo(account);
	}


	@Override
	public int modifyMyInfo(UserInfo userInfo) {
		return userMapper.modifyMyInfo(userInfo);
	}


	@Override
	public List<UserInfo> queryUsersInfo(List<String> accounts) {
		return userMapper.queryUsersInfo(accounts);
	}


	@Override
	public int checkAccount(String account) {
		return userMapper.checkAccount(account);
	}


	@Override
	public int resetPassword(User user) {
		return userMapper.resetPassword(user);
	}


	@Override
	public int resetProhibition(String account) {
		return userMapper.resetProhibition(account);
	}


	@Override
	public int accountBan(User user) {
		return userMapper.accountBan(user);
	}


	@Override
	public List<Map<String, Object>> queryEveryMonthRegisterNum(int year) {
		return userMapper.queryEveryMonthRegisterNum(year);
	}


	@Override
	public List<Map<String, Object>> queryUserinfo(Map<String, Object> map) {
		return userMapper.queryUserinfo(map);
	}


	@Override
	public int queryUserNum() {
		return userMapper.queryUserNum();
	}

}
