/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import presynchronisation.Initiateur;
import synchronisable.centrale.CentSynchro;
import synchronisable.param.UsefulParam;
import synchronisable.peripherique.PeriSynchro;
import static synchronisation.Sychronisator.checkConnectiont;
import usefull.dao.exception.ConnectionError;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class Function {
    public static <CentS extends CentSynchro, PeriS extends PeriSynchro> HashMap<String, Object> synchroniser(
                List<List<String>> liAllSynchroName
    ) throws Exception{
        if(liAllSynchroName == null){
            liAllSynchroName = SynchronisationConfig.getAllSynchroName();
        }
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        HashMap<String, Object> hmapScope = new HashMap<>(20);
        hmapResponse.put("scope", hmapScope);
        hmapScope.put("all_synchro_name", liAllSynchroName);
        try {
            try {
                liAllSynchroName.get(0).removeAll(UsefulParam.PSEUDO_PARAMETRE);
                liAllSynchroName.get(2).removeAll(SynchronisationConfig.SYNCHRO_NAME_LEVEL_2_PSEUDO_PARAMETRE);
                Connection centConnection = null; Connection periConnection = null;
                try {
                    try {
                        Connection[] checkConnectiont = checkConnectiont(centConnection, periConnection);
                        centConnection = checkConnectiont[0]; periConnection = checkConnectiont[1];
                    } catch (Exception e) {
                        hmapScope.put("page_notification_message", e.getMessage());
                        throw e;
                    }
                    Initiateur.initiateur(centConnection, periConnection);
                    Sychronisator.synchroniser(centConnection, periConnection, liAllSynchroName);
                } catch (Exception e) {
                    throw e;
                }finally{
                    if((centConnection!=null) && (!centConnection.isClosed())){
                        try {
                            centConnection.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(Sychronisator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if((periConnection!=null) && (!periConnection.isClosed())){
                        try {
                            periConnection.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(Sychronisator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                if(liAllSynchroName.isEmpty()){//synchronisation succed
                    hmapScope.put("page_notification_title", "Terminée");
                    hmapScope.put("page_notification_message", "Synchronisation terminée avec succès.");
                    hmapScope.put("exit_bttn", "OK");
                }
                hmapResponse.put("status", true);
            } catch (SynchroNameNotFoundException  e) {
                hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                throw e;
            }catch (ClassNotFoundException e) {
                hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                throw e;
            }catch(ConnectionError e){
                hmapScope.put("page_notification_message", e.getMessage());
                if(e.isRetry_possiblility()){
                    hmapScope.put("retry_bttn", "RECOMMENCER");
                }
                throw e;
            }
        } catch (Exception e) {
            if(!(hmapScope.containsKey("page_notification_message"))){
                    hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
            }
            hmapResponse.put("status", false);
            //throw e;
        }
        boolean status = true;
        if(hmapResponse.containsKey("status")){
            status = (Boolean)hmapResponse.get("status");//false
        }
        if(!status){//misy erreur
            hmapScope.put("page_notification_class", "danger");
            if(!hmapScope.containsKey("page_notification_title")){//default title
                hmapScope.put("page_notification_title", "Erreur");
            }
        }else
            hmapScope.put("page_notification_class", "info");
        hmapScope.put("exit_bttn", "OK");
        return hmapResponse;
    }

}
