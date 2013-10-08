package de.enterprise.lokaAndroid.database;

public class UserTable implements UserColumns{

	public static final String TABLE_NAME = "user";
	
	public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS user ("+
	"_id INTEGER PRIMARY KEY);";
	
	public static final String SQL_DROP = "DROP TABLE IF EXISTS "+TABLE_NAME;
	
	public static final String STMT_MIN_INSERT = "INSERT INTO user (_id) VALUES (?)";
	
	public static final String STMT_GET_ALL = "SELECT * FROM user";
	
	public static final String[] ALL_COLUMNS = new String[]{ID};
	
}
