package com.zzhou.entrance.guard.bean;

import java.util.ArrayList;
import java.util.List;

public class BaiduFacecheck {
    int log_id;
    int result_num;
    List<BaiduFacecheckResult> result;

    public int getLog_id() {
        return log_id;
    }

    public void setLog_id(int log_id) {
        this.log_id = log_id;
    }

    public int getResult_num() {
        return result_num;
    }

    public void setResult_num(int result_num) {
        this.result_num = result_num;
    }

    public List<com.zzhou.entrance.guard.bean.BaiduFacecheckResult> getResult() {
        return result;
    }

    public void setResult(List<com.zzhou.entrance.guard.bean.BaiduFacecheckResult> result) {
        this.result = result;
    }
}

