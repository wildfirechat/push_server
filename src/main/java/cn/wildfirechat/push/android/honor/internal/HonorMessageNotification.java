package cn.wildfirechat.push.android.honor.internal;

import com.google.gson.annotations.SerializedName;

public class HonorMessageNotification {
    public String importance = "NORMAL";
    @SerializedName("click_action")
    public ClickAction clickAction;
//    public String ticker;
//    public String notify_summary;
//
    public String channel_id;
//    public int style = 0;
//    public int notify_id;
//    public String visibility;
//    public String when;
}
