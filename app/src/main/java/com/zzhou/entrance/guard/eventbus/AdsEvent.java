package com.zzhou.entrance.guard.eventbus;

/**
 * <desc>
 * Created by The Moss on 2018/9/14.
 */

public class AdsEvent {
    int type;//图片0\视频1
    String url;

    public AdsEvent(int type, String url) {
        this.type = type;
        this.url = url;
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
