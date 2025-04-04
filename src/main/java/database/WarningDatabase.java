package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class WarningDatabase {
    private static final String DB_URL = "jdbc:sqlite:roles.db";

    public static int addWarning(String guildId, String userId){
        String today = LocalDateTime.now().toString(); // YYYY-MM-DD OR DD-MM-YYYY
        try(Connection connection = DriverManager.getConnection(DB_URL)){

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT warnings FROM user_warnings WHERE guild_id =? AND user_id = ? AND warning_date = ?");

            statement.setString(1, guildId);
            statement.setString(2, userId);
            statement.setString(3, today);
            ResultSet resultSet = statement.executeQuery();

            int warnings = 1;
            if(resultSet.next()){
                warnings = resultSet.getInt("warnings") + 1;

                PreparedStatement update = connection.prepareStatement(
                        "UPDATE user_warnings SET warnings = ? WHERE guild_id = ? AND warning_date = ?");

                update.setInt(1, warnings);
                update.setString(2, guildId);
                update.setString(3, userId);
                update.setString(4, today);

                update.executeUpdate();
            } else {
                PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO user_warning (guild_id, user_id, warning_date, warnings) VALUES (?, ?, ?, ?)");

                insert.setString(1, guildId);
                insert.setString(2, userId);
                insert.setString(3, today);
                insert.setInt(4, warnings);

                insert.executeUpdate();
            }
            return warnings;
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
}
