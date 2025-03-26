package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RoleDatabase {
   private static final String DB_URL = "jdbc:sqlite:roles.db";

    public static void SaveRoleForGuild(String guildId, String roleId, String roleName){
        try(Connection connection = DriverManager.getConnection(DB_URL);
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO server_roles (guild_id, role_id, role_name) VALUES (?, ?, ?)" +
                         "ON CONFLICT(guild_id) DO UPDATE SET role_id = ?, role_name =?"
            )){
            statement.setString(1, guildId);
            statement.setString(2, roleId); // Save roleId
            statement.setString(3, roleName); // Save roleName
            statement.setString(4, roleId); // Update roleId
            statement.setString(5, roleName);

            statement.executeUpdate();
            System.out.println("Role saved successfully for guild: " + guildId);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getRoleForGuild(String guildId){
        try(Connection connection = DriverManager.getConnection(DB_URL);
        PreparedStatement statement = connection.prepareStatement(
                "SELECT role_name FROM server_roles WHERE guild_id = ?")){
            statement.setString(1, guildId);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                return resultSet.getString("role_name");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
