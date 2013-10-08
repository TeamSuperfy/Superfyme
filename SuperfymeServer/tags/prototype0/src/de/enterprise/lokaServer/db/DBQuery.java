package de.enterprise.lokaServer.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.enterprise.lokaServer.pojos.DoubleLocPojo;
import de.enterprise.lokaServer.pojos.LocationPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.pojos.PostPojo;
import de.enterprise.lokaServer.pojos.UserPojo;

public class DBQuery {

	private DBConnection dbConnection;

	public DBQuery() {
		dbConnection = new DBConnection();
	}

	public boolean query(String sql) {
		boolean result = false;
		try {
			result = dbConnection.getStatement().execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void close() {
		dbConnection.close();
	}

	public void insertPostPojo(PostPojo post) {
		LocationPojo loc = post.getLoc();
		MessagePojo message = post.getMessage();
		String sql = "INSERT INTO " + SQLInfo.TABLE_POSTS + " ("
				+ SQLInfo.FIELD_LAT + ", " + SQLInfo.FIELD_LON + ", "
				+ SQLInfo.FIELD_USER_ID + ", " + SQLInfo.FIELD_DATE + ", "
				+ SQLInfo.FIELD_PIC_URL + ", " + SQLInfo.FIELD_TEXT + ")"
				+ " VALUES ('" + loc.getLatitude() + "', '"
				+ loc.getLongitude() + "', '" + message.getUser().getId()
				+ "', '" + message.getDate() + "', '" + message.getPicURL()
				+ "', '" + message.getText() + "')";
		executeSQL(sql);
	}

	/**
	 * inserts new user into db and returns the id of him
	 * 
	 * @param userName
	 * @return new id of user
	 */
	public int insertNewUser() {
		executeSQL("INSERT INTO "+SQLInfo.TABLE_USER+" ("+SQLInfo.FIELD_ID+") VALUES (DEFAULT)");
		int autoIncKeyFromFunc = -1;
		ResultSet rs = executeSQLQuery("SELECT LAST_INSERT_ID()");

		try {
			if (rs.next()) {
				autoIncKeyFromFunc = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return autoIncKeyFromFunc;
	}

	public ArrayList<PostPojo> getPostPojos(DoubleLocPojo dblLoc) {
		int[] upLeftCoords = new int[] { dblLoc.getUpLeft().getLatitude(),
				dblLoc.getUpLeft().getLongitude() };
		int[] bottomRightCoords = new int[] {
				dblLoc.getBottomRight().getLatitude(),
				dblLoc.getBottomRight().getLongitude() };
		/**
		 * [0 1 2 3 ] [left, up, right, bottom]
		 */
		int[] boundingBox = new int[] { upLeftCoords[1], bottomRightCoords[0],
				bottomRightCoords[1], upLeftCoords[0] };

		String sql = "SELECT * FROM " + SQLInfo.TABLE_POSTS + " WHERE "
				+ SQLInfo.FIELD_LAT + " < " + boundingBox[3] + " AND "
				+ SQLInfo.FIELD_LAT + " > " + boundingBox[1] + " AND "
				+ SQLInfo.FIELD_LON + " < " + boundingBox[2] + " AND "
				+ SQLInfo.FIELD_LON + " > " + boundingBox[0];

		ResultSet result = executeSQLQuery(sql);

		if (result != null) {
			ArrayList<PostPojo> posts = new ArrayList<PostPojo>();
			try {
				while (result.next()) {
					PostPojo post = new PostPojo();
					post.setId(result.getInt(SQLInfo.FIELD_ID));
					post.setLoc(new LocationPojo(result
							.getInt(SQLInfo.FIELD_LAT), result
							.getInt(SQLInfo.FIELD_LON)));
					MessagePojo msg = new MessagePojo();
					msg.setDate(result.getLong(SQLInfo.FIELD_DATE));
					msg.setPicURL(result.getString((SQLInfo.FIELD_PIC_URL)));
					msg.setText(result.getString(SQLInfo.FIELD_TEXT));
					msg.setUser(new UserPojo(result.getInt(SQLInfo.FIELD_USER_ID)));
					post.setMessage(msg);

					posts.add(post);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return posts;
		}

		return null;

	}

	public UserPojo getUser(int id) {

		String sql = "SELECT * FROM " + SQLInfo.TABLE_USER + " WHERE "
				+ SQLInfo.FIELD_ID + " = '" + id + "'";

		ResultSet result = executeSQLQuery(sql);

		if (result != null) {
			UserPojo user = new UserPojo();
			try {
				if (result.next()) {
					user.setId(result.getInt(SQLInfo.FIELD_ID));
					return user;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	private void executeSQL(String sql) {
		try {
			dbConnection.getStatement().execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private ResultSet executeSQLQuery(String sql) {
		ResultSet result = null;
		try {
			result = dbConnection.getStatement().executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public PostPojo getSinglePost(int id) {

		String sql = "SELECT * FROM " + SQLInfo.TABLE_POSTS + " WHERE "
				+ SQLInfo.FIELD_ID + " = '" + id + "'";

		ResultSet result = executeSQLQuery(sql);

		if (result != null) {
			PostPojo post = new PostPojo();
			try {
				if (result.next()) {
					post = new PostPojo();
					post.setId(result.getInt(SQLInfo.FIELD_ID));
					post.setLoc(new LocationPojo(result
							.getInt(SQLInfo.FIELD_LAT), result
							.getInt(SQLInfo.FIELD_LON)));
					MessagePojo msg = new MessagePojo();
					msg.setDate(result.getLong(SQLInfo.FIELD_DATE));
					msg.setPicURL(result.getString((SQLInfo.FIELD_PIC_URL)));
					msg.setText(result.getString(SQLInfo.FIELD_TEXT));
					msg.setUser(getUser(result.getInt(SQLInfo.FIELD_USER_ID)));
					post.setMessage(msg);
					return post;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

}
