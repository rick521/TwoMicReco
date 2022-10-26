package com.example.audiorecord;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;



public class AudioRecoderUtils {
    String TAG = "AudioRecoder";
    private boolean isRecording = false;
    private boolean isPlay = false;

    byte[] mBuffer = new byte[10485760];
    Handler mHandler = new Handler(Looper.myLooper()) {
    };
    static int mBufferCount = 0;
    AudioRecord audioRecord;
    AudioTrack mAudioTrack;
    //16K采集率
    int frequency = 16000;
    //录音通道--立体音录音
    int channel = AudioFormat.CHANNEL_IN_STEREO;
    //16Bit
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    AudioManager mAudioManager;

    public AudioRecoderUtils() {

    }

    public boolean getPlay(){
        return isPlay;
    }

    //停止录音
    public void StopRecord() {
        isRecording = false;
        if(audioRecord != null){
            audioRecord.release();
        }
    }

    private AudioManager.OnAudioFocusChangeListener mAudioFocusChange = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    //start();
                    break;
            }
        }
    };

    //申请音频焦点
    private void beforePlay(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(mAudioFocusChange, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    //退出释放
    public void releaseAudioManager(){
        if (audioRecord != null) {
            audioRecord.release();
        }
        if (mAudioTrack != null) {
            mAudioTrack.release();
        }
        if(mAudioManager != null){
            mAudioManager.abandonAudioFocus(mAudioFocusChange);
        }
    }

    //开始录音
    public void StartRecord(Context context) {
        new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            public void run() {
                mBufferCount = 0;
                isPlay = false;
                beforePlay(context);
                int bufferSize = AudioRecord.getMinBufferSize(frequency, channel, audioEncoding);
                if (audioRecord != null) {
                    audioRecord.release();
                }
                long count = 0;
                long start = System.currentTimeMillis();
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_PERFORMANCE, frequency,channel, audioEncoding, bufferSize);
                isRecording = true;
                audioRecord.startRecording();
                while (isRecording) {
                    int n = audioRecord.read(mBuffer, mBufferCount, 12000);
                    if (n > 0) {
                        mBufferCount += n;
                        long cc = Math.round(((double) (System.currentTimeMillis() - start)) / 1000.0d);
                        if (cc != count) {
                            count = cc;
                        }
                    } else if (n < 0) {

                    }
                }
            }
        }).start();
        new Thread(new Runnable() {
            public void run() {
                savePCM(mBuffer, mBufferCount, "stereo.pcm");
            }
        }).start();
    }
    /**
     //播放音频 --- 在这里把主副MIC录制的数据单独提取出来，因为是用的CHANNEL_IN_STEREO，
     //注意，因为把数据区分出来了所以在new AudioTrack的时候要用CHANNEL_IN_MONO单通道播放
     //如果要播放录制的原音可以就要用CHANNEL_IN_STEREO，直接用
     //mAudioTrack.write(mBuffer, 0, mBufferCount);
     //  mAudioTrack.play();
     */
    public void SaveRecord(Context context){

            new Thread(new Runnable() {
                public void run() {
                    byte[] bsleft = new byte[(mBufferCount / 2)];
                    int i = 0;
                    while (i < mBufferCount - 1 && (i / 2) + 1 < mBufferCount / 2) {
                        bsleft[i / 2] = mBuffer[i];
                        bsleft[(i / 2) + 1] = mBuffer[i + 1];
                        i += 4;
                    }
                    savePCM(bsleft, bsleft.length, "left.pcm");
                    byte[] bsright = new byte[(mBufferCount / 2)];
                    int j = 2;
                    while (j < mBufferCount - 1 && (j / 2) + 1 < mBufferCount / 2) {
                        bsright[(j / 2) - 1] = mBuffer[j];
                        bsright[j / 2] = mBuffer[j + 1];
                        bsright[(j / 2) + 1] = 0;
                        j += 4;
                    }
                    savePCM(bsright, bsright.length, "right.pcm");

                }

            }).start();
        Toast.makeText(context,"成功",Toast.LENGTH_LONG).show();

    }
    public void PlayRecord(boolean isLeft) {
        if (mAudioTrack != null) {
            mAudioTrack.release();
        }
        isPlay = true;
        mAudioTrack = new AudioTrack(3, 16000, 4, 2, mBuffer.length, 0);
        if (isLeft) {
            new Thread(new Runnable() {
                public void run() {
                    byte[] bsleft = new byte[(mBufferCount / 2)];
                    int i = 0;
                    while (i < mBufferCount - 1 && (i / 2) + 1 < mBufferCount / 2) {
                        bsleft[i / 2] = mBuffer[i];
                        bsleft[(i / 2) + 1] = mBuffer[i + 1];
                        i += 4;
                    }
                    savePCM(bsleft, bsleft.length, "left.pcm");
                    if (mAudioTrack != null) {
                        try {
                            mAudioTrack.write(bsleft, 0, bsleft.length);
                            mAudioTrack.play();
                        } catch (Exception e) {
                            isPlay = false;
                        }
                    } else {
                        isPlay = false;
                    }
                }
            }).start();
        } else {
            new Thread(new Runnable() {
                public void run() {
                    byte[] bsright = new byte[(mBufferCount / 2)];
                    int j = 2;
                    while (j < mBufferCount - 1 && (j / 2) + 1 < mBufferCount / 2) {
                        bsright[(j / 2) - 1] = mBuffer[j];
                        bsright[j / 2] = mBuffer[j + 1];
                        bsright[(j / 2) + 1] = 0;
                        j += 4;
                    }
                    savePCM(bsright, bsright.length, "right.pcm");
                    if (mAudioTrack != null) {
                        try {
                            mAudioTrack.write(bsright, 0, bsright.length);
                            mAudioTrack.play();
                        } catch (Exception e) {
                        }
                    } else {
                    }
                }
            }).start();
        }

    }

    //把录制的音频以PCM的格式写入存储里面
    public String savePCM(byte[] data, int count, String name) {
        Log.d(TAG, "开始写入 count = " + count);
        String str = null;
        if (count <= 0) {
            return str;
        }
        try {

            File fout = new File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/" + name);
            OutputStream out = new FileOutputStream(fout);
            out.write(data, 0, count);
            out.close();
            Log.d(TAG, "写入成功");
            return fout.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "写入失败");
            return str;
        }
    }

}

