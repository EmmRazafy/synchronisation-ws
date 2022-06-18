/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisable.param;

import synchronisable.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import usefull.NumberFormatHelper;
import usefull.dao.Helper;
import usefull.dao.exception.IndefinedStaionTypeException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class FunctionRead {
    
    public static <SynchroType extends Synchro> HashMap<String, Object> indexe(String sationCibleType) throws Exception {
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        HashMap<String, Object> hmapScope = new HashMap<>(50);
        hmapResponse.put("scope", hmapScope);
        Connection connection = null;
        TimeZone timeZone  = null;
        
        try {        
            
            if(Helper.centraleStationType.equals(sationCibleType)){
                try {
                    connection = Helper.getCentConn();
                    timeZone = TimeZone.getTimeZone(Helper.getCentPeriTimeZone(connection, Helper.centraleStationType)[0]);
                } catch (SQLException e) {
                    hmapScope.put("page_notification_message", "problème de connection en ligne.");
                    throw e;
                }catch(Exception e){
                    hmapScope.put("page_notification_message", "un problème est survenu <br> pendant la connection en ligne.<br>("+e.getMessage()+")");
                    throw e;
                }
            }else if(Helper.peripheriqueStationType.equals(sationCibleType)){
                try {
                    connection = Helper.getPeriConn();
                    timeZone = TimeZone.getTimeZone(Helper.getCentPeriTimeZone(connection, Helper.peripheriqueStationType)[1]);
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
            hmapResponse.put("status", true);
            hmapScope.put("param", UsefulParam.getAllParamData(sationCibleType, connection, NumberFormatHelper.CURRENCY_CODE));
        } catch (Exception e) {
            hmapResponse.put("status", false);
            hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
            hmapScope.put("page_notification_class", "danger");
            if(!hmapScope.containsKey("page_notification_title")){//default title
                hmapScope.put("page_notification_title", "Erreur");
            }
//            throw e;
        }finally{
            if(connection!=null){
                try {
                        connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(FunctionRead.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return hmapResponse;
    }
}
