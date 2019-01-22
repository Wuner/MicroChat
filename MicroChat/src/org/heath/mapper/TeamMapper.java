package org.heath.mapper;

import java.util.List;
import java.util.Map;

import org.heath.entity.Team;
import org.heath.entity.TeamMember;
import org.heath.entity.TeamRelationship;

public interface TeamMapper {
	int addTeam(Team team);
	int invitation(List<TeamRelationship> teamRelationships);
	int addTeamMember(List<TeamMember> teamMembers);
	String queryTeamMemberByTid(String tid);
	int modifyTeamMemberByTid(Map<String, String> map);
	int modifyTeamMemberByTidAndAccount(TeamMember teamMember);
	int delTeamMemberByTidAndAccount(Map<String, String> map);
	int modifyTeamByTid(Team team);
	Team queryTeamInfoByTid(String tid);
	List<Team> queryTeams(String text);
	int applyJoinTeam(TeamRelationship teamRelationship);
	int queryTeamRelationshipByTidAndMore(TeamRelationship teamRelationship);
	int modifyApplyJoinTeam(TeamRelationship teamRelationship);
	int queryTeamRelationshipNoticeNumByAccount(String account);
	List<TeamRelationship> queryTeamRelationshipNoticeByAccount(String account);
	int modifyTeamRelationshipById(TeamRelationship teamRelationship);
	int delTeamMemberByTid(String tid);
	int delTeamByTid(String tid);
	int delTeamRelationshiByTid(String tid);
	int modifyTeamMember(Map<String, String> map);
}
