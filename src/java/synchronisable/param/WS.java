/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisable.param;

import synchronisable.*;
import com.google.gson.Gson;
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
@Path("Parametres")
public class WS {
    private final static Gson GSON = new Gson();

    @GET
    @Path("{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String read(@PathParam("sationCibleType") String sationCibleType) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = FunctionRead.indexe(sationCibleType);
        return GSON.toJson(hmapResponse);
    }
    
}
