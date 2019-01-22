package heath.com.microchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyco.animation.BaseAnimatorSet;
import com.flyco.animation.BounceEnter.BounceTopEnter;
import com.flyco.animation.SlideExit.SlideBottomExit;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.heath.recruit.utils.ThreadUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.util.List;

import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.entity.FriendBean;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.mine.ForgetPasswordActivity;
import heath.com.microchat.service.IMService;
import heath.com.microchat.service.IUserService;
import heath.com.microchat.service.ServiceRulesException;
import heath.com.microchat.service.impl.UserServiceImpl;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private IUserService userService = new UserServiceImpl();

    private EditText mEtAccount;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private ACache aCache;
    private TextView mTvRegister;
    private Gson gson;
    private LinearLayout mLlParent;

    private LoadingUtils loadingUtils;
    private MicroChatDB mcDB;
    private TextView mForgetPassword;
    private BaseAnimatorSet mBasIn;
    private BaseAnimatorSet mBasOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initListener();
        init();
    }

    private void init() {
        mBasIn = new BounceTopEnter();
        mBasOut = new SlideBottomExit();
        loadingUtils.creat();
        String account = aCache.getAsString("account");
        String token = aCache.getAsString("token");
        String autoLogin = aCache.getAsString("autoLogin");
        if (autoLogin != null && autoLogin.equals("0")) {
            mEtAccount.setText(account);
            mEtPassword.setText(token);
        }
    }

    private void initView() {
        mEtAccount = this.findViewById(R.id.et_account);
        mEtPassword = this.findViewById(R.id.et_passwords);
        mBtnLogin = this.findViewById(R.id.btn_login);
        mTvRegister = this.findViewById(R.id.register);
        mLlParent = this.findViewById(R.id.ll_parent);
        mForgetPassword = findViewById(R.id.forget_password);
        loadingUtils = new LoadingUtils(LoginActivity.this, "登录中");
        aCache = ACache.get(this);
        gson = new Gson();
        mcDB = new MicroChatDB(this);
    }

    private void initListener() {
        mBtnLogin.setOnClickListener(this);
        mTvRegister.setOnClickListener(this);
        mForgetPassword.setOnClickListener(this);
        mLlParent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int statusBarHeight;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                // 使用最外层布局填充，进行测算计算
                mLlParent.getWindowVisibleDisplayFrame(r);
                int screenHeight = getWindow().getDecorView().getRootView().getHeight();
                int heightDiff = screenHeight - (r.bottom - r.top);
                if (heightDiff > 100) {
                    // 如果超过100个像素，它可能是一个键盘。获取状态栏的高度
                    statusBarHeight = 0;
                }
                try {
                    Class<?> c = Class.forName("com.android.internal.R$dimen");
                    Object obj = c.newInstance();
                    Field field = c.getField("status_bar_height");
                    int x = Integer.parseInt(field.get(obj).toString());
                    statusBarHeight = getApplicationContext().getResources().getDimensionPixelSize(x);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int realKeyboardHeight = heightDiff - statusBarHeight - Common.getBottomStatusHeight(getApplicationContext());
                Log.e("键盘", "keyboard height(单位像素) = " + realKeyboardHeight);
                if (realKeyboardHeight > 100) {
                    aCache.put("KeyboardHeight", realKeyboardHeight + "");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                String account = mEtAccount.getText().toString().toLowerCase();
                String token = mEtPassword.getText().toString();
                loadingUtils.show();
                loginAPP(account, token);
                break;
            case R.id.register:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;

            default:
                break;
            case R.id.forget_password:
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
                break;
        }
    }

    private void loginIM(String account, String token) {
        LoginInfo info = new LoginInfo(account, token); // config...
        RequestCallback<LoginInfo> callback =
                new RequestCallback<LoginInfo>() {

                    @Override
                    public void onException(Throwable arg0) {
                        System.out.println("--------------------------------");
                        System.out.println(arg0);
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == 302) {
                            Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        } else if (code == 408) {
                            Toast.makeText(LoginActivity.this, "登录超时", Toast.LENGTH_SHORT).show();
                        } else if (code == 415) {
                            Toast.makeText(LoginActivity.this, "未开网络", Toast.LENGTH_SHORT).show();
                        } else if (code == 416) {
                            Toast.makeText(LoginActivity.this, "连接有误，请稍后重试", Toast.LENGTH_SHORT).show();
                        } else if (code == 417) {
                            Toast.makeText(LoginActivity.this, "该账号已在另一端登录", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "未知错误，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSuccess(LoginInfo loginInfo) {
                        Log.e("TAG", "onSuccess: " + loginInfo + "======================================================");
                        aCache.put("autoLogin", "1");
                        Intent server = new Intent(LoginActivity.this,
                                IMService.class);
                        startService(server);
                        startActivity(new Intent(LoginActivity.this, TabHostActivity.class));
                        finish();
                    }
                };
        NIMClient.getService(AuthService.class).login(info)
                .setCallback(callback);
    }

    private int loginAPP(final String account, final String password) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                Bundle data = new Bundle();
                try {
                    final JSONObject loginData = new JSONObject();
                    loginData.put("account", account);
                    loginData.put("password", password);
                    String result = userService.Login(loginData);
                    JSONObject resultObj = new JSONObject(result);
                    String code = resultObj.getString("code");
                    String msg = resultObj.getString("msg");
                    Log.e("朋友", "run: " + result);
                    if (code.equals("200")) {
                        message.what = 200;
                        UserInfo userInfo = gson.fromJson(resultObj.getJSONObject("userInfo").toString(), UserInfo.class);
                        aCache.put("userInfo", (Serializable) userInfo);
                        List<FriendBean> friendBeans = gson.fromJson(resultObj.getJSONArray("friends").toString(), new TypeToken<List<FriendBean>>() {
                        }.getType());
                        updateFriends(friendBeans);
                        updateUserInfos(friendBeans);
                        aCache.put("account", resultObj.getString("account"));
                        aCache.put("token", password);
                        loginIM(resultObj.getString("account"), resultObj.getString("token"));
                    } else if (code.equals("414")) {
                        message.what = 414;
                    } else if (code.equals("808")) {
                        message.what = 808;
                    }
                    data.putSerializable("Msg", msg);
                    message.setData(data);
                    handler.sendMessage(message);
                } catch (ConnectTimeoutException e) {
                    e.printStackTrace();
                    data.putSerializable("Msg",
                            Common.MSG_REQUEST_TIMEOUT);
                    message.setData(data);
                    handler.sendMessage(message);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    data.putSerializable("Msg",
                            Common.MSG_RESPONSE_TIMEOUT);
                    message.setData(data);
                    handler.sendMessage(message);
                } catch (ServiceRulesException e) {
                    e.printStackTrace();
                    data.putSerializable("Msg",
                            e.getMessage());
                    message.setData(data);
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    data.putSerializable("Msg",
                            Common.MSG_LOGIN_ERROR);
                    message.setData(data);
                    handler.sendMessage(message);
                }
            }
        });
        return 0;
    }

    private class IHandler extends Handler {

        private final WeakReference<Activity> mActivity;

        public IHandler(LoginActivity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            ((LoginActivity) mActivity.get()).loadingUtils.dismiss();
            int flag = msg.what;
            String Msg = (String) msg.getData().getSerializable(
                    "Msg");
            switch (flag) {
                case 0:
                    ((LoginActivity) mActivity.get()).showTip(Msg);
                    break;
                case 200:
                    ((LoginActivity) mActivity.get())
                            .showTip(Msg);
                    break;
                case 404:
                    ((LoginActivity) mActivity.get())
                            .showTip(Msg);
                    break;
                case 414:
                    ((LoginActivity) mActivity.get()).showTip(Msg);
                    break;
                case 808:
                    NormalDialogOneBtn(Msg);
                    break;
                case 1000:
                    ((LoginActivity) mActivity.get()).showTip(Msg);
                    break;

                default:
                    break;
            }

        }
    }

    private void NormalDialogOneBtn(String msg) {
        final NormalDialog dialog = new NormalDialog(this);
        dialog.content(msg)//
                .btnNum(1)
                .btnText("确定")//
                .showAnim(mBasIn)//
                .dismissAnim(mBasOut)//
                .show();

        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
            }
        });
    }

    private IHandler handler = new IHandler(this);

    private void showTip(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void updateFriends(List<FriendBean> friendBeans) {
        mcDB.getWritableDatabase().beginTransaction();
        for (FriendBean friendBean : friendBeans) {
            mcDB.getWritableDatabase().execSQL("replace into " + MicroChatDB.T_FRIENDS + "(" + MicroChatDB.FriendsTable.ID + "," + MicroChatDB.FriendsTable.ACCOUNT + "," + MicroChatDB.FriendsTable.FROM_ACCOUNT + "," + MicroChatDB.FriendsTable.REMARKS + ") values ( '" + friendBean.getId() + "','" + friendBean.getAccount() + "','" + friendBean.getFromAccount() + "','" + friendBean.getRemarks() + "')");
        }
        mcDB.getWritableDatabase().setTransactionSuccessful();
        mcDB.getWritableDatabase().endTransaction();
    }

    private void updateUserInfos(List<FriendBean> friendBeans) {
        mcDB.getWritableDatabase().beginTransaction();
        for (FriendBean friendBean : friendBeans) {
            String sql = "replace into " + MicroChatDB.T_USERINFO
                    + "(" + MicroChatDB.UserInfoTable.ID + ","
                    + MicroChatDB.UserInfoTable.ACCOUNT + ","
                    + MicroChatDB.UserInfoTable.ICON + ","
                    + MicroChatDB.UserInfoTable.SIGN + ","
                    + MicroChatDB.UserInfoTable.EMAIL + ","
                    + MicroChatDB.UserInfoTable.BIRTH + ","
                    + MicroChatDB.UserInfoTable.MOBILE + ","
                    + MicroChatDB.UserInfoTable.GENDER + ","
                    + MicroChatDB.UserInfoTable.NICKNAME + ","
                    + MicroChatDB.UserInfoTable.EX + ") " +
                    "values ( '" + friendBean.getUserInfo().getId() + "','"
                    + friendBean.getUserInfo().getAccount() + "','"
                    + friendBean.getUserInfo().getIcon() + "','"
                    + friendBean.getUserInfo().getSign() + "','"
                    + friendBean.getUserInfo().getEmail() + "','"
                    + friendBean.getUserInfo().getBirth() + "','"
                    + friendBean.getUserInfo().getMobile() + "','"
                    + friendBean.getUserInfo().getGender() + "','"
                    + friendBean.getUserInfo().getNickname() + "','"
                    + friendBean.getUserInfo().getEx() + "')";
            mcDB.getWritableDatabase().execSQL(sql);
        }
        mcDB.getWritableDatabase().setTransactionSuccessful();
        mcDB.getWritableDatabase().endTransaction();
    }

}
