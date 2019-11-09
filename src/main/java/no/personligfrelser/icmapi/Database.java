package no.personligfrelser.icmapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;

@Component
public class Database {
	private static final String driver = "com.mysql.jdbc.Driver";

	@Value(value = "${db.url}")
	private String url;
	@Value("${db.username}")
	private String username;
	@Value("${db.password}")
	private String password;

	private Connection db;

	@PostConstruct
	private void init() {
		try {
			// Register JDBC driver and create a connection to db
			Class.forName("com.mysql.cj.jdbc.Driver");
			db = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			System.out.println("Couldn't establish connection to database.");
		}
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
