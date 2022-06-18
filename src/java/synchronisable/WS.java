/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisable;

import com.google.gson.Gson;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import presynchronisation.Initiateur;
import synchronisable.exception.NullArgumentException;
import usefull.StringHelper;
import usefull.dao.Helper;

/**
 *
 * @author P12A-92-Emmanuel
 */
@Path("Synchronisable")
public class WS {
    private final static Gson GSON = new Gson();
    
    @POST
    @Path("read/{synchroName}/{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String read(@PathParam("synchroName") String synchroName, JsonObject jsonObject, @PathParam("sationCibleType") String stationCibleType) throws Exception{//peri synchro par défaut
        HashMap<String, Object> indexsNameValue = new HashMap<>(0);
        indexsNameValue = GSON.fromJson(jsonObject.toString(), indexsNameValue.getClass());
        HashMap<String, Object> hmapResponse = FunctionRead.indexe(synchroName, indexsNameValue, stationCibleType, null);
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("read/{synchroName}/{synchroId}/{sationCibleType}") 
   @Produces(MediaType.APPLICATION_JSON)
    public static String read(@PathParam("synchroName") String synchroName, @PathParam("synchroId") String synchroId, @PathParam("sationCibleType") String stationCibleType, @QueryParam("etablissmt") Long etablssmtId, @QueryParam("story") Long synchroStoryId) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = (synchroStoryId == null)? FunctionRead.indexe(synchroName, synchroId, stationCibleType, etablssmtId): FunctionRead.indexe(synchroName, synchroId, stationCibleType, etablssmtId, synchroStoryId.toString());
        return GSON.toJson(hmapResponse);
    }

    @GET
    @Path(Historique.SYNCHRO_NAME+"/{synchroName}/{synchroId}/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}/{stationCibleType}") 
    @Produces(MediaType.APPLICATION_JSON)
    public static String read(@PathParam("synchroName") String synchroName, @PathParam("synchroId") long synchroId, @PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, @PathParam("stationCibleType") String stationCibleType, @QueryParam("start") String strDateTimeStart, @QueryParam("end") String strDateTimeEnd) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = Historique.pagine(stationCibleType, synchroName, synchroId, pagntStartNum, pagntFootMaxSize, pagntMaxSize, strDateTimeStart, strDateTimeEnd);
        return GSON.toJson(hmapResponse);
    }
    
    @POST
    @Path("create/{synchroName}/{sationCibleType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String create(
            @PathParam("synchroName") String synchroName, @PathParam("sationCibleType") String sationCibleType,
            JsonObject jsonObject
    ) throws Exception{//peri synchro par défaut
//        System.out.println("jsonObject: "+jsonObject.toString());
        boolean jsonObjectIsNull = jsonObject == null;
        String userId = jsonObjectIsNull? null : jsonObject.getString("user_id",null);
        String send   = jsonObjectIsNull? null : jsonObject.getString("send",null);
        String[] orderedStringifiedIndexs = null; 
        String[] orderedStringifiedDatas = null;
        HashMap<String, String> otherParam = new HashMap<>(0);
        if(!jsonObjectIsNull) {
            orderedStringifiedIndexs = jsonObject.containsKey("ordered_indexe") ? GSON.fromJson(jsonObject.getJsonArray("ordered_indexe").toString(), String[].class) : null;
            orderedStringifiedDatas =  jsonObject.containsKey("ordered_data") ? GSON.fromJson(jsonObject.getJsonArray("ordered_data").toString(), String[].class) : null;
            if(jsonObject.containsKey("other_params")){
                otherParam = GSON.fromJson(jsonObject.getJsonObject("other_params").toString(), otherParam.getClass());
            }
        }
        HashMap<String, Object> hmapResponse = FunctionCreate.indexe(synchroName, sationCibleType, userId, send, orderedStringifiedIndexs, orderedStringifiedDatas, otherParam);
        return GSON.toJson(hmapResponse);
    }
    
    @POST
    @Path("update/{synchroName}/{synchroId}/{sationCibleType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String update(
            @PathParam("synchroName") String synchroName, @PathParam("synchroId") String synchroId, @PathParam("sationCibleType") String sationCibleType,
            JsonObject jsonObject
    ) throws Exception{//peri synchro par défaut
//        System.out.println("jsonObject: "+jsonObject.toString());
        boolean jsonObjectIsNull = jsonObject == null;
        String userId = jsonObjectIsNull? null : jsonObject.getString("user_id",null);
        String send   = jsonObjectIsNull? null : jsonObject.getString("send",null);
        String[] orderedStringifiedIndexs = null; 
        String[] orderedStringifiedDatas = null;
        String[] isUpdatedOrderedIndexs = null; 
        String[] isUpdatedOrderedDatas = null;
        HashMap<String, String> otherParam = new HashMap<>(0);
        if(!jsonObjectIsNull) {
            orderedStringifiedIndexs = jsonObject.containsKey("ordered_indexe") ? GSON.fromJson(jsonObject.getJsonArray("ordered_indexe").toString(), String[].class) : null;
            orderedStringifiedDatas =  jsonObject.containsKey("ordered_data") ? GSON.fromJson(jsonObject.getJsonArray("ordered_data").toString(), String[].class) : null;
        
            isUpdatedOrderedIndexs = jsonObject.containsKey("is_updated_ordered_indexe") ? GSON.fromJson(jsonObject.getJsonArray("is_updated_ordered_indexe").toString(), String[].class) : null;
            isUpdatedOrderedDatas =  jsonObject.containsKey("is_updated_ordered_data") ? GSON.fromJson(jsonObject.getJsonArray("is_updated_ordered_data").toString(), String[].class) : null;

            if(jsonObject.containsKey("other_params")){
                otherParam = GSON.fromJson(jsonObject.getJsonObject("other_params").toString(), otherParam.getClass());
            }
        }
        Long etablssmtId = null;
        try {etablssmtId = StringHelper.checkLong(jsonObject.get("etablissmt").toString()); if(new Long(0).compareTo(etablssmtId) == 0)throw new NullArgumentException();} catch (Exception e) {etablssmtId = null;}
        HashMap<String, Object> hmapResponse = FunctionUpdate.indexe(synchroName, synchroId, sationCibleType, userId, send, orderedStringifiedIndexs, orderedStringifiedDatas, etablssmtId, isUpdatedOrderedIndexs, isUpdatedOrderedDatas, otherParam);
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("delete/{synchroName}/{synchroId}/{sationCibleType}/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String delete(@PathParam("synchroName") String synchroName, @PathParam("synchroId") String synchroId,@PathParam("sationCibleType") String sationCibleType, @PathParam("userId") String userId, @QueryParam("etablissmt") Long etablssmtId) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = FunctionDelete.indexe(synchroName, synchroId, sationCibleType, userId, etablssmtId);
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("export_csv/{synchroName}/{stationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String exportCSV(@PathParam("synchroName") String synchroName, @PathParam("stationCibleType") String stationCibleType, @QueryParam("deleted") boolean deleted) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = ExportCSV.getDatas(stationCibleType, synchroName, deleted, null);
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("export_csv/{synchroName}/{synchroId}/{stationCibleType}") 
    @Produces(MediaType.APPLICATION_JSON)
    public static String exportCSV(@PathParam("synchroName") String synchroName, @PathParam("synchroId") long synchroId, @PathParam("stationCibleType") String stationCibleType) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = ExportCSV.getDatas(stationCibleType, synchroName, false, synchroId);
        return GSON.toJson(hmapResponse);
    }
    
        
    @POST
    @Path("initlise-new-station")
    @Produces(MediaType.APPLICATION_JSON)
    public static String initliseNewStation(JsonObject jsonObject) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = new HashMap<>(2);
        try {
            HashMap<String, Object> data = new HashMap<>(0);
            data = GSON.fromJson(jsonObject.toString(), data.getClass());
            Connection centConnection = Helper.getCentConn();
            Connection periConnection = Helper.getPeriConn();
            String nom = (String) data.get("nom_station");
            String userId = data.get("user_id").toString();
            Date date = new Date();
            String timezone = (String) data.get("time_zone");
            String desc = (String) data.get("desc");
            Initiateur.initiateur(centConnection, periConnection, nom, userId, date, timezone, desc);
            hmapResponse.put("status", true);
            hmapResponse.put("message", "Création terminé avec succès!");
        } catch (Exception e) {
            hmapResponse.put("status", false);
            hmapResponse.put("message", "Erreur! ("+StringHelper.coalesce(e.getMessage(), e.getClass().getName())+")");
        }return GSON.toJson(hmapResponse);
    }
}
