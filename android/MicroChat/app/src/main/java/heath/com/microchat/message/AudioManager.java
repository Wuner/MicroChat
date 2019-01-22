package heath.com.microchat.message;

import android.media.MediaRecorder;

import java.io.File;
import java.util.UUID;

import heath.com.microchat.BaseActivity;

public class AudioManager {

    private MediaRecorder mMediaRecorder;
    private String mCurFilePath;

    private static AudioManager mInstance;
    private boolean isPrepared;

    private AudioManager() {
    }

    /*
     * 回调准备完毕
     *
     * */
    public interface AudioStateListener {
        void wellPrepared();
    }

    public AudioStateListener mListener;

    public void setOnAudioStateListener(AudioStateListener listener) {
        mListener = listener;
    }

    public static AudioManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager();
                }
            }
        }
        return mInstance;
    }

    public void prepareAudio() {
        try {
            isPrepared = false;
            String fileName = generateFileName();
            File file = new File(BaseActivity.cache, fileName);
            mCurFilePath = file.getAbsolutePath();
            mMediaRecorder = new MediaRecorder();
            //设置输出文件
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            //设置MediaRecorder的音频源为麦克风
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置音频格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            //设置音频编码为amr
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            //准备结束
            isPrepared = true;
            if (mListener != null)
                mListener.wellPrepared();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getVoiceLevel(int maxLevel) {
        try {
            //getMaxAmplitude获取振幅 1-32767
            return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void release(){
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    public void cancel(){
        release();
        if (mCurFilePath != null){
            File file = new File(mCurFilePath);
            file.delete();
            mCurFilePath = null;
        }
    }

    public String getCurFilePath() {
        return mCurFilePath;
    }

    /*
     *  随机生成文件名
     * */
    private String generateFileName() {
        return UUID.randomUUID().toString() + ".amr";
    }
}
