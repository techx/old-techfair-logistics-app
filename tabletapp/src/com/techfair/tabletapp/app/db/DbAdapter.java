package com.techfair.tabletapp.app.db;

import java.util.ArrayList;
import java.util.List;

import com.techfair.tabletapp.Globals;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbAdapter {

	public static final String DATABASE_NAME = "techietalkie.db";
	
	public static final String TABLE_SERVER = "server";
	public static final String SERVER_ID = "_id";
	public static final String SERVER_NAME = "name";
	public static final String SERVER_HOST = "host";
	public static final String SERVER_PORT = "port";
	public static final String SERVER_USERNAME = "username";
	public static final String SERVER_PASSWORD = "password";
	public static final String TABLE_SERVER_CREATE_SQL = "CREATE TABLE `"+TABLE_SERVER+"` ("
			   + "`"+SERVER_ID+"` INTEGER PRIMARY KEY AUTOINCREMENT,"
			   + "`"+SERVER_NAME+"` TEXT NOT NULL,"
			   + "`"+SERVER_HOST+"` TEXT NOT NULL,"
			   + "`"+SERVER_PORT+"` INTEGER,"
			   + "`"+SERVER_USERNAME+"` TEXT NOT NULL,"
			   + "`"+SERVER_PASSWORD+"` TEXT"
			   + ");";
	
	public static final String TABLE_FAVOURITES = "favourites";
	public static final String FAVOURITES_ID = "_id";
	public static final String FAVOURITES_CHANNEL = "channel";
	public static final String FAVOURITES_SERVER = "server";
	public static final String TABLE_FAVOURITES_CREATE_SQL = "CREATE TABLE `"+TABLE_FAVOURITES+"` ("
			   +"`"+FAVOURITES_ID+"` INTEGER PRIMARY KEY AUTOINCREMENT,"
			   +"`"+FAVOURITES_CHANNEL+"` TEXT NOT NULL,"
			   +"`"+FAVOURITES_SERVER+"` INTEGER NOT NULL"
			   +");";
	
	public static final Integer DATABASE_VERSION = 3;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(TABLE_SERVER_CREATE_SQL);
			db.execSQL(TABLE_FAVOURITES_CREATE_SQL);
		}

		@Override
		public void onUpgrade(
			final SQLiteDatabase db,
			final int oldVersion,
			final int newVersion) {
			Log.w(Globals.LOG_TAG, "Database upgrade from " + oldVersion +
								  " to " + newVersion);
			if (oldVersion == 1) {
				db.execSQL("ALTER TABLE `"+TABLE_SERVER+"` RENAME TO `"+TABLE_SERVER+"_old`");
				onCreate(db);
				db.execSQL("INSERT INTO `"+TABLE_SERVER+"` SELECT "
						   + "`"+SERVER_ID+"`, '', `"+SERVER_HOST+"`, `"+SERVER_PORT+"`, `"+SERVER_USERNAME+"`, `"+SERVER_PASSWORD+"` "
						   + "FROM `server_old`");
				db.execSQL("DROP TABLE `"+TABLE_SERVER+"_old`");
			}
		}
	}

	private final Context context;
	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	
	public DbAdapter(final Context context_) {
		context = context_;
	}

	public final void close() {
		dbHelper.close();
	}

	public final long createServer(
		final String name,
		final String host,
		final int port,
		final String username,
		final String password) {
		final ContentValues values = new ContentValues();
		values.put(SERVER_NAME, name);
		values.put(SERVER_HOST, host);
		values.put(SERVER_PORT, port);
		values.put(SERVER_USERNAME, username);
		values.put(SERVER_PASSWORD, password);
		return db.insert(TABLE_SERVER, null, values);
	}

	public final boolean deleteServer(final long serverId) {
		return db.delete(TABLE_SERVER, SERVER_ID + " = " + serverId, null) > 0;
	}

	public final List<Server> fetchAllServers() {
		final Cursor c = db.query(
			TABLE_SERVER,
			new String[] { SERVER_ID, SERVER_NAME, SERVER_HOST,
					SERVER_PORT, SERVER_USERNAME, SERVER_PASSWORD },
			null,
			null,
			null,
			null,
			null);
		
		List<Server> servers = new ArrayList<Server>();
		servers = parseServers(c);
		
		c.close();
		
		return servers;
	}
	
	/**
	 * Parses all server objects from the passed cursor.
	 * @param cursor
	 * @return
	 */
	private List<Server> parseServers(Cursor c) {		
		List<Server> servers = new ArrayList<Server>();

		c.moveToFirst();
		while(!c.isAfterLast()) {
			Server server = new Server(c.getInt(c.getColumnIndex(SERVER_ID)),
					c.getString(c.getColumnIndex(SERVER_NAME)), 
					c.getString(c.getColumnIndex(SERVER_HOST)), 
					c.getInt(c.getColumnIndex(SERVER_PORT)), 
					c.getString(c.getColumnIndex(SERVER_USERNAME)),
					c.getString(c.getColumnIndex(SERVER_PASSWORD)));
			servers.add(server);
			c.moveToNext();
		}
		
		return servers;
	}

	public final Server fetchServer(final long serverId) {
		final Cursor c = db.query(
			TABLE_SERVER,
			new String[] { SERVER_ID, SERVER_NAME, SERVER_HOST,
					SERVER_PORT, SERVER_USERNAME, SERVER_PASSWORD },
			SERVER_ID + " = " + serverId,
			null,
			null,
			null,
			null);
		
		Server server = parseServers(c).get(0);
		
		c.close();

		return server;
	}

	public final DbAdapter open() {
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public final void updateServer(
		final long id,
		final String name,
		final String host,
		final int port,
		final String username,
		final String password) {
		final ContentValues values = new ContentValues();
		values.put(SERVER_NAME, name);
		values.put(SERVER_HOST, host);
		values.put(SERVER_PORT, port);
		values.put(SERVER_USERNAME, username);
		values.put(SERVER_PASSWORD, password);
		db.update(
			TABLE_SERVER,
			values,
			SERVER_ID + "=?",
			new String[] { Long.toString(id) });
	}
	
	public long createFavourite(long serverId, int channelId) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FAVOURITES_CHANNEL, channelId);
		contentValues.put(FAVOURITES_SERVER, serverId);
		return db.insert(TABLE_FAVOURITES, null, contentValues);
	}
	
	private List<Favourite> parseFavourites(Cursor c) {
		List<Favourite> favourites = new ArrayList<Favourite>();

		c.moveToFirst();
		while(!c.isAfterLast()) {
			Favourite favourite = new Favourite(c.getInt(c.getColumnIndex(FAVOURITES_ID)),
					c.getInt(c.getColumnIndex(FAVOURITES_SERVER)), 
					c.getInt(c.getColumnIndex(FAVOURITES_CHANNEL)));
			favourites.add(favourite);
			c.moveToNext();
		}
		
		return favourites;
	}
	
	public List<Favourite> fetchAllFavourites() {

		final Cursor c = db.query(
			TABLE_FAVOURITES,
			new String[] { FAVOURITES_ID, FAVOURITES_CHANNEL, FAVOURITES_SERVER},
			null,
			null,
			null,
			null,
			null);
		
		List<Favourite> favourites = new ArrayList<Favourite>();
		favourites = parseFavourites(c);
		
		c.close();
		
		return favourites;
	}
	
	public boolean deleteFavourite(final long favouriteId) {
		return db.delete(TABLE_FAVOURITES, FAVOURITES_ID + " = " + favouriteId, null) > 0;
	}
}