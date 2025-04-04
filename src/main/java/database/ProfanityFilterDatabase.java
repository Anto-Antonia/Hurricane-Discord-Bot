package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfanityFilterDatabase {
    private static final String DB_URL = "jdbc:sqlite:roles.db";

    public static void toggleFilter(String guildId, boolean enabled){
        try(Connection connection = DriverManager.getConnection(DB_URL);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO profanity_filter (guild_id, enabled) VALUES (?, ?)" +
                          "ON CONFLICT(guild_id) DO UPDATE SET enabled = ?"
            )){
            preparedStatement.setString(1, guildId);
            preparedStatement.setString(2, String.valueOf(enabled ? 1 : 0));
            preparedStatement.setString(3, String.valueOf(enabled ? 1 : 0));

            preparedStatement.executeUpdate();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isFilterEnabled(String guildId){
        try(Connection connection = DriverManager.getConnection(DB_URL);
            PreparedStatement statement = connection.prepareStatement(
                "SELECT enabled FROM profanity_filter WHERE guild_id = ?")){

            statement.setString(1, guildId);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                int enabled = resultSet.getInt("enabled");
                return enabled == 1;
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void addBannedWord(String guildId, String word){
        try(Connection connection = DriverManager.getConnection(DB_URL);
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO banned_words (guild_id, word) VALUES (?, ?)")){

            statement.setString(1, guildId);
            statement.setString(2, word.toLowerCase());

            statement.executeUpdate();
            System.out.println("Banned word added to the list! ");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void removeBannedWord(String guildId, String word){
        try(Connection connection = DriverManager.getConnection(DB_URL);
            PreparedStatement statement = connection.prepareStatement(
             "DELETE FROM banned_words WHERE guild_id = ? AND word = ?")){

            statement.setString(1, guildId);
            statement.setString(2, word.toLowerCase());

            statement.executeUpdate();
            System.out.println("Removed the banned word successfully.");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static List<String> getBannedWords(String guildId){
        List<String> words = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(DB_URL);
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT word FROM banned_words WHERE guild_id = ?")){

            statement.setString(1, guildId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                words.add(resultSet.getString("word"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return words;
    }
}
