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

public class HouseData {
    /*id*/
    String id;
    /*添加、删除 默认0 添加*/
    int isAdd;
    //房号
    String no;
    //对应手机号？呼叫中心
    String phone;
    /*用户权限*/
    int jurisdiction;
    public static List<HouseData> fromJSONObject(JSONObject json){
        List<HouseData> houses = new ArrayList();
        if (json.has("data") && !json.isNull("data")) {
            try {
                JSONArray jar = json.getJSONArray("data");
                for (int i = 0; i < jar.length(); i++) {
                    JSONObject obj = jar.getJSONObject(i);
                    HouseData houseData = new HouseData();
                    houseData.id = obj.optString("id");
                    houseData.isAdd = obj.optInt("isAdd");
                    houseData.no = obj.optString("no");
                    houseData.phone = obj.optString("p");
                    houseData.jurisdiction = obj.optInt("jurisdiction");
                    houses.add(houseData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return houses;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public static HouseData fromCursor(CursorHelper cursorHelper) {
        HouseData houseData = new HouseData();
        houseData.id = cursorHelper.getString(Ws.HouseTable.ID);
        houseData.no = cursorHelper.getString(Ws.HouseTable.NO);
        houseData.phone = cursorHelper.getString(Ws.HouseTable.PHONE);
        houseData.jurisdiction = cursorHelper.getInt(Ws.HouseTable.JURISDICTION);
        return houseData;
    }

    @Override
    public String toString() {
        return "HouseData{" +
                "id='" + id + '\'' +
                ", isAdd=" + isAdd +
                ", no='" + no + '\'' +
                ", phone='" + phone + '\'' +
                ", jurisdiction=" + jurisdiction +
                '}';
    }
}
