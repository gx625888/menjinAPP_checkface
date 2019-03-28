package com.zzhou.entrance.guard.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <desc>
 * Created by The Moss on 2018/9/14.
 */

public class Ads implements Serializable{
    int type;//0图，1视频
    String url;

    public static List<Ads> fromJSONObject(JSONObject json){
        List<Ads> adsList = new ArrayList();
        if (json.has("data") && !json.isNull("data")) {
            try {
                JSONArray jar = json.getJSONArray("data");
                for (int i = 0; i < jar.length(); i++) {
                    JSONObject obj = jar.getJSONObject(i);
                    Ads ads = new Ads();
                    ads.type = obj.optInt("type");
                    ads.url = obj.optString("url");
                    adsList.add(ads);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return adsList;
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
