package me.perotin.playerchannels.storage.mysql;

import me.perotin.playerchannels.objects.ChatRole;
import me.perotin.playerchannels.objects.Chatroom;
import me.perotin.playerchannels.objects.GlobalChatroom;
import me.perotin.playerchannels.objects.PlayerChannelUser;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;

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
//                System.out.println("Database connection successful.");
            }
        } catch (SQLException e) {
//            System.err.println("Database connection failed: " + e.getMessage());
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
        Bukkit.getConsoleSender().sendMessage("[PlayerChannels] Stored chatroom: " + chatroom.getName() + " in database: " + database);
        storeMembers(chatroom);
    }

    private String getDatabaseUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
    }

    public void closeConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
        }
    }

    public Map<UUID, ChatRole> getChatroomMembers(String chatroomName) throws SQLException {
        Map<UUID, ChatRole> members = new HashMap<>();
        String query = "SELECT memberUUID, `rank` FROM members WHERE chatroomName = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, chatroomName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID memberUUID = UUID.fromString(rs.getString("memberUUID"));
                    int rankValue = rs.getInt("rank");
                    ChatRole role = ChatRole.getRole(rankValue);
                    members.put(memberUUID, role);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }


    public List<Chatroom> getAllChatrooms() throws SQLException {
        List<Chatroom> chatrooms = new ArrayList<>();
        String query = "SELECT * FROM chatrooms";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String owner = rs.getString("owner");
                String name = rs.getString("name");
                String description = rs.getString("description");
                boolean isPublic = rs.getBoolean("isPublic");
                boolean nicknamesEnabled = rs.getBoolean("nicknamesEnabled");
                boolean isServerOwned = rs.getBoolean("isServerOwned");
                boolean hidden = rs.getBoolean("hidden");

                GlobalChatroom chatroom = new GlobalChatroom(UUID.fromString(owner), name, description, isPublic, true, isServerOwned, nicknamesEnabled, hidden, false);
                chatrooms.add(chatroom);
            }
        } catch (SQLException e) {
            if (!e.getSQLState().equals("42S02")) {
                e.printStackTrace();
            }
        }
        return chatrooms;
    }

    public void deleteChannel(String chatroom)  {
        Bukkit.getConsoleSender().sendMessage("[PlayerChannels] Deleting chatroom from database: " + chatroom);
        String memberDeleteQuery = "DELETE FROM members WHERE chatroomName = ?"; // delete all instances chatroomName = this
        String query = "DELETE FROM chatrooms WHERE name = ?";
        try (PreparedStatement memberStmt = connection.prepareStatement(memberDeleteQuery);
             PreparedStatement chatroomStmt = connection.prepareStatement(query)) {

            // Set the chatroom name in the delete statement for members
            memberStmt.setString(1, chatroom);
             memberStmt.executeUpdate();

            // Set the chatroom name in the delete statement for chatrooms
            chatroomStmt.setString(1, chatroom);
            int chatroomsDeleted = chatroomStmt.executeUpdate();
            Bukkit.getConsoleSender().sendMessage("[PlayerChannels] Deleted chatroom: " + chatroom + " (" + chatroomsDeleted + " rows affected)");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void storeMembers(Chatroom chatroom) {
        Connection connection = null;
        PreparedStatement statement = null;
        Bukkit.getConsoleSender().sendMessage("[PlayerChannels] Storing members of: " + chatroom + " in database: " + database);
        try {
            connection = DriverManager.getConnection(getDatabaseUrl(), username, password);

            // Create the members table if it does not exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS members (" +
                    "chatroomName VARCHAR(255) NOT NULL, " +
                    "memberUUID VARCHAR(36) NOT NULL, " +
                    "`rank` INT NOT NULL, " +
                    "UNIQUE (chatroomName, memberUUID), " +
                    "FOREIGN KEY (chatroomName) REFERENCES chatrooms(name))";
            statement = connection.prepareStatement(createTableSQL);
            statement.executeUpdate();
            statement.close();

            // Insert into query
            String query = "REPLACE INTO members (chatroomName, memberUUID, `rank`) VALUES (?, ?, ?)";
            statement = connection.prepareStatement(query);

            for (UUID member : chatroom.getMemberMap().keySet()) {
                statement.setString(1, chatroom.getName());
                statement.setString(2, member.toString());
                statement.setInt(3, chatroom.getMemberMap().get(member).getValue());

                statement.addBatch();
            }

            statement.executeBatch();

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
        Bukkit.getConsoleSender().sendMessage("[PlayerChannels] Stored members for chatroom: " + chatroom.getName() + " in database: " + database);
    }

    public void updateChannelFields(Chatroom chatroom) {
        Connection connection = null;
        PreparedStatement statement;

        try {
            connection = DriverManager.getConnection(getDatabaseUrl(), username, password);

            String updateFields = "UPDATE `chatrooms`" +
                    "SET description = ?, owner = ?, isPublic = ?, hidden = ?, nicknamesEnabled = ?" +
                    " WHERE name = ?";

            statement = connection.prepareStatement(updateFields);
            statement.setString(1, chatroom.getDescription());
            statement.setString(2, chatroom.getOwner().toString());
            statement.setBoolean(3, chatroom.isPublic());
            statement.setBoolean(4, chatroom.isHidden());
            statement.setBoolean(5, chatroom.isNicknamesEnabled());
            statement.setString(6, chatroom.getName());
            Bukkit.getConsoleSender().sendMessage("[PC] Executing statement: " + statement );
            statement.executeUpdate();
            statement.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void updateMemberInDatabase(String chatroom, String memberUUID, int rank, OperationType operationType) {
        Connection connection = null;
        PreparedStatement statement = null;
        Bukkit.getConsoleSender().sendMessage("[PlayerChannels] " + operationType + " member: " + memberUUID + " in chatroom: " + chatroom + " in database: " + database);

        try {
            connection = DriverManager.getConnection(getDatabaseUrl(), username, password);

            // Create the members table if it does not exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS members (" +
                    "chatroomName VARCHAR(255) NOT NULL, " +
                    "memberUUID VARCHAR(36) NOT NULL, " +
                    "`rank` INT NOT NULL, " +
                    "UNIQUE (chatroomName, memberUUID), " +
                    "FOREIGN KEY (chatroomName) REFERENCES chatrooms(name))";
            statement = connection.prepareStatement(createTableSQL);
            statement.executeUpdate();
            statement.close();
            String query;

            switch (operationType) {
                case ADD:
                    // Add the member
                    query = "REPLACE INTO members (chatroomName, memberUUID, `rank`) VALUES (?, ?, ?)";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, chatroom);
                    statement.setString(2, memberUUID);
                    statement.setInt(3, rank);

                case REMOVE:
                    // Remove the member
                    query = "DELETE FROM members WHERE chatroomName = ? AND memberUUID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, chatroom);
                    statement.setString(2, memberUUID);
                case RANK_CHANGE:
                    query = "UPDATE members SET `rank` = (?) WHERE memberUUID = ? AND chatroomName = ? ";
                    statement = connection.prepareStatement(query);
                    statement.setInt(1, rank);
                    statement.setString(2, memberUUID);
                    statement.setString(3, chatroom);
            }

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

        Bukkit.getConsoleSender().sendMessage("[PlayerChannels] " + operationType + " member: " + memberUUID + " in chatroom: " + chatroom + " in database: " + database);
    }

    public enum OperationType {
        ADD,
        REMOVE,
        RANK_CHANGE
    }




}
