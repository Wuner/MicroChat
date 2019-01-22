package heath.com.microchat.message;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import heath.com.microchat.R;

public class DialogManager {
    private Dialog mDialog;
    private Context mContext;

    private ImageView mIvDialogRecorderIcon;
    private ImageView mIvDialogRecorderVoice;
    private TextView mTvDialogRecorderLabel;

    public DialogManager(Context context) {
        this.mContext = context;
    }

    public void showRecordingDialog() {
        mDialog = new Dialog(mContext, R.style.AudioDialog_Theme);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_recorder, null);
        mDialog.setContentView(view);
        mIvDialogRecorderIcon = mDialog.findViewById(R.id.iv_dialog_recorder_icon);
        mIvDialogRecorderVoice = mDialog.findViewById(R.id.iv_dialog_recorder_voice);
        mTvDialogRecorderLabel = mDialog.findViewById(R.id.tv_dialog_recorder_label);
        mDialog.show();
    }

    public void recording() {
        if (mDialog != null && mDialog.isShowing()){
            mIvDialogRecorderIcon.setVisibility(View.VISIBLE);
            mIvDialogRecorderVoice.setVisibility(View.VISIBLE);
            mTvDialogRecorderLabel.setVisibility(View.VISIBLE);
            mIvDialogRecorderIcon.setImageResource(R.drawable.recorder);
            mTvDialogRecorderLabel.setText(mContext.getResources().getString(R.string.tv_dialog_recorder_label_normal));
        }
    }

    public void wantToCancel() {
        if (mDialog != null && mDialog.isShowing()){
            mIvDialogRecorderIcon.setVisibility(View.VISIBLE);
            mIvDialogRecorderVoice.setVisibility(View.GONE);
            mTvDialogRecorderLabel.setVisibility(View.VISIBLE);
            mIvDialogRecorderIcon.setImageResource(R.drawable.cancel);
            mTvDialogRecorderLabel.setText(mContext.getResources().getString(R.string.tv_dialog_recorder_label_up));
        }
    }

    public void tooShort() {
        if (mDialog != null && mDialog.isShowing()){
            mIvDialogRecorderIcon.setVisibility(View.VISIBLE);
            mIvDialogRecorderVoice.setVisibility(View.GONE);
            mTvDialogRecorderLabel.setVisibility(View.VISIBLE);
            mIvDialogRecorderIcon.setImageResource(R.drawable.voice_to_short);
            mTvDialogRecorderLabel.setText(mContext.getResources().getString(R.string.tv_dialog_recorder_label_too_short));
        }
    }

    public void dimiss() {
        if (mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void updateVoiceLevel(int level) {
        if (mDialog != null && mDialog.isShowing()){
            int resId = mContext.getResources().getIdentifier("v"+level,"drawable",mContext.getPackageName());
            mIvDialogRecorderVoice.setImageResource(resId);
        }
    }
}
