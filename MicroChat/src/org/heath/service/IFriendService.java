package org.heath.service;

import java.util.List;
import java.util.Map;

import org.heath.entity.Friend;
import org.heath.entity.FriendsRelationship;
import org.heath.entity.UserInfo;

public interface IFriendService {

	List<UserInfo> queryFriends(String text);
	int checkFriends(FriendsRelationship relationship);
	void addFriendRelationship(FriendsRelationship relationship);
	void modifyFriendRelationship(FriendsRelationship relationship);
	int queryReqAddNums(String parameter);
	List<Map<String, Object>> queryFriendsNotice(String account);
	void modifyFriendRelationshipState(FriendsRelationship relationship);
	void addFriend(FriendsRelationship relationship);
	List<Friend> queryAllFriends(String account);
	Map<String, Object> queryFriendInfoByAccount(FriendsRelationship relationship);
	int modifyFriendRemarks(Friend friend);
	int delFriend(Friend friend);
}
