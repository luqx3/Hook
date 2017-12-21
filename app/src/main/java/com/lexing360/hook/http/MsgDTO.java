package com.lexing360.hook.http;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzb on 2017/12/20.
 */

public class MsgDTO {
    public List<WechatTextSingle> list = new ArrayList<>() ;
    public MsgDTO(){

    }
    public MsgDTO(List<WechatTextSingle> list){
        this.list=list;
    }

    public void setList(List<WechatTextSingle> list ){
        this.list=list;
    }

    public List<WechatTextSingle> getList() {
        return list;
    }
}
