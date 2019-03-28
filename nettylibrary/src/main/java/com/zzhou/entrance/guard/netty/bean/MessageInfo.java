package com.zzhou.entrance.guard.netty.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <desc>
 * Created by The Moss on 2018/9/11.
 */

public class MessageInfo implements Parcelable {
    /*指令*/
    int cmd;
    /*内容体*/
    String content;
    /*成功\失败*/
    int result;

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.cmd);
        dest.writeString(this.content);
        dest.writeInt(this.result);
    }

    public MessageInfo() {
    }

    protected MessageInfo(Parcel in) {
        this.cmd = in.readInt();
        this.content = in.readString();
        this.result = in.readInt();
    }

    public static final Parcelable.Creator<MessageInfo> CREATOR = new Parcelable.Creator<MessageInfo>() {
        @Override
        public MessageInfo createFromParcel(Parcel source) {
            return new MessageInfo(source);
        }

        @Override
        public MessageInfo[] newArray(int size) {
            return new MessageInfo[size];
        }
    };
}
