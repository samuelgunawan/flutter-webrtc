package com.cloudwebrtc.webrtc;

import android.os.Handler;
import android.os.Looper;

import com.cloudwebrtc.webrtc.record.AudioSamplesInterceptor;
import com.cloudwebrtc.webrtc.utils.AnyThreadSink;
import com.cloudwebrtc.webrtc.utils.AudioLevelCalculator;
import com.cloudwebrtc.webrtc.utils.ConstraintsMap;

import org.webrtc.audio.JavaAudioDeviceModule;

import io.flutter.plugin.common.EventChannel;

public class AudioInputListener implements EventChannel.StreamHandler, JavaAudioDeviceModule.SamplesReadyCallback {

    private static final int AUDIO_INPUT_INTERCEPTOR_ID = 999999998;

    EventChannel eventChannel;
    EventChannel.EventSink eventSink;
    private AudioSamplesInterceptor audioSamplesInterceptor;

    final Handler handler = new Handler();
    final int delay = 250; //milliseconds
    private Runnable runnable;

    private JavaAudioDeviceModule.AudioSamples audioSamples;
    private long timestamp = 0L;
    private long sendTimestamp = 0L;
    private double oldDb = 0.0;

    public void dispose() {
        if (eventChannel != null)
            eventChannel.setStreamHandler(null);

        if (audioSamplesInterceptor != null)
            audioSamplesInterceptor.detachCallback(AUDIO_INPUT_INTERCEPTOR_ID);

        eventSink = null;
        audioSamples = null;
    }

    public void setInterceptor(AudioSamplesInterceptor audioSamplesInterceptor) {
        if (audioSamplesInterceptor != null)
            audioSamplesInterceptor.detachCallback(AUDIO_INPUT_INTERCEPTOR_ID);
        this.audioSamplesInterceptor = audioSamplesInterceptor;
    }

    public void setEventChannel(EventChannel eventChannel) {
        if (this.eventChannel != null)
            this.eventChannel.setStreamHandler(null);

        this.eventChannel = eventChannel;
        this.eventChannel.setStreamHandler(this);
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink sink) {
        eventSink = new AnyThreadSink(sink);
    }

    @Override
    public void onCancel(Object o) {
        eventSink = null;
        audioSamples = null;
    }

    public void start() {
        try {
            audioSamples = null;
            audioSamplesInterceptor.attachCallback(AUDIO_INPUT_INTERCEPTOR_ID, this);
            handler.postDelayed(runnable = () -> {
                //MAIN THREAD: NEED TO MOVE TO BACKGROUND THREAD
                sendEvent();
                handler.postDelayed(runnable, delay);
            }, delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (audioSamplesInterceptor != null)
            audioSamplesInterceptor.detachCallback(AUDIO_INPUT_INTERCEPTOR_ID);
        handler.removeCallbacks(runnable);
        audioSamples = null;
    }

    @Override
    public void onWebRtcAudioRecordSamplesReady(JavaAudioDeviceModule.AudioSamples audioSamples) {
        this.audioSamples = audioSamples;
        timestamp = System.currentTimeMillis();
    }

    private void sendEvent() {
        if (audioSamples == null || timestamp == sendTimestamp) return;
        final byte[] data = audioSamples.getData();
        if(data == null) return;
        int level
                = AudioLevelCalculator.calculateAudioLevel(data, 0, data.length);
        level = AudioLevelCalculator.MIN_AUDIO_LEVEL - level;
        double newDb = (double) level;
        double deltaDb = Math.abs(oldDb - newDb);
        if(deltaDb < 20.0) return;
        oldDb = newDb;
        ConstraintsMap params = new ConstraintsMap();
        params.putInt("audioFormat", audioSamples.getAudioFormat());
        params.putInt("channelCount", audioSamples.getChannelCount());
        params.putInt("sampleRate", audioSamples.getSampleRate());
        params.putByte("data", data);
        params.putDouble("dB", newDb);
        params.putDouble("diff", deltaDb);
//        params.putDouble("dB", AudioLevelCalculator.calculateDecibel(audioSamples.getData()));
        eventSink.success(params.toMap());
        sendTimestamp = timestamp;
    }
}
