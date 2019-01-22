package org.heath.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.ibatis.annotations.Param;
import org.heath.entity.Message;
import org.heath.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@ResponseBody
@RequestMapping("message")
public class MessageController {

	@Autowired
	@Qualifier("messageServiceImpl")
	IMessageService iMessageService;

	public void setiMessageService(IMessageService iMessageService) {
		this.iMessageService = iMessageService;
	}

	// 添加消息记录
	@RequestMapping("addMessageInfo.action")
	private Map<String, Object> addMessageInfo(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		Message message = (Message) JSONObject.toBean(JSONObject.fromObject(parameterData), Message.class);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			iMessageService.addMessageInfo(message);
			returnMap.put("code", "200");
			returnMap.put("msg", "添加成功");
		} catch (Exception e) {
			returnMap.put("code", "414");
			returnMap.put("msg", "服务器异常");
		}
		System.out.println(returnMap.toString());
		return returnMap;
	}

	// 未读消息所有数量
	@RequestMapping("queryAllUnreadMessageCount.action")
	private Map<String, Object> queryAllUnreadMessageCount(@Param("parameterData") String parameterData)
			throws ClientProtocolException, IOException {
		String account = JSONObject.fromObject(parameterData).getString("account");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			int count = iMessageService.queryAllUnreadMessageCount(account);
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

	// 查询消息
	@RequestMapping("queryMessage.action")
	private Map<String, Object> queryUserinfo(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize,
			@Param("content") String content, @Param("searchType") int searchType,
			@Param("messageType") int messageType, @Param("sessionType") int sessionType,
			@Param("accurateType") int accurateType) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String, Object> message = new HashMap<String, Object>();
			message.put("start", (pageNum - 1) * pageSize);
			message.put("pageSize", pageSize);
			switch (searchType) {
			case 1:
				String[] contents = content.split(" ");
				for (int i = 0; i < contents.length; i++) {
					switch (i) {
					case 0:
						message.put("account", contents[i]);
						break;
					case 1:
						message.put("content", contents[i]);
						break;
					case 2:
						message.put("sendTime", contents[i]);
						break;

					default:
						break;
					}
				}
				break;
			case 2:
				message.put("account", content);
				break;
			case 3:
				message.put("content", content);
				break;
			case 4:
				message.put("sendTime", content);
				break;

			default:
				break;
			}
			switch (messageType) {
			case 2:
				message.put("messageType", "text");
				break;
			case 3:
				message.put("messageType", "image");
				break;
			case 4:
				message.put("messageType", "voice");
				break;
			case 5:
				message.put("messageType", "video");
				break;

			default:
				break;
			}
			switch (sessionType) {
			case 2:
				message.put("sessionType", "P2P");
				break;
			case 3:
				message.put("sessionType", "Team");
				break;
			default:
				break;
			}
			message.put("accurateType", accurateType);
			System.out.println(message + "=========================================");
			List<Message> list = iMessageService.queryMessage(message);
			int count = iMessageService.queryMessageCount(message);
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

}
