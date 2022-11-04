package com.example.audiorecorder;

public class transmitterSetting {

    int frequency;
    float duration;
    boolean checkedF;

    transmitterSetting (int _frequency, float _duration)
    {
        frequency = _frequency;
        duration = _duration;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public void setCheckedF(boolean checkedF) {
        this.checkedF = checkedF;
    }
}