package cn.wildfirechat.push.android.honor.internal;

import java.util.List;

public class AndroidNotification {
    public String title;
    public String body;
    public ClickAction clickAction;
    public String image;
    /**
     * 0：默认样式
     * 1：大文本样式
     */
    public Integer style = 0;
    public String bigTitle;
    public String bigBody;
    public String importance;
    public String when;
    public List<Button> buttons;
    public BadgeNotification badge;
    public Integer notifyId;
    public String tag;
    public String group;
}
