package db.adapter;

import com.keenan.gather.MainActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("NewApi")
public class DBAdapter {
	// columns for contacts table
	public static final String CONTACTS_ROWID = "_id";
	public static final String CONTACTS_NAME = "name";
	public static final String CONTACTS_MOBILE = "mobile";
	public static final String CONTACTS_EMAIL = "email";
	public static final String CONTACTS_TYPE = "type";
	public static final String CONTACTS_DEPARTMENT = "department";
	public static final String CONTACTS_ADDEDBY = "addedby";

	// columns for user table
	public static final String USER_ROWID = "_id";
	public static final String USER_FULLNAME = "fullname";
	public static final String USER_PASSWORD = "password";
	public static final String USER_MOBILE = "mobile";
	public static final String USER_EMAIL = "email";

	public static final String[] ALL_CONTACTS = { CONTACTS_ROWID,
			CONTACTS_NAME, CONTACTS_MOBILE, CONTACTS_EMAIL, CONTACTS_TYPE,
			CONTACTS_DEPARTMENT, CONTACTS_ADDEDBY };
	public static final String[] ALL_USER = { USER_ROWID, USER_FULLNAME,
			USER_PASSWORD, USER_MOBILE, USER_EMAIL };
	private static final String TAG = "DBAdapter";

	private static final String DATABASE_NAME = "GatherNTECDB";
	private static final String TABLE_CONTACTS = "contacts";
	private static final String TABLE_USER = "user";
	private static final int DATABASE_VERSION = 5;

	// query for table contacts
	private static final String TABLE_CONTACTS_CREATE = "CREATE TABLE "
			+ TABLE_CONTACTS + "(" + CONTACTS_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + CONTACTS_NAME
			+ " VARCHAR NOT NULL," + CONTACTS_MOBILE + " VARCHAR, "
			+ CONTACTS_EMAIL + " VARCHAR , " + CONTACTS_TYPE + " VARCHAR ,"
			+ CONTACTS_DEPARTMENT + " VARCHAR ," + CONTACTS_ADDEDBY
			+ " VARCHAR " + ")";

	// query for table user
	private static final String TABLE_USER_CREATE = "CREATE TABLE "
			+ TABLE_USER + "(" + USER_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + USER_FULLNAME
			+ " VARCHAR NOT NULL," + USER_PASSWORD + " VARCHAR, " + USER_EMAIL
			+ " VARCHAR , " + USER_MOBILE + " VARCHAR" + ")";
	private final Context context;

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			try {
				db.execSQL(TABLE_CONTACTS_CREATE);
				db.execSQL(TABLE_USER_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS contacts");
			db.execSQL("DROP TABLE IF EXISTS user");
			onCreate(db);
		}
	}

	// ---opens the database---
	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	// ---closes the database---
	public void close() {
		DBHelper.close();
	}

	/* METHODS and QUERIES FOR CONTACTS */

	// ---insert a record into the database---
	public long insertContact(String name, String mobile, String email,
			String type, String department, String addedby) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(CONTACTS_NAME, name);
		initialValues.put(CONTACTS_MOBILE, mobile);
		initialValues.put(CONTACTS_EMAIL, email);
		initialValues.put(CONTACTS_TYPE, type);
		initialValues.put(CONTACTS_DEPARTMENT, department);
		initialValues.put(CONTACTS_ADDEDBY, addedby);

		return db.insert(TABLE_CONTACTS, null, initialValues);

	}

	// ---deletes a particular record---
	public boolean deleteContact(long rowId) {
		return db.delete(TABLE_CONTACTS, CONTACTS_ROWID + "=" + rowId, null) > 0;
	}

	// ---deletes a particular existing record---
	public boolean deleteExistingContact(String mobile) {

		return db.delete(TABLE_CONTACTS, CONTACTS_MOBILE + "= " + "'" + mobile
				+ "'", null) > 0;

	}

	// ---retrieves all the records---
	public Cursor getAllContacts(String user) {
		Cursor mCursor = db.query(TABLE_CONTACTS, new String[] {
				CONTACTS_ROWID, CONTACTS_NAME, CONTACTS_MOBILE, CONTACTS_EMAIL,
				CONTACTS_TYPE, CONTACTS_DEPARTMENT, CONTACTS_ADDEDBY },
				CONTACTS_ADDEDBY + "='" + user + "'", null, null, null,
				CONTACTS_NAME + " COLLATE NOCASE ASC;");
		if (mCursor != null) {
			mCursor.moveToFirst();
		}

		return mCursor;

	}

	// ---retrieves a particular record---
	public Cursor getContact(String mobile, String addedby) throws SQLException {
		Cursor mCursor = db.query(true, TABLE_CONTACTS, ALL_CONTACTS,
				CONTACTS_MOBILE + "=" + "'" + mobile + "'" + "AND "
						+ CONTACTS_ADDEDBY + "= '" + addedby + "'", null, null,
				null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// ---updates a record---
	public boolean updateContact(String name, String mobile, String email,
			String type, String department) {

		ContentValues args = new ContentValues();
		args.put(CONTACTS_NAME, name);
		args.put(CONTACTS_MOBILE, mobile);
		args.put(CONTACTS_EMAIL, email);
		args.put(CONTACTS_TYPE, type);
		args.put(CONTACTS_DEPARTMENT, department);
		return db.update(TABLE_CONTACTS, args, CONTACTS_MOBILE + "=" + mobile,
				null) > 0;
	}

	/* END FOR CONTACTS */

	/* METHODS and QUERIES FOR USER */

	public long insertUser(String fullname, String password, String mobile,
			String email) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(USER_FULLNAME, fullname);
		initialValues.put(USER_PASSWORD, password);
		initialValues.put(USER_MOBILE, mobile);
		initialValues.put(USER_EMAIL, email);
		return db.insert(TABLE_USER, null, initialValues);

	}

	// for logging in
	public Cursor getSingleUser(String mobile, String password) {
		{
			Cursor mCursor =
			// db.rawQuery("SELECT * FROM " +TABLE_USER+
			// " WHERE "+USER_USERNAME+ "=" +username+ " AND =" +password ,
			// null);
			db.query(TABLE_USER, ALL_USER, USER_MOBILE + "= '" + mobile + "'"
					+ " AND " + USER_PASSWORD + "='" + password + "'", null,
					null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}

	}

	// for profile
	public Cursor getUser(String mobile) {
		{
			Cursor mCursor =
			// db.rawQuery("SELECT * FROM " +TABLE_USER+
			// " WHERE "+USER_USERNAME+ "=" +username+ " AND =" +password ,
			// null);
			db.query(TABLE_USER, ALL_USER, USER_MOBILE + "= '" + mobile + "'",
					null, null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}
	}

	public Cursor userCheckIfExist(String mobile, String email)
			throws SQLException {
		Cursor mCursor = db.query(true, TABLE_USER, ALL_USER, USER_MOBILE
				+ "='" + mobile + "'OR " + USER_EMAIL + "='" + email + "'",
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor userPasswordRecovery(String mobile, String email) {
		Cursor mCursor = db.query(true, TABLE_USER, ALL_USER, USER_MOBILE
				+ "='" + mobile + "'OR " + USER_EMAIL + "='" + email + "'",
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
}
