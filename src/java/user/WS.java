package user;

import com.google.gson.Gson;
import java.util.HashMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import usefull.dao.type.DBEnumType;
import user.gestion_login.GestionLoginFunction;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author P12A-92-Emmanuel
 */
@Path("User")
public class WS {
    private final static Gson GSON = new Gson();
    
    @GET
    @Path("genres")
    @Produces(MediaType.APPLICATION_JSON)
    public static String getAllGenres(){
        return GSON.toJson(DBEnumType.STRINGIFIED_GENRE_ID_NOM);
    }
    
    @POST
    @Path("check-field")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String checkField(JsonObject json){
        HashMap<String, Object> hmapResponse = Function.checkField(json.getString("name",null), json.getString("value",null));
        return GSON.toJson(hmapResponse);
    }
    
    @POST
    @Path("sing-up")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String singUp(JsonObject json){
        HashMap<String, Object> hmapResponse = Function.singUp(json.getString("genre",null), json.getString("nom",null), json.getString("email",null), json.getString("tel1",null), json.getString("tel2",null), json.getString("tel3",null), json.getString("desc",null), json.getString("pwd",null), json.getString("pwd_repeat",null));
        return GSON.toJson(hmapResponse);
    }
    
    
    @POST
    @Path("login/{sationCibleType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String login(@PathParam("sationCibleType") String stationCibleType, JsonObject json){
        HashMap<String, Object> hmapResponse = Function.logIn(stationCibleType, json.getString("email",null), json.getString("pwd", null));
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("gestion_login/{onglet}/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String pagine(@PathParam("onglet") String onglet, @PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = GestionLoginFunction.pagine(onglet, pagntStartNum, pagntFootMaxSize, pagntMaxSize);
        return GSON.toJson(hmapResponse);
    }
    
    @POST
    @Path("gestion_login/{onglet}/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String pagine(@PathParam("onglet") String onglet, @PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, JsonObject json) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = GestionLoginFunction.pagine(onglet, pagntStartNum, pagntFootMaxSize, pagntMaxSize);
        return GSON.toJson(hmapResponse);
    }
    
    @POST
    @Path("gestion_login/{action_uri}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String pagine(@PathParam("action_uri") String actionUri, JsonObject json) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = GestionLoginFunction.executeWithConnection(actionUri, GSON.fromJson(json.toString(), new HashMap<String, Object>(0).getClass()));
        return GSON.toJson(hmapResponse);
    }
}
