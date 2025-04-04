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

            // Table 1: for saving custom roles
            String sql = "CREATE TABLE IF NOT EXISTS server_roles (" +
                        "guild_id TEXT PRIMARY KEY," +
                        "role_id TEXT NOT NULL," +
                        "role_name TEXT NOT NULL)";

            // Table 2: for filter enable state
            String createFilterTable = "CREATE TABLE IF NOT EXISTS profanity_filter (" +
                        "guild_id TEXT PRIMARY KEY," +
                        "enabled INTEGER NOT NULL)";

            //Table 3: for banned words per guild
            String createBannedWordsTable = "CREATE TABLE IF NOT EXISTS banned_words (" +
                        "guild_id TEXT NOT NULL," +
                        "word TEXT NOT NULL," +
                        "PRIMARY KEY(guild_id, word))";

            // Table 4: for user warnings per day
            String createWarningTable = "CREATE TABLE IF NOT EXISTS user_warnings (" +
                        "guild_id TEXT NOT NULL," +
                        "user_id TEXT NOT NULL," +
                        "warning_date TEXT NOT NULL," + // YYYY-MM-DD
                        "warnings INTEGER NOT NULL," +
                        "PRIMARY KEY(guild_id, user_id, warning_date))";

            statement.executeUpdate(sql);
            statement.executeUpdate(createFilterTable);
            statement.executeUpdate(createBannedWordsTable);
            statement.executeUpdate(createWarningTable);
            System.out.println("Database and table created successfully!");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
