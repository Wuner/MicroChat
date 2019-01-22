package heath.com.microchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
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
import java.net.SocketTimeoutException;
import java.util.List;

import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.db.MicroChatDB;
import heath.com.microchat.entity.FriendBean;
import heath.com.microchat.entity.UserInfo;
import heath.com.microchat.service.IMService;
import heath.com.microchat.service.IUserService;
import heath.com.microchat.service.ServiceRulesException;
import heath.com.microchat.service.impl.UserServiceImpl;
import heath.com.microchat.utils.ACache;
import heath.com.microchat.utils.Common;

public class SplashActivity extends BaseActivity {

	private IUserService userService = new UserServiceImpl();
	private ACache aCache;
	private Gson gson = new Gson();
	private MicroChatDB mcDB;
	private BaseAnimatorSet mBasIn;
	private BaseAnimatorSet mBasOut;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        aCache = ACache.get(this);
		// 停留3s,进入登录界面
		ThreadUtils.runInThread(new Runnable() {
			@Override
			public void run() {
				// 休眠3s
				SystemClock.sleep(3000);
				// 进入主界面
				init();
			}
		});
	}

	private void init() {
		mBasIn = new BounceTopEnter();
		mBasOut = new SlideBottomExit();
		String account = aCache.getAsString("account");
		String token = aCache.getAsString("token");
		String autoLogin = aCache.getAsString("autoLogin");
		mcDB = new MicroChatDB(this);
		if (autoLogin != null && autoLogin.equals("1")) {
			loginAPP(account, token);
		}else{
            Intent intent = new Intent(SplashActivity.this,
                    LoginActivity.class);
            startActivity(intent);
			finish();
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
							Toast.makeText(SplashActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
						} else if (code == 408) {
							Toast.makeText(SplashActivity.this, "登录超时", Toast.LENGTH_SHORT).show();
						} else if (code == 415) {
							Toast.makeText(SplashActivity.this, "未开网络", Toast.LENGTH_SHORT).show();
						} else if (code == 416) {
							Toast.makeText(SplashActivity.this, "连接有误，请稍后重试", Toast.LENGTH_SHORT).show();
						} else if (code == 417) {
							Toast.makeText(SplashActivity.this, "该账号已在另一端登录", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(SplashActivity.this, "未知错误，请稍后重试", Toast.LENGTH_SHORT).show();
						}
						startActivity(new Intent(SplashActivity.this, LoginActivity.class));
						finish();
					}

					@Override
					public void onSuccess(LoginInfo loginInfo) {
						Log.e("TAG", "onSuccess: " + loginInfo + "======================================================");
						Intent server = new Intent(SplashActivity.this,
								IMService.class);
						startService(server);
						startActivity(new Intent(SplashActivity.this, TabHostActivity.class));
						finish();
					}
				};
		NIMClient.getService(AuthService.class).login(info)
				.setCallback(callback);
	}

	private int loginAPP(final String account, final String password) {
		Thread thread = new Thread(new Runnable() {
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
					if (code.equals("200")) {
						message.what = 200;
						UserInfo userInfo = gson.fromJson(resultObj.getJSONObject("userInfo").toString(),UserInfo.class);
						aCache.put("userInfo",(Serializable) userInfo);
						List<FriendBean> friendBeans = gson.fromJson(resultObj.getJSONArray("friends").toString(), new TypeToken<List<FriendBean>>() {
						}.getType());
						Log.i("朋友", "run: "+result);
						updateFriends(friendBeans);
						updateUserInfos(friendBeans);
						loginIM(resultObj.getString("account"), resultObj.getString("token"));
					} else if (code.equals("414")) {
						message.what = 414;
					}else if (code.equals("808")) {
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
		thread.start();
		return 0;
	}

	private class IHandler extends Handler {

		private final WeakReference<Activity> mActivity;

		public IHandler(SplashActivity activity) {
			mActivity = new WeakReference<Activity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			int flag = msg.what;
			String Msg = (String) msg.getData().getSerializable(
					"Msg");
			switch (flag) {
				case 0:
					((SplashActivity) mActivity.get()).showTip(Msg);
					((SplashActivity) mActivity.get()).startActivity(new Intent(mActivity.get(), LoginActivity.class));
					((SplashActivity) mActivity.get()).finish();
					break;
				case 200:
					((SplashActivity) mActivity.get())
							.showTip(Msg);
					break;
				case 404:
					((SplashActivity) mActivity.get())
							.showTip(Msg);
					break;
				case 414:
					((SplashActivity) mActivity.get()).showTip(Msg);
					((SplashActivity) mActivity.get()).startActivity(new Intent(mActivity.get(), LoginActivity.class));
					((SplashActivity) mActivity.get()).finish();
					break;
				case 808:
					NormalDialogOneBtn(Msg);
					break;
				case 1000:
					((SplashActivity) mActivity.get()).showTip(Msg);
					break;

				default:
					break;
			}

		}
	}

	private SplashActivity.IHandler handler = new SplashActivity.IHandler(this);

	private void showTip(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	public void updateFriends(final List<FriendBean> friendBeans) {
		ThreadUtils.runInUIThread(new Runnable() {
			@Override
			public void run() {
				mcDB.getWritableDatabase().beginTransaction();
				for (FriendBean friendBean : friendBeans) {
					String sql = "replace into " + MicroChatDB.T_FRIENDS + "("
							+ MicroChatDB.FriendsTable.ID + ","
							+ MicroChatDB.FriendsTable.ACCOUNT + ","
							+ MicroChatDB.FriendsTable.FROM_ACCOUNT + ","
							+ MicroChatDB.FriendsTable.REMARKS + ") values ( '"
							+ friendBean.getId() + "','"
							+ friendBean.getAccount() + "','"
							+ friendBean.getFromAccount() + "','"
							+ friendBean.getRemarks() + "')";
					mcDB.getWritableDatabase().execSQL(sql);
				}
				mcDB.getWritableDatabase().setTransactionSuccessful();
				mcDB.getWritableDatabase().endTransaction();
			}
		});
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
			Log.e("dql", "updateUserInfos: "+sql);
			mcDB.getWritableDatabase().execSQL(sql);
		}
		mcDB.getWritableDatabase().setTransactionSuccessful();
		mcDB.getWritableDatabase().endTransaction();
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
				startActivity(new Intent(SplashActivity.this, LoginActivity.class));
				finish();
			}
		});
	}
}