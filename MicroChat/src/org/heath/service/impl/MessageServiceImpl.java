package org.heath.service.impl;

import java.util.List;
import java.util.Map;

import org.heath.entity.Message;
import org.heath.mapper.MessageMapper;
import org.heath.service.IMessageService;

public class MessageServiceImpl implements IMessageService {
	
	private MessageMapper messageMapper;

	public void setMessageMapper(MessageMapper messageMapper) {
		this.messageMapper = messageMapper;
	}


	@Override
	public void addMessageInfo(Message message) {
		messageMapper.addMessageInfo(message);
	}


	@Override
	public int queryAllUnreadMessageCount(String account) {
		return messageMapper.queryAllUnreadMessageCount(account);
	}


	@Override
	public int queryUnreadMessageCountByAccount(Map<String, Object> parameter) {
		return messageMapper.queryUnreadMessageCountByAccount(parameter);
	}


	@Override
	public List<Message> queryMessage(Map<String, Object> message) {
		return messageMapper.queryMessage(message);
	}


	@Override
	public int queryMessageCount(Map<String, Object> message) {
		return messageMapper.queryMessageCount(message);
	}

}
