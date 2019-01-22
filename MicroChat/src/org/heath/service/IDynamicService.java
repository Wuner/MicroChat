package org.heath.service;

import java.util.List;
import java.util.Map;

import org.heath.entity.CommentReply;
import org.heath.entity.Dynamic;
import org.heath.entity.Follow;
import org.heath.entity.Praise;

public interface IDynamicService {

	int release(Dynamic dynamic);
	List<Dynamic> queryDynamicByAccount(Map<String, Object> map);
	Dynamic queryDynamicById(String id);
	String queryPraiseByDynamicIdAndAccount(Praise praise);
	int addPraise(Praise praise);
	int modifyPraiseByDynamicIdAndAccount(Praise praise);
	String queryFollowByFollowAccountAndAccount(Follow follow);
	int addFollow(Follow follow);
	int modifyFollowByFollowAccountAndAccount(Follow follow);
	int quantityPraiseNum(Dynamic dynamic);
	int addCommentReply(CommentReply commentReply);
	List<String> queryAllFriendsAccount(String account);
	String queryCommentReplyNums(Map<String, Object> map);
	String queryPraiseNums(Map<String, Object> map);
	List<String> queryDynamicNums(String account);
	String queryFollowNums(String account);
	List<String> queryFollowAccountByAccount(String account);
	List<Dynamic> queryDynamic(Map<String, Object> map);
	int queryDynamicCount(Map<String, Object> map);
	int delDynamic(String id);
}
