package me.perotin.playerchannels.storage.mysql;

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
}
