package heath.com.microchat.team;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.entity.TeamBean;
import heath.com.microchat.service.ITeamService;
import heath.com.microchat.service.impl.TeamServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.ToastUtil;
import heath.com.microchat.utils.UploadServerUtils;

public class CreateTeamActivity extends BaseActivity implements View.OnClickListener {

    private LoadingUtils loadingUtils;
    private ImageView MivAddTeamIcon;
    private int IMAGE = 1;
    private Button mBtnSubmit;
    private EditText mEtTeamName;
    private LinearLayout mLlReturn;

    private ITeamService teamServiceImpl;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);
        initView();
        init();
        initListener();
    }

    private void init() {
        loadingUtils.creat();
        mBtnSubmit.setEnabled(false);
        mBtnSubmit.setBackgroundColor(getResources().getColor(R.color.gray));
    }

    private void initView() {
        loadingUtils = new LoadingUtils(CreateTeamActivity.this, "创建中");
        MivAddTeamIcon = this.findViewById(R.id.iv_add_team_icon);
        mBtnSubmit = this.findViewById(R.id.btn_submit);
        mEtTeamName = this.findViewById(R.id.et_team_name);
        mLlReturn = this.findViewById(R.id.ll_return);

        teamServiceImpl = new TeamServiceImpl();
        gson = new Gson();
    }

    private void initListener() {
        MivAddTeamIcon.setOnClickListener(this);
        mBtnSubmit.setOnClickListener(this);
        mLlReturn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.iv_add_team_icon:
                //调用相册
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
                break;
            case R.id.btn_submit:
                loadingUtils.show();
                final String teamName = mEtTeamName.getText().toString();
                if (teamName.length() < 2) {
                    Toast.makeText(this, "输入群名少于2个字", Toast.LENGTH_SHORT).show();
                    loadingUtils.dismiss();
                    return;
                } else {
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String resultPhth = UploadServerUtils.uploadLogFile(Common.HTTP_ADDRESS + "upload/fileUpload.action", aCache.getAsString("tempImagePath"), Common.TEAM_FOLDER_PATH);
                                if (resultPhth.equals("error")) {
                                    ToastUtil.toastOnUiThread(CreateTeamActivity.this, "创建失败，请重新创建");
                                    loadingUtils.dismissOnUiThread();
                                    return;
                                }
                                TeamBean teamBean = new TeamBean();
                                teamBean.setTname(teamName);
                                teamBean.setOwner(aCache.getAsString("account"));
                                teamBean.setMembers(new JSONArray().toString());
                                teamBean.setAnnouncement("");
                                teamBean.setIntro("");
                                teamBean.setMsg("欢迎加入");
                                teamBean.setMagree("1");
                                teamBean.setJoinmode("1");
                                teamBean.setCustom("");
                                teamBean.setIcon(resultPhth);
                                teamBean.setBeinvitemode("0");
                                teamBean.setInvitemode("0");
                                teamBean.setUptinfomode("0");
                                teamBean.setUpcustommode("0");
                                teamBean.setTeamMemberLimit("200");
                                JSONObject parameterData = new JSONObject(gson.toJson(teamBean));
                                String result = teamServiceImpl.create(parameterData);
                                JSONObject resultObj = new JSONObject(result);
                                if (resultObj.getString("code").equals("200")){
                                    loadingUtils.dismissOnUiThread();
                                    ToastUtil.toastOnUiThread(CreateTeamActivity.this, resultObj.getString("msg"));
                                    finish();
                                }else{
                                    loadingUtils.dismissOnUiThread();
                                    ToastUtil.toastOnUiThread(CreateTeamActivity.this, resultObj.getString("msg"));
                                }
                            } catch (Exception e) {
                                loadingUtils.dismissOnUiThread();
                                e.printStackTrace();
                                ToastUtil.toastOnUiThread(CreateTeamActivity.this, Common.MSG_SERVER_ERROR);
                            }
                        }
                    });
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            final String imagePath = c.getString(columnIndex);
            aCache.put("tempImagePath", imagePath);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            MivAddTeamIcon.setImageBitmap(bitmap);
            mBtnSubmit.setEnabled(true);
            mBtnSubmit.setBackgroundColor(getResources().getColor(R.color.deepskyblue));
            c.close();
        }
    }
}
