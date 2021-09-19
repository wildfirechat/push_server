package cn.wildfirechat.push;


public class PushMessage {
    public String sender;
    public String senderName;
    public int convType;
    public String target;
    public String targetName;
    public String userId;
    public int line;
    public int cntType;
    public long serverTime;
    //消息的类型，普通消息通知栏；voip要透传。
    public int pushMessageType;
    //推送类型，android推送分为小米/华为/魅族等。ios分别为开发和发布。
    public int pushType;
    public String pushContent;
    public String pushData;
    public int unReceivedMsg;
    public int mentionedType;
    public String packageName;
    public String deviceToken;
    public String voipDeviceToken;
    public boolean isHiddenDetail;
    public String language;
    public long messageId;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getConvType() {
        return convType;
    }

    public void setConvType(int convType) {
        this.convType = convType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getCntType() {
        return cntType;
    }

    public void setCntType(int cntType) {
        this.cntType = cntType;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public int getPushMessageType() {
        return pushMessageType;
    }

    public void setPushMessageType(int pushMessageType) {
        this.pushMessageType = pushMessageType;
    }

    public int getPushType() {
        return pushType;
    }

    public void setPushType(int pushType) {
        this.pushType = pushType;
    }

    public String getPushContent() {
        return pushContent;
    }

    public void setPushContent(String pushContent) {
        this.pushContent = pushContent;
    }

    public String getPushData() {
        return pushData;
    }

    public void setPushData(String pushData) {
        this.pushData = pushData;
    }

    public int getUnReceivedMsg() {
        return unReceivedMsg;
    }

    public void setUnReceivedMsg(int unReceivedMsg) {
        this.unReceivedMsg = unReceivedMsg;
    }

    public int getMentionedType() {
        return mentionedType;
    }

    public void setMentionedType(int mentionedType) {
        this.mentionedType = mentionedType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getVoipDeviceToken() {
        return voipDeviceToken;
    }

    public void setVoipDeviceToken(String voipDeviceToken) {
        this.voipDeviceToken = voipDeviceToken;
    }

    public boolean isHiddenDetail() {
        return isHiddenDetail;
    }

    public void setHiddenDetail(boolean hiddenDetail) {
        isHiddenDetail = hiddenDetail;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
