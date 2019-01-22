package heath.com.microchat.message;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import heath.com.microchat.R;

public class AudioRecorderButton extends android.support.v7.widget.AppCompatButton implements AudioManager.AudioStateListener {

    private static final int STATE_NORMAL = 1;//默认状态
    private static final int STATE_RECORDING = 2;//录音
    private static final int STATE_WANT_TO_CANCEL = 3;//取消录音

    private static final int DISTANCE_Y_CANCEL = 50;

    private static final int maxLevel = 7;
    private long mTime;
    private boolean mReady;//是否触发longclick


    private int mCurState = STATE_NORMAL;//当前状态
    private boolean isRecording = false;//是否真正录音

    private DialogManager mDialogManager;
    private AudioManager mAudioManager;
    private Handler mHandler;

    public AudioRecorderButton(Context context) {
        this(context, null);
    }

    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDialogManager = new DialogManager(getContext());
        mAudioManager = AudioManager.getInstance();
        mAudioManager.setOnAudioStateListener(this);
        mHandler = new IHandler();
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mReady = true;
                mAudioManager.prepareAudio();
                return false;
            }
        });
    }

    /**
     *录音完成后的回调
     */
    public interface AudioFinishRecorderListener{
        void onFinish(long seconds,String filePath);
    }

    private AudioFinishRecorderListener mListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener){
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mReady){
                    reset();
                    return super.onTouchEvent(event);
                }
                if (!isRecording || mTime < 600){
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS,1300);
                }else if (mCurState == STATE_RECORDING) {
                    mDialogManager.dimiss();
                    mAudioManager.release();
                    if (mListener != null){
                        mListener.onFinish(mTime,mAudioManager.getCurFilePath());
                    }
                } else if (mCurState == STATE_WANT_TO_CANCEL) {
                    mDialogManager.dimiss();
                    mAudioManager.cancel();

                }
                reset();
                break;
        }

        return super.onTouchEvent(event);
    }

    private void reset() {
        changeState(STATE_NORMAL);
        isRecording = false;
        mReady = false;
        mTime = 0;
    }

    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        if (y < -DISTANCE_Y_CANCEL || y > DISTANCE_Y_CANCEL + getHeight()) {
            return true;
        }
        return false;
    }

    private void changeState(int state) {
        if (mCurState != state) {
            mCurState = state;
            switch (state) {
                case STATE_NORMAL:
                    setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_recorder_normal));
                    setText(getResources().getString(R.string.tv_say_recorder_normal));
                    break;
                case STATE_RECORDING:
                    setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_recorder_recording));
                    setText(getResources().getString(R.string.tv_say_recorder_recording));
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_recorder_recording));
                    setText(getResources().getString(R.string.tv_say_recorder_want_cancel));
                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }

    private static final int MSG_AUDIO_PREPARED = 0x110;
    private static final int MSG_VOICE_CHANGED = 0x111;
    private static final int MSG_DIALOG_DIMISS = 0x112;

    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mTime += 100;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class IHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case MSG_VOICE_CHANGED:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(maxLevel));
                    break;
                case MSG_DIALOG_DIMISS:
                    mDialogManager.dimiss();
                    break;
            }
        }
    }

}
