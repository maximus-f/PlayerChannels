package me.perotin.playerchannels.storage.mysql;

import me.perotin.playerchannels.objects.Chatroom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/* Created by Perotin on 8/14/19 */
public class SQLHandler  {


    private String host, database, username, password;
    private int port;

    public SQLHandler(String host, String database, String username, String password, int port) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.port = port;
    }



    public String getHost() {
        return host;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }


    public void storeChatroom(Chatroom chatroom) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(getDatabaseUrl(), username, password);

            String query = "REPLACE INTO chatrooms (ownerUuid, name, description, isPublic, isSaved, nicknamesEnabled, isServerOwned, isGlobal, hidden) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            statement = connection.prepareStatement(query);

            statement.setString(1, chatroom.getOwner().toString());
            statement.setString(2, chatroom.getName());
            statement.setString(3, chatroom.getDescription());
            statement.setBoolean(4, chatroom.isPublic());
            statement.setBoolean(5, chatroom.isSaved());
            statement.setBoolean(6, chatroom.isNicknamesEnabled());
            statement.setBoolean(7, chatroom.isServerOwned());
            statement.setBoolean(8, chatroom.isGlobal());
            statement.setBoolean(9, chatroom.isHidden());

            statement.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try { statement.close(); } catch (SQLException e) { /* Ignored */ }
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException e) { /* Ignored */ }
            }
        }
    }

    private String getDatabaseUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
    }


}
