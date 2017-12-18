package com.lexing360.hook.message;


import com.lexing360.hook.message.model.MsgInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzb on 2017/12/15.
 */

public class MsgStat {
    public List<MsgInfo> msgList = null;
    public MsgStat(List<MsgInfo> msgList) {
        this.msgList = msgList;
    }
}
