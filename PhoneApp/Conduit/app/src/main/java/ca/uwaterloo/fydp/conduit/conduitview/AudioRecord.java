package ca.uwaterloo.fydp.conduit.conduitview;

import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.IOException;

public class AudioRecord {
    private static String mOutputFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    public void SetOutputFile(String fileName) {
        mOutputFileName = fileName;
    }

    public String getOutputFileName() {
        return mOutputFileName;
    }

    // start and stop recording audio
    public void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    // start and stop playing audio
    public void onPlay(boolean start, MediaDataSource mediaDataSource) {
        if (start) {
            startPlaying(mediaDataSource);
        } else {
            stopPlaying();
        }
    }

    private void startPlaying(MediaDataSource mediaDataSource) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mediaDataSource);
            mPlayer.setVolume(1.0f, 1.0f);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            // error something went wrong
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        if (mRecorder != null) {
            stopRecording();
        }

        mRecorder = new MediaRecorder();
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioSamplingRate(8000); // 8kHz
        mRecorder.setAudioEncodingBitRate(12200);
        mRecorder.setMaxDuration(4000);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mOutputFileName);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mRecorder.start();
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }
}
