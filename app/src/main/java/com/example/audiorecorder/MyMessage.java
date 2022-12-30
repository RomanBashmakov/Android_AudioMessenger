package com.example.audiorecorder;

public class MyMessage {
    String messageData;
    boolean input0output1;

    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }

    MyMessage (String _messageData, boolean _input0output1){
        messageData = _messageData;
        input0output1 = _input0output1;
    }
}
