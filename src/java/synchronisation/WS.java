/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisation;

import com.google.gson.Gson;
import java.util.HashMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author P12A-92-Emmanuel
 */
@Path("Synchroniseur")
public class WS {
    private final static Gson GSON = new Gson();
//    private final static Type type = new GenericType<ArrayList<ArrayList<String>>>(){}.getType();
    
    @GET/*@POST*/
    @Path("synchroniser")
//    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String synchroniser(/*String json*/) throws Exception{//mety ho null
//        GenericType<ArrayList<ArrayList<String>>> genericType = new GenericType<ArrayList<ArrayList<String>>>(){};
//        ArrayList<List<String>> liAllInitialSynchroName = GSON.fromJson(json, type);
        HashMap<String, Object> hmapResponse = Function.synchroniser(null);
        return GSON.toJson(hmapResponse);
    }
}
