package ch.royalarchive.androidclient.photo;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import ch.bergturbenthal.image.provider.Client;
import ch.royalarchive.androidclient.R;

public class PhotoOverviewActivity extends Activity implements LoaderCallbacks<Cursor> {
	
	private static final String CURR_ITEM_INDEX = "currentItemIndex";

	private SimpleCursorAdapter cursorAdapter;
	private Uri albumUri;
	private int currentItemIndex;

	private GridView gridview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// get album id out of intent
		Bundle bundle = getIntent().getExtras();
		albumUri = Uri.parse( bundle.getString("album_uri"));
		setContentView(R.layout.photo_overview);

		// Create an empty adapter we will use to display the loaded data.
		cursorAdapter = new PhotoOverviewAdapter(this, R.layout.photo_overview_item);

		gridview = (GridView) findViewById(R.id.photo_overview);
		gridview.setAdapter(cursorAdapter);

		// Handle click on photo
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = new Intent(PhotoOverviewActivity.this, PhotoDetailViewActivity.class);
				intent.putExtra("album_uri", albumUri.toString());
				intent.putExtra("actPos", position);
				startActivityForResult(intent, 1);
			}
		});
		gridview.setWillNotCacheDrawing(false);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Bundle bundle = data.getExtras();
		currentItemIndex = bundle.getInt(CURR_ITEM_INDEX);
		gridview.setSelection(currentItemIndex);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, albumUri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		cursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		cursorAdapter.swapCursor(null);
	}

}
