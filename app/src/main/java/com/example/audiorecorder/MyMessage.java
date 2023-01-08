package com.example.audiorecorder;

public class MyMessage {
    String messageData;
    String deliveredTime;
    boolean input0output1;

    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }

    MyMessage (String _messageData, boolean _input0output1, String _deliveredTime){
        messageData = _messageData;
        input0output1 = _input0output1;
        deliveredTime = _deliveredTime;
    }
}
