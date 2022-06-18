/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autreWs;

import com.google.gson.Gson;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import synchronisable.FunctionRead;
import synchronisable.convention.Convention;
import synchronisable.convention.ConventionCentSynchro;
import synchronisable.param.UsefulParam;
import synchronisable.param.exception.ParamException;
import usefull.ArrayHelper;
import usefull.HashHelper;
import usefull.NumberFormatHelper;
import usefull.StringHelper;
import usefull.dao.Helper;
import usefull.dao.exception.IndefinedStaionTypeException;
import usefull.dao.type.DBEnumType;

/**
 *
 * @author P12A-92-Emmanuel
 */
@Path("autreWs")
public class WS {
    private final static Gson GSON = new Gson();
    private final static Long LONG_ZERO = new Long(0); 
    
    @GET
    @Path("convention_details/{convention_id}/{synchro_name}/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}/{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String pagineConventionDetails(@PathParam("convention_id") long conventionId , @PathParam("synchro_name") String synchroName, @PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, @PathParam("sationCibleType") String sationCibleType) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = Function.executeWithConnection(sationCibleType, "pagineConventionDetails", HashHelper.newHashMap(new String[]{"conventionId", "synchroName", "pagntStartNum", "pagntFootMaxSize", "pagntMaxSize"}, new Object[]{conventionId, synchroName, pagntStartNum, pagntFootMaxSize, pagntMaxSize}));
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("pagine/{synchro_name}/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}/{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String pagine(@PathParam("synchro_name") String synchroName, @PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, @PathParam("sationCibleType") String sationCibleType) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = Function.executeWithConnection(sationCibleType, "pagine", HashHelper.newHashMap(new String[]{"synchroName", "pagntStartNum", "pagntFootMaxSize", "pagntMaxSize"}, new Object[]{synchroName, pagntStartNum, pagntFootMaxSize, pagntMaxSize}));
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("findListOfConsommationEvent/{type_mtrl_id}/{lieu_id}/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}/{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String findListOfConsommationEvent(@PathParam("type_mtrl_id") long typeMtrlId, @PathParam("lieu_id") long lieuId, @PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, @PathParam("sationCibleType") String sationCibleType) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = Function.executeWithConnection(sationCibleType, "findListOfConsommationEvent", HashHelper.newHashMap(new String[]{"typeMtrlId", "lieuId", "pagntStartNum", "pagntFootMaxSize", "pagntMaxSize"}, new Object[]{typeMtrlId, lieuId, pagntStartNum, pagntFootMaxSize, pagntMaxSize}));
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("findListOfConsommationEvent/{type_mtrl_id}/{lieu_id}/{etablssmt_id}/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}/{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String findListOfConsommationEvent(@PathParam("type_mtrl_id") long typeMtrlId, @PathParam("lieu_id") long lieuId, @PathParam("etablssmt_id") long etablssmtId, @PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, @PathParam("sationCibleType") String sationCibleType) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = Function.executeWithConnection(sationCibleType, "findListOfConsommationEvent", HashHelper.newHashMap(new String[]{"typeMtrlId", "lieuId", "etablssmtId", "pagntStartNum", "pagntFootMaxSize", "pagntMaxSize"}, new Object[]{typeMtrlId, lieuId, etablssmtId, pagntStartNum, pagntFootMaxSize, pagntMaxSize}));
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("findListOfMateriel/{type_mtrl_id}/{etablssmt_id}/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}/{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String findMateriels(@PathParam("type_mtrl_id") long typeMtrlId, @PathParam("etablssmt_id") long etablssmtId, @PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, @PathParam("sationCibleType") String sationCibleType, @QueryParam("deleted") boolean deleted) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = Function.executeWithConnection(sationCibleType, "findListOfMateriel", HashHelper.newHashMap(new String[]{"typeMtrlId", "etablssmtId", "pagntStartNum", "pagntFootMaxSize", "pagntMaxSize", "deleted"}, new Object[]{typeMtrlId, etablssmtId, pagntStartNum, pagntFootMaxSize, pagntMaxSize, deleted}));
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("findListOfMateriel/{type_mtrl_id}/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}/{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String findMateriels(@PathParam("type_mtrl_id") long typeMtrlId, @PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, @PathParam("sationCibleType") String sationCibleType, @QueryParam("deleted") boolean deleted) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = Function.executeWithConnection(sationCibleType, "findListOfMateriel", HashHelper.newHashMap(new String[]{"typeMtrlId", "pagntStartNum", "pagntFootMaxSize", "pagntMaxSize", "deleted"}, new Object[]{typeMtrlId, pagntStartNum, pagntFootMaxSize, pagntMaxSize, deleted}));
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("findMateriels/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}/{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String findMateriels(@PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, @PathParam("sationCibleType") String sationCibleType) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = Function.executeWithConnection(sationCibleType, "findMateriels", HashHelper.newHashMap(new String[]{"pagntStartNum", "pagntFootMaxSize", "pagntMaxSize"}, new Object[]{pagntStartNum, pagntFootMaxSize, pagntMaxSize}));
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("findMateriels/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}/{etablssmt_id}/{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String findMateriels(@PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, @PathParam("etablssmt_id") Long etablssmtId, @PathParam("sationCibleType") String sationCibleType) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = Function.executeWithConnection(sationCibleType, "findMateriels", HashHelper.newHashMap(new String[]{"pagntStartNum", "pagntFootMaxSize", "pagntMaxSize", "etablssmtId"}, new Object[]{pagntStartNum, pagntFootMaxSize, pagntMaxSize, etablssmtId}));
        return GSON.toJson(hmapResponse);
    }
    
    @GET
    @Path("findEtablissements/{pagnt_start_num}/{pagnt_foot_max_size}/{pagnt_max_size}/{sationCibleType}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String findEtablissements(@PathParam("pagnt_start_num") long pagntStartNum, @PathParam("pagnt_foot_max_size") int pagntFootMaxSize, @PathParam("pagnt_max_size") int pagntMaxSize, @PathParam("sationCibleType") String sationCibleType) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = Function.executeWithConnection(sationCibleType, "findEtablissements", HashHelper.newHashMap(new String[]{"pagntStartNum", "pagntFootMaxSize", "pagntMaxSize"}, new Object[]{pagntStartNum, pagntFootMaxSize, pagntMaxSize}));
        return GSON.toJson(hmapResponse);
    }
    
    @POST
    @Path("totaliser-accompagnement-et-serveur")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String getTotaliserAccompagnementAndServeur(JsonObject jsonObject) throws Exception{//peri synchro par défaut
//        System.out.println("jsonObject: "+jsonObject.toString());
        long ordiClientNbr = 0;
        BigDecimal ordiClientPrxU = BigDecimal.ZERO;
        BigDecimal ordiClientRedU = null;
        Long ordiClientRedMarge = null;
        long ordiServrNbr = 0;
        BigDecimal ordiServrPrxU = BigDecimal.ZERO;
        BigDecimal ordiServrRedU = null;
        Long ordiServrRedMarge = null;
        BigDecimal redFinale = null;
        String redFinaleTypeVal = null;
        HashMap<String, Object> hmapResponse = new HashMap<>(3);

        try {
            ConventionCentSynchro conventionCentSynchro = new ConventionCentSynchro();
            conventionCentSynchro.setNbrEleve(200);//isFinished true
            HashMap<Integer, String> errorSaisieIndiceValue = new HashMap<>(10);
            try { conventionCentSynchro.setOrderedStringifiedDatas(GSON.fromJson(jsonObject.get("accompgmt").toString(), String.class), 3); if(conventionCentSynchro.getOrdiClientPrxU() != null) ordiClientPrxU = conventionCentSynchro.getOrdiClientPrxU(); } catch (Exception e) { hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Erreur de saisie!");  errorSaisieIndiceValue.put(3, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); }
            try { conventionCentSynchro.setOrderedStringifiedDatas(GSON.fromJson(jsonObject.get("ordi_client_nbr").toString(), String.class), 4); if(conventionCentSynchro.getOrdiClientNbr() != null) ordiClientNbr = conventionCentSynchro.getOrdiClientNbr(); } catch (Exception e) { hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Erreur de saisie!");  errorSaisieIndiceValue.put(4, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); }
            try { conventionCentSynchro.setOrderedStringifiedDatas(GSON.fromJson(jsonObject.get("ordi_client_red_mg").toString(), String.class), 5); if(conventionCentSynchro.getOrdiClientRedMarge() != null) ordiClientRedMarge = conventionCentSynchro.getOrdiClientRedMarge(); } catch (Exception e) { hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Erreur de saisie!");  errorSaisieIndiceValue.put(5, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); }
            try { 
                conventionCentSynchro.setOrderedStringifiedDatas(GSON.fromJson(jsonObject.get("ordi_client_red_u").toString(), String.class), 7); 
                if(conventionCentSynchro.getOrdiClientRedU() != null) 
                    ordiClientRedU = conventionCentSynchro.getOrdiClientRedU(); 
            } catch (Exception e) { 
                hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Erreur de saisie!");  errorSaisieIndiceValue.put(7, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); 
            }
            try { conventionCentSynchro.setOrderedStringifiedDatas(GSON.fromJson(jsonObject.get("ordi_srvr_nbr").toString(), String.class), 8); if(conventionCentSynchro.getOrdiServrNbr() != null) ordiServrNbr = conventionCentSynchro.getOrdiServrNbr(); } catch (Exception e) { hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Erreur de saisie!");  errorSaisieIndiceValue.put(8, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); }
            try { conventionCentSynchro.setOrderedStringifiedDatas(GSON.fromJson(jsonObject.get("ordi_srvr_mtt_u").toString(), String.class), 9); if(conventionCentSynchro.getOrdiServrPrxU() != null) ordiServrPrxU = conventionCentSynchro.getOrdiServrPrxU(); } catch (Exception e) { hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Erreur de saisie!");  errorSaisieIndiceValue.put(9, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); }
            try { conventionCentSynchro.setOrderedStringifiedDatas(GSON.fromJson(jsonObject.get("ordi_srvr_red_mg").toString(), String.class), 10); if(conventionCentSynchro.getOrdiServrRedMarge() != null) ordiServrRedMarge = conventionCentSynchro.getOrdiServrRedMarge(); } catch (Exception e) { hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Erreur de saisie!");  errorSaisieIndiceValue.put(10, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); }
            try { conventionCentSynchro.setOrderedStringifiedDatas(GSON.fromJson(jsonObject.get("ordi_srvr_red_u").toString(), String.class), 12); if(conventionCentSynchro.getOrdiServrRedU() != null) ordiServrRedU = conventionCentSynchro.getOrdiServrRedU(); } catch (Exception e) { hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Erreur de saisie!");  errorSaisieIndiceValue.put(12, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); }
            try { conventionCentSynchro.setOrderedStringifiedDatas(              jsonObject.getString("reduction_finale_type", null), 13); if(conventionCentSynchro.getRedFinaleTypeVal() != null) redFinaleTypeVal = conventionCentSynchro.getRedFinaleTypeVal(); } catch (Exception e) { hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Erreur de saisie!"); errorSaisieIndiceValue.put(13, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); }
            try { conventionCentSynchro.setOrderedStringifiedDatas(GSON.fromJson(jsonObject.get("reduction_finale_val").toString(), String.class), 14); if(conventionCentSynchro.getRedFinale() != null) redFinale = conventionCentSynchro.getRedFinale(); } catch (Exception e) { hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Erreur de saisie!");  errorSaisieIndiceValue.put(14, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); }
            if(!errorSaisieIndiceValue.isEmpty()){
                hmapResponse.put("ordered_data_errors", errorSaisieIndiceValue);
                return GSON.toJson(hmapResponse);
            }
            NumberFormat MONEY_DECIMAL_FORMAT = NumberFormatHelper.MONEY_DECIMAL_FORMAT();
            List<HashMap<String, String>> data = new ArrayList<>(4);
            BigDecimal accompagnemt = Convention.getMontantTotalApayer(ordiClientNbr, ordiClientPrxU, ordiClientRedU, ordiClientRedMarge, LONG_ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LONG_ZERO, BigDecimal.ZERO, null, null);
            BigDecimal serveurs = Convention.getMontantTotalApayer(LONG_ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LONG_ZERO, ordiServrNbr, ordiServrPrxU, ordiServrRedU, ordiServrRedMarge, BigDecimal.ZERO, null, null);
            data.add(HashHelper.newHashMap(new String[]{"title", "value"}, new String[]{ "Accompagnement", MONEY_DECIMAL_FORMAT.format(accompagnemt) }));
            data.add(HashHelper.newHashMap(new String[]{"title", "value"}, new String[]{ "Serveur(s)", MONEY_DECIMAL_FORMAT.format(serveurs) }));
            data.add(HashHelper.newHashMap(new String[]{"title", "value"}, new String[]{ "Total Accompagnement et Serveur(s)", MONEY_DECIMAL_FORMAT.format(accompagnemt.add(serveurs)) }));
            if((redFinale!= null) && (BigDecimal.ZERO.compareTo(redFinale)<0) )
                data.add(HashHelper.newHashMap(new String[]{"title", "value"}, new String[]{ "Total Après Réduction", MONEY_DECIMAL_FORMAT.format( Convention.getMontantTotalApayer(ordiClientNbr, ordiClientPrxU, ordiClientRedU, ordiClientRedMarge, ordiServrNbr, ordiServrPrxU, ordiServrRedU, ordiServrRedMarge, redFinale, redFinaleTypeVal, null) ) }));
            hmapResponse.put("data", data);
            hmapResponse.put("status", true);
        } catch (Exception e) {
            hmapResponse.put("status", false);hmapResponse.put("errorMessage", "Un problème est survenu.<br>("+e.getMessage()+")");
        }return GSON.toJson(hmapResponse);
    }

    @POST
    @Path("conevention-param-autoload/{etablssmt_id}/{sationCibleType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static String getConeventionParam(@PathParam("etablssmt_id") Long etablssmtId, @PathParam("sationCibleType") String stationCibleType, JsonObject jsonObject) throws Exception{//peri synchro par défaut
        HashMap<String, Object> hmapResponse = new HashMap<>(3);
        Connection connection = null;
        try {
            if(Helper.centraleStationType.equals(stationCibleType)){
                try {
                    connection = Helper.getCentConn();
                } catch (SQLException e) {
                    hmapResponse.put("errorMessage", "Problème de connection en ligne.");
                    throw e;
                }catch(Exception e){
                    hmapResponse.put("errorMessage", "Un problème est survenu <br> pendant la connection en ligne.<br>"+e.getMessage());
                    throw e;
                }
            }else if(Helper.peripheriqueStationType.equals(stationCibleType)){
                try {
                    connection = Helper.getPeriConn();
                } catch (SQLException e) {
                    hmapResponse.put("errorMessage", "problème de connection locale.");
                    throw e;
                }catch(Exception e){
                    hmapResponse.put("errorMessage", "Un problème est survenu <br> pendant la connection en locale.<br>"+e.getMessage());
                    throw e;
                }
            }else{
                throw new IndefinedStaionTypeException();
            }
            
            HashMap<Integer, Object> orderedData = new HashMap<>(10);
            int[] dataIndiceChfEtablissmtNbrEleveOrdiClientServr = new int[]{1, 2, 4, 8};
            boolean[] isToLoadChfEtablissmtNbrEleveOrdiClientServr = new boolean[]{
                GSON.fromJson(jsonObject.getJsonObject("chef_etablissmt").get("is_to_load").toString(), Boolean.class), 
                GSON.fromJson(jsonObject.getJsonObject("nbr_eleve").get("is_to_load").toString(), Boolean.class), 
                GSON.fromJson(jsonObject.getJsonObject("ordi_client_nbr").get("is_to_load").toString(), Boolean.class), 
                GSON.fromJson(jsonObject.getJsonObject("ordi_srvr_nbr").get("is_to_load").toString(), Boolean.class)
            };
            int length = dataIndiceChfEtablissmtNbrEleveOrdiClientServr.length;
            ConventionCentSynchro convention = new ConventionCentSynchro();
            
            ConventionCentSynchro fixedConvention = new ConventionCentSynchro();
            fixedConvention.setNbrEleve(150);//@ zay convention isFinished()=> new nbrEleve must not null
            
            if(!ArrayHelper.every(false, isToLoadChfEtablissmtNbrEleveOrdiClientServr)){
                Object[] chfEtablissmtNbrEleveOrdiClientServr = Convention.getChfEtablissmtNbrEleveOrdiClientServr(connection, ""+etablssmtId);
                
                int nbrEleveIndice = 1;
                if(isToLoadChfEtablissmtNbrEleveOrdiClientServr[nbrEleveIndice]){
                    try {
                        convention.setNbrEleve((Integer)chfEtablissmtNbrEleveOrdiClientServr[nbrEleveIndice]);
                    } catch (Exception e) { 
                        hmapResponse.put("status", false); 
                        hmapResponse.put("errorMessage", "Nombre d'élève trouvé "+chfEtablissmtNbrEleveOrdiClientServr[nbrEleveIndice]+" invalide!<br>"
                                            + StringHelper.coalesce(e.getMessage(), e.getClass().getName()) 
                        );throw e;
                    }
                }
                
                int nbrOrdiClientIndice = 2;
                if(isToLoadChfEtablissmtNbrEleveOrdiClientServr[nbrOrdiClientIndice]){
                    try {
                        fixedConvention.setOrdiClientNbr((Long)chfEtablissmtNbrEleveOrdiClientServr[nbrOrdiClientIndice]);
                    } catch (Exception e) { 
                        hmapResponse.put("status", false); 
                        hmapResponse.put("errorMessage", "Nombre d'ordi client trouvé "+chfEtablissmtNbrEleveOrdiClientServr[nbrOrdiClientIndice]+" invalide!<br>"
                                            + StringHelper.coalesce(e.getMessage(), e.getClass().getName()) 
                        );throw e;
                    }
                }                
                
                int nbrOrdiServrIndice = 3;
                if(isToLoadChfEtablissmtNbrEleveOrdiClientServr[nbrOrdiServrIndice]){
                    try {
                        fixedConvention.setOrdiServrNbr((Long)chfEtablissmtNbrEleveOrdiClientServr[nbrOrdiServrIndice]);
                    } catch (Exception e) { 
                        hmapResponse.put("status", false); 
                        hmapResponse.put("errorMessage", "Nombre de Serveurs trouvé "+chfEtablissmtNbrEleveOrdiClientServr[nbrOrdiServrIndice]+" invalide!<br>"
                                            + StringHelper.coalesce(e.getMessage(), e.getClass().getName()) 
                        );throw e;
                    }
                }                
                
                for (int i = 0; i < length; i++) {
                    if(isToLoadChfEtablissmtNbrEleveOrdiClientServr[i])
                        orderedData.put(dataIndiceChfEtablissmtNbrEleveOrdiClientServr[i], HashHelper.newHashMap(new String[]{"value"}, new Object[]{chfEtablissmtNbrEleveOrdiClientServr[i]}) );
                }
            }
            int[] dataIndiceAcmpmtSrvrPrxU = new int[]{3, 9};
            boolean[] isToLoadAcmpmtSrvrPrxU = new boolean[]{
                GSON.fromJson(jsonObject.getJsonObject("accompgmt").get("is_to_load").toString(), Boolean.class), 
                GSON.fromJson(jsonObject.getJsonObject("ordi_srvr_mtt_u").get("is_to_load").toString(), Boolean.class)
            };
            if(!ArrayHelper.every(false, isToLoadAcmpmtSrvrPrxU)){
                if(convention.getNbrEleve() == null){//is_to_autoload nbrEleve = false => nbrEleve inputed
                    try {
                        fixedConvention.setNbrEleve(GSON.fromJson(jsonObject.getJsonObject("nbr_eleve").get("input").toString(), Integer.class));
                    } catch (Exception e) { 
                        hmapResponse.put("status", false); 
                        hmapResponse.put("errorTitle", "Erreur de saisie!");
                        hmapResponse.put("errorMessage", "Nombre d'élève "+ StringHelper.coalesce(GSON.fromJson(jsonObject.getJsonObject("nbr_eleve").get("input").toString(), Integer.class), "") +" invalide!<br>"
                                            + StringHelper.coalesce(e.getMessage(), e.getClass().getName()) 
                        );throw e;
                    }
                }BigDecimal paramValue = new BigDecimal(convention.getNbrEleve());
                int indice = 0;
                if(isToLoadAcmpmtSrvrPrxU[indice]){
                    try {
                        orderedData.put(dataIndiceAcmpmtSrvrPrxU[indice], HashHelper.newHashMap(new String[]{"value"}, new Object[]{ 
                            (BigDecimal)UsefulParam.getValAndTypeValAndPlafondMajorant(UsefulParam.PRX_U_ORDI_CLIENT_PARAM_NAME, paramValue, stationCibleType, connection, "Veuillez paramètrer le montant annuel de l'accompagnement <br> au cas où le nombre d'élèves = "+ convention.getNbrEleve()+" ." )[0] 
                        }) );
                    } catch (ParamException e) {
                        hmapResponse.put("status", false); 
                        hmapResponse.put("errorMessage", e.getMessage());
                        throw e;
                    }
                }
                if(isToLoadAcmpmtSrvrPrxU[++indice]){
                    try {
                        orderedData.put(dataIndiceAcmpmtSrvrPrxU[indice], HashHelper.newHashMap(new String[]{"value"}, new Object[]{ 
                            (BigDecimal)UsefulParam.getValAndTypeValAndPlafondMajorant(UsefulParam.PRX_U_ORDI_SERVEUR_PARAM_NAME, paramValue, stationCibleType, connection, "Veuillez paramètrer le montant annuel par serveur<br> au cas où le nombre d'élèves = "+ convention.getNbrEleve()+" .")[0]
                        }) );
                    } catch (ParamException e) {
                        hmapResponse.put("status", false); 
                        hmapResponse.put("errorMessage", e.getMessage());
                        throw e;
                    }
                }
            }


            
            int[] dataIndiceOrdiClientRedMargePrxU = new int[]{5, 7};
            boolean[] isToLoadOrdiClientRedMargePrxU = new boolean[]{
                GSON.fromJson(jsonObject.getJsonObject("ordi_client_red_mg").get("is_to_load").toString(), Boolean.class), 
                GSON.fromJson(jsonObject.getJsonObject("ordi_client_red_u").get("is_to_load").toString(), Boolean.class)
            };
            if(!ArrayHelper.every(false, isToLoadOrdiClientRedMargePrxU)){
                if(fixedConvention.getOrdiClientNbr() == null){//is_to_autoload nbrOrdiClient = false => nbrOrdiClient inputed
                    try {
                        fixedConvention.setOrdiClientNbr(GSON.fromJson(jsonObject.getJsonObject("ordi_client_nbr").get("input").toString(), Long.class));
                    } catch (Exception e) { 
                        hmapResponse.put("status", false); 
                        hmapResponse.put("errorTitle", "Erreur de saisie!");
                        hmapResponse.put("errorMessage", "Nombre d'ordi client "+ StringHelper.coalesce(GSON.fromJson(jsonObject.getJsonObject("ordi_client_nbr").get("input").toString(), Integer.class), "") +" invalide!<br>"
                                            + StringHelper.coalesce(e.getMessage(), e.getClass().getName()) 
                        );throw e;
                    }
                }
                
                Object[] ordiClientRedValAndTypeValAndMarge = null;
                try {
                    ordiClientRedValAndTypeValAndMarge = UsefulParam.getValAndTypeValAndPlafondMajorant(UsefulParam.RED_U_ORDI_CLIENT_PARAM_NAME, new BigDecimal(fixedConvention.getOrdiClientNbr().longValue()), stationCibleType, connection);
                } catch (ParamException e) {
                    ordiClientRedValAndTypeValAndMarge = new Object[]{BigDecimal.ZERO, DBEnumType.PARAM_TYPE_VAL_MONTANT, BigDecimal.ZERO};
                }
                Object[] ordiClientRedMargePrxU = new Object[]{ordiClientRedValAndTypeValAndMarge[2], ordiClientRedValAndTypeValAndMarge[0]};
                int length2 = dataIndiceOrdiClientRedMargePrxU.length;
                for (int i = 0; i < length2; i++) {
                    if(isToLoadOrdiClientRedMargePrxU[i])
                        orderedData.put(dataIndiceOrdiClientRedMargePrxU[i], HashHelper.newHashMap(new String[]{"value"}, new Object[]{ordiClientRedMargePrxU[i]}) );
                }
            }
            
            
            
            
            
            
            
            
            
            
            
            
            
//            boolean dataErrorExist = false;
//            try {
//                if(isToLoad)
//                else
//                    
//            } catch (Exception e) { hmapResponse.put("status", false); dataErrorExist = true; hmapResponse.put("errorMessage", "Erreur!"); ;error  errorSaisieIndiceValue.put(3, StringHelper.coalesce(e.getMessage(), e.getClass().getName()) ); }
//
//
//            if(!dataErrorExist)
//                return GSON.toJson(hmapResponse);
            
            
            hmapResponse.put("ordered_data", orderedData);
            hmapResponse.put("status", true);
        } catch (Exception e) {
            hmapResponse.put("status", false); 
            if(!hmapResponse.containsKey("errorMessage"))
                hmapResponse.put("errorMessage", "Un problème est survenu.<br>("+StringHelper.coalesce(e.getMessage(), e.getClass().getName())+")");
//                   throw e;//*******LLLLLLLL*******************
        }finally{
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(FunctionRead.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }return GSON.toJson(hmapResponse);
    }

}
/*
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
        HashMap<String, String> otherParam = new HashMap<>(0);
        if(!jsonObjectIsNull){
            if(jsonObject.containsKey("other_params"))
                otherParam = GSON.fromJson(jsonObject.getJsonObject("other_params").toString(), otherParam.getClass());
        }
        String[] orderedStringifiedIndexs = null; 
        String[] orderedStringifiedDatas = null;
        if(!jsonObjectIsNull) {
            orderedStringifiedIndexs = jsonObject.containsKey("ordered_indexe") ? GSON.fromJson(jsonObject.getJsonArray("ordered_indexe").toString(), String[].class) : null;
            orderedStringifiedDatas =  jsonObject.containsKey("orderedData") ? GSON.fromJson(jsonObject.getJsonArray("orderedData").toString(), String[].class) : null;
        }
        HashMap<String, Object> hmapResponse = FunctionCreate.findEtablissements(synchroName, sationCibleType, userId, send, orderedStringifiedIndexs, orderedStringifiedDatas, otherParam);
        return GSON.toJson(hmapResponse);
    }
*/