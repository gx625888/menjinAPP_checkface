package com.zzhou.entrance.guard.bean;

import com.zzhou.entrance.guard.source.CursorHelper;
import com.zzhou.entrance.guard.source.Ws;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <desc>
 * Created by The Moss on 2018/9/13.
 */

public class AccountData {
    /*id*/
    String id;
    /*卡号*/
    String card;
    /*添加、删除 默认0 添加*/
    int isAdd;
    //房号
    String no;
    //对应手机号？呼叫中心
    String phone;
    /*用户权限*/
    int jurisdiction;

    public static List<AccountData> fromJSONObject(JSONObject json){
        List<AccountData> accounts = new ArrayList();
        if (json.has("data") && !json.isNull("data")) {
            try {
                JSONArray jar = json.getJSONArray("data");
                for (int i = 0; i < jar.length(); i++) {
                    JSONObject obj = jar.getJSONObject(i);
                    AccountData accountData = new AccountData();
                    accountData.id = obj.optString("id");
                    accountData.card = obj.optString("card");
                    accountData.isAdd = obj.optInt("isAdd");
                    accountData.no = obj.optString("no");
                    accountData.phone = obj.optString("phone");
                    accountData.jurisdiction = obj.optInt("jurisdiction");
                    accounts.add(accountData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return accounts;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public int getIsAdd() {
        return isAdd;
    }

    public void setIsAdd(int isAdd) {
        this.isAdd = isAdd;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(int jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public static AccountData fromCursor(CursorHelper cursorHelper) {
        AccountData accountData = new AccountData();
        accountData.id = cursorHelper.getString(Ws.AccountTable.ACCOUNT_ID);
        accountData.card = cursorHelper.getString(Ws.AccountTable.CARD);
        accountData.no = cursorHelper.getString(Ws.AccountTable.NO);
        accountData.phone = cursorHelper.getString(Ws.AccountTable.PHONE);
        accountData.jurisdiction = cursorHelper.getInt(Ws.AccountTable.JURISDICTION);
        return accountData;
    }

    @Override
    public String toString() {
        return "AccountData{" +
                "id='" + id + '\'' +
                ", card='" + card + '\'' +
                ", isAdd=" + isAdd +
                ", no='" + no + '\'' +
                ", phone='" + phone + '\'' +
                ", jurisdiction=" + jurisdiction +
                '}';
    }
}
