package heath.com.microchat.utils;

import android.app.Activity;
import android.app.Application;
import android.widget.Toast;

public class ToastUtil {

	private static Toast toast = null;

	public static void toastOnUiThread(Activity activity, final String message) {

		final Application application = activity.getApplication();

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (null == toast) {
					toast = Toast.makeText(application, message,
							Toast.LENGTH_SHORT);
				}
				toast.setText(message);
				toast.show();
			}
		});

	}

	public static void toastOnUiThread(Activity activity, final int resId) {
		toastOnUiThread(activity, activity.getString(resId));
	}
}