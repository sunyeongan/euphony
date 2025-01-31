package co.euphony.rx;

import android.util.Log;

import co.euphony.common.Constants;

public class EuPI {
    public enum EuPIStatus {
        KEY_DOWN, KEY_UP, KEY_PRESSED, KEY_NONE
    }

    private int mKey;
    private int mFreqIndex;
    private double mThreshold;
    private double mInputThreshold;
    EuPIStatus mTrigger;
    EuPIStatus mStatus;
    EuPICallDetector mAPICallback;
    boolean isActive = false;

    public EuPI(int key, EuPICallDetector callback) {
        mKey = key;
        mFreqIndex = calculateFreqIndex(key);
        mAPICallback = callback;
        mTrigger = EuPIStatus.KEY_PRESSED;
        mInputThreshold = 0;
        setStatus(EuPIStatus.KEY_NONE);
    }

    public EuPI(int key, EuPIStatus trigger, EuPICallDetector callback) {
        mKey = key;
        mFreqIndex = calculateFreqIndex(key);
        mAPICallback = callback;
        mTrigger = trigger;
        mInputThreshold = 0;
        setStatus(EuPIStatus.KEY_NONE);
    }

    public EuPI(int key, double threshold, EuPIStatus trigger, EuPICallDetector callback) {
        mKey = key;
        mAPICallback = callback;
        mTrigger = trigger;
        mInputThreshold = threshold;
        setStatus(EuPIStatus.KEY_NONE);
    }

    public EuPIStatus feedAmplitude(float ampLeft, float ampCenter, float ampRight) {
        final float ampDiff = ampCenter - (ampLeft + ampRight) / 2;
        final boolean compared = compareThreshold(ampDiff);

        if(!isActive && compared) {
            setStatus(EuPIStatus.KEY_DOWN);
            Log.d("TEST", "amp difference : " + ampDiff);
            isActive = true;
        } else if (isActive && compared) {
            setStatus(EuPIStatus.KEY_PRESSED);
            isActive = true;
        } else if (isActive){
            setStatus(EuPIStatus.KEY_UP);
            isActive = false;
        } else {
            setStatus(EuPIStatus.KEY_NONE);
        }

        return getStatus();
    }

    public int getKey() {
        return mKey;
    }

    public boolean compareThreshold(float amp) { return amp >= mThreshold; }

    public void setTrigger(EuPIStatus trigger) {
        mTrigger = trigger;
    }

    public EuPIStatus getTrigger() {
        return mTrigger;
    }

    public void setStatus(EuPIStatus status) {
        mStatus = status;
        switch(status) {
            case KEY_UP:
            case KEY_NONE:
            default:
                mThreshold = (mInputThreshold == 0) ? 0.0007 : mInputThreshold;
                break;
            case KEY_DOWN:
            case KEY_PRESSED:
                mThreshold = (mInputThreshold == 0) ? 0.0005 : mInputThreshold - 0.0002;
                break;
        }
    }

    public EuPIStatus getStatus() {
        return mStatus;
    }

    public int getFreqIndex() {
        return mFreqIndex;
    }

    public EuPICallDetector getCallback() {
        return mAPICallback;
    }

    private int calculateFreqIndex(int freq) {
        double freqRatio = ((float)freq) / (float) Constants.HALF_SAMPLERATE;
        return (int) Math.round((freqRatio * (float) (Constants.FFT_SIZE >> 1)));
    }
}
