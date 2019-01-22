package heath.com.microchat.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import heath.com.microchat.BaseActivity;
import heath.com.microchat.LoginActivity;
import heath.com.microchat.R;
import heath.com.microchat.service.IUserService;
import heath.com.microchat.service.ServiceRulesException;
import heath.com.microchat.service.impl.UserServiceImpl;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ToastUtil;

public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {

    private IUserService userService = new UserServiceImpl();

    private LinearLayout mLlReturn;
    private Button mBtnResetPassword;
    private EditText mEtAccount;
    private EditText mEtpassword;
    private EditText mEtVerCode;
    private Button mBtnGetCode;
    private LoadingUtils loadingUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        initView();
        init();
        initListener();
        // 注册一个事件回调，用于处理SMSSDK接口请求的结果
        SMSSDK.registerEventHandler(eventHandler);
    }

    @Override
    protected void onDestroy() {
        SMSSDK.unregisterEventHandler(eventHandler);
        super.onDestroy();
    }

    private void initView() {
        mLlReturn = this.findViewById(R.id.ll_return);
        mBtnResetPassword = this.findViewById(R.id.btn_reset_password);
        mEtAccount = this.findViewById(R.id.et_account);
        mEtpassword = this.findViewById(R.id.et_passwords);
        mEtVerCode = this.findViewById(R.id.et_ver_code);
        mBtnGetCode = this.findViewById(R.id.btn_get_code);
        loadingUtils = new LoadingUtils(ForgetPasswordActivity.this, "重置密码中");

    }

    private void init() {
        loadingUtils.creat();
    }

    private void initListener() {
        mLlReturn.setOnClickListener(this);
        mBtnResetPassword.setOnClickListener(this);
        mBtnGetCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final String account = mEtAccount.getText().toString();
        final String password = mEtpassword.getText().toString();
        final String code = mEtVerCode.getText().toString();
        switch (view.getId()) {
            case R.id.ll_return:
                startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.btn_reset_password:
                if (code.length() >= 4) {
                    if (Common.isPhone(account)) {
                        if (password.length() >= 6) {
                            loadingUtils.show();
                            // 提交验证码，其中的code表示验证码，如“1357”
                            SMSSDK.submitVerificationCode("86", account, code);
                        } else {
                            Toast.makeText(this, "密码少于6位", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "请输入正确手机号码", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "验证码有误", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btn_get_code:
                if (Common.isPhone(account)) {
                    CountDownTimer time = new CountDownTimer(60000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            mBtnGetCode.setEnabled(false);
                            mBtnGetCode.setText(((millisUntilFinished / 1000)) + "s");
                            mBtnGetCode.setBackgroundResource(R.color.gray);
                        }

                        @Override
                        public void onFinish() {
                            mBtnGetCode.setEnabled(true);
                            mBtnGetCode.setText("重新获取验证码");
                            mBtnGetCode.setBackgroundResource(R.color.deepskyblue);
                        }
                    }.start();
                    // 请求验证码，其中country表示国家代码，如“86”；phone表示手机号码，如“13800138000”
                    SMSSDK.getVerificationCode("86", account);

                } else {
                    ToastUtil.toastOnUiThread(ForgetPasswordActivity.this, "请输入正确手机号码");
                }

                break;
            default:
                break;
        }
    }

    private void resetPassword(final String account, final String password) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                Bundle data = new Bundle();
                try {
                    final JSONObject registerData = new JSONObject();
                    registerData.put("bindMobile", account);
                    registerData.put("password", password);
                    String result = userService.resetPassword(registerData);
                    JSONObject resultObj = new JSONObject(result);
                    String code = resultObj.getString("code");
                    String msg = resultObj.getString("msg");
                    if (code.equals("200")) {
                        message.what = 200;
                    } else if (code.equals("414")) {
                        message.what = 414;
                    } else {
                        message.what = 1000;
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
                            Common.MSG_REGISTER_ERROR);
                    message.setData(data);
                    handler.sendMessage(message);
                }
            }
        });
        thread.start();
    }

    private static class IHandler extends Handler {

        private final WeakReference<Activity> mActivity;

        public IHandler(ForgetPasswordActivity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            ((ForgetPasswordActivity) mActivity.get()).loadingUtils.dismiss();
            int flag = msg.what;
            String Msg = (String) msg.getData().getSerializable(
                    "Msg");
            switch (flag) {
                case 0:
                    ((ForgetPasswordActivity) mActivity.get()).showTip(Msg);
                    break;
                case 200:
                    ((ForgetPasswordActivity) mActivity.get())
                            .showTip(Msg);
                    ((ForgetPasswordActivity) mActivity.get()).finish();
                    break;
                case 414:
                    ((ForgetPasswordActivity) mActivity.get()).showTip(Msg);
                    break;
                case 1000:
                    ((ForgetPasswordActivity) mActivity.get()).showTip(Msg);
                    break;

                default:
                    break;
            }

        }
    }

    private IHandler handler = new IHandler(this);

    private void showTip(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    EventHandler eventHandler = new EventHandler() {
        public void afterEvent(int event, int result, Object data) {
            // afterEvent会在子线程被调用，因此如果后续有UI相关操作，需要将数据发送到UI线程
            Message msg = new Message();
            msg.arg1 = event;
            msg.arg2 = result;
            msg.obj = data;
            new Handler(Looper.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    int event = msg.arg1;
                    int result = msg.arg2;
                    Object data = msg.obj;
                    if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            // TODO 处理成功得到验证码的结果
                            // 请注意，此时只是完成了发送验证码的请求，验证码短信还需要几秒钟之后才送达
                            Log.e("TAG", "handleMessage: " + "成功发送验证码===================================");
                        } else {
                            // TODO 处理错误的结果
                            Log.e("TAG", "handleMessage: " + "失败发送验证码===================================");
                            ((Throwable) data).printStackTrace();
                        }
                    } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            // TODO 处理验证码验证通过的结果
                            final String account = mEtAccount.getText().toString();
                            final String password = mEtpassword.getText().toString();
                            resetPassword(account, password);
                        } else {
                            // TODO 处理错误的结果
                            ((Throwable) data).printStackTrace();
                            ToastUtil.toastOnUiThread(ForgetPasswordActivity.this, Common.MSG_CODE_ERROR);
                            loadingUtils.dismiss();
                        }
                    }
                    // TODO 其他接口的返回结果也类似，根据event判断当前数据属于哪个接口
                    return false;
                }
            }).sendMessage(msg);
        }
    };
}
