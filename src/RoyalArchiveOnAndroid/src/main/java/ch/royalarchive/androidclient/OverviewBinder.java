package ch.royalarchive.androidclient;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class OverviewBinder implements ViewBinder {
	
	private static String TAG = OverviewBinder.class.getSimpleName();
	
	private Map<String, SoftReference<Bitmap>> bitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>();
	private Map<View, AsyncTask<Void, Void, Void>> runningBgTasks = new WeakHashMap<View, AsyncTask<Void, Void, Void>>();
	
	@Override
	public boolean setViewValue(final View view, Cursor cursor, int columnIndex) {
		
		if (!(view instanceof ImageView)) {
			return false;
		}
		
		AsyncTask<Void, Void, Void> runningOldTask = runningBgTasks.get(view);
		
		if (runningOldTask != null) {
			runningOldTask.cancel(false);
		}
		
		final String thumbnailUriString = cursor.getString(columnIndex);
		final ImageView imageView = (ImageView) view;
		// skip this entry
		if (thumbnailUriString == null) {
			imageView.setImageResource(android.R.drawable.picture_frame);
			return true;
		}
		
		SoftReference<Bitmap> cacheReference = bitmapCache.get(thumbnailUriString);
		if (cacheReference != null) {
			Bitmap cachedBitmap = cacheReference.get();
			if (cachedBitmap != null) {
				imageView.setImageBitmap(cachedBitmap);
				return true;
			}
		}
		imageView.setImageResource(android.R.drawable.picture_frame);
		final Uri uri = Uri.parse(thumbnailUriString);

		AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

			private Bitmap bitmap;

			@Override
			protected Void doInBackground(Void... params) {
				try {
					// get the real image
					InputStream imageStream = view.getContext().getContentResolver().openInputStream(uri);
					try {
						int imageLength = view.getContext().getResources().getDimensionPixelSize(R.dimen.image_width);

						Bitmap fullBitmap = BitmapFactory.decodeStream(imageStream);
						double scaleX = 1.0 * imageLength / fullBitmap.getWidth();
						double scaleY = 1.0 * imageLength / fullBitmap.getHeight();
						double scale = Math.max(scaleX, scaleY);

						bitmap = Bitmap.createScaledBitmap(fullBitmap, (int) Math.round(fullBitmap.getWidth() * scale),
								(int) Math.round(fullBitmap.getHeight() * scale), true);
						bitmapCache.put(thumbnailUriString, new SoftReference<Bitmap>(bitmap));
						return null;
					} finally {
						if(imageStream != null) {
							imageStream.close();
						}
					}
				} catch (Throwable t) {
					Log.i(TAG, "Cannot load image");
					return null;
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				imageView.setImageBitmap(bitmap);
				runningBgTasks.remove(imageView);
			}
		};
		runningBgTasks.put(view, asyncTask);
		asyncTask.execute();
		return true;
	}

}
