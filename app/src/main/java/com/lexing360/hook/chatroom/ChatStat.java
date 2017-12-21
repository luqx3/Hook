package com.lexing360.hook.chatroom;


import com.lexing360.hook.chatroom.model.MsgInfo;

import java.util.List;

/**
 * Created by zzb on 2017/12/15.
 */

public class ChatStat {
    public List<MsgInfo> msgList = null;
    public ChatStat(List<MsgInfo> msgList) {
        this.msgList = msgList;
    }
}
