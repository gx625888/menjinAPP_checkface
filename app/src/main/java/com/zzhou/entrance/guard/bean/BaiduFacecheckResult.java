package com.zzhou.entrance.guard.bean;

import java.util.List;

public class BaiduFacecheckResult {
    String group_id;
    String uid;
    String user_info;
    List scores;

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUser_info() {
        return user_info;
    }

    public void setUser_info(String user_info) {
        this.user_info = user_info;
    }

    public List getScores() {
        return scores;
    }

    public void setScores(List scores) {
        this.scores = scores;
    }
}
