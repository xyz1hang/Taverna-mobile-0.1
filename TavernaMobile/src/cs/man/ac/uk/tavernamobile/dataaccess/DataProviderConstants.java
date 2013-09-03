package cs.man.ac.uk.tavernamobile.dataaccess;

import android.net.Uri;

public class DataProviderConstants {
	
	// database table fields
	public static final String WF_ID = "WF_ID";
	public static final String WF_RUN_ID = "WF_RUN_ID";
	public static final String WorkflowTitle = "Workflow_Title";
	public static final String WorkflowUri = "Workflow_Uri";
	public static final String Version = "Version";
	public static final String WorkflowFileName = "Workflow_FileName";
	public static final String Avatar = "Avatar";
	public static final String UploaderName = "Uploader_Name";
	public static final String LastLaunch = "Last_Launch";
	public static final String FirstLaunch = "First_Launch";
	public static final String Run_Id = "Run_ID";

	// database related constants
	public static final String DATABASE_FILE_NAME = "LaunchHistory.db";
	
	// name of the workflow details table
	public static final String WF_TABLE_NAME = "LaunchHistory";
	
	// name of the table that stores runs and workflow reference
	public static final String WF_RUN_TABLE_NAME = "WorkflowRuns";
	
	// String that used to indicate the join query
	// that used to retrieve workflow detail of Runs retrieved
	public static final String WF_RUN_JOIN_TABLE = "LHWFRJoin";
	
	public static final int DATABASE_VERSION = 1;

	// The URI scheme used for content URIs
	public static final String SCHEME = "content";

	// The provider's authority
	public static final String AUTHORITY = "cs.man.ac.uk.tavernamobile";

	// The DataProvider content URI
	public static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

	// table content URI
	public static final Uri WF_TABLE_CONTENTURI = Uri.withAppendedPath(CONTENT_URI, WF_TABLE_NAME);
	
	// run table content URI
	public static final Uri RUN_TABLE_CONTENTURI = Uri.withAppendedPath(CONTENT_URI, WF_RUN_TABLE_NAME);
	
	// join table content URI
	public static final Uri WF_RUN_JOIN_TABLE_CONTENTURI = Uri.withAppendedPath(CONTENT_URI, WF_RUN_JOIN_TABLE);
}
