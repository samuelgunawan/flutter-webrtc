package com.cloudwebrtc.webrtc.utils;

/*
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.util.Log;

import java.util.ArrayList;

/**
 * Implements the calculation of audio level as defined by RFC 6465 &quot;A
 * Real-time Transport Protocol (RTP) Header Extension for Mixer-to-Client Audio
 * Level Indication&quot;.
 *
 * @author Lyubomir Marinov
 */
public class AudioLevelCalculator {
    /**
     * The maximum audio level.
     */
    public static final byte MAX_AUDIO_LEVEL = 0;

    /**
     * The minimum audio level.
     */
    public static final byte MIN_AUDIO_LEVEL = 127;

    private static final double MAX_AMP = Math.pow(2, 15) + 0.0;

    /**
     * Calculates the audio level of a signal with specific <tt>samples</tt>.
     *
     * @param samples the samples of the signal to calculate the audio level of
     * @param offset  the offset in <tt>samples</tt> in which the samples start
     * @param length  the length in bytes of the signal in <tt>samples<tt>
     *                starting at <tt>offset</tt>
     * @return the audio level of the specified signal
     */
    public static byte calculateAudioLevel(
            byte[] samples,
            int offset,
            int length) {
        double rms = 0; // root mean square (RMS) amplitude

        for (; offset < length; offset += 2) {
            double sample = ArrayIOUtils.readShort(samples, offset);

            sample /= Short.MAX_VALUE;
            rms += sample * sample;
        }

        int sampleCount = length / 2;

        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        double db;

        if (rms > 0) {
            db = 20 * Math.log10(rms);
            // XXX The audio level is expressed in -dBov.
            db = -db;
            // Ensure that the calculated audio level is within the range
            // between MIN_AUDIO_LEVEL and MAX_AUDIO_LEVEL.
            if (db > MIN_AUDIO_LEVEL)
                db = MIN_AUDIO_LEVEL;
            else if (db < MAX_AUDIO_LEVEL)
                db = MAX_AUDIO_LEVEL;
        } else {
            db = MIN_AUDIO_LEVEL;
        }

        return (byte) db;
    }

    public static double calculateDecibel(byte[] samples) {
        int length = samples.length;
        double rms = 0; // root mean square (RMS) amplitude

        for (int offset = 0; offset < length; offset += 2) {
            double sample = ArrayIOUtils.readShort(samples, offset);
            sample /= Short.MAX_VALUE;
            rms += sample * sample;
        }

//        Log.d("COBACOBA","raw RMS: "+ rms);
        int sampleCount = length / 2;

        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        double db;
        if (rms > 0) {
            db = 20 * Math.log10(MAX_AMP * rms);
        } else {
            db = 0;
        }

        return db;
    }

    public static double calculateDecibel2(byte[] samples) {
        double sum = 0.0;
        for (byte sample : samples) {
            sum += sample * sample;
        }
        double rms = Math.sqrt(sum / samples.length);

        return 20 * Math.log10(MAX_AMP * rms);
    }

    public static double calculateDecibel3(byte[] samples) {
        ArrayList<Double> audioBufferList = new ArrayList<>();
        for (byte impulse:samples) {
            double normalizedImpulse = impulse / MAX_AMP;
            audioBufferList.add(normalizedImpulse);
        }

        double min = audioBufferList.get(0);
        double max = audioBufferList.get(audioBufferList.size() - 1);
        double mean = 0.5 * (Math.abs(min) + Math.abs(max));

        return 20 * Math.log10(MAX_AMP * mean);
    }
}