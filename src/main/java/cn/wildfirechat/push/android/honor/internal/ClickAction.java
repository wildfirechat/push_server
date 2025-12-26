package cn.wildfirechat.push.android.honor.internal;

public class ClickAction {
    /**
     * 消息点击行为类型，取值如下：
     * <p>
     * 1：打开应用自定义页面
     * 2：点击后打开特定URL
     * 3：点击后打开应用
     */

    public Integer type = 3;

    public String intent;
    public String url;
    public String action;
}
