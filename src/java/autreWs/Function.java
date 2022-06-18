/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autreWs;

import autreWs.convention_details.Factures;
import autreWs.convention_details.Charges;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import synchronisable.FunctionRead;
import synchronisable.exception.NullArgumentException;
import synchronisable.param.exception.ParamException;
import synchronisation.SynchronisationConfig;
import usefull.HashHelper;
import usefull.UrlHelper;
import usefull.dao.Helper;
import usefull.dao.exception.IndefinedStaionTypeException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class Function {
    public static void main(String[] args) {
        HashMap<String, Object> pagntFoot = getPagntFoot(0, 5, 10,2, "baseurl");
        System.out.println("autreWs.Function.main()");
    }
    
    public static HashMap<String, Object> getPagntFoot(long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize,long nbrTotalLigne, String baseUri, String[]... keyValParams){
        long longPagntFootMaxSize = ((Number)pagntFootMaxSize).longValue();
        long longPagntMaxSize = ((Number)pagntMaxSize).longValue();
        
        long nbrTotalLigneParPage = (longPagntFootMaxSize*longPagntMaxSize);
        long range = pagntStartNum / nbrTotalLigneParPage;
        long pagntMinNum = Math.max(0, range*nbrTotalLigneParPage);
        long pagntMaxNum = Math.min(((range+1)*nbrTotalLigneParPage-1), nbrTotalLigne-1);
        HashMap<String, Object> foot = new HashMap<>(3);
        if(0<pagntMinNum)
          foot.put("previous", baseUri+"/from"+(pagntMinNum-1));
        if(pagntMaxNum<nbrTotalLigne-1)
          foot.put("next", baseUri+"/from"+(pagntMaxNum+1));
        
        long nbrLigneConcernée = (pagntMaxNum - pagntMinNum+1);
        int nbrPage = ((Number)(nbrLigneConcernée/longPagntMaxSize)).intValue();
        if(nbrLigneConcernée%longPagntMaxSize != 0)//mbola misy reste
            nbrPage++;
        HashMap[] middles = new HashMap[nbrPage];
        long expectedNextPagntStartNum = pagntMaxNum+1;
        foot.put("middle", middles);int middleIndice = 0;
        long i = pagntMinNum;
        String param = UrlHelper.getGetParam(keyValParams);
        while(i < expectedNextPagntStartNum){
            long min=i;long max=Math.min(min+longPagntMaxSize-1, nbrTotalLigne-1) ;
            String label = (min!=max)? ""+(min+1)+" à "+(max+1): ""+(min+1);
            HashMap<String, Object> middle = HashHelper.newHashMap(new String[]{"href", "value"}, new Object[]{baseUri+"/from"+min+param, label});
            if(min <= pagntStartNum && pagntStartNum <= max)
              middle.put("is_active", true);
            middles[middleIndice++] =  middle;   
            i=max+1;
        }return foot;
    }

  public static HashMap<String, Object> executeWithConnection(String stationCibleType, String functionName, HashMap<String, Object> args) throws Exception{
    HashMap<String, Object> hmapResponse = new HashMap<>(4);
    HashMap<String, Object> hmapScope = new HashMap<>(20);
    hmapResponse.put("scope", hmapScope);
    Connection connection = null;
    try {
        if(Helper.centraleStationType.equals(stationCibleType)){
            try {
                connection = Helper.getCentConn();
            } catch (SQLException e) {
                hmapScope.put("page_notification_message", "problème de connection en ligne.");
                throw e;
            }catch(Exception e){
                hmapScope.put("page_notification_message", "un problème est survenu <br> pendant la connection en ligne.<br>("+e.getMessage()+")");
                throw e;
            }
        }else if(Helper.peripheriqueStationType.equals(stationCibleType)){
            try {
                connection = Helper.getPeriConn();
            } catch (SQLException e) {
                hmapScope.put("page_notification_message", "problème de connection locale.");
                throw e;
            }catch(Exception e){
                hmapScope.put("page_notification_message", "un problème est survenu <br> pendant la connection en locale.<br>("+e.getMessage()+")");
                throw e;
            }
        }else{
            hmapResponse.put("path_error", true);
            throw new IndefinedStaionTypeException();
        }
        /*code*/
        long pagntStartNum = (Long)args.get("pagntStartNum");
        int pagntFootMaxSize = (Integer)args.get("pagntFootMaxSize");
        int pagntMaxSize = (Integer)args.get("pagntMaxSize");
        Long etablssmtId = (Long)args.get("etablssmtId");
        
        List<List<String>> allSynchroName = SynchronisationConfig.getAllSynchroName();
        
        if("findEtablissements".equals(functionName))
            Etablissement.findEtablissements(hmapResponse, stationCibleType, connection, pagntStartNum, pagntFootMaxSize, pagntMaxSize);
        else if("findMateriels".equals(functionName))
            Materiel.findMateriels(hmapResponse, stationCibleType, connection, pagntStartNum, pagntFootMaxSize, pagntMaxSize, etablssmtId);
        else if("findListOfMateriel".equals(functionName)){
            long typeMtrlId = (Long)args.get("typeMtrlId");
            boolean deleted = (Boolean)args.get("deleted");
            ListeMateriel.findListOfMateriel(hmapResponse, stationCibleType, connection, typeMtrlId, pagntStartNum, pagntFootMaxSize, pagntMaxSize, etablssmtId, deleted);
        }else if("findListOfConsommationEvent".equals(functionName)){
            long typeMtrlId = (Long)args.get("typeMtrlId");
            long lieuId = (Long)args.get("lieuId");
            boolean isInEtablissmtContext = ((etablssmtId != null) && (etablssmtId.compareTo(new Long(0)) != 0));
            MaterielConsommable.findListOfEvent(hmapResponse, stationCibleType, connection, typeMtrlId, lieuId, pagntStartNum, pagntFootMaxSize, pagntMaxSize, isInEtablissmtContext);
        }else if("pagine".equals(functionName))
            Autres.pagine((String)args.get("synchroName"), hmapResponse, stationCibleType, connection, pagntStartNum, pagntFootMaxSize, pagntMaxSize);
        else if("pagineConventionDetails".equals(functionName)){
            long conventionId = (Long)args.get("conventionId");
            String synchroName = (String)args.get("synchroName");
            if(allSynchroName.get(4).get(0).equalsIgnoreCase(synchroName))//Facture
                Factures.pagine(conventionId, hmapResponse, stationCibleType, connection, pagntStartNum, pagntFootMaxSize, pagntMaxSize);
            else//allSynchroName.get(4).get(1).toLowerCase()Charge
                Charges.pagine(conventionId, hmapResponse, stationCibleType, connection, pagntStartNum, pagntFootMaxSize, pagntMaxSize);
        }else{
            hmapResponse.put("path_error", true);
            throw new NullArgumentException("Indefined function name "+functionName+".");
        }
        /*code*/
        if(!hmapResponse.containsKey("status"))
            hmapResponse.put("status", true);
    } catch (Exception e) {
        hmapResponse.put("status", false);
        if(!hmapScope.containsKey("page_notification_message")){
            if(e instanceof ParamException){
                hmapScope.put("page_notification_message", e.getMessage());
                hmapScope.put("primary_bttn",HashHelper.newHashMap(new String[]{"value","uri"}, new String[]{"Paramètre","Parametres"}));
            }else
                hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
        }
        hmapScope.put("page_notification_class", "danger");
        if(!hmapScope.containsKey("page_notification_title")){//default title
            hmapScope.put("page_notification_title", "Erreur");
        }
//            throw e;//*******LLLLLLLL*******************
    }finally{
        if(connection!=null){
            try {
                    connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(FunctionRead.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }return hmapResponse;
  }

}
