/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisable;

import static autreWs.Function.getPagntFoot;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import synchronisation.SynchronisationConfig;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;
import usefull.SimpleDateFormatHelper;
import usefull.dao.Helper;
import usefull.dao.exception.IndefinedStaionTypeException;
import usefull.dao.exception.UnsupportedKeyTypeException;
import java.util.Date;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class Historique {
    public final static String SYNCHRO_NAME = "Historique";

    private static<SynchroType extends Synchro>  void pagine(SynchroType synchroType, HashMap<String, Object> hmapResponse, Connection connection, Timestamp dateTimeStart, Timestamp dateTimeEnd, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize) throws SQLException, UnsupportedKeyTypeException, Exception{
        pagntStartNum = Math.max(0, pagntStartNum);
        pagntFootMaxSize = Math.max(1, pagntFootMaxSize); 
        pagntMaxSize = Math.max(1, pagntMaxSize); 

        HashMap<String, Object> hmapScope = (HashMap<String, Object>) hmapResponse.get("scope");
        HashMap<String, Object> pagntData = new HashMap<>(6);
        hmapScope.put("pagnt_data", pagntData);
        
        /*
        boolean isPeriSynchro = synchroType instanceof PeriSynchro; 
        List<HashMap<String, String>> title = new ArrayList<>(5);
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Date"}));
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Login"}));
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Action"}));
        if(isPeriSynchro)
            title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"<i class=\"fas fa-sync-alt\"></i>Synchronisation"}));
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{""}));

        pagntData.put("head", title);
      */long[] nbrTotalLigne = new long[]{0};
        pagntData.put("body", synchroType.getRecentsStories(connection, pagntMaxSize, pagntStartNum, dateTimeStart, dateTimeEnd, nbrTotalLigne));
        pagntData.put("foot", getPagntFoot(pagntStartNum, pagntFootMaxSize, pagntMaxSize, nbrTotalLigne[0], SYNCHRO_NAME));
    }
    
    public static <SynchroType extends Synchro> HashMap<String, Object> pagine(String stationCibleType, String synchroName, long synchroId, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize, String strDateTimeStart, String strDateTimeEnd) throws Exception{
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        HashMap<String, Object> hmapScope = new HashMap<>(20);
        hmapResponse.put("scope", hmapScope);
        hmapScope.put("synchro_name", synchroName);
        String[] centSPeriSAndCentSStoryPeriSStoryClsName = null;
        SynchroType synchroType = null;
        Connection connection = null;
        TimeZone timeZone  = null;
        
        try {        
            try {
                centSPeriSAndCentSStoryPeriSStoryClsName = SynchronisationConfig.getCentSPeriSAndCentSStoryPeriSStoryClsName(synchroName); 
            } catch (SynchroNameNotFoundException e) {
                hmapResponse.put("error_message", "Element "+synchroName+" inconnu!");
                throw e;
            }
            
            if(Helper.centraleStationType.equals(stationCibleType)){
                try {
                    synchroType = ((Class<SynchroType>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[0])).newInstance();
                } catch (ClassNotFoundException e) {
                    hmapScope.put("error_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                    throw e;
                }

                try {
                    connection = Helper.getCentConn();
                    timeZone = TimeZone.getTimeZone(Helper.getCentPeriTimeZone(connection, Helper.centraleStationType)[0]);
                } catch (SQLException e) {
                    hmapScope.put("error_message", "Problème de connection en ligne.");
                    throw e;
                }catch(Exception e){
                    hmapScope.put("error_message", "Un problème est survenu <br> pendant la connection en ligne.<br>("+e.getMessage()+")");
                    throw e;
                }
            }else if(Helper.peripheriqueStationType.equals(stationCibleType)){
                try {
                    synchroType = ((Class<SynchroType>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[1])).newInstance();
                } catch (ClassNotFoundException e) {
                    hmapScope.put("error_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                    throw e;
                }
                try {
                    connection = Helper.getPeriConn();
                    timeZone = TimeZone.getTimeZone(Helper.getCentPeriTimeZone(connection, Helper.peripheriqueStationType)[1]);
                } catch (SQLException e) {
                    hmapScope.put("error_message", "problème de connection locale.");
                    throw e;
                }catch(Exception e){
                    hmapScope.put("error_message", "un problème est survenu <br> pendant la connection en locale.<br>("+e.getMessage()+")");
                    throw e;
                }
            }else{
                hmapResponse.put("error_message", "Indefined StationType ("+synchroName+")!");
                throw new IndefinedStaionTypeException();
            }
            /*code*/
                synchroType.setId(""+synchroId);
                Timestamp dateTimeStart = null;
                Timestamp dateTimeEnd = null;
                SimpleDateFormat sdfTimestampDisplay = SimpleDateFormatHelper.getSdfTimestampDisplay(timeZone, "yyyy-MM-dd'T'HH:mm");
                try {
                    Date date = sdfTimestampDisplay.parse(strDateTimeStart); 
                    dateTimeStart = new Timestamp(date.getYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds(), 0);
                } catch (Exception e) {}
                try {
                    Date date = sdfTimestampDisplay.parse(strDateTimeEnd); 
                    dateTimeEnd = new Timestamp(date.getYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds(), 0);
                } catch (Exception e) {}
                if(!Boolean.FALSE.equals(hmapResponse.get("status")))
                    pagine(synchroType, hmapResponse, connection, dateTimeStart, dateTimeEnd, pagntStartNum, pagntFootMaxSize, pagntMaxSize);
            /*code*/
            if(!hmapResponse.containsKey("status"))
                hmapResponse.put("status", true);        
        } catch (Exception e) {
           if(e instanceof InvocationTargetException) e = (Exception) e.getCause();
            hmapResponse.put("status", false);
            if(!hmapScope.containsKey("error_message"))
                hmapScope.put("error_message", "Un problème est survenu.<br>("+e.getMessage()+")");

//            throw e;//*******LLLLLLLL*******************
        }finally{
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(FunctionRead.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }return hmapResponse;
    }

}
