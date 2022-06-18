/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import synchronisable.centrale.CentSynchro;
import synchronisable.peripherique.PeriSynchro;
import usefull.dao.exception.ConnectionError;
import synchroniseur.SynchroniseurManager;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;
import usefull.dao.Helper;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class Sychronisator {    
        public static <CentS extends CentSynchro, PeriS extends PeriSynchro> void synchroniser(
                List<List<String>> liAllSynchroName
    ) throws  SynchroNameNotFoundException, ClassNotFoundException, ConnectionError, Exception{
        synchroniser(null, null, liAllSynchroName);
    }
    
    
    public static <CentS extends CentSynchro, PeriS extends PeriSynchro> void synchroniser(
                Connection centConnection, Connection periConnection, List<List<String>> liAllSynchroName
    ) throws  SynchroNameNotFoundException, ClassNotFoundException, ConnectionError, Exception{
        int level_count = liAllSynchroName.size();
        Timestamp dateSynchronisation = new Timestamp(new Date().getTime());
        SynchroniseurManager<CentS, PeriS> synchroniseurInstance = null;
        try {
            
            for (int level = 0; level < level_count; level++) {
                List<String> synchroNames = liAllSynchroName.get(level);
                int synchroNamesUIInterferanceCount = 0;
                int  synchroNames_count = synchroNames.size();
                for (int synchroNameInd = 0; synchroNameInd < synchroNames_count; synchroNameInd++) {
                    String theSynchroName = synchroNames.get(synchroNameInd);
                    String[] centSPeriSAndCentSStoryPeriSStoryClsName = SynchronisationConfig.getCentSPeriSAndCentSStoryPeriSStoryClsName(theSynchroName); 
                    Class<CentS> centCls = (Class<CentS>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[0]);
                    Class<PeriS> periCls = (Class<PeriS>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[1]);
                    try {
                        Connection[] checkConnectiont = checkConnectiont(centConnection, periConnection);
                        centConnection = checkConnectiont[0]; periConnection = checkConnectiont[1];
                        if(synchroniseurInstance == null){
                            synchroniseurInstance = new SynchroniseurManager<>(
                                centConnection,
                                centCls,

                                periConnection,
                                periCls,

                                dateSynchronisation
                            );
                        }else{
                            synchroniseurInstance.setCentConnection(centConnection);synchroniseurInstance.setCentSCls(centCls);
                            synchroniseurInstance.setPeriConnection(periConnection);synchroniseurInstance.setPeriSCls(periCls);
                            synchroniseurInstance.setDateSynchronisation(dateSynchronisation);
                        }
                       long theSynchroNameUIInterferanceCount = synchroniseurInstance.synchroniserSynchronisableIdSynchro();
                       synchroNamesUIInterferanceCount += theSynchroNameUIInterferanceCount;
                       if(theSynchroNameUIInterferanceCount==0){
                            synchroNames.remove(synchroNameInd--);
                            synchroNames_count--;
                       }else{
                           synchroNames.set(synchroNameInd, theSynchroName + "::" + theSynchroNameUIInterferanceCount);
                       }
                    } catch (SQLException e) {
                        if(centConnection!=null){
                            try {
                                centConnection.close();
                            } catch (SQLException ex) {
                                Logger.getLogger(Sychronisator.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        if(periConnection!=null){
                            try {
                                periConnection.close();
                            } catch (SQLException ex) {
                                Logger.getLogger(Sychronisator.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        throw e;
                    }
                }
                if(synchroNamesUIInterferanceCount == 0){
                    liAllSynchroName.remove(level--);
                    level_count--;
                }else
                    break;//must synchronize all the last level before synchronizen the next
            }
            
        } catch (Exception e) {
            throw e;
        }finally{
            if(centConnection!=null){
                try {
                    centConnection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Sychronisator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(periConnection!=null){
                try {
                    periConnection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Sychronisator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
            
    public static  Connection[] checkConnectiont(Connection centConnection, Connection periConnection) throws Exception{
        boolean shouldReOpenCentConnection = (centConnection == null)? true : centConnection.isClosed();
        if(shouldReOpenCentConnection){
            try{
                centConnection = Helper.getCentConn();
            } catch (SQLException e) {
                throw new ConnectionError("problème de connection en ligne.<br>("+e.getMessage()+")", true);
            }catch(Exception e){
                throw new ConnectionError("un problème est survenu <br> pendant la connection en ligne.<br>("+e.getMessage()+")", false);
            }
        }
        boolean shouldReOpenPeriConnection = (periConnection == null)? true : periConnection.isClosed();
        if(shouldReOpenPeriConnection){
            try{
                periConnection = Helper.getPeriConn();
            } catch (SQLException e) {
                throw new ConnectionError("problème de connection en locale.<br>("+e.getMessage()+")");
            }catch(Exception e){
                throw new ConnectionError("un problème est survenu <br> pendant la connection en locale.<br>("+e.getMessage()+")");
            }
        }return new Connection[]{centConnection, periConnection};
    }
}
