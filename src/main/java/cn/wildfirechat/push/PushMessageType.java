package cn.wildfirechat.push;

public interface PushMessageType {
    int PUSH_MESSAGE_TYPE_NORMAL = 0;
    int PUSH_MESSAGE_TYPE_VOIP_INVITE = 1;
    int PUSH_MESSAGE_TYPE_VOIP_BYE = 2;
    int PUSH_MESSAGE_TYPE_FRIEND_REQUEST = 3;
    int PUSH_MESSAGE_TYPE_VOIP_ANSWER = 4;
    int PUSH_MESSAGE_TYPE_RECALLED = 5;
    int PUSH_MESSAGE_TYPE_DELETED = 6;
}
