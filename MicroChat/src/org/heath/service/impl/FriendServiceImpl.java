package org.heath.service.impl;

import java.util.List;
import java.util.Map;

import org.heath.entity.Friend;
import org.heath.entity.FriendsRelationship;
import org.heath.entity.UserInfo;
import org.heath.mapper.FriendMapper;
import org.heath.service.IFriendService;

public class FriendServiceImpl implements IFriendService{
	
	private FriendMapper friendMapper;
	
	public void setFriendMapper(FriendMapper friendMapper) {
		this.friendMapper = friendMapper;
	}
	
	@Override
	public List<UserInfo> queryFriends(String text) {
		return friendMapper.queryFriends(text);
	}

	@Override
	public int checkFriends(FriendsRelationship relationship) {
		return friendMapper.checkFriends(relationship);
	}

	@Override
	public void addFriendRelationship(FriendsRelationship relationship) {
		friendMapper.addFriendRelationship(relationship);
	}

	@Override
	public void modifyFriendRelationship(FriendsRelationship relationship) {
		friendMapper.modifyFriendRelationship(relationship);
	}

	@Override
	public int queryReqAddNums(String parameter) {
		return friendMapper.queryReqAddNums(parameter);
	}

	@Override
	public List<Map<String, Object>> queryFriendsNotice(String account) {
		return friendMapper.queryFriendsNotice(account);
	}

	@Override
	public void modifyFriendRelationshipState(FriendsRelationship relationship) {
		friendMapper.modifyFriendRelationshipState(relationship);
	}

	@Override
	public void addFriend(FriendsRelationship relationship) {
		friendMapper.addFriend(relationship);
	}

	@Override
	public List<Friend> queryAllFriends(String account) {
		return friendMapper.queryAllFriends(account);
	}

	@Override
	public Map<String, Object> queryFriendInfoByAccount(FriendsRelationship relationship) {
		return friendMapper.queryFriendInfoByAccount(relationship);
	}

	@Override
	public int modifyFriendRemarks(Friend friend) {
		return friendMapper.modifyFriendRemarks(friend);
	}

	@Override
	public int delFriend(Friend friend) {
		return friendMapper.delFriend(friend);
	}

}
