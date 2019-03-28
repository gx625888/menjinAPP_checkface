package com.zzhou.entrance.guard.source;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.zzhou.entrance.guard.util.LogUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


/**
 * A Provider for IM
 *
 * @author zhouzhen
 */
public class WsProvider extends ContentProvider {

    private static final String TAG = "WsProvider";
    private static final boolean DBG = true;

    private static final String DATABASE_NAME = "zguard.db";
    private static final int DATABASE_VERSION = 5;

    private static final int MATCH_ACCOUNTS = 1;
    private static final int MATCH_ACCOUNT_BY_ID = 2;
    private static final int MATCH_HOUSE = 11;
    private static final int MATCH_HOUSE_BY_NO = 12;

    protected final UriMatcher mUrlMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private DatabaseHelper mOpenHelper;
    private String mDatabaseName;
    private int mDatabaseVersion;

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, mDatabaseName, null, mDatabaseVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Ws.AccountTable.CREATE_TABLE_SQL);
            db.execSQL(Ws.HouseTable.CREATE_TABLE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            destroyOldTables(db);
            onCreate(db);
        }

        private void destroyOldTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + Ws.AccountTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Ws.HouseTable.TABLE_NAME);
        }

    }

    public WsProvider() {
        this(DATABASE_NAME, DATABASE_VERSION);
        setupImUrlMatchers(Ws.AUTHORITY);
    }

    protected WsProvider(String databaseName, int databaseVersion) {
        this.mDatabaseName = databaseName;
        this.mDatabaseVersion = databaseVersion;
    }

    private void setupImUrlMatchers(String authority) {
        mUrlMatcher.addURI(authority, Ws.AccountTable.PATH, MATCH_ACCOUNTS);
        mUrlMatcher.addURI(authority, Ws.AccountTable.PATH_CARD + "/#", MATCH_ACCOUNT_BY_ID);
        mUrlMatcher.addURI(authority, Ws.HouseTable.PATH, MATCH_HOUSE);
        mUrlMatcher.addURI(authority, Ws.HouseTable.PATH_NO + "/#", MATCH_HOUSE_BY_NO);
    }

    @Override
    public String getType(Uri uri) {
        int match = mUrlMatcher.match(uri);
        switch (match) {
            case MATCH_ACCOUNTS:
                return Ws.AccountTable.CONTENT_TYPE;
            case MATCH_ACCOUNT_BY_ID:
                return Ws.AccountTable.CONTENT_ITEM_TYPE;
            case MATCH_HOUSE:
                return Ws.HouseTable.CONTENT_TYPE;
            case MATCH_HOUSE_BY_NO:
                return Ws.HouseTable.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknow URL");
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection,
                        final String selection, final String[] selectionArgs,
                        final String sortOrder) {
        if (DBG) {
            log("url " + uri.toString());
        }
        return queryInternal(uri, projection, selection, selectionArgs,
                sortOrder);
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        if (DBG) {
            log("url " + uri.toString());
        }
        Uri result;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            result = insertInternal(uri, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (result != null) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (DBG) {
            log("url " + uri.toString());
        }
        int result;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            result = deleteInternal(uri, selection, selectionArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (result > 0) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }

        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int result = 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            result = updateInternal(uri, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return result;
    }

    private int updateInternal(Uri url, ContentValues values, String selection,
                               String[] selectionArgs) {
        String tableToChanged = null;
        long userStutas;
        StringBuilder whereClause = new StringBuilder();
        if (selection != null) {
            whereClause.append(selection);
        }

        int match = mUrlMatcher.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (match) {
            case MATCH_ACCOUNTS:
                tableToChanged = Ws.AccountTable.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("can't update the url" + url);
        }

        int count = db.update(tableToChanged, values, whereClause.toString(),
                selectionArgs);

        return count;
    }

    public Uri insertInternal(Uri uri, ContentValues values) {
        Uri resultUri = null;
        long rowId = 0;
        if (DBG) {
            log("url " + uri.toString());
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = mUrlMatcher.match(uri);

        switch (match) {
            case MATCH_ACCOUNTS:
                int isAdd = 0;
                if (values.containsKey("isAdd")) {
                    isAdd = values.getAsInteger("isAdd");
                    values.remove("isAdd");
                }
                if (isAdd == 0) {
                    rowId = db.insert(Ws.AccountTable.TABLE_NAME, null, values);
                    if (rowId > 0) {
                        resultUri = Uri.parse(Ws.AccountTable.CONTENT_URI + "/" + rowId);
                    }
                } else {
                    delete(Uri.parse(Ws.AccountTable.CONTENT_URI + "/" + values.getAsString(Ws.AccountTable.ACCOUNT_ID)), null
                            , null);
                }
                break;
            case MATCH_HOUSE:
                    rowId = db.insert(Ws.HouseTable.TABLE_NAME, null, values);
                    if (rowId > 0) {
                        resultUri = Uri.parse(Ws.HouseTable.CONTENT_URI + "/" + rowId);
                    }
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: "
                        + uri);
        }
        return resultUri;
    }

    public Cursor queryInternal(Uri uri, String[] projection, String selection,
                                String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        StringBuilder whereClause = new StringBuilder();
        if (selection != null) {
            whereClause.append(selection);
        }

        String groupBy = null;
        String limit = null;

        int match = mUrlMatcher.match(uri);
        if (DBG) {
            log("query url " + uri + ", match " + match + ", where "
                    + selection);
            if (selectionArgs != null) {
                for (String arg : selectionArgs) {
                    log(" selectionArg:" + arg);
                }
            }
        }

        switch (match) {
            case MATCH_ACCOUNT_BY_ID:
                appendWhere(whereClause, Ws.AccountTable.CARD, "=", uri
                        .getPathSegments().get(1));
            case MATCH_ACCOUNTS:
                qb.setTables(Ws.AccountTable.TABLE_NAME);
                break;
            case MATCH_HOUSE:
                qb.setTables(Ws.HouseTable.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknow URL");
        }

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = null;

        try {
            c = qb.query(db, projection, whereClause.toString(), selectionArgs,
                    groupBy, null, sortOrder, limit);
            if (c != null) {
                c.setNotificationUri(getContext().getContentResolver(), uri);
            }
        } catch (Exception e) {
            LogUtils.e("query db caugh \n" + e);
        }

        return c;
    }

    private int deleteInternal(Uri url, String userWhere, String[] whereArgs) {
        String tableToChange;

        String idColumnName = null;
        String changedItemId = null;

        StringBuilder whereClause = new StringBuilder();
        if (userWhere != null) {
            whereClause.append(userWhere);
        }

        int match = mUrlMatcher.match(url);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (match) {
            case MATCH_ACCOUNTS:
                tableToChange = Ws.AccountTable.TABLE_NAME;
                break;
            case MATCH_ACCOUNT_BY_ID:
                tableToChange = Ws.AccountTable.TABLE_NAME;
                changedItemId = url.getPathSegments().get(1);
                idColumnName = Ws.AccountTable.CARD;
                break;
            case MATCH_HOUSE:
                tableToChange = Ws.HouseTable.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Can't delete the url");
        }

        if (idColumnName == null) {
            idColumnName = "_id";
        }

        if (changedItemId != null) {
            appendWhere(whereClause, idColumnName, "=", changedItemId);
        }

        if (DBG)
            log("delete from " + url + " WHERE  " + whereClause);

        int count = db.delete(tableToChange, whereClause.toString(), whereArgs);

        return count;
    }

    private void appendValuesFromUrl(ContentValues values, Uri url,
                                     String... columns) {
        if (url.getPathSegments().size() <= columns.length) {
            throw new IllegalArgumentException("Not enough values in url");
        }
        for (int i = 0; i < columns.length; i++) {
            if (values.containsKey(columns[i])) {
                throw new UnsupportedOperationException(
                        "Cannot override the value for " + columns[i]);
            }
            values.put(columns[i],
                    decodeURLSegment(url.getPathSegments().get(i + 1)));
        }
    }

    private static String decodeURLSegment(String segment) {
        try {
            return URLDecoder.decode(segment, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // impossible
            return segment;
        }
    }

    private static void appendWhere(StringBuilder where, String columnName,
                                    String condition, Object value) {
        if (where.length() > 0) {
            where.append(" AND ");
        }
        where.append(columnName).append(condition);
        if (value != null) {
            DatabaseUtils.appendValueToSql(where, value);
        }
    }

    static void log(String message) {
        Log.d(TAG, message);
    }
}