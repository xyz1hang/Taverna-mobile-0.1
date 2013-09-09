package cs.man.ac.uk.tavernamobile.dataaccess;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class RunHistoryHelper extends ContentProvider {

	private static Context myContext;

	//private static String DATABASE_PATH; // = "/data/data/cs.man.ac.uk.tavernamobile/databases/";
	
	// the database helper
	private DatabaseHelper mDbHelper;
	// the database
	private static SQLiteDatabase mDb;
	
    // Constants for building SQLite tables during initialization
    private static final String TEXT_TYPE = "TEXT";
    private static final String PRIMARY_KEY_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String FOREIGN_KEY_TYPE = "FOREIGN KEY";
    private static final String REFERENCES_TYPE = "REFERENCES ";
    private static final String BLOB_TYPE = "BLOB";
    
	public static final String TABLE_CREATE_STATEMENT = "CREATE TABLE" + " " +
			DataProviderConstants.WF_TABLE_NAME + " " +
            "(" + " " +
            DataProviderConstants.WF_ID + " " + PRIMARY_KEY_TYPE + " ," +
            DataProviderConstants.WorkflowTitle + " " + TEXT_TYPE + " ," +
            DataProviderConstants.WorkflowFilePath + " " + TEXT_TYPE + " ," +
            DataProviderConstants.WorkflowUri + " " + TEXT_TYPE + " ," +
            DataProviderConstants.Version + " " + TEXT_TYPE + ", " +
            DataProviderConstants.UploaderName + " " + TEXT_TYPE + ", " +
            //DataProviderConstants.Run_Id + " " + TEXT_TYPE + ", " +
            DataProviderConstants.LastLaunch + " " + TEXT_TYPE + ", " +
            DataProviderConstants.FirstLaunch + " " + TEXT_TYPE + ", " +
            DataProviderConstants.Avatar + " " + BLOB_TYPE +
            ")";
	
	public static final String REFERENCE_TABLE_CREATE_STATEMENT = "CREATE TABLE" + " " +
			DataProviderConstants.WF_RUN_TABLE_NAME + " " +
            "(" + " " +
            DataProviderConstants.WF_RUN_ID + " " + PRIMARY_KEY_TYPE + " ," +
            DataProviderConstants.Run_Id + " " + TEXT_TYPE + ", " +
            DataProviderConstants.WF_ID + " " + TEXT_TYPE + ", " +
            FOREIGN_KEY_TYPE + "(" + DataProviderConstants.WF_ID + ") " + 
            REFERENCES_TYPE + DataProviderConstants.WF_TABLE_NAME + 
            "(" + DataProviderConstants.WF_ID + ")" + ")";
	
	private static final int WF_TABLE = 1;
    private static final int RUN_TABLE = 2;
    private static final int JOIN_TABLE = 3;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sURIMatcher.addURI(DataProviderConstants.AUTHORITY, DataProviderConstants.WF_TABLE_NAME, WF_TABLE);
        sURIMatcher.addURI(DataProviderConstants.AUTHORITY, DataProviderConstants.WF_RUN_TABLE_NAME, RUN_TABLE);
        sURIMatcher.addURI(DataProviderConstants.AUTHORITY, DataProviderConstants.WF_RUN_JOIN_TABLE, JOIN_TABLE);
    }
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		//private static Context liteHelperContext;

		public DatabaseHelper(Context context) {
			super(context, DataProviderConstants.DATABASE_FILE_NAME, null, 
					DataProviderConstants.DATABASE_VERSION);
			//liteHelperContext = context;
		}

		/*public void createDataBase() throws IOException {

			boolean dbExist = checkDataBase();

			// Create a new one if it doesn't exist
			if (!dbExist) {
				// this invokes the onCreate method
				// this.getReadableDatabase();
				
				// Create the database by copying database
				// file to the system
				try {
					copyDataBase();
				} catch (IOException e) {
					throw new Error("Error copying database");
				}
			}
		}

		// Check if the database already exist
		private boolean checkDataBase() {
			File dbFile = new File(DATABASE_PATH + DataProviderConstants.DATABASE_NAME);
			return dbFile.exists();
		}

		*//**
		 * Copies database from local "assets" folder to the just created empty
		 * database in the system folder, from where it can be accessed and
		 * handled. This is done by transferring byte stream.
		 * *//*
		private void copyDataBase() throws IOException {

			// Open local db file as the input stream
			InputStream localDbFileis = liteHelperContext.getAssets().open(DataProviderConstants.DATABASE_NAME);

			// Path of the database folder in the system
			String systemDbFilePath = DATABASE_PATH + DataProviderConstants.DATABASE_NAME;
			OutputStream systemDbFileOs = new FileOutputStream(systemDbFilePath);

			// transfer the bytes
			byte[] buffer = new byte[1024];
			int length;
			while ((length = localDbFileis.read(buffer)) > 0) {
				systemDbFileOs.write(buffer, 0, length);
			}

			// Close the streams
			systemDbFileOs.flush();
			systemDbFileOs.close();
			localDbFileis.close();
		}

		public void openDataBase() throws SQLException {
			String dbPath = DATABASE_PATH + DataProviderConstants.DATABASE_NAME;
			mDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
		}*/

		@Override
		public synchronized void close() {
			if (mDb != null)
				mDb.close();

			super.close();
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// specify database structure by SQLite Browser is
			// much more convenient than using SQL statement
			// for simple table
			// db.execSQL(DATABASE_CREATE);
			db.execSQL(TABLE_CREATE_STATEMENT);
			db.execSQL(REFERENCE_TABLE_CREATE_STATEMENT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Log.w(TAG, "Upgrading database from version " + oldVersion +
			// " to "
			// + newVersion + ", which will destroy all old data");
			// db.execSQL("DROP TABLE IF EXISTS LaunchHistory");
			// onCreate(db);
		}
	}// End of DatabaseHelper Class

	
//	public void createAndOpenDatabase() {
//		try {
//			mDbHelper.createDataBase();
//		} catch (IOException e) {
//			throw new Error("Unable to create database");
//		}
//
//		try {
//			mDbHelper.openDataBase();
//		} catch (SQLException sqle) {
//			throw sqle;
//		}
//	}
	
	public RunHistoryHelper() {}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 *//*
	public RunHistoryHelper(Context context) {
		myContext = context;
		DATABASE_PATH = myContext.getFilesDir().getParentFile().getPath() + "/databases/";
	}

	*//**
	 * Open the workflow Record database. If it cannot be opened, try to create
	 * a new instance of the database. If it cannot be created, throw an
	 * exception to signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         Initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 *//*
	public RunHistoryHelper open() throws SQLException {
		mDbHelper = new DatabaseHelper(myContext);
		createAndOpenDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}*/

	/**
	 * Create a new workflow record. If the workflow record is successfully
	 * created return the new rowId for that Record, otherwise return a -1 to
	 * indicate failure.
	 * 
	 * @return rowId or -1 if failed
	 */
//	public long insertWorkflowRecord(String title, String version,
//			String filename, String uploader, byte[] avatar, String uri,
//			String run_id) {
//		
//		ContentValues valuesToInsert = new ContentValues();
//
//		valuesToInsert.put(DataProviderConstants.WorkflowTitle, title);
//		valuesToInsert.put(DataProviderConstants.Version, version);
//		valuesToInsert.put(DataProviderConstants.WorkflowFileName, filename);
//		valuesToInsert.put(DataProviderConstants.UploaderName, uploader);
//		valuesToInsert.put(DataProviderConstants.Avatar, avatar);
//		valuesToInsert.put(DataProviderConstants.WorkflowUri, uri);
//		valuesToInsert.put(DataProviderConstants.Run_Id, run_id);

		// insert via content provider
		// getContext().getContentResolver().insert(DataProviderConstants.TABLE_CONTENTURI, valuesToInsert);
		// return 0;
		
		// direct database access
		// return mDb.insert(DataProviderConstants.DATABASE_TABLE, null, valuesToInsert);
//	}

	/**
	 * Delete the Record with the given rowId
	 * 
	 * @param rowId
	 *            id of Record to delete
	 * @return true if deleted, false otherwise
	 *//*
	public boolean deleteRecord(long id) {
		return mDb.delete(DATABASE_TABLE, ID + "=" + id, null) > 0;
	}*/
	
	/**
	 * Delete everything from database table
	 * 
	 * @return 0
	 *//*
	public int deleteAllRecords() {
		return mDb.delete(DataProviderConstants.DATABASE_TABLE, null, null);
	}*/

	/**
	 * Return a Cursor over the list of all Records in the database
	 * 
	 * @return Cursor over all Records
	 */
	/*public Cursor fetchAllRecords() {

		String[] projection = new String[] { 
				DataProviderConstants.WorkflowTitle,
				DataProviderConstants.WorkflowUri, 
				DataProviderConstants.Version, 
				DataProviderConstants.WorkflowFileName, 
				DataProviderConstants.UploaderName, 
				DataProviderConstants.Avatar,
				DataProviderConstants.Run_Id };
		
		return getContext().getContentResolver().query(DataProviderConstants.TABLE_CONTENTURI, projection, null, null, null);
		
		return mDb.query(DataProviderConstants.DATABASE_TABLE, new String[] { 
				DataProviderConstants.WorkflowTitle,
				DataProviderConstants.WorkflowUri, 
				DataProviderConstants.Version, 
				DataProviderConstants.WorkflowFileName, 
				DataProviderConstants.UploaderName, 
				DataProviderConstants.Avatar,
				DataProviderConstants.Run_Id }, null, null, null, null, null);
	}

	*//**
	 * Return a Cursor positioned at the Record that matches the given rowId
	 * 
	 * @param id
	 *            id of Record to retrieve
	 * @return Cursor positioned to matching Record, if found
	 * @throws SQLException
	 *             if Record could not be found/retrieved
	 *//*
	public Cursor fetchOneRecord(String title, String version)
			throws SQLException {
		Cursor mCursor = mDb.query(true, DataProviderConstants.DATABASE_TABLE, new String[] {
				DataProviderConstants.WorkflowTitle, 
				DataProviderConstants.WorkflowUri, 
				DataProviderConstants.Version, 
				DataProviderConstants.WorkflowFileName,
				DataProviderConstants.UploaderName, 
				DataProviderConstants.Avatar }, 
				DataProviderConstants.WorkflowTitle + "= \"" + title
				+ "\" AND " + DataProviderConstants.Version + "= \"" + version + "\"", null, null,
				null, null, null);
		
		String[] projection = new String[] {
				DataProviderConstants.WorkflowTitle,
				DataProviderConstants.WorkflowUri,
				DataProviderConstants.Version, 
				DataProviderConstants.WorkflowFileName,
				DataProviderConstants.UploaderName, 
				DataProviderConstants.Avatar};
		
		String selection = DataProviderConstants.WorkflowTitle + "= ? AND " + DataProviderConstants.Version + "= ?" ;
		String[] selectionArgs = new String[] {title, version};
		
		return getContext().getContentResolver().query(DataProviderConstants.TABLE_CONTENTURI, projection, selection, selectionArgs, null);
	}

	public Cursor fetchRecordById(long id) {
		Cursor mCursor = mDb.query(true, DataProviderConstants.DATABASE_TABLE, new String[] {
				DataProviderConstants.WorkflowTitle, 
				DataProviderConstants.WorkflowUri, 
				DataProviderConstants.Version, 
				DataProviderConstants.WorkflowFileName,
				DataProviderConstants.UploaderName, 
				DataProviderConstants.Avatar }, 
				DataProviderConstants.ID + "=" + id, null, null, null, null,
				null);
		return mCursor;
	}

	*//**
	 * Update the Record using the details provided. The Record to be updated is
	 * specified using the rowId
	 * 
	 * @return true if the Record was successfully updated, false otherwise
	 *//*
	public boolean updateRecord(long id, String title, String version,
			String filename, String uploader, byte[] avatar, String run_ID) {

		ContentValues args = new ContentValues();
		args.put(DataProviderConstants.WorkflowTitle, title);
		args.put(DataProviderConstants.Version, version);
		args.put(DataProviderConstants.WorkflowFileName, filename);
		args.put(DataProviderConstants.UploaderName, uploader);
		args.put(DataProviderConstants.Avatar, avatar);
		args.put(DataProviderConstants.Run_Id, run_ID);

		return mDb.update(DataProviderConstants.DATABASE_TABLE, args, DataProviderConstants.ID + "=" + id, null) > 0;
	}

	public boolean updateRunID(String title, String version, String run_ID) {

		ContentValues args = new ContentValues();
		args.put(DataProviderConstants.Run_Id, run_ID);
		
		String where = "WorkflowTitle = ? AND Version = ?";
		String[] selectionArgs = new String[] {title, version};
		
		int numOfRow = getContext().getContentResolver().update(DataProviderConstants.TABLE_CONTENTURI, args, where, selectionArgs);
		return numOfRow > 0;

		//return mDb.update(DATABASE_TABLE, args, WorkflowTitle + "= \"" + title
				//+ "\" AND " + Version + "= \"" + version + "\"", null) > 0;
	}*/

	@Override
	public boolean onCreate() {
		myContext = getContext();
		//DATABASE_PATH = myContext.getFilesDir().getParentFile().getPath() + "/databases/";
		mDbHelper = new DatabaseHelper(myContext);
		//createAndOpenDatabase();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		int match = sURIMatcher.match(uri);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
        switch (match)
        {
            case WF_TABLE:
            	
                Cursor resultCursor = db.query(
                		DataProviderConstants.WF_TABLE_NAME,
        	            projection,
        	            selection, 
        	            selectionArgs, null, null, sortOrder);
                
                // Sets the ContentResolver to watch this content URI for data changes
                resultCursor.setNotificationUri(getContext().getContentResolver(), uri);
            	return resultCursor;
            case RUN_TABLE:
                Cursor resultCursor1 = db.query(
                		DataProviderConstants.WF_RUN_TABLE_NAME,
        	            projection,
        	            selection, 
        	            selectionArgs, null, null, sortOrder);
                
                // Sets the ContentResolver to watch this content URI for data changes
                resultCursor1.setNotificationUri(getContext().getContentResolver(), uri);
            	return resultCursor1;
            // case that is specifically for the specific join query
            // to retrieve workflow details corresponding to run ID
            // retrieved. 
            // TODO: Haven't found any other elegant way to execute complex query
            // via ContentProvider
            case JOIN_TABLE:
            	// the "selection" parameter should be the composed
            	// query part in this case
            	String theQuery = "SELECT r.Run_Id, l.Workflow_Title, l.Version, l.Uploader_Name "+
            					  "FROM "+DataProviderConstants.WF_TABLE_NAME+" l "+
            					  "INNER JOIN "+DataProviderConstants.WF_RUN_TABLE_NAME+" r "+
            					  "ON l.WF_ID = r.WF_ID "+
            					  "WHERE l.WF_ID IN ("+
            					  "SELECT r1.WF_ID FROM "+DataProviderConstants.WF_RUN_TABLE_NAME+" r1 "+
            					  "WHERE r1.Run_Id IN ("+selection+"))";
            	Cursor results = db.rawQuery(theQuery, null);
            	//results.setNotificationUri(getContext().getContentResolver(), uri);
            	return results;
        }

        return null;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		int match = sURIMatcher.match(uri);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
        switch (match)
        {
        	case WF_TABLE:
        		long id = db.insert(DataProviderConstants.WF_TABLE_NAME, null, values);
        		// If the insert succeeded, notify the change and 
                // return the new row's content URI.
                if (id != -1) {
                    //getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }
        	
        	case RUN_TABLE:
        		long id1 = -1;
        		try{
        			id1 = db.insert(DataProviderConstants.WF_RUN_TABLE_NAME, null, values);
        		}catch(Exception e){
        			e.printStackTrace();
        		}
        		
        		// If the insert succeeded, notify the change and 
                // return the new row's content URI.
                if (id1 != -1) {
                    //getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id1));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }
        }
        
        return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		int match = sURIMatcher.match(uri);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		switch (match)
        {
        	case WF_TABLE:
		        // Updates the table
		        int numOfRows = db.update(
		        		DataProviderConstants.WF_TABLE_NAME,
		                values,
		                selection,
		                selectionArgs);
		
		        // If the update succeeded, notify the change and 
		        // return the number of updated rows.
		        if (numOfRows != 0) {
		            // getContext().getContentResolver().notifyChange(uri, null);
		            return numOfRows;
		        } else {
		            throw new SQLiteException("Update error:" + uri);
		        }
            
		    case RUN_TABLE:
		    	// Updates the table
		        int numOfRows1 = db.update(
		        		DataProviderConstants.WF_RUN_TABLE_NAME,
		                values,
		                selection,
		                selectionArgs);
		
		        // If the update succeeded, notify the change and 
		        // return the number of updated rows.
		        if (numOfRows1 != 0) {
		            // getContext().getContentResolver().notifyChange(uri, null);
		            return numOfRows1;
		        } else {
		            throw new SQLiteException("Update error:" + uri);
		        }
        }
		
		return 0;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int match = sURIMatcher.match(uri);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		switch (match)
        {
        	case WF_TABLE:
		        // Updates the table
		        int numOfRows = db.delete(
		        		DataProviderConstants.WF_TABLE_NAME,
		                selection,
		                selectionArgs);
		
		        // If the delete succeeded, notify the change and 
		        // return the number of updated rows.
		        if (numOfRows != 0) {
		            // getContext().getContentResolver().notifyChange(uri, null);
		            return numOfRows;
		        } else {
		            throw new SQLiteException("Delete error:" + uri);
		        }
            
		    case RUN_TABLE:
		    	// Updates the table
		        int numOfRows1 = db.delete(
		        		DataProviderConstants.WF_RUN_TABLE_NAME,
		                selection,
		                selectionArgs);
		
		        // If the delete succeeded, notify the change and 
		        // return the number of updated rows.
		        if (numOfRows1 != 0) {
		            // getContext().getContentResolver().notifyChange(uri, null);
		            return numOfRows1;
		        } else {
		            throw new SQLiteException("Delete error:" + uri);
		        }
        }
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}
}
