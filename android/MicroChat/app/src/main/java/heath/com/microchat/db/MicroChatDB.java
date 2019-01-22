package heath.com.microchat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class MicroChatDB extends SQLiteOpenHelper {

    public static final String T_MESSAGE = "t_message";
    public static final String T_FRIENDS = "t_friends";
    public static final String T_USERINFO = "t_userinfo";

    public MicroChatDB(Context context) {
        super(context, "MicroChat.db", null, 4);
    }

    public class MessageTable implements BaseColumns {
        /**
         * from_account;//发送者 account//接收者 content//消息内容 status//发送状态 message_type//消息类型
         * send_time//发送时间
         * duration 语音时间
         */
        public static final String ID = "_id";
        public static final String FROM_ACCOUNT = "from_account";
        public static final String ACCOUNT = "account";
        public static final String CONTENT = "content";
        public static final String THUMB = "thumb";
        public static final String STATE = "state";
        public static final String MESSAGE_TYPE = "message_type";
        public static final String SEND_TIME = "send_time";
        public static final String DURATION = "duration";
        public static final String SESSION_TYPE = "session_type";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
    }

    public class FriendsTable implements BaseColumns {
        public static final String FROM_ACCOUNT = "from_account";
        public static final String ID = "_id";
        public static final String ACCOUNT = "account";
        public static final String REMARKS = "remarks";
    }

    public class UserInfoTable implements BaseColumns {
        public static final String ID = "_id";
        public static final String ACCOUNT = "account";
        public static final String ICON = "icon";
        public static final String SIGN = "sign";
        public static final String EMAIL = "email";
        public static final String BIRTH = "birth";
        public static final String MOBILE = "mobile";
        public static final String GENDER = "gender";
        public static final String NICKNAME = "nickname";
        public static final String EX = "ex";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String message = "CREATE TABLE " + T_MESSAGE
                + "(" + MessageTable.ID + " TEXT PRIMARY KEY,"
                + MessageTable.FROM_ACCOUNT + " TEXT,"
                + MessageTable.ACCOUNT + " TEXT,"
                + MessageTable.CONTENT + " TEXT,"
                + MessageTable.THUMB + " TEXT,"
                + MessageTable.STATE + " TEXT,"
                + MessageTable.MESSAGE_TYPE + " TEXT,"
                + MessageTable.SEND_TIME + " TEXT,"
                + MessageTable.SESSION_TYPE + " TEXT,"
                + MessageTable.WIDTH + " TEXT,"
                + MessageTable.HEIGHT + " TEXT,"
                + MessageTable.DURATION + " TEXT);";
        String friends = "CREATE TABLE " + T_FRIENDS
                + "(" + FriendsTable.ID + " INTEGER PRIMARY KEY,"
                + FriendsTable.FROM_ACCOUNT + " TEXT,"
                + FriendsTable.ACCOUNT + " TEXT,"
                + FriendsTable.REMARKS + " TEXT);";
        String userinfo = "CREATE TABLE " + T_USERINFO
                + "(" + UserInfoTable.ID + " INTEGER PRIMARY KEY,"
                + UserInfoTable.ACCOUNT
                + " TEXT," + UserInfoTable.ICON
                + " TEXT," + UserInfoTable.SIGN
                + " TEXT," + UserInfoTable.EMAIL
                + " TEXT," + UserInfoTable.BIRTH
                + " TEXT," + UserInfoTable.MOBILE
                + " TEXT," + UserInfoTable.GENDER
                + " TEXT," + UserInfoTable.NICKNAME
                + " TEXT," + UserInfoTable.EX + " TEXT);";
        db.execSQL(message);
        db.execSQL(friends);
        db.execSQL(userinfo);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_MESSAGE);
        db.execSQL("DROP TABLE IF EXISTS " + T_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERINFO);
        onCreate(db);
    }
}
