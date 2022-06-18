/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisable;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import synchronisation.SynchronisationConfig;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;
import usefull.StringHelper;
import usefull.dao.Helper;
import usefull.dao.exception.IndefinedStaionTypeException;
import usefull.dao.exception.PKNotFoundException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class ExportCSV {
    public static <SynchroType extends Synchro> HashMap<String, Object> getDatas(String stationCibleType, String synchroName, boolean isInDeleteConstraint, Long synchroId) throws Exception{
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        String[] centSPeriSAndCentSStoryPeriSStoryClsName = null;
        SynchroType synchroType = null;
        Connection connection = null;
        
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
                    hmapResponse.put("error_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                    throw e;
                }

                try {
                    connection = Helper.getCentConn();
                } catch (SQLException e) {
                    hmapResponse.put("error_message", "Problème de connection en ligne.");
                    throw e;
                }catch(Exception e){
                    hmapResponse.put("error_message", "Un problème est survenu <br> pendant la connection en ligne.<br>("+e.getMessage()+")");
                    throw e;
                }
            }else if(Helper.peripheriqueStationType.equals(stationCibleType)){
                try {
                    synchroType = ((Class<SynchroType>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[1])).newInstance();
                } catch (ClassNotFoundException e) {
                    hmapResponse.put("error_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                    throw e;
                }
                try {
                    connection = Helper.getPeriConn();
                } catch (SQLException e) {
                    hmapResponse.put("error_message", "problème de connection locale.");
                    throw e;
                }catch(Exception e){
                    hmapResponse.put("error_message", "un problème est survenu <br> pendant la connection en locale.<br>("+e.getMessage()+")");
                    throw e;
                }
            }else{
                hmapResponse.put("error_message", "Indefined StationType ("+synchroName+")!");
                throw new IndefinedStaionTypeException();
            }
            /*code*/
                if(synchroId == null){
                    String deletedConstraint = (isInDeleteConstraint)? " AND t_synchro_date_suppr IS NOT NULL " : " AND t_synchro_date_suppr IS NULL ";
                    hmapResponse.put("csv_data", synchroType.getAllCsVData(connection, deletedConstraint));
                }else{
                    synchroType = (SynchroType) synchroType.findByPk(synchroId.toString(), connection);
                    hmapResponse.put("csv_data", synchroType.getThisCsVData(connection));
                }
            /*code*/
            if(!hmapResponse.containsKey("status"))
                hmapResponse.put("status", true);        
        } catch (Exception e) {
           if(e instanceof InvocationTargetException) e = (Exception) e.getCause();
            hmapResponse.put("status", false);
            if(!hmapResponse.containsKey("error_message")){
                if(e instanceof PKNotFoundException){
                    hmapResponse.put("error_message", e.getMessage());
                }else{
                    hmapResponse.put("error_message", "Un problème est survenu.<br>("+StringHelper.coalesce(e.getMessage(), e.getClass().getName())+")");
                }
            }

            throw e;//*******LLLLLLLL*******************
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
