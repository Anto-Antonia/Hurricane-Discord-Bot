package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:roles.db"; // SQLite file

        try(Connection connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement()){

            String sql = "CREATE TABLE IF NOT EXISTS server_roles (" +
                        "guild_id TEXT PRIMARY KEY, " +
                        "role_id TEXT NOT NULL," +
                        "role_name TEXT NOT NULL)";

            statement.executeUpdate(sql);
            System.out.println("Database and table created successfully!");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
