package ch.royalarchive.androidclient.photo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import ch.bergturbenthal.image.provider.Client;
import ch.royalarchive.androidclient.PhotoBinder;
import ch.royalarchive.androidclient.R;

public class PhotoOverviewAdapter extends SimpleCursorAdapter {
	
	private static final String[] FROM = new String[] { 
		Client.AlbumEntry.THUMBNAIL,
		Client.AlbumEntry.NAME };

	private static final int[] TO = new int[] {
		R.id.photos_item_image,
		R.id.photo_name};

	public PhotoOverviewAdapter(Context context, int layout) {
		super(context, layout, null, FROM, TO, 0);
		
		// set photo overview view binder
		setViewBinder(new PhotoBinder(false, context));
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View newView = super.getView(position, convertView, parent);
		newView.setTag(getCursor().getInt(getCursor().getColumnIndexOrThrow(Client.AlbumEntry.ID)));
		return newView;
	}

}
