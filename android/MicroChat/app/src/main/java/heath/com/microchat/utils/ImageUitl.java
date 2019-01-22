package heath.com.microchat.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.InputStream;

import heath.com.microchat.service.CacheService;

public class ImageUitl {
	private static String SDPATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator;

	private File cache;

	public ImageUitl(File cache) {
		this.cache = cache;
	}

	public static void SimpleShowImage(String url, ImageView iv) {
		String path = SDPATH + "mcc" + File.separator + url;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		Bitmap bm = BitmapFactory.decodeFile(path, options);
		iv.setImageBitmap(bm);
	}

	public static void setImageBitmap(final String url,final ImageView iv) {
		com.heath.recruit.utils.ThreadUtils.runInThread(new Runnable() {
			@Override
			public void run() {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpPost httpget = new HttpPost(url);
				try {
					HttpResponse resp = httpclient.execute(httpget);
					// 判断是否正确执行
					if (HttpStatus.SC_OK == resp.getStatusLine().getStatusCode()) {
						// 将返回内容转换为bitmap
						HttpEntity entity = resp.getEntity();
						InputStream in = entity.getContent();
						Bitmap mBitmap = BitmapFactory.decodeStream(in);
						// 向handler发送消息，执行显示图片操作
						iv.setImageBitmap(mBitmap);
					}
				} catch (Exception e) {
					System.out.println(e);
				} finally {
					httpclient.getConnectionManager().shutdown();
				}
			}
		});
	}

	public void asyncloadImage(ImageView iv_header, String path) {
		CacheService service = new CacheService();
		AsyncImageTask task = new AsyncImageTask(service, iv_header);
		task.execute(path);
	}

	private final class AsyncImageTask extends AsyncTask<String, Integer, Uri> {

		private CacheService service;
		private ImageView iv_header;

		public AsyncImageTask(CacheService service, ImageView iv_header) {
			this.service = service;
			this.iv_header = iv_header;
		}

		// 后台运行的子线程子线程
		@Override
		protected Uri doInBackground(String... params) {
			try {
				return service.getImageURI(params[0], cache);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		// 这个放在在ui线程中执行
		@Override
		protected void onPostExecute(Uri result) {
			super.onPostExecute(result);
			// 完成图片的绑定
			if (iv_header != null && result != null) {
				iv_header.setImageURI(result);
			}
		}
	}
}
