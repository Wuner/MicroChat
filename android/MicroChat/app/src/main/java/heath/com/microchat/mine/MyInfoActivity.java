package heath.com.microchat.mine;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.friend.FriendsNoticeProcessedActivity;
import heath.com.microchat.service.IUserService;
import heath.com.microchat.service.impl.UserServiceImpl;
import heath.com.microchat.team.TeamManageActivity;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.BottomMenu;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.ImageUitl;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;
import heath.com.microchat.utils.UploadServerUtils;

public class MyInfoActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout mRlIcon;
    private RelativeLayout mRlNickname;
    private RelativeLayout mRlSex;
    private RelativeLayout mRlEmail;
    private RelativeLayout mRlSign;
    private RelativeLayout mRlBirth;
    private RelativeLayout mRlMobile;
    private RelativeLayout mRlConstellation;
    private LinearLayout mLlReturn;
    private ImageView mIvIcon;
    private TextView mTvNickname;
    private TextView mTvAccount;
    private TextView mTvSex;
    private TextView mTvEmail;
    private TextView mTvSign;
    private TextView mTvBirth;
    private TextView mTvMobile;
    private TextView mTvConstellation;
    private ACache aCache;
    private IUserService userService;
    //调用系统相册-选择图片
    private static final int IMAGE = 1;
    private static Handler handler;
    private UserInfo userInfo;
    private TimePickerView pvTime;
    private LoadingUtils loadingUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        initView();
        initData();
        initListener();
        initTimePicker();
    }

    private void initView() {
        mRlIcon = this.findViewById(R.id.rl_icon);
        mRlNickname = this.findViewById(R.id.rl_nickname);
        mRlSex = this.findViewById(R.id.rl_sex);
        mRlEmail = this.findViewById(R.id.rl_email);
        mRlSign = this.findViewById(R.id.rl_sign);
        mRlBirth = this.findViewById(R.id.rl_birth);
        mRlMobile = this.findViewById(R.id.rl_mobile);
        mRlConstellation = this.findViewById(R.id.rl_constellation);

        mLlReturn = this.findViewById(R.id.ll_return);
        mIvIcon = this.findViewById(R.id.iv_icon);

        mTvNickname = this.findViewById(R.id.tv_nickname);
        mTvAccount = this.findViewById(R.id.tv_account);
        mTvSex = this.findViewById(R.id.tv_sex);
        mTvEmail = this.findViewById(R.id.tv_email);
        mTvSign = this.findViewById(R.id.tv_sign);
        mTvBirth = this.findViewById(R.id.tv_birth);
        mTvMobile = this.findViewById(R.id.tv_mobile);
        mTvConstellation = this.findViewById(R.id.tv_constellation);

        aCache = ACache.get(this);
        userService = new UserServiceImpl();
        handler = new IHandler();
        loadingUtils = new LoadingUtils(MyInfoActivity.this, "正在修改资料");

    }

    private void initData() {
        loadingUtils.creat();
        userInfo = (UserInfo) aCache.getAsObject("userInfo");
        if (userInfo.getIcon() != null) {
            ImageUitl imageUitl = new ImageUitl(cache);
            imageUitl.asyncloadImage(mIvIcon, Common.HTTP_ADDRESS + Common.USER_FOLDER_PATH + "/" + userInfo.getIcon());
        }
        if (userInfo.getNickname() != null) {
            mTvNickname.setText(userInfo.getNickname());
        }
        if (userInfo.getAccount() != null) {
            mTvAccount.setText(userInfo.getAccount());
        }
        if (userInfo.getGender() != null) {
            if (userInfo.getGender().equals("0")) {
                mTvSex.setText("未知");
            } else if (userInfo.getGender().equals("1")) {
                mTvSex.setText("男");
            } else {
                mTvSex.setText("女");
            }
        }
        if (userInfo.getEmail() != null) {
            mTvEmail.setText(userInfo.getEmail());
        }
        if (userInfo.getSign() != null) {
            mTvSign.setText(userInfo.getSign());
        }
        if (userInfo.getBirth() != null) {
            mTvBirth.setText(userInfo.getBirth());
        }
        if (userInfo.getMobile() != null) {
            mTvMobile.setText(userInfo.getMobile());
        }
        Log.e("TAG", "run: " + userInfo.getEx() + "0---------------------------");
        if (userInfo.getEx() != null) {
            try {
                JSONObject ex = new JSONObject(userInfo.getEx().replace("\"", ""));
                if (ex.has("constellation"))
                    mTvConstellation.setText(ex.getString("constellation"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initListener() {
        mRlIcon.setOnClickListener(this);
        mRlNickname.setOnClickListener(this);
        mLlReturn.setOnClickListener(this);
        mRlSex.setOnClickListener(this);
        mRlEmail.setOnClickListener(this);
        mRlSign.setOnClickListener(this);
        mRlBirth.setOnClickListener(this);
        mRlMobile.setOnClickListener(this);
        mRlConstellation.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        HashMap<String,Object> map;
        switch (view.getId()) {
            case R.id.ll_return:
                finish();
                break;
            case R.id.rl_icon:
                //调用相册
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
                break;

            case R.id.rl_nickname:
                intent = new Intent(
                        MyInfoActivity.this,
                        UpdateInfoActivity.class);
                map = new HashMap<>();
                map.put("updateInfo", getResources().getString(R.string.tv_nickname));
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;
            case R.id.rl_sex:
                String[] texts = new String[]{getResources().getString(R.string.btn_boy), getResources().getString(R.string.btn_girl)};
                int[] ids = new int[]{R.id.btn1, R.id.btn2};
                int[] index = new int[]{0, 1};
                List<Map<String, Object>> list = Common.setBtn(texts, ids, index);
                BottomMenu menuWindow = new BottomMenu(MyInfoActivity.this, clickListener, list);
                menuWindow.show();
                break;
            case R.id.rl_email:
                intent = new Intent(
                        MyInfoActivity.this,
                        UpdateInfoActivity.class);
                map = new HashMap<>();
                map.put("updateInfo", getResources().getString(R.string.tv_email));
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;
            case R.id.rl_sign:
                intent = new Intent(
                        MyInfoActivity.this,
                        UpdateInfoActivity.class);
                map = new HashMap<>();
                map.put("updateInfo", getResources().getString(R.string.tv_sign));
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;
            case R.id.rl_birth:
                pvTime.show(view);
                break;
            case R.id.rl_mobile:
                intent = new Intent(
                        MyInfoActivity.this,
                        UpdateInfoActivity.class);
                map = new HashMap<>();
                map.put("updateInfo", getResources().getString(R.string.tv_mobile));
                intent.putExtra("map", map);
                startActivityForResult(intent, 0);
                break;
            case R.id.rl_constellation:
                pvTime.show(view);
                break;

            default:
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
                    final String resultPath = UploadServerUtils.uploadLogFile(Common.HTTP_ADDRESS + "upload/fileUpload.action", imagePath, Common.USER_FOLDER_PATH);
                    if (resultPath.equals("error")) {
                        loadingUtils.dismissOnUiThread();
                        ToastUtil.toastOnUiThread(MyInfoActivity.this, "上传失败，请重新上传");
                        return;
                    }
                    JSONObject parameterData = new JSONObject();
                    try {
                        parameterData.put("account", aCache.getAsString("account"));
                        parameterData.put("icon", resultPath);
                        String result = userService.updateMyInfo(parameterData);
                        Log.e("TAG", "run: " + result + "0---------------------------");
                        JSONObject resultObj = new JSONObject(result);
                        if (resultObj.getString("code").equals("200")) {
                            userInfo.setIcon(resultPath);
                            aCache.put("userInfo", (Serializable) userInfo);
                            MineFragment.updateData();
                            updateData();
                        }else{
                            ToastUtil.toastOnUiThread(MyInfoActivity.this,resultObj.get("msg").toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
            loadingUtils.dismiss();
            initData();
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn1:
                    loadingUtils.show();
                    com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject parameterData = new JSONObject();
                            try {
                                parameterData.put("account", aCache.getAsString("account"));
                                parameterData.put("gender", "1");
                                String result = userService.updateMyInfo(parameterData);
                                Log.e("TAG", "run: " + result + "0---------------------------");
                                JSONObject resultObj = new JSONObject(result);
                                if (resultObj.getString("code").equals("200")) {
                                    userInfo.setGender("1");
                                    aCache.put("userInfo", (Serializable) userInfo);
                                    MineFragment.updateData();
                                    updateData();
                                }else{
                                    ToastUtil.toastOnUiThread(MyInfoActivity.this,resultObj.get("msg").toString());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case R.id.btn2:
                    loadingUtils.show();
                    com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject parameterData = new JSONObject();
                            try {
                                parameterData.put("account", aCache.getAsString("account"));
                                parameterData.put("gender", "0");
                                String result = userService.updateMyInfo(parameterData);
                                Log.e("TAG", "run: " + result + "0---------------------------");
                                JSONObject resultObj = new JSONObject(result);
                                if (resultObj.getString("code").equals("200")) {
                                    userInfo.setGender("2");
                                    aCache.put("userInfo", (Serializable) userInfo);
                                    MineFragment.updateData();
                                    updateData();
                                }else{
                                    ToastUtil.toastOnUiThread(MyInfoActivity.this,resultObj.get("msg").toString());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    private void initTimePicker() {//Dialog 模式下，在底部弹出

        pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(final Date date, View v) {
                loadingUtils.show();
                com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject parameterData = new JSONObject();
                        try {
                            String dateStr = getTime(date);
                            JSONObject ex = new JSONObject();
                            ex.put("age", Common.getAgeByBirthday(date));
                            ex.put("constellation", Common.getConstellation(date));
                            parameterData.put("account", aCache.getAsString("account"));
                            parameterData.put("birth", dateStr);
                            parameterData.put("ex", String.valueOf(ex));
                            String result = userService.updateMyInfo(parameterData);
                            Log.e("TAG", "run: " + result + "0---------------------------");
                            JSONObject resultObj = new JSONObject(result);
                            if (resultObj.getString("code").equals("200")) {
                                userInfo.setBirth(dateStr);
                                userInfo.setEx(String.valueOf(ex));
                                aCache.put("userInfo", (Serializable) userInfo);
                                MineFragment.updateData();
                                updateData();
                            }else{
                                ToastUtil.toastOnUiThread(MyInfoActivity.this,resultObj.get("msg").toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        })
                .setContentTextSize(18)//滚轮文字大小
                .setType(new boolean[]{true, true, true, false, false, false})// 默认全部显示
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确认")//确认按钮文字
                .setTitleSize(20)//标题文字大小
                .setTitleText("选择生日")//标题文字
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(true)//是否循环滚动
                .setTitleColor(getResources().getColor(R.color.white))//标题文字颜色
                .setSubmitColor(getResources().getColor(R.color.white))//确定按钮文字颜色
                .setCancelColor(getResources().getColor(R.color.white))//取消按钮文字颜色
                .setTitleBgColor(getResources().getColor(R.color.deepskyblue))//标题背景颜色 Night mode
                .setBgColor(getResources().getColor(R.color.white))//滚轮背景颜色 Night mode
                .setDividerColor(getResources().getColor(R.color.gray))
                .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isDialog(true)//是否显示为对话框样式
                .build();

        Dialog mDialog = pvTime.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTime.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
            }
        }
    }

    private String getTime(Date date) {//可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
}
