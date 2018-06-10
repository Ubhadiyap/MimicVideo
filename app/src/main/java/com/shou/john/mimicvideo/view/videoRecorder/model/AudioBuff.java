package com.shou.john.mimicvideo.view.videoRecorder.model;

public class AudioBuff {
    public boolean isReadyToFill;
    public int audioFormat = -1;
    public byte[] buff;

    public AudioBuff(int audioFormat, int size) {
        isReadyToFill = true;
        this.audioFormat = audioFormat;
        buff = new byte[size];
    }
}
