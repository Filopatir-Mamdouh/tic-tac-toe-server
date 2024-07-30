package com.example.tic_tac_toeserver.models;

import com.example.tic_tac_toeserver.constants.RequestType;
import com.example.tic_tac_toeserver.database.apiFunctions;
import com.example.tic_tac_toeserver.logic.GameHandler;
import com.example.tic_tac_toeserver.logic.PlayerHandler;
import com.example.tic_tac_toeserver.logic.Server;
import com.example.tic_tac_toeserver.logic.UserHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;




public class Response {
    public static String getResponse(String response, UserHandler userHandler){
        JSONObject object = new JSONObject(response);
        RequestType type = RequestType.valueOf(object.getString("RequestType"));
        return switch (type) {
            case Register -> Register(response)+"";
            case Login -> Login(response, userHandler);
            case RequestGame -> RequestGame(response)+"";
            case RequestGameResponse -> RequestGameResponse(object);
//            case PlayAgain -> "";
//            case Surrender -> "";
            case PlayerList -> getPlayList();
            case Scoreboard -> getScoreBoard();
//            case GameHistory -> "";
            default -> "";
        };
    }

    private static String RequestGameResponse(JSONObject object) {
        if (object.getBoolean("Value")){
            UserHandler user = Server.clients.get(object.getInt("userid"));
            UserHandler opponent = Server.clients.get(object.getInt("opponentid"));
            user.send("{\"RequestType\":\"RequestGameResponse\", \"Value\":"+true+", \"opponent\":"+opponent.getPlayer().toString()+"}");
            new GameHandler(user,opponent);
            System.out.println("Sent to opponent");
            return "{\"RequestType\":\"RequestGameResponse\", \"Value\":"+true+", \"opponent\":"+user.getPlayer().toString()+"}";
        }
        return "{\"RequestType\":\"RequestGameResponse\", \"Value\":"+false+"}";
    }

    private static String Login(String json, UserHandler userHandler){
        JSONObject object = new JSONObject(json);
        User user = User.toUser(object.getJSONObject("User"));
        apiFunctions api = new apiFunctions();
        ResultSet rs = api.read("SELECT * FROM user WHERE username = \""+user.getUsername()+"\" AND password = \""+user.getPassword()+"\"");
        try {
            if (rs.next()) {
                ResultSet fs = api.read("SELECT * FROM player WHERE userid= "+rs.getInt("userid"));
                if (fs.next()) {
                    userHandler.setPlayer(new Player(fs));
                    Server.clients.put(fs.getInt("userid"),userHandler);
                }
                    return "{\"RequestType\":\"Login\",\"Player\":"+ new Player(fs).toString() +"}";
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "{\"RequestType\":\"Login\",\"invalid\":\"Invalid username or password\"}";
    }

    private static String Register(String response) {
        JSONObject jsonObject = new JSONObject(response);
        String reply= "";
        apiFunctions api = new apiFunctions();
        JSONObject object = jsonObject.getJSONObject("User");
        String username = object.getString("username");
        String email = object.getString("email");
        String password = object.getString("password");

        int rsInsertPlayer = api.create("INSERT INTO player (username, isloggedin,isingame, score, wins, draws, losses) VALUES ('"+username+"', 0,0,0,0,0,0)");
        int rsInsertUser = api.create("INSERT INTO user (username,email, password) VALUES ('"+username+"', '"+email+"', '"+password+"')");
        return "{\"RequestType\":\"Register\",\"value\":"+rsInsertPlayer+rsInsertUser+"}";
    }


    private static String getPlayList(){
        String reply= "";
        apiFunctions api = new apiFunctions();
        ResultSet rs = api.read("SELECT * FROM player WHERE isloggedin = 1");
        try {
            reply = "{\"RequestType\":\"PlayerList\",\"PlayList\":[";
            while (rs.next()) {
                reply += new Player(rs).toString() +",";
            }
            reply += "]}";
        } catch (SQLException ignored){

        }
        System.out.println(reply);
        return reply;
    }
    private static String getScoreBoard(){
        String reply= "";
        apiFunctions api = new apiFunctions();
        ResultSet rs = api.read("SELECT * FROM player ORDER BY score DESC LIMIT 20 ");

        try {
            reply = "{\"RequestType\":\"Scoreboard\",\"Scoreboard\":[";
            while (rs.next()) {
                reply += "{\"username\":\""+rs.getString("username")+"\" , \"score\":"+rs.getInt("score")+"},";
            }
            reply += "]}";
        } catch (SQLException ignored){

        }
        System.out.println(reply);
        return reply;
    }
    private static String RequestGame(String response){
        JSONObject object = new JSONObject(response);
        int userid = object.getInt("userid");
        int opponentid = object.getInt("opponentid");
        UserHandler user = Server.clients.get(userid);
        UserHandler opponent = Server.clients.get(opponentid);
        opponent.send("{\"RequestType\":\"RequestGame\",\"Player\":"+user.getPlayer().toString()+"}");
        return "{\"RequestType\":\"Ignore\"}";
    }
}
