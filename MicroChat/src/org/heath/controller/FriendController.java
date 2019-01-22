package org.heath.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.ibatis.annotations.Param;
import org.heath.entity.Friend;
import org.heath.entity.FriendsRelationship;
import org.heath.entity.User;
import org.heath.entity.UserInfo;
import org.heath.service.IFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@ResponseBody
@RequestMapping("friend")
public class FriendController {

	@Autowired
	@Qualifier("friendServiceImpl")
	IFriendService iFriendService;

	public void setiFriendService(IFriendService iFriendService) {
		this.iFriendService = iFriendService;
	}

	// 搜索好友
	@RequestMapping("queryFriends.action")
	private Map<String, Object> queryFriends(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		String text = JSONObject.fromObject(parameterData).getString("text");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			List<UserInfo> uinfos = iFriendService.queryFriends(text);
			returnMap.put("code", "200");
			returnMap.put("msg", "搜索完成");
			returnMap.put("uinfos", uinfos);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 添加好友
	@RequestMapping("addFriend.action")
	private Map<String, Object> addFriend(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		FriendsRelationship relationship = (FriendsRelationship) JSONObject.toBean(JSONObject.fromObject(parameterData),
				FriendsRelationship.class);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			System.out.println(relationship.toString());
			int count = iFriendService.checkFriends(relationship);
			System.out.println(count);
			if (count < 1) {
				iFriendService.addFriendRelationship(relationship);
			} else {
				iFriendService.modifyFriendRelationship(relationship);
			}
			returnMap.put("code", "200");
			returnMap.put("msg", "请求成功，等待对方同意");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 请求添加的数量
	@RequestMapping("queryReqAddNums.action")
	private Map<String, Object> queryReqAddNums(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		String account = JSONObject.fromObject(parameterData).getString("account");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			int count = iFriendService.queryReqAddNums(account);
			System.out.println(count);
			returnMap.put("code", "200");
			returnMap.put("count", count);
			returnMap.put("msg", "请求成功");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 好友通知列表查询
	@RequestMapping("queryFriendsNotice.action")
	private Map<String, Object> queryFriendsNotice(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		String account = JSONObject.fromObject(parameterData).getString("account");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			List<Map<String, Object>> friendsNotices = iFriendService.queryFriendsNotice(account);
			System.out.println(friendsNotices);
			returnMap.put("code", "200");
			returnMap.put("friendsNotices", friendsNotices);
			returnMap.put("msg", "请求成功");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 好友关系处理
	@RequestMapping("modifyFriendRelationshipState.action")
	private Map<String, Object> modifyFriendRelationshipState(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		FriendsRelationship relationship = (FriendsRelationship) JSONObject.toBean(JSONObject.fromObject(parameterData),
				FriendsRelationship.class);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			iFriendService.modifyFriendRelationshipState(relationship);
			if (relationship.getState().equals("1")) {
				iFriendService.addFriend(relationship);
			}
			returnMap.put("code", "200");
			returnMap.put("msg", "请求成功");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 好友列表查询
	@RequestMapping("queryAllFriends.action")
	private Map<String, Object> queryAllFriends(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		JSONObject object = JSONObject.fromObject(parameterData);
		String account = object.getString("account");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			List<Friend> friends = iFriendService.queryAllFriends(account);
			System.out.println(friends);
			returnMap.put("code", "200");
			returnMap.put("friends", JSONArray.fromObject(friends));
			returnMap.put("msg", "请求成功");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 根据account查询好友信息
	@RequestMapping("queryFriendInfoByAccount.action")
	private Map<String, Object> queryFriendInfoByAccount(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		FriendsRelationship relationship = (FriendsRelationship) JSONObject.toBean(JSONObject.fromObject(parameterData),
				FriendsRelationship.class);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String, Object> friendInfo = iFriendService.queryFriendInfoByAccount(relationship);
			System.out.println(friendInfo);
			returnMap.put("code", "200");
			returnMap.put("friendInfo", friendInfo);
			returnMap.put("msg", "请求成功");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 修改备注
	@RequestMapping("modifyFriendRemarks.action")
	private Map<String, Object> modifyFriendRemarks(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Friend friend = (Friend) JSONObject.toBean(JSONObject.fromObject(parameterData), Friend.class);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			System.out.println(friend.toString());
			int count = iFriendService.modifyFriendRemarks(friend);
			System.out.println(count);
			if (count > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "修改成功");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "修改失败，请重新修改");
			}
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 删除好友
	@RequestMapping("delFriend.action")
	private Map<String, Object> delFriend(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Friend friend = (Friend) JSONObject.toBean(JSONObject.fromObject(parameterData), Friend.class);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			System.out.println(friend.toString());
			int count = iFriendService.delFriend(friend);
			System.out.println(count);
			if (count > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "删除成功");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "删除失败，请重新删除");
			}
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}
	
	// 更新好友好友
	@RequestMapping("updateFriend.action")
	private Map<String, Object> updateFriend(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		User user = (User) JSONObject.toBean(JSONObject.fromObject(parameterData), User.class);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			List<Friend> friends = iFriendService.queryAllFriends(user.getAccount());
			returnMap.put("code", "200");
			returnMap.put("friends", friends);
			returnMap.put("msg", "删除成功");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

}
