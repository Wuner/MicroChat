package org.heath.service.impl;

import java.util.List;
import java.util.Map;

import org.heath.entity.Team;
import org.heath.entity.TeamMember;
import org.heath.entity.TeamRelationship;
import org.heath.mapper.TeamMapper;
import org.heath.service.ITeamService;

public class TeamServiceImpl implements ITeamService {
	
	private TeamMapper teamMapper;

	public void setTeamMapper(TeamMapper teamMapper) {
		this.teamMapper = teamMapper;
	}

	@Override
	public int addTeam(Team team) {
		return teamMapper.addTeam(team);
	}

	@Override
	public int invitation(List<TeamRelationship> teamRelationships) {
		return teamMapper.invitation(teamRelationships);
	}

	@Override
	public int addTeamMember(List<TeamMember> teamMembers) {
		return teamMapper.addTeamMember(teamMembers);
	}

	@Override
	public String queryTeamMemberByTid(String tid) {
		return teamMapper.queryTeamMemberByTid(tid);
	}

	@Override
	public int modifyTeamMemberByTid(Map<String, String> map) {
		return teamMapper.modifyTeamMemberByTid(map);
	}

	@Override
	public int modifyTeamMemberByTidAndAccount(TeamMember teamMember) {
		return teamMapper.modifyTeamMemberByTidAndAccount(teamMember);
	}

	@Override
	public int delTeamMemberByTidAndAccount(Map<String, String> map) {
		return teamMapper.delTeamMemberByTidAndAccount(map);
	}

	@Override
	public int modifyTeamByTid(Team team) {
		return teamMapper.modifyTeamByTid(team);
	}

	@Override
	public Team queryTeamInfoByTid(String tid) {
		return teamMapper.queryTeamInfoByTid(tid);
	}

	@Override
	public List<Team> queryTeams(String text) {
		return teamMapper.queryTeams(text);
	}

	@Override
	public int applyJoinTeam(TeamRelationship teamRelationship) {
		return teamMapper.applyJoinTeam(teamRelationship);
	}

	@Override
	public int queryTeamRelationshipByTidAndMore(TeamRelationship teamRelationship) {
		return teamMapper.queryTeamRelationshipByTidAndMore(teamRelationship);
	}

	@Override
	public int modifyApplyJoinTeam(TeamRelationship teamRelationship) {
		return teamMapper.modifyApplyJoinTeam(teamRelationship);
	}

	@Override
	public int queryTeamRelationshipNoticeNumByAccount(String account) {
		return teamMapper.queryTeamRelationshipNoticeNumByAccount(account);
	}

	@Override
	public List<TeamRelationship> queryTeamRelationshipNoticeByAccount(String account) {
		return teamMapper.queryTeamRelationshipNoticeByAccount(account);
	}

	@Override
	public int modifyTeamRelationshipById(TeamRelationship teamRelationship) {
		return teamMapper.modifyTeamRelationshipById(teamRelationship);
	}

	@Override
	public int delTeamMemberByTid(String tid) {
		return teamMapper.delTeamMemberByTid(tid);
	}

	@Override
	public int delTeamByTid(String tid) {
		return teamMapper.delTeamByTid(tid);
	}

	@Override
	public int delTeamRelationshiByTid(String tid) {
		return teamMapper.delTeamRelationshiByTid(tid);
	}

	@Override
	public int modifyTeamMember(Map<String, String> map) {
		return teamMapper.modifyTeamMember(map);
	}


}
