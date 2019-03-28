package com.zzhou.entrance.guard.source;

import android.net.Uri;
import android.provider.BaseColumns;


public class Ws {

    public static final String AUTHORITY = "com.zzhou.entrance.guard";

    private Ws() {
    }

    public interface AccountColumns {
        /*用户账号*/
        String ACCOUNT_ID = "account_id";
        /*卡号*/
        String CARD = "card";
        /*房号*/
        String NO = "no";
        /*对应手机号*/
        String PHONE = "phone";
        /*权限*/
        String JURISDICTION = "jurisdiction";
    }
    public static final class AccountTable implements AccountColumns,BaseColumns {
        private AccountTable() {
        }

        public static final String PATH = "accounts";
        public static final String PATH_CARD = "accountCard";
        /**
         * 此表的uri
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        public static final Uri CONTENT_URI_CARD = Uri.parse("content://" + AUTHORITY + "/" + PATH_CARD);
        /**
         * MIME TYPE
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-accounts";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-accounts";

        public static final String TABLE_NAME = "accounts";
        public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + "("
                + "_id INTEGER PRIMARY KEY,"
                + ACCOUNT_ID + " TEXT,"
                + CARD + " TEXT,"
                + NO + " TEXT,"
                + PHONE + " TEXT,"
                + JURISDICTION + " INTEGER"
                + ");";
    }

    public interface HouseColumns {
        /*id*/
        String ID = "id";
        /*房号*/
        String NO = "no";
        /*对应手机号*/
        String PHONE = "phone";
        /*权限*/
        String JURISDICTION = "jurisdiction";
    }
    public static final class HouseTable implements HouseColumns,BaseColumns {
        private HouseTable() {
        }

        public static final String PATH = "house";
        public static final String PATH_NO = "houseNo";
        /**
         * 此表的uri
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
        public static final Uri CONTENT_URI_CARD = Uri.parse("content://" + AUTHORITY + "/" + PATH_NO);
        /**
         * MIME TYPE
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-house";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-house";

        public static final String TABLE_NAME = "house";
        public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + "("
                + "_id INTEGER PRIMARY KEY,"
                + ID + " TEXT,"
                + NO + " TEXT,"
                + PHONE + " TEXT,"
                + JURISDICTION + " INTEGER"
                + ");";
    }
}

