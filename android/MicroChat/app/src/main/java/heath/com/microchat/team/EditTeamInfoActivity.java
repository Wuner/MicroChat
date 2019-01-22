package heath.com.microchat.team;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;
import heath.com.microchat.utils.UploadServerUtils;

public class EditTeamInfoActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlReturn;
    private ImageView mIvTeamIcon;
    private TextView mTvTeamName;
    private RelativeLayout mRlTeamName;
    private TextView mTvTeamIntroduce;
    private RelativeLayout mRlTeamIntroduce;
    private static Team team;
    private LoadingUtils loadingUtils;
    private int IMAGE = 1;
    private ITeamService teamServiceImpl;
    private static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_team_info);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        Intent intent = getIntent();
        team = (Team) intent.getSerializableExtra("team");
        mLlReturn = findViewById(R.id.ll_return);
        mIvTeamIcon = findViewById(R.id.iv_team_icon);
        mTvTeamName = findViewById(R.id.tv_team_name);
        mRlTeamName = findViewById(R.id.rl_team_name);
        mTvTeamIntroduce = findViewById(R.id.tv_team_introduce);
        mRlTeamIntroduce = findViewById(R.id.rl_team_introduce);
        loadingUtils = new LoadingUtils(EditTeamInfoActivity.this, "努力加载中");
        loadingUtils.creat();
        teamServiceImpl = new TeamServiceImpl();
        handler = new IHandler();
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mIvTeamIcon.setOnClickListener(this);
        mRlTeamName.setOnClickListener(this);
        mRlTeamIntroduce.setOnClickListener(this);
    }

    private void initData() {
        ImageUitl imageUitl = new ImageUitl(cache);
        imageUitl.asyncloadImage(mIvTeamIcon, Common.HTTP_ADDRESS + Common.TEAM_FOLDER_PATH + "/" + team.getIcon());
        mTvTeamName.setText(team.getName());
        mTvTeamIntroduce.setText(team.getIntroduce());
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Map<String, Object> map;
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.iv_team_icon:
                //调用相册
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
                break;
            case R.id.rl_team_name:
                intent = new Intent(
                        EditTeamInfoActivity.this,
                        UpdateTeamMemberInfoActivity.class);
                map = new HashMap<>();
                map.put("updateInfo", getResources().getString(R.string.tv_team_name));
                map.put("team", team);
                map.put("from", "EditTeamInfoActivity");
                intent.putExtra("map", (Serializable) map);
                startActivityForResult(intent, 0);
                break;
            case R.id.rl_team_introduce:
                intent = new Intent(
                        EditTeamInfoActivity.this,
                        UpdateTeamMemberInfoActivity.class);
                map = new HashMap<>();
                map.put("updateInfo", getResources().getString(R.string.tv_team_introduce));
                map.put("team", team);
                map.put("from", "EditTeamInfoActivity");
                intent.putExtra("map", (Serializable) map);
                startActivityForResult(intent, 0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            loadingUtils.show();
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            final String imagePath = c.getString(columnIndex);
            com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    final String resultPath = UploadServerUtils.uploadLogFile(Common.HTTP_ADDRESS + "upload/fileUpload.action", imagePath, Common.TEAM_FOLDER_PATH);
                    if (resultPath.equals("error")) {
                        ToastUtil.toastOnUiThread(EditTeamInfoActivity.this, "上传失败，请重新上传");
                        return;
                    }
                    JSONObject parameterData = new JSONObject();
                    try {
                        parameterData.put("tid", team.getId());
                        parameterData.put("owner", team.getCreator());
                        parameterData.put("icon", resultPath);
                        String result = teamServiceImpl.modifyTeamByTid(parameterData);
                        Log.e("TAG", "run: " + result + "0---------------------------");
                        JSONObject resultObj = new JSONObject(result);
                        if (resultObj.getString("code").equals("200")) {
                            getTeamInfo();
                        } else {
                            ToastUtil.toastOnUiThread(EditTeamInfoActivity.this, resultObj.get("msg").toString());
                            loadingUtils.dismissOnUiThread();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        loadingUtils.dismissOnUiThread();
                    }
                }
            });
            c.close();
        }
    }

    public static void updateData() {
        Message message = new Message();
        Bundle data = new Bundle();
        message.what = 1;
        message.setData(data);
        handler.sendMessage(message);
    }

    private class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            initData();
            loadingUtils.dismissOnUiThread();
        }
    }

    public static void getTeamInfo() {
        NIMClient.getService(TeamService.class).searchTeam(team.getId()).setCallback(new RequestCallback<Team>() {
            @Override
            public void onSuccess(Team t) {
                // 查询成功，获得群组资料
                team = t;
                updateData();
                TeamInfoActivity.getTeamInfo();
            }

            @Override
            public void onFailed(int code) {
                // 失败
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
            }
        });
    }

}
