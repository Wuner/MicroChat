package heath.com.microchat.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {
    public static final String HTTP_ADDRESS = "http://192.168.43.155:8080/MicroChat/";
//    public static final String HTTP_ADDRESS = "http://192.168.1.128:8080/MicroChat/";
//    public static final String HTTP_ADDRESS = "http://192.168.0.130:8080/MicroChat/";
//    public static final String HTTP_ADDRESS = "http://testwx.club:8080/MicroChat/";
    public static final String MSG_SERVER_ERROR = "请求服务器错误！";
    public static final String MSG_REQUEST_TIMEOUT = "请求连接服务器超时！";
    public static final String MSG_RESPONSE_TIMEOUT = "服务器响应超时！";
    public static final String MSG_SEND_EXCEPTION = "发送异常！";
    public static final String NO_MORE_DATA = "没有更多数据了！";
    public static final String MSG_RESPONSE_NOT_STARTING = "服务器未启动，请联系管理员！";
    public static final String MSG_REGISTER_ERROR = "注册出错！";
    public static final String MSG_LOGIN_ERROR = "登录出错！";
    public static final String MSG_CODE_ERROR = "验证码错误！";
    public static final String USER_FOLDER_PATH = "upload/user_icon";
    public static final String TEAM_FOLDER_PATH = "upload/team_icon";
    public static final String MESSAGE_PATH = "upload/message";
    public static final String DYNAMIC_PICTURE_PATH = "upload/dynamic_picture";
    public static final String DYNAMIC_VIDEO_PATH = "upload/dynamic_video";
    public static final String SESSION_TYPE_TEAM = "Team";
    public static final String SESSION_TYPE_P2P = "P2P";
    public static final String DYNAMIC_TYPE_IMAGE_TEXT = "ImageText";
    public static final String DYNAMIC_TYPE_VIDEO = "Video";
    public static final int NETWORK_PICTURE = 0;//网络图片
    public static final int LOCAL_PICTURE = 1;//本地图片

    public static boolean isEmail(String string) {
        if (string == null)
            return false;
        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(string);
        if (m.matches())
            return true;
        else
            return false;
    }

    private final static int[] dayArr = new int[]{20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};
    private final static String[] constellationArr = new String[]{"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};


    public static String getConstellation(Date birthday) {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthday)) {
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        cal.setTime(birthday);
        int monthBirth = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        return dayOfMonthBirth < dayArr[monthBirth - 1] ? constellationArr[monthBirth - 1] : constellationArr[monthBirth];
    }

    /**
     * 根据用户生日计算年龄
     */
    public static int getAgeByBirthday(Date birthday) {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthday)) {
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(birthday);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                age--;
            }
        }
        return age;
    }

    //判断是否是手机号
    private static final String PHONE_PATTERN = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17([0,1,6,7,]))|(18[0-2,5-9]))\\d{8}$";

    public static final boolean isPhone(String phonenumber) {
        boolean isPhone = Pattern.compile(PHONE_PATTERN).matcher(phonenumber).matches();
        return isPhone;
    }

    //隐藏键盘
    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static List<HashMap<String, Object>> removal(List<HashMap<String, Object>> recents) {
        List<HashMap<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < recents.size(); i++) {
            HashMap<String, Object> recent = recents.get(i);
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).get("fromAccount").equals(recent.get("fromAccount"))) {
                    list.remove(j);
                }
            }
            list.add(recent);
        }
        Log.e("list去重", "removal: " + list.toString());
        return list;
    }

    /**
     * 获取 虚拟按键的高度
     *
     * @param context
     * @return
     */
    public static int getBottomStatusHeight(Context context) {
        int totalHeight = getDpi(context);

        int contentHeight = getScreenHeight(context);

        return totalHeight - contentHeight;
    }

    /**
     * 获取精确的屏幕大小
     */
    private static int getDpi(Context context) {
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
            dpi = displayMetrics.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    private static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public static void asyncloadText(TextView view, String text) {
        Common.AsyncTextTask task = new Common.AsyncTextTask(view);
        task.execute(text);
    }

    private final static class AsyncTextTask extends AsyncTask<String, String, String> {

        private TextView view;

        public AsyncTextTask(TextView view) {
            this.view = view;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return params[0];
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String text) {
            // TODO Auto-generated method stub
            view.setText(text);
        }
    }

    //给btn控件设置属性
    public static List<Map<String, Object>> setBtn(String[] texts, int[] ids, int[] index) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("text", texts[i]);
            map.put("id", ids[i]);
            map.put("index", index[i]);
            list.add(map);
        }
        return list;
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static JSONArray getTeamMembers(String tid) {
        final JSONArray memberArray = new JSONArray();
        NIMClient.getService(TeamService.class).queryMemberList(tid).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {
                for (TeamMember member : members) {
                    if (member.isInTeam()) {
                        Log.e("组员id", "run: " + member.getAccount());
                        memberArray.put(member.getAccount());
                    }
                }
            }
        });
        return memberArray;
    }

    public static JSONArray remove(JSONArray array, String item) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            if (array.get(i).equals(item)) {
                array.remove(i);
            }
        }
        return array;
    }

    public static String getRandomCode() {
        String[] code = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
                "u", "v", "w", "s", "y", "z"
        };
        String randomCode = "";
        for (int i = 0; i < 20; i++) {
            int random = (int) (Math.random() * 36);
            randomCode += code[random];
        }
        return randomCode;
    }

    //根据路径得到视频缩略图
    public static Bitmap getVideoPhoto(String videoPath) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(videoPath);
        Bitmap bitmap = media.getFrameAtTime();
        return bitmap;
    }

    //获取视频总时长
    public static int getVideoDuration(String path) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        String duration = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); //
        return Integer.parseInt(duration);
    }

    public static void showSoftInputFromWindow(Activity activity, EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }

    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    private static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    public static String conversionTime(long time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String lastSendTime = simpleDateFormat.format(date);
        String timeStr = null;
        try {
            if (TimeUtils.IsToday(lastSendTime)) {
                timeStr = lastSendTime.substring(11, 16);
            } else if (TimeUtils.IsYesterday(lastSendTime)) {
                timeStr = "昨天";
            } else if (!TimeUtils.IsToyear(lastSendTime)) {
                timeStr = lastSendTime.substring(0, 10);
            } else {
                timeStr = lastSendTime.substring(5, 10);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStr;
    }
    public static String conversionTimeDate(long time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String lastSendTime = simpleDateFormat.format(date);
        return lastSendTime;
    }

}
