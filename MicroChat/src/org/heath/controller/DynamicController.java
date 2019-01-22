package org.heath.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.ibatis.annotations.Param;
import org.heath.entity.CommentReply;
import org.heath.entity.Dynamic;
import org.heath.entity.Follow;
import org.heath.entity.Praise;
import org.heath.service.IDynamicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@ResponseBody
@RequestMapping("dynamic")
public class DynamicController {

	@Autowired
	@Qualifier("dynamicServiceImpl")
	IDynamicService iDynamicService;

	public void setiDynamicService(IDynamicService iDynamicService) {
		this.iDynamicService = iDynamicService;
	}

	// 发布动态
	@RequestMapping("release.action")
	private Map<String, Object> release(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Dynamic dynamic = (Dynamic) JSONObject.toBean(JSONObject.fromObject(parameterData), Dynamic.class);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			int count = iDynamicService.release(dynamic);
			System.out.println(count);
			if (count > 0) {
				returnMap.put("code", "200");
				returnMap.put("msg", "发布成功");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "发布失败");
			}
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 查询我的动态
	@RequestMapping("queryDynamicByAccount.action")
	private Map<String, Object> queryDynamicByAccount(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		JSONObject object = JSONObject.fromObject(parameterData);
		String account = object.getString("account");
		List<String> accounts = new ArrayList<String>();
		Map<String, Object> map = new HashMap<String, Object>();
		accounts.add(account);
		map.put("accounts", accounts);
		map.put("state", 1);
		if (object.has("ssid")) {
			map.put("ssid", object.getString("ssid"));
		}
		if (object.has("sid")) {
			map.put("sid", object.getString("sid"));
		}
		if (object.has("eid")) {
			map.put("eid", object.getString("eid"));
		}
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			List<Dynamic> dynamics = iDynamicService.queryDynamicByAccount(map);
			System.out.println(dynamics.toString());
			returnMap.put("code", "200");
			returnMap.put("dynamics", dynamics);
			returnMap.put("msg", "查询成功");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 查询好友动态
	@RequestMapping("queryDynamicFriendsByAccount.action")
	private Map<String, Object> queryDynamicFriendsByAccount(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			JSONObject object = JSONObject.fromObject(parameterData);
			String account = object.getString("account");
			List<String> accounts = iDynamicService.queryAllFriendsAccount(account);
			if (accounts.size() == 0) {
				returnMap.put("code", "200");
				returnMap.put("dynamics", new ArrayList<Dynamic>());
				returnMap.put("msg", "查询成功");
			} else {
				Map<String, Object> map = new HashMap<String, Object>();
				accounts.add(account);
				map.put("accounts", accounts);
				map.put("state", 1);
				if (object.has("ssid")) {
					map.put("ssid", object.getString("ssid"));
				}
				if (object.has("sid")) {
					map.put("sid", object.getString("sid"));
				}
				if (object.has("eid")) {
					map.put("eid", object.getString("eid"));
				}
				List<Dynamic> dynamics = iDynamicService.queryDynamicByAccount(map);
				System.out.println(dynamics.toString());
				returnMap.put("code", "200");
				returnMap.put("dynamics", dynamics);
				returnMap.put("msg", "查询成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 查询图文动态
	@RequestMapping("queryDynamicByImageTextType.action")
	private Map<String, Object> queryDynamicByImageTextType(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		JSONObject object = JSONObject.fromObject(parameterData);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("state", 1);
		if (object.has("type")) {
			map.put("type", object.getString("type"));
		}
		if (object.has("ssid")) {
			map.put("ssid", object.getString("ssid"));
		}
		if (object.has("sid")) {
			map.put("sid", object.getString("sid"));
		}
		if (object.has("eid")) {
			map.put("eid", object.getString("eid"));
		}
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			List<Dynamic> dynamics = iDynamicService.queryDynamicByAccount(map);
			System.out.println(dynamics.toString());
			returnMap.put("code", "200");
			returnMap.put("dynamics", dynamics);
			returnMap.put("msg", "查询成功");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 点赞
	@RequestMapping("praise.action")
	private Map<String, Object> praise(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		JSONObject object = JSONObject.fromObject(parameterData);
		String id = object.getString("dynamicId");
		String account = object.getString("account");
		Praise praise = new Praise();
		praise.setAccount(account);
		praise.setDynamicId(id);
		Dynamic dynamic = new Dynamic();
		dynamic.setId(id);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String state = iDynamicService.queryPraiseByDynamicIdAndAccount(praise);
			if (state == null) {
				iDynamicService.addPraise(praise);
				dynamic.setPraiseNums("+1");
				iDynamicService.quantityPraiseNum(dynamic);
			} else if (state.equals("1")) {
				praise.setState("0");
				iDynamicService.modifyPraiseByDynamicIdAndAccount(praise);
				dynamic.setPraiseNums("-1");
				iDynamicService.quantityPraiseNum(dynamic);
			} else {
				praise.setState("1");
				iDynamicService.modifyPraiseByDynamicIdAndAccount(praise);
				dynamic.setPraiseNums("+1");
				iDynamicService.quantityPraiseNum(dynamic);
			}
			Dynamic dynamic2 = iDynamicService.queryDynamicById(id);
			returnMap.put("code", "200");
			returnMap.put("dynamic", dynamic2);
			returnMap.put("msg", "请求成功");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 评论回复
	@RequestMapping("addCommentReply.action")
	private Map<String, Object> addCommentReply(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		CommentReply commentReply = (CommentReply) JSONObject.toBean(JSONObject.fromObject(parameterData),
				CommentReply.class);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			int count = iDynamicService.addCommentReply(commentReply);
			System.out.println(count);
			if (count > 0) {
				Dynamic dynamic = iDynamicService.queryDynamicById(commentReply.getDynamicId());
				returnMap.put("code", "200");
				returnMap.put("dynamic", dynamic);
				returnMap.put("msg", "请求成功");
			} else {
				returnMap.put("code", "414");
				returnMap.put("msg", "请求失败");
			}
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 评论回复
	@RequestMapping("queryDynamicNums.action")
	private Map<String, Object> queryDynamicNums(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		JSONObject object = JSONObject.fromObject(parameterData);
		String account = object.getString("account");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			List<String> ids = iDynamicService.queryDynamicNums(account);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("state", "1");
			map.put("dynamicIds", ids);
			String commentReplyNums = iDynamicService.queryCommentReplyNums(map);
			String praiseNums = iDynamicService.queryPraiseNums(map);
			String followNums = iDynamicService.queryFollowNums(account);
			returnMap.put("code", "200");
			returnMap.put("dynamicNums", ids.size() + "");
			returnMap.put("commentReplyNums", commentReplyNums);
			returnMap.put("praiseNums", praiseNums);
			returnMap.put("followNums", followNums);
			returnMap.put("msg", "请求成功");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 关注
	@RequestMapping("follow.action")
	private Map<String, Object> follow(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		JSONObject object = JSONObject.fromObject(parameterData);
		String followAccount = object.getString("followAccount");
		String account = object.getString("account");
		Follow follow = new Follow();
		follow.setAccount(account);
		follow.setFollowAccount(followAccount);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String state = iDynamicService.queryFollowByFollowAccountAndAccount(follow);
			if (state == null) {
				iDynamicService.addFollow(follow);
				follow.setState("1");
			} else if (state.equals("1")) {
				follow.setState("0");
				iDynamicService.modifyFollowByFollowAccountAndAccount(follow);
			} else {
				follow.setState("1");
				iDynamicService.modifyFollowByFollowAccountAndAccount(follow);
			}
			returnMap.put("code", "200");
			returnMap.put("state", follow.getState());
			returnMap.put("msg", "请求成功");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 查询关注动态
	@RequestMapping("queryDynamicFollowByAccount.action")
	private Map<String, Object> queryDynamicFollowByAccount(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			JSONObject object = JSONObject.fromObject(parameterData);
			String account = object.getString("account");
			List<String> accounts = iDynamicService.queryFollowAccountByAccount(account);
			if (accounts.size() == 0) {
				returnMap.put("code", "200");
				returnMap.put("dynamics", new ArrayList<Dynamic>());
				returnMap.put("msg", "查询成功");
			} else {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("accounts", accounts);
				map.put("state", 1);
				if (object.has("ssid")) {
					map.put("ssid", object.getString("ssid"));
				}
				if (object.has("sid")) {
					map.put("sid", object.getString("sid"));
				}
				if (object.has("eid")) {
					map.put("eid", object.getString("eid"));
				}
				List<Dynamic> dynamics = iDynamicService.queryDynamicByAccount(map);
				System.out.println(dynamics.toString());
				returnMap.put("code", "200");
				returnMap.put("dynamics", dynamics);
				returnMap.put("msg", "查询成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 查询动态
	@RequestMapping("queryDynamic.action")
	private Map<String, Object> queryUserinfo(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize,
			@Param("content") String content, @Param("searchType") int searchType,
			@Param("dynamicType") int dynamicType, @Param("accurateType") int accurateType) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("start", (pageNum - 1) * pageSize);
			map.put("pageSize", pageSize);
			switch (searchType) {
			case 1:
				String[] contents = content.split(" ");
				for (int i = 0; i < contents.length; i++) {
					switch (i) {
					case 0:
						map.put("account", contents[i]);
						break;
					case 1:
						map.put("content", contents[i]);
						break;
					case 2:
						map.put("sendTime", contents[i]);
						break;

					default:
						break;
					}
				}
				break;
			case 2:
				map.put("account", content);
				break;
			case 3:
				map.put("content", content);
				break;
			case 4:
				map.put("sendTime", content);
				break;

			default:
				break;
			}
			switch (dynamicType) {
			case 2:
				map.put("dynamicType", "ImageText");
				break;
			case 3:
				map.put("dynamicType", "Video");
				break;
			default:
				break;
			}
			map.put("accurateType", accurateType);
			System.out.println(map + "=========================================");
			List<Dynamic> list = iDynamicService.queryDynamic(map);
			int count = iDynamicService.queryDynamicCount(map);
			returnMap.put("code", "200");
			returnMap.put("msg", "查询成功");
			returnMap.put("list", list);
			returnMap.put("count", count);
			System.out.println(returnMap + "=================返回========================");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("code", "404");
			returnMap.put("msg", "发生异常");
		}
		return returnMap;
	}

	// 删除动态
	@RequestMapping("delDynamic.action")
	private Map<String, Object> delDynamic(@Param("id") String id)
			throws ClientProtocolException, IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			int count = iDynamicService.delDynamic(id);
			if(count>0){
				returnMap.put("code", "200");
				returnMap.put("msg", "删除成功");
			}else{
				returnMap.put("code", "404");
				returnMap.put("msg", "删除失败");
			}
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

}
