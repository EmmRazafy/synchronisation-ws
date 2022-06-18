/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisation.u_i_conflict.solve;

import com.google.gson.Gson;
import java.sql.SQLException;
import java.util.HashMap;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author P12A-92-Emmanuel
 */
@Path("ConflitIndexeUnique")
public class WS {
    private final static Gson GSON = new Gson();
    
    @GET
    @Path("solve/{synchroName}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String solve(@PathParam("synchroName") String synchroName) throws SQLException, Exception{
        HashMap<String, Object> hmapResponse = Function.solve(synchroName);
        return GSON.toJson(hmapResponse);
    }
    
    @POST
    @Path("solve/{synchroName}/{conflict_count}/{periSynchroId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String solve(
            @PathParam("synchroName") String synchroName, @PathParam("conflict_count") String conflictCount, @PathParam("periSynchroId") String periSynchroId, 
            JsonObject jsonObject 
    ) throws Exception{
        boolean jsonObjectIsNull = jsonObject == null;
        String userId = jsonObjectIsNull? null :  jsonObject.getString("user_id",null);
        String option = jsonObjectIsNull? null :  jsonObject.getString("option",null);
        String delete_option_error =  jsonObjectIsNull? null : jsonObject.getString("delete_option_error",null);
        String[] centOrderedStringifiedIndexes = null;
        String[] periOrderedStringifiedIndexes = null;
        String[] centIsUpdatedOrderedIndexs = null;
        String[] periIsUpdatedOrderedIndexs = null;
        if(!jsonObjectIsNull) {
            centOrderedStringifiedIndexes = jsonObject.containsKey("cent_ordered_indexes") ? GSON.fromJson(jsonObject.getJsonArray("cent_ordered_indexes").toString(), String[].class) : null;
            periOrderedStringifiedIndexes = jsonObject.containsKey("peri_ordered_indexes") ? GSON.fromJson(jsonObject.getJsonArray("peri_ordered_indexes").toString(), String[].class) : null;
            
            centIsUpdatedOrderedIndexs = jsonObject.containsKey("cent_is_updated_ordered_indexes") ? GSON.fromJson(jsonObject.getJsonArray("cent_is_updated_ordered_indexes").toString(), String[].class) : null;
            periIsUpdatedOrderedIndexs = jsonObject.containsKey("peri_is_updated_ordered_indexes") ? GSON.fromJson(jsonObject.getJsonArray("peri_is_updated_ordered_indexes").toString(), String[].class) : null;
        }
        HashMap<String, Object> hmapResponse = Function.solve(synchroName, conflictCount, periSynchroId, userId, option, delete_option_error, centOrderedStringifiedIndexes, periOrderedStringifiedIndexes, centIsUpdatedOrderedIndexs, periIsUpdatedOrderedIndexs);
        return GSON.toJson(hmapResponse);
    }
}
