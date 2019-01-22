package heath.com.microchat.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import heath.com.microchat.TabHostActivity;
import heath.com.microchat.db.MicroChatDB;

public class MicroChatProvider extends ContentProvider {
    private static final String AUTHORITIES = MicroChatProvider.class
            .getCanonicalName();

    public static final int MESSAGE = 1;
    public static final int COUNT = 2;
    public static final int ALL_COUNT = 3;
    public static final int QUERY_FRIENDS = 4;
    public static final int TEAM_COUNT = 5;
    public static final int QUERY_USERINFO = 6;

    public static Uri URI_MESSAGE = Uri.parse("content://" + AUTHORITIES + "/message");
    public static Uri URI_COUNT = Uri.parse("content://" + AUTHORITIES + "/count");
    public static Uri URI_ALL_COUNT = Uri.parse("content://" + AUTHORITIES + "/all_count");
    public static Uri URI_QUERY_FRIENDS = Uri.parse("content://" + AUTHORITIES + "/query_friends");
    public static Uri URI_TEAM_COUNT = Uri.parse("content://" + AUTHORITIES + "/team_count");
    public static Uri URI_QUERY_USERINFO = Uri.parse("content://" + AUTHORITIES + "/query_userinfo");
    private MicroChatDB mHelper;

    static UriMatcher mUriMatcher;
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 添加一个匹配的规则
        mUriMatcher.addURI(AUTHORITIES, "/message", MESSAGE);
        mUriMatcher.addURI(AUTHORITIES, "/count", COUNT);
        mUriMatcher.addURI(AUTHORITIES, "/all_count", ALL_COUNT);
        mUriMatcher.addURI(AUTHORITIES, "/query_friends", QUERY_FRIENDS);
        mUriMatcher.addURI(AUTHORITIES, "/team_count", TEAM_COUNT);
        mUriMatcher.addURI(AUTHORITIES, "/query_userinfo", QUERY_USERINFO);
    }
    @Override
    public String getType(Uri arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean onCreate() {
        mHelper = new MicroChatDB(getContext());
        if (mHelper != null) {
            return true;
        }
        return false;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (mUriMatcher.match(uri)) {
            case MESSAGE:
                // 插入之后对于的id
                long id = mHelper.getWritableDatabase().insert(MicroChatDB.T_MESSAGE,
                        "", values);
                if (id > 0) {
                    System.out
                            .println("--------------MESSAGEProvider insertSuccess--------------");
                    uri = ContentUris.withAppendedId(uri, id);
                    // 发送数据改变的信号
                    getContext().getContentResolver().notifyChange(
                            MicroChatProvider.URI_MESSAGE, null);
                }
                break;

            default:
                break;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleteCount = 0;
        switch (mUriMatcher.match(uri)) {
            case MESSAGE:
                // 具体删除了几条数据
                deleteCount = mHelper.getWritableDatabase().delete(
                        MicroChatDB.T_MESSAGE, selection, selectionArgs);
                if (deleteCount > 0) {
                    System.out
                            .println("--------------MESSAGEProvider deleteSuccess--------------");
                    // 发送数据改变的信号
                    getContext().getContentResolver().notifyChange(
                            MicroChatProvider.URI_MESSAGE, null);
                }
                break;
            case QUERY_FRIENDS:
                // 具体删除了几条数据
                deleteCount = mHelper.getWritableDatabase().delete(
                        MicroChatDB.T_FRIENDS, selection, selectionArgs);
                if (deleteCount > 0) {
                    System.out
                            .println("--------------MESSAGEProvider deleteSuccess--------------");
                }
                break;

            default:
                break;
        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int updateCount = 0;
        switch (mUriMatcher.match(uri)) {
            case MESSAGE:
                // 更新了几条数据
                updateCount = mHelper.getWritableDatabase().update(
                        MicroChatDB.T_MESSAGE, values, selection, selectionArgs);
                if (updateCount > 0) {
                    TabHostActivity.queryMessageCount();
                    System.out
                            .println("--------------MESSAGEProvider updateSuccess--------------");
                }
                break;
            case QUERY_FRIENDS:
                // 更新了几条数据
                updateCount = mHelper.getWritableDatabase().update(
                        MicroChatDB.T_FRIENDS, values, selection, selectionArgs);
                if (updateCount > 0) {
                    System.out
                            .println("--------------MESSAGEProvider updateSuccess--------------");
                }
                break;

            default:
                break;
        }
        return updateCount;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        switch (mUriMatcher.match(uri)) {
            case MESSAGE:
                cursor = mHelper.getReadableDatabase()
                        .query(MicroChatDB.T_MESSAGE, projection, selection,
                                selectionArgs, null, null, sortOrder);
                System.out
                        .println("--------------MESSAGEProvider querySuccess--------------");
                break;
            case COUNT:
                cursor = db
                        .rawQuery(
                                "select COUNT(*) count from t_message where state = 0 and from_account = ? and account = ?",
                                selectionArgs);
                break;
            case TEAM_COUNT:
                cursor = db
                        .rawQuery(
                                "select COUNT(*) count from t_message where state = 0 and (from_account = ? or account = ?)",
                                selectionArgs);
                break;
            case ALL_COUNT:
                cursor = db
                        .rawQuery(
                                "select COUNT(*) count from t_message where state = 0",
                                selectionArgs);
                break;
            case QUERY_FRIENDS:
                cursor = db
                        .rawQuery(
                                "select f.remarks,f.account,ui.account from_account,ui.icon,ui.sign,ui.email,ui.birth,ui.mobile,ui.gender,ui.nickname,ui.ex from "+MicroChatDB.T_FRIENDS+" f left join "+MicroChatDB.T_USERINFO+" ui on f.from_account = ui.account where f.account = ?",
                                selectionArgs);
                break;
            case QUERY_USERINFO:
                cursor = db
                        .rawQuery(
                                "select * from "+MicroChatDB.T_USERINFO+" where account = ?",
                                selectionArgs);
                break;
            default:
                break;
        }
        return cursor;
    }
}