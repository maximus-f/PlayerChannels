package me.perotin.playerchannels.storage.mysql;

import me.perotin.playerchannels.objects.Chatroom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/* Created by Perotin on 8/14/19 */
public class SQLHandler  {

    private Connection connection;
    private String host, database, username, password;
    private int port;

    public SQLHandler(String host, String database, String username, String password, int port) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.port = port;

        String url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
        try {
            this.connection = DriverManager.getConnection(url, username, password);
            if (this.connection != null && !this.connection.isClosed()) {
                System.out.println("Database connection successful.");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
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

    public Connection getConnection() {
        return connection;
    }


    public void storeChatroom(Chatroom chatroom) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(getDatabaseUrl(), username, password);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS chatrooms (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "owner VARCHAR(36) NOT NULL, " +
                    "name VARCHAR(255) UNIQUE NOT NULL, " +
                    "description TEXT, " +
                    "isPublic BOOLEAN NOT NULL, " +
                    "nicknamesEnabled BOOLEAN NOT NULL, " +
                    "isServerOwned BOOLEAN NOT NULL, " +
                    "hidden BOOLEAN NOT NULL)";
            statement = connection.prepareStatement(createTableSQL);
            statement.executeUpdate();
            statement.close();

            String query = "REPLACE INTO chatrooms (owner, name, description, isPublic, nicknamesEnabled, isServerOwned, hidden) VALUES (?, ?, ?, ?, ?, ?, ?)";

            statement = connection.prepareStatement(query);

            statement.setString(1, chatroom.getOwner().toString());
            statement.setString(2, chatroom.getName());
            statement.setString(3, chatroom.getDescription());
            statement.setBoolean(4, chatroom.isPublic());
            statement.setBoolean(5, chatroom.isNicknamesEnabled());
            statement.setBoolean(6, chatroom.isServerOwned());
            statement.setBoolean(7, chatroom.isHidden());

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

    public void closeConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
        }
    }


}
