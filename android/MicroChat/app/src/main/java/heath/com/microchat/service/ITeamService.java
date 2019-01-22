package heath.com.microchat.service;

import org.json.JSONObject;

public interface ITeamService {
    String create(JSONObject parameterData) throws Exception;
    String invitation(JSONObject parameterData) throws Exception;
    String modifyTeamMember(JSONObject parameterData) throws Exception;
    String removeMember(JSONObject parameterData) throws Exception;
    String delTeamMemberByTidAndAccount(JSONObject parameterData) throws Exception;
    String modifyTeamByTid(JSONObject parameterData) throws Exception;
    String queryTeamInfoByTid(JSONObject parameterData) throws Exception;
    String queryTeams(JSONObject parameterData) throws Exception;
    String applyJoinTeam(JSONObject parameterData) throws Exception;
    String queryTeamRelationshipNoticeNumByAccount(JSONObject parameterData) throws Exception;
    String queryTeamRelationshipNoticeByAccount(JSONObject parameterData) throws Exception;
    String modifyTeamRelationshipById(JSONObject parameterData) throws Exception;
    String dissolution(JSONObject parameterData) throws Exception;
    String mute(JSONObject parameterData) throws Exception;
    String muteMember(JSONObject parameterData) throws Exception;
}
