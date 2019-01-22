package heath.com.microchat.service.impl;

import org.json.JSONObject;

import heath.com.microchat.service.ITeamService;
import heath.com.microchat.utils.ClientUtils;
import heath.com.microchat.utils.Common;

public class TeamServiceImpl implements ITeamService {
    @Override
    public String create(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/create.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String invitation(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/invitation.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String modifyTeamMember(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/modifyTeamMemberNick.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String removeMember(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/removeMember.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String delTeamMemberByTidAndAccount(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/delTeamMemberByTidAndAccount.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String modifyTeamByTid(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/modifyTeamByTid.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryTeamInfoByTid(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/queryTeamInfoByTid.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryTeams(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/queryTeams.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String applyJoinTeam(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/applyJoinTeam.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryTeamRelationshipNoticeNumByAccount(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/queryTeamRelationshipNoticeNumByAccount.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String queryTeamRelationshipNoticeByAccount(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/queryTeamRelationshipNoticeByAccount.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String modifyTeamRelationshipById(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/modifyTeamRelationshipById.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String dissolution(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/dissolution.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String mute(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/mute.action";
        return ClientUtils.client(parameterData, url);
    }

    @Override
    public String muteMember(JSONObject parameterData) throws Exception {
        String url = Common.HTTP_ADDRESS + "team/muteMember.action";
        return ClientUtils.client(parameterData, url);
    }
}
