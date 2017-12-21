package com.lexing360.hook.common;

/**
 * Created by zzb on 2017/12/21.
 */

public class Message {
    static public String ERROR ="ERROR";
    static public String FINISH ="FINISH";
    public String TYPE="";
    public String MSG="";
    Message(String msg,String ... params){
        MSG=msg;
        if(params.length>0){
            TYPE=params[0];
        }
    }
}
