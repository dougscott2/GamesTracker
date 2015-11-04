package com.company;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by DrScott on 11/3/15.
 */
public class MainTest {

    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./test");
        Main.createTables(conn);
        return conn;
    }

    public void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE users");
        stmt.execute("DROP TABLE games");
        conn.close();
    }
    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        User user = Main.selectUser(conn, "Alice");
        endConnection(conn);
        assertTrue(user != null);
    }
    @Test
    public void testMessage() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertGame(conn,1, "Halo", "PS4");
        Game game = Main.selectGame(conn, 1);
        endConnection(conn);
        assertTrue(game != null);
    }
    @Test
    public void testGames() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertUser(conn, "Bob", "");
        Main.insertGame(conn, 1,"Halo", "PC");
        Main.insertGame(conn, 2, "Drake's forture", "PS4");
        ArrayList<Game> games = Main.selectGames(conn);
        endConnection(conn);
        assertTrue(games!= null);
    }
    @Test
    public void testEditGame() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertGame(conn, 1, "Halo", "Xbox One");
        Main.editGame(conn, "Drake's Fortune", "PS4", 1);
        ArrayList<Game> games = Main.selectGames(conn);
        endConnection(conn);
        assertTrue(games!=null);
    }
    @Test
    public void testDeleteGame() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertGame(conn, 1, "Halo", "Xbox One");
        Main.deleteGame(conn, 1);
        ArrayList<Game> games = Main.selectGames(conn);
        endConnection(conn);
        assertTrue(games!=null);
    }

}