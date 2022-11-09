package com.example.audiorecorder;

public interface SetTransmitterSettingCallback {
    void setChosenTransmitterSetting();
    void deleteSetting(int Position);
    void checkSetting(int Position);
    void setBitDuration(float BD);
    void setFrequency(int frequency);
}
