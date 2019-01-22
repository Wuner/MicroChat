package org.heath.service.impl;

import java.util.List;
import java.util.Map;

import org.heath.entity.CommentReply;
import org.heath.entity.Dynamic;
import org.heath.entity.Follow;
import org.heath.entity.Praise;
import org.heath.mapper.DynamicMapper;
import org.heath.service.IDynamicService;

public class DynamicServiceImpl implements IDynamicService{
	
	private DynamicMapper dynamicMapper;

	public void setDynamicMapper(DynamicMapper dynamicMapper) {
		this.dynamicMapper = dynamicMapper;
	}

	@Override
	public int release(Dynamic dynamic) {
		return dynamicMapper.release(dynamic);
	}

	@Override
	public List<Dynamic> queryDynamicByAccount(Map<String, Object> map) {
		return dynamicMapper.queryDynamicByAccount(map);
	}

	@Override
	public int addPraise(Praise praise) {
		return dynamicMapper.addPraise(praise);
	}

	@Override
	public int quantityPraiseNum(Dynamic dynamic) {
		return dynamicMapper.quantityPraiseNum(dynamic);
	}

	@Override
	public Dynamic queryDynamicById(String id) {
		return dynamicMapper.queryDynamicById(id);
	}

	@Override
	public String queryPraiseByDynamicIdAndAccount(Praise praise) {
		return dynamicMapper.queryPraiseByDynamicIdAndAccount(praise);
	}

	@Override
	public int modifyPraiseByDynamicIdAndAccount(Praise praise) {
		return dynamicMapper.modifyPraiseByDynamicIdAndAccount(praise);
	}

	@Override
	public int addCommentReply(CommentReply commentReply) {
		return dynamicMapper.addCommentReply(commentReply);
	}

	@Override
	public List<String> queryAllFriendsAccount(String account) {
		return dynamicMapper.queryAllFriendsAccount(account);
	}

	@Override
	public String queryCommentReplyNums(Map<String, Object> map) {
		return dynamicMapper.queryCommentReplyNums(map);
	}

	@Override
	public String queryPraiseNums(Map<String, Object> map) {
		return dynamicMapper.queryPraiseNums(map);
	}

	@Override
	public List<String> queryDynamicNums(String account) {
		return dynamicMapper.queryDynamicNums(account);
	}

	@Override
	public String queryFollowNums(String account) {
		return dynamicMapper.queryFollowNums(account);
	}

	@Override
	public String queryFollowByFollowAccountAndAccount(Follow follow) {
		return dynamicMapper.queryFollowByFollowAccountAndAccount(follow);
	}

	@Override
	public int addFollow(Follow follow) {
		return dynamicMapper.addFollow(follow);
	}

	@Override
	public int modifyFollowByFollowAccountAndAccount(Follow follow) {
		return dynamicMapper.modifyFollowByFollowAccountAndAccount(follow);
	}

	@Override
	public List<String> queryFollowAccountByAccount(String account) {
		return dynamicMapper.queryFollowAccountByAccount(account);
	}

	@Override
	public List<Dynamic> queryDynamic(Map<String, Object> map) {
		return dynamicMapper.queryDynamic(map);
	}

	@Override
	public int queryDynamicCount(Map<String, Object> map) {
		return dynamicMapper.queryDynamicCount(map);
	}

	@Override
	public int delDynamic(String id) {
		return dynamicMapper.delDynamic(id);
	}

}
