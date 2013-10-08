package de.enterprise.lokaAndroid.database;

public class ImageTable implements ImageColumns{

	public static final String TABLE_NAME = "imageTable";
	
	public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS imageTable ("+
	"_id INTEGER PRIMARY KEY, url TEXT);";
	
	public static final String SQL_DROP = "DROP TABLE IF EXISTS "+TABLE_NAME;
	
	public static final String STMT_MIN_INSERT = "INSERT INTO imageTable (_id, url) VALUES (?, ?)";
	
	public static final String STMT_GET_ALL = "SELECT * FROM imageTable";
	
	public static final String STMT_GET_URL = "SELECT url FROM imageTable WHERE _id = ?";
	
	public static final String[] ALL_COLUMNS = new String[]{ID, URL};
	
}
