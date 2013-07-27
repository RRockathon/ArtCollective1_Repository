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
		final String tabList = getMyType();
		Log.i("Load the tab data", tabList);
		final DatabaseRefresher dbRefresher = new DatabaseRefresher();
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
		Cursor cursor = null;
		String[] names = null;
		int[] ids = null;
		cursor = dbRefresher.getDataListCursor(Lister.this, eventType);
		
		if(Constants.ARTS.equals(eventType)){
			names = new String[] { "name", "artistname", };
			 ids = new int[] { R.id.lister_row1,
						R.id.lister_row2 };
		} else if(Constants.ARTISTS.equals(eventType)) {
			names = new String[] {"name", "city", "state", "country"};
			 ids = new int[] { R.id.lister_row1,
						R.id.lister_row2, R.id.lister_row3,
						R.id.lister_row4  };
		} else if(Constants.ART_EVENTS.equals(eventType)){
			 names = new String[] { "title", "location", };
			 ids = new int[] { R.id.lister_row1,
						R.id.lister_row2 };
			 
		}
		
		startManagingCursor(cursor);
		
		ViewAdapter eventViewAdapter = new ViewAdapter(Lister.this,
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

	private ArtEventData getArtEventModel(int position) {
		if (position > eventList.size() - 1) {
			return eventList.get(eventList.size() - 1);
		}
		return eventList.get(position);
	}

	

	
//	/**
//	 * Adapter for binding data to list
//	 */
//	class EventViewAdapter extends SimpleCursorAdapter {
//
//		public EventViewAdapter(Context context, int layout, Cursor c,
//				String[] from, int[] to) {
//			super(context, layout, c, from, to);
//			setViewBinder(new EventListViewBinder());
//		}
//	}
	
	/**
	 * Adapter for binding data to list
	 */
	class ViewAdapter extends SimpleCursorAdapter {

		public ViewAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			setViewBinder(new ListViewBinder());
		}
	}
	

	class ListViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			int dateColIndex = 5;
			return false;
		}

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
	
//	class EventDataAdapter extends ArrayAdapter<String> {
//		EventDataAdapter() {
//			super(Lister.this, R.layout.lister_row, titles);
//		}
//
//		public View getView(int position, View convertView, ViewGroup parent) {
//			View row = convertView;
//			ViewWrapper wrapper = null;
//			if (row == null) {
//				LayoutInflater inflater = getLayoutInflater();
//				row = inflater.inflate(R.layout.lister_row, parent, false);
//				wrapper = new ViewWrapper(row);
//				row.setTag(wrapper);
//			} else {
//				wrapper = (ViewWrapper) row.getTag();
//			}
//			wrapper.getRow1().setText(getArtEventModel(position).getTitle());
//			wrapper.getRow2().setText(getArtEventModel(position).getLocation());
//			return (row);
//		}
//
//		class ViewWrapper {
//			View base;
//			TextView row1 = null;
//			TextView row2 = null;
//			TextView row3 = null;
//			TextView row4 = null;
//
//			ViewWrapper(View base) {
//				this.base = base;
//			}
//
//			TextView getRow1() {
//				if (row1 == null) {
//					row1 = (TextView) base
//							.findViewById(R.id.lister_row1);
//				}
//				return (row1);
//			}
//
//			TextView getRow2() {
//				if (row2 == null) {
//					row2 = (TextView) base
//							.findViewById(R.id.lister_row2);
//				}
//				return (row2);
//			}
//			TextView getRow3() {
//				if (row3 == null) {
//					row3 = (TextView) base
//							.findViewById(R.id.lister_row3);
//				}
//				return (row3);
//			}
//
//			TextView getRow4() {
//				if (row4 == null) {
//					row4 = (TextView) base
//							.findViewById(R.id.lister_row4);
//				}
//				return (row4);
//			}
//		}
//	}


}
