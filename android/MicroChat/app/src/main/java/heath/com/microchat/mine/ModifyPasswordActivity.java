package heath.com.microchat.mine;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


import org.json.JSONObject;

import heath.com.microchat.BaseActivity;
import heath.com.microchat.R;
import heath.com.microchat.service.IUserService;
import heath.com.microchat.service.impl.UserServiceImpl;
import heath.com.microchat.utils.ClearEditText;
import heath.com.microchat.utils.Common;
import heath.com.microchat.utils.LoadingUtils;
import heath.com.microchat.utils.ThreadUtils;
import heath.com.microchat.utils.ToastUtil;

public class ModifyPasswordActivity extends BaseActivity implements View.OnClickListener {
    private ClearEditText mEtAccount;
    private ClearEditText mEtPasswords;
    private ClearEditText mEtNewPasswords;
    private Button mBtnModifyPassword;
    private LoadingUtils loadingUtils;
    private IUserService userService;
    private LinearLayout mLlReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);
        initView();
        init();
        initListener();
    }

    private void initView() {
        mEtAccount = findViewById(R.id.et_account);
        mEtPasswords = findViewById(R.id.et_passwords);
        mEtNewPasswords = findViewById(R.id.et_new_passwords);
        mBtnModifyPassword = findViewById(R.id.btn_modify_password);
        mLlReturn = findViewById(R.id.ll_return);
    }

    private void init() {
        loadingUtils = new LoadingUtils(ModifyPasswordActivity.this, "修改密码中");
        loadingUtils.creat();
        userService = new UserServiceImpl();
    }

    private void initListener() {
        mBtnModifyPassword.setOnClickListener(this);
        mLlReturn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_modify_password:
                loadingUtils.show();
                modifyPassword();
                break;
            case R.id.ll_return:
                finish();
                break;
        }
    }

    private void modifyPassword() {
        final String account = mEtAccount.getText().toString();
        final String password = mEtPasswords.getText().toString();
        final String newPassword = mEtNewPasswords.getText().toString();
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                JSONObject parameterData = new JSONObject();
                try {
                    parameterData.put("account", account);
                    parameterData.put("password", password);
                    parameterData.put("newPassword", newPassword);
                    String result = userService.modifyPassword(parameterData);
                    JSONObject resultObj = new JSONObject(result);
                    String code = resultObj.getString("code");
                    String msg = resultObj.getString("msg");
                    loadingUtils.dismissOnUiThread();
                    if (code.equals("200")) {
                        aCache.put("token", newPassword);
                        ToastUtil.toastOnUiThread(ModifyPasswordActivity.this, msg);
                        finish();
                    } else {
                        ToastUtil.toastOnUiThread(ModifyPasswordActivity.this, msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.toastOnUiThread(ModifyPasswordActivity.this, Common.MSG_SEND_EXCEPTION);
                    loadingUtils.dismissOnUiThread();
                }
            }
        });
    }
}
