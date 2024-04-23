package cn.wildfirechat.push.hm.payload.internal;

public class ClickAction {
    /**
     * 0：打开应用首页
     * <p>
     * 1：打开应用自定义页面
     */
    public int actionType;

    public String action;
    public String uri;
    public String data;
}
