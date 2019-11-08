package no.personligfrelser.icmapi;

import java.sql.*;

public class Database {
	private static final String driver = "com.mysql.jdbc.Driver";
	private static final String url = "jdbc:mysql://localhost/icm_api";
	private static final String username = "root";
	private static final String password = "";

	private final Connection db;

	public Database() throws Exception {
		// Register JDBC driver and create a connection to db
		Class.forName("com.mysql.cj.jdbc.Driver");
		db = DriverManager.getConnection(url, username, password);
	}

	public ResultSet query(String sql) {
		ResultSet rs = null;

		try {
			if (db == null || db.isClosed()) throw new Exception("Database is not connected");
			Statement stmt = db.createStatement();

			rs = stmt.executeQuery(sql);

			/* Processing data
			while (rs.next()) {
				System.out.println(rs.getString("name") + " : " + rs.getString("location"));
			} */

			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;
	}

	public Statement getStatement() {
		try {
			return db.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Connection getDb() {
		return db;
	}
}
