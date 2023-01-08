package com.example.audiorecorder;

public interface SetTransmitterSettingCallback {
    void deleteSetting(int Position);
    void checkSetting(int Position);
    void saveSetting(int Position);
    void setBitDuration(float BD);
    void setFrequency(int frequency);
    void newBDSetting(float typedBD, int Position);
    void newFrequencySetting(int typedFrequency, int Position);
}
