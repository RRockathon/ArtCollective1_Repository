package com.rrockathon.artcollective;

import static com.rrockathon.artcollective.Constants.LAST_UPDATED_PREF;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class DatabaseRefresher extends Service {

	//note: workaround for rare case when background thread and foreground compete (short of using a lock)
	public static boolean updating;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		updating = true;
		Log.i(DatabaseRefresher.class.getName(),String.format("service id=%d started", startId));

		new Thread(new Runnable() {
			@Override
			public void run() {
				new DataFetchRunnable(DatabaseRefresher.this, Constants.ART_EVENTS).run();
			}
		}).start();
	}

	private static SQLiteDatabase getDatabase(Context aContext) {
		SQLiteDatabase database = new SQLiteOpenHelper(aContext, "events", null, 1) {
			@Override
			public void onCreate(SQLiteDatabase database) {
				Log.i(DatabaseRefresher.class.getName(), "new database; creating tables");
				database
						.execSQL("create table events (_id integer primary key, name text, title text, description text, location text, website text, telephone text)");
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				db.execSQL("DROP TABLE IF EXISTS events");
				onCreate(db);
			}
		}.getWritableDatabase();
		return database;
	}

	public static class DataFetchRunnable implements Runnable {
		ProgressDialog progressDialog = null;

		Context context;
		SharedPreferences settings;
		SQLiteDatabase database;

		final private String eventType;

		public DataFetchRunnable(Context aContext, String anEventType) {
			context = aContext;
			eventType = anEventType;
			settings = PreferenceManager.getDefaultSharedPreferences(context);
		}

		public DataFetchRunnable(ProgressDialog pd, Context aContext, String anEventType) {
			progressDialog = pd;
			context = aContext;
			eventType = anEventType;
			settings = PreferenceManager.getDefaultSharedPreferences(context);
		}

		@Override
		public void run() {
			
			if(updating && progressDialog==null || !updating && progressDialog!=null) {
				Log.i(DatabaseRefresher.class.getName(), "about to open database");

				try {
					database = getDatabase(context);

					final List<ArtEventData> eventList;

					if (eventType.equals(Constants.ART_EVENTS)) {
						eventList = new ArtEventLoader().getArtEventsData();
						
					} else {
						throw new RuntimeException("knew we should have used an enum!");
					}

					if(eventList.size()>0) {
						database.beginTransaction();

						// remove all of the existing events
						database.execSQL("delete from events");

						final SQLiteStatement compiledStatement = database
								.compileStatement("insert into events (_id, name, title, description, location, website, telephone) values (null, ?,?,?,?,?,?)");

						insertEvents(compiledStatement, eventList);

						database.setTransactionSuccessful();

						database.endTransaction();
					}

				} catch (Exception e) {
					// no network connection, no problem
				} finally {
					close(database);
				}
			}
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.cancel();
			}
			if (eventType.equals(Constants.ART_EVENTS)) {
				updating = false;
				final Editor edit = settings.edit();
				edit.putLong(LAST_UPDATED_PREF, System.currentTimeMillis());
				edit.commit();
			}
		}

	}

	public static void close(SQLiteDatabase database) {
		if (database != null) {
			database.close();
			SQLiteDatabase.releaseMemory();
			database=null;
		}

		Log.i(DatabaseRefresher.class.getName(), "database closed");

	}

	public static long count(Context aContext, String anEventType) {
		int count = 0;
		SQLiteDatabase database;
		database = getDatabase(aContext);
		database.beginTransaction();

		Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM events", null);
		if (null == cursor) {
			return 0;
		}
		cursor.moveToFirst();
		count = cursor.getInt(0);
		cursor.close();

		database.endTransaction();
		database.close();
		SQLiteDatabase.releaseMemory();
		database=null;
		return count;
	}

	public Cursor getEventDataListCursor(Context aContext, String category) {
		SQLiteDatabase database = getDatabase(aContext);
		Cursor cursor = database.rawQuery("SELECT * FROM events", null);
		if (null == cursor) {
			return null;
		}
		return cursor;
	}

	private static void insertEvents(final SQLiteStatement compiledStatement, final List<ArtEventData> eventList) {
		for (ArtEventData eventData : eventList) {
			setString(compiledStatement, 1, eventData.getName());
			setString(compiledStatement, 2, eventData.getTitle());
			setString(compiledStatement, 3, eventData.getDescription());
			setString(compiledStatement, 4, eventData.getLocation());
			setString(compiledStatement, 5, eventData.getWebsite());
			setString(compiledStatement, 6, eventData.getTelephone());
			compiledStatement.execute();
		}
		Log.i(DatabaseRefresher.class.getName(), String.format("inserted %d rows", eventList.size()));
	}

	private static void setString(SQLiteStatement statement, int i, String s) {
		if (s == null) {
			statement.bindNull(i);
		} else {
			statement.bindString(i, s);
		}
	}

	private static void setDate(SQLiteStatement statement, int i, Date d) {
		if (d == null) {
			statement.bindNull(i);
		} else {
			statement.bindLong(i, d.getTime());
		}
	}
}
