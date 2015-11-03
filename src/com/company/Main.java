package com.company;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void createTables(Connection conn) throws SQLException {  //method to create tables
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS games (id IDENTITY, user_id INT, title VARCHAR, system VARCHAR)");

    }
    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }

    public static User selectUser (Connection conn, String name) throws SQLException {
        User user = null;
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()){
            user = new User();
            user.id = results.getInt("id");
            user.password = results.getString("password");
        }
        return user;
    }

    static void insertGame(Connection conn, int id, String title, String system) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO games VALUES (NULL, ?, ?, ?)");//null for auto increment for id
        stmt.setInt(1, id);
        stmt.setString(2, title);
        stmt.setString(3, system);
        stmt.execute();
    }

    public static Game selectGame (Connection conn, int id) throws SQLException{
        Game game = null;
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games INNER JOIN users ON  games.user_id = " +
                "users.id WHERE games.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()){
            game = new Game();
            game.id = results.getInt("games.id");
            game.title = results.getString("games.title");
            game.username = results.getString("users.name");
            game.system = results.getString("games.system");
        }
        return game;
    }

    public static ArrayList<Game> selectGames (Connection conn, int id ) throws SQLException{
        ArrayList<Game> games = new ArrayList();
       // Game game = null;
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games INNER JOIN users ON games.user_id" +
                " = users.id  WHERE games.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        while (results.next()){
           Game game = new Game();
            game.id = results.getInt("games.id");
            game.title = results.getString("games.title");
            game.username = results.getString("users.name");
            game.system = results.getString("games.system");
            games.add(game);
        }
        return games;

    }




    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);



    //    ArrayList<Game> games = new ArrayList();
      //  HashMap<String, User> users = new HashMap();
        // final User DOUG = new User(); //test for passwords
        // DOUG.password="1234";
        //users.put("DOUG", DOUG);

        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    HashMap m = new HashMap();
                    ArrayList<Game> games = selectGames(conn, 1);
                    m.put("username", username);
                    m.put("games", games);
                    if (username == null) {
                        return new ModelAndView(m, "not-logged-in.html");
                    }
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    String password = request.queryParams("password");
                    if (username.isEmpty() || password.isEmpty()){
                        Spark.halt(403);
                    }
                    User user = selectUser(conn, username);
                    if (user == null){
                        insertUser(conn, username, password);
                       // user = new User();
                        //user.password = password;
                        //users.put(username, user);
                    } else if (!password.equals(user.password)){
                        Spark.halt(403);
                    }
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/add-game",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                   String title = request.queryParams("newGame");
                   String system = request.queryParams("system");

                    try{
                        User me = selectUser(conn, username);
                        insertGame(conn, me.id, title, system);

                    }catch (Exception e){

                    }
                    response.redirect("/");
                    return "";
                })
        );
     /*   Spark.get(
                "/delete-game",
                ((request, response) -> {
                    String id = request.queryParams("id");
                    try {
                        int idNum = Integer.valueOf(id);
                        games.remove(idNum - 1);
                        for (int i = 0; i < games.size(); i++) {
                            games.get(i).id = i + 1;
                        }
                    } catch (Exception e){}
                    response.redirect("/");
                    return "";
                })
        );

        Spark.get(
                "/edit-game",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    String id = request.queryParams("id");
                    m.put("id", id);
                    return new ModelAndView(m, "/edit-game.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/edit-game",
                ((request, response) -> {
                    String id = request.queryParams("id");
                    try{
                        int idNum = Integer.valueOf(id);
                        //Game game = games.get(idNum - 1);
                        //game.title = request.queryParams("newGame");
                        games.get(idNum-1).title = request.queryParams("editGame");
                        games.get(idNum-1).system = request.queryParams("editSystem");
                    }
                    catch (Exception e){

                    }
                    response.redirect("/");
                    return "";
                })
        ); */
        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );

    }
}

