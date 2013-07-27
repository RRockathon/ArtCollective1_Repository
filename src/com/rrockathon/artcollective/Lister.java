package com.rrockathon.artcollective;

import static com.rrockathon.artcollective.Constants.CURRENT_SITE_ID_PREF;

import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * List sites and allow selecting them to see details.
 * 
 */
public abstract class Lister extends ListActivity {
	private static List<ArtEventData> eventList;
	private EditText editText;
	private String[] titles;
	private static ArtEventData currentEvent;

	public static ArtEventData getCurrentEvent() {
		return currentEvent;
	}

	public static void setCurrentEvent(ArtEventData currentEvent) {
		Lister.currentEvent = currentEvent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lister);
		fillData();
	}

	abstract String getMyType();

	private void fillData() {
		
		final DatabaseRefresher dbRefresher = new DatabaseRefresher();
		
		
		final String tabList = getMyType();
		Log.i("Load the tab data", tabList);
		if (tabList.equals(Constants.ART_EVENTS)
				|| tabList.equals(Constants.ARTS)
				|| tabList.equals(Constants.ARTISTS)) {
			
			if (DatabaseRefresher.count(this, tabList) == 0) {
				final ProgressDialog progressDialog = new ProgressDialog(
						Lister.this);
				progressDialog.setCancelable(true);
				progressDialog.setIndeterminate(true);
				progressDialog.setTitle(tabList.toUpperCase());
				progressDialog.setMessage("Initial Download Please Wait...");
				progressDialog.show();
				progressDialog
						.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface arg0) {
								setContentToCursor(dbRefresher, tabList);
							}
						});
				new Thread(new DatabaseRefresher.DataFetchRunnable(
						progressDialog, this, tabList)).start();
			} else {
				setContentToCursor(dbRefresher, tabList);
			}

		} else {
			return;
		}
	}

	private void setContentToCursor(final DatabaseRefresher dbRefresher,
			final String eventType) {
		Cursor cursor = dbRefresher.getEventDataListCursor(Lister.this,
				eventType);
		startManagingCursor(cursor);
		String[] names = new String[] { "title", "location", };
		int[] ids = new int[] { R.id.lister_row_site_title,
				R.id.lister_row_site_location };
		EventViewAdapter eventViewAdapter = new EventViewAdapter(Lister.this,
				R.layout.lister_row, cursor, names, ids);
		setListAdapter(eventViewAdapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.lister);
	}

	private ArtEventData getModel(int position) {
		if (position > eventList.size() - 1) {
			return eventList.get(eventList.size() - 1);
		}
		return eventList.get(position);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		SQLiteCursor cursor = (SQLiteCursor) ((SimpleCursorAdapter) l
				.getAdapter()).getCursor();
		cursor.moveToPosition(position);
		String name = cursor.getString(cursor.getColumnIndex("name"));
		String title = cursor.getString(cursor.getColumnIndex("title"));
		String description = cursor.getString(cursor
				.getColumnIndex("description"));
		String location = cursor.getString(cursor.getColumnIndex("location"));
		String website = cursor.getString(cursor.getColumnIndex("website"));
		String telephone = cursor.getString(cursor.getColumnIndex("telephone"));

		currentEvent = new ArtEventData(name, title, description,
				location, website, telephone);

		if (Constants.debug)
			Log.d("Lister", currentEvent.getDescription());
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		Editor edit = settings.edit();
		edit.putLong(CURRENT_SITE_ID_PREF, id);
		edit.commit();
		Intent i = new Intent(this, Details.class);
		startActivity(i);
		sendBroadcast(i);
	}

	class EventDataAdapter extends ArrayAdapter<String> {
		EventDataAdapter() {
			super(Lister.this, R.layout.lister_row, titles);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ViewWrapper wrapper = null;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.lister_row, parent, false);
				wrapper = new ViewWrapper(row);
				row.setTag(wrapper);
			} else {
				wrapper = (ViewWrapper) row.getTag();
			}
			wrapper.getTitle().setText(getModel(position).getTitle());
			wrapper.getLocation().setText(getModel(position).getLocation());
			return (row);
		}

		class ViewWrapper {
			View base;
			TextView title = null;
			TextView location = null;

			ViewWrapper(View base) {
				this.base = base;
			}

			TextView getTitle() {
				if (title == null) {
					title = (TextView) base
							.findViewById(R.id.lister_row_site_title);
				}
				return (title);
			}

			TextView getLocation() {
				if (location == null) {
					location = (TextView) base
							.findViewById(R.id.lister_row_site_location);
				}
				return (location);
			}
		}
	}

	/**
	 * Adapter for binding data to list
	 */
	class EventViewAdapter extends SimpleCursorAdapter {

		public EventViewAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			setViewBinder(new EventListViewBinder());
		}
	}

	class EventListViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			int dateColIndex = 5;
			return false;
		}

	}

}
