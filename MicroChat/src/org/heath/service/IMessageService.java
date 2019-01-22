package org.heath.service;

import java.util.List;
import java.util.Map;

import org.heath.entity.Message;

public interface IMessageService {

	void addMessageInfo(Message message);
	int queryAllUnreadMessageCount(String account);
	int queryUnreadMessageCountByAccount(Map<String, Object> parameter);
	List<Message> queryMessage(Map<String, Object> message);
	int queryMessageCount(Map<String, Object> message);
}
