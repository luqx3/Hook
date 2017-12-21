package com.lexing360.hook.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

/**
 * Created by zzb on 2017/12/20.
 */

public class redService extends AccessibilityService {
    /**
     * 微信几个页面的包名+地址。用于判断在哪个页面 LAUCHER-微信聊天界面，LUCKEY_MONEY_RECEIVER-点击红包弹出的界面
     */
    private String LAUCHER = "com.tencent.mm.ui.LauncherUI";
    private String FMESSAGE_CONVERSATION = "com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI";
    private String SAYHI_WITH_SNS="com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI";
    private String CONTACT_INFO_UI="com.tencent.mm.plugin.profile.ui.ContactInfoUI";
    //com.tencent.mm.ui.base.r

    /**
     * 用于判断是否点击过了
     */
    private boolean isOpen;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            //通知栏来信息，判断是否含有微信红包字样，是的话跳转
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                for (CharSequence text : texts) {
                    String content = text.toString();
                    if (!TextUtils.isEmpty(content)) {
                        //判断是否含有"请求添加你为好友"字样
                        if (content.contains("请求添加你为朋友")) {
                            //如果有则打开微信红包页面
                            openPage(event);

                            isOpen = false;
                        }
                    }
                }
                break;
            //界面跳转的监听
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                //判断是否是新的朋友页面
                if (LAUCHER.equals(FMESSAGE_CONVERSATION)) {
                    //获取当前聊天页面的根布局
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    //开始找红包
                    findRedPacket(rootNode);
                }

                //判断是否是朋友验证页面
                if (SAYHI_WITH_SNS.equals(className)) {
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    //开始抢红包
                    findComplite(rootNode);
                }

                //判断是否是红包领取后的详情界面
                if (CONTACT_INFO_UI.equals(className)) {
                    //返回桌面
                    //backHome();
                }
                break;
            default:
                break;
        }

    }


    /**
     * 开启红包所在的聊天页面
     */
    private void openPage(AccessibilityEvent event) {
        //A instanceof B 用来判断内存中实际对象A是不是B类型，常用于强制转换前的判断
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            //打开对应的聊天界面
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回桌面
     */
    private void backHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    /**
     * 开始打开红包
     */
    private void findComplite(AccessibilityNodeInfo rootNode) {
        if(rootNode==null){
            return;
        }
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            //如果node为空则跳过该节点
            if (node == null) {
                continue;
            }
            if ("android.widget.TextView".equals(node.getClassName()) && "完成".equals(node.getText().toString())) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }else {
                findComplite(node);
            }
        }
    }

    private void findRedPacket(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            //从最后一行开始找起
            for (int i = rootNode.getChildCount() - 1; i >= 0; i--) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                //如果node为空则跳过该节点
                if (node == null) {
                    continue;
                }
                CharSequence text = node.getText();
                if (text != null && text.toString().equals("接收")) {
                    AccessibilityNodeInfo parent = node.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            //模拟点击
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            //isOpenRP用于判断该红包是否点击过
                            isOpen = true;
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
                //判断是否已经打开过那个最新的红包了，是的话就跳出for循环，不是的话继续遍历
                if (isOpen) {
                    break;
                } else {
                    findRedPacket(node);
                }

            }
        }
    }

    /**
     * 服务连接
     */
    @Override
    protected void onServiceConnected() {
        Toast.makeText(this, "服务开启", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }

    /**
     * 服务断开
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "服务已被关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "服务已被中断", Toast.LENGTH_SHORT).show();
    }
}
