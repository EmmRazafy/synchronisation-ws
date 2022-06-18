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
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import synchronisable.admnstrtf.AdmnstrtfCentSynchro;
import synchronisable.admnstrtf.AdmnstrtfPeriSynchro;
import synchronisable.centrale.CentSynchro;
import synchronisable.centrale.CentSynchroStory;
import synchronisable.convention.ConventionCentSynchro;
import synchronisable.convention.ConventionPeriSynchro;
import synchronisable.convention_charge.ConventionChargeCentSynchro;
import synchronisable.convention_charge.ConventionChargePeriSynchro;
import synchronisable.facture.FactureCentSynchro;
import synchronisable.facture.FacturePeriSynchro;
import synchronisable.mtrl_cnsmble.EventCentSynchro;
import synchronisable.mtrl_cnsmble.EventPeriSynchro;
import synchronisable.mtrl_simple.MtrlSimple;
import synchronisable.mtrl_simple.MtrlSimpleCentSynchro;
import synchronisable.mtrl_simple.MtrlSimplePeriSynchro;
import synchronisable.param.exception.ParamException;
import synchronisable.peripherique.PeriSynchro;
import synchronisable.peripherique.PeriSynchroStory;
import synchronisation.SynchronisationConfig;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;
import usefull.HashHelper;
import usefull.SimpleDateFormatHelper;
import usefull.dao.Helper;
import usefull.dao.TimeZoneHelper;
import usefull.dao.exception.IndefinedStaionTypeException;
import usefull.dao.exception.PKNotFoundException;
import usefull.dao.exception.UnsupportedKeyTypeException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class FunctionRead {
    final static int RECENTS_STORIES_COUNT = 4;
    
    public static TimeZone getTimeZone(String sationCibleType, Connection connection) throws IndefinedStaionTypeException, UnsupportedKeyTypeException, SQLException, Exception{
        return TimeZoneHelper.getTimeZone(sationCibleType, connection);
    }
            
    public static <SynchroType extends Synchro> HashMap<String, Object> indexe(String synchroName, Object data, String stationCibleType, Long etablssmtId/*null by default*/, String ...synchroStoryId) throws Exception {
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        HashMap<String, Object> hmapScope = new HashMap<>(51);
        hmapResponse.put("scope", hmapScope);
        hmapScope.put("synchro_name", synchroName);
        String[] centSPeriSAndCentSStoryPeriSStoryClsName = null;
        SynchroType synchroType = null;
        Connection connection = null;
        TimeZone timeZone  = null;
        Class synchroCls =  null;
        boolean isInStoryContext = synchroStoryId.length != 0;
        hmapScope.put("is_in_story_context", isInStoryContext);
        try {        
            try {
                centSPeriSAndCentSStoryPeriSStoryClsName = SynchronisationConfig.getCentSPeriSAndCentSStoryPeriSStoryClsName(synchroName); 
            } catch (SynchroNameNotFoundException e) {
                hmapResponse.put("path_error", true);
                throw e;
            }
            
            if(Helper.centraleStationType.equals(stationCibleType)){
                try {
                    synchroType = ((Class<SynchroType>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[0])).newInstance();
                    synchroCls =  Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[0]);
                } catch (ClassNotFoundException e) {
                    hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                    throw e;
                }

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
            }else if(Helper.peripheriqueStationType.equals(stationCibleType)){
                try {
                    synchroType = ((Class<SynchroType>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[1])).newInstance();
                    synchroCls =  Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[1]);
                } catch (ClassNotFoundException e) {
                    hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                    throw e;
                }
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
            HashMap<String,Object> synchro = new HashMap<>(22);
            hmapScope.put("synchro", synchro);
            synchro.put("name", synchroName);
            if(data instanceof String){
                String synchroId = (String)data;
                synchro.put("id", synchroId);
                hmapScope.put("synchro_id",synchroId);
            }
            try {
                Class<SynchroType> synchroTypeCls =  (Class<SynchroType>) synchroType.getClass();
                if(!isInStoryContext){
                    synchroType = synchroType.findBy(data, connection, synchroTypeCls);
                }else{
                    String synchroId = (String)data;
                    if(Helper.centraleStationType.equals(stationCibleType)){
                        CentSynchroStory sStoryInst = new CentSynchroStory((CentSynchro) synchroType);
                        List<CentSynchroStory> orderedLastNotThrwsedNotNullValue = SynchroStory.getOrderedLastNotThrwsedNotNullValue(connection, sStoryInst, synchroId, synchroStoryId[0]);
                        if(orderedLastNotThrwsedNotNullValue.isEmpty())
                            throw new PKNotFoundException();
                        synchroType = (SynchroType) CentSynchroStory.getCentSynchroRestitution(synchroId, orderedLastNotThrwsedNotNullValue, synchroCls);
                    }else{
                        PeriSynchroStory sStoryInst = new PeriSynchroStory((PeriSynchro) synchroType);
                        List<PeriSynchroStory> orderedLastNotThrwsedNotNullValue = SynchroStory.getOrderedLastNotThrwsedNotNullValue(connection, sStoryInst, synchroId, synchroStoryId[0]);
                        if(orderedLastNotThrwsedNotNullValue.isEmpty())
                            throw new PKNotFoundException();
                        synchroType = (SynchroType) PeriSynchroStory.getPeriSynchroRestitution(synchroId, orderedLastNotThrwsedNotNullValue, synchroCls);
                    }synchroType = (SynchroType) synchroType.findWithAllForFindByElements(connection);
                }
                synchro.put("id", synchroType.getId());
                hmapScope.put("synchro_id",synchroType.getId());

                /*******************************************ForFindBy*******************************************/
                try {hmapScope.put("etablissmt",((AdmnstrtfCentSynchro)synchroType).getEtablissement());} catch (ClassCastException e) {try {hmapScope.put("etablissmt",((AdmnstrtfPeriSynchro)synchroType).getEtablissement());} catch (ClassCastException e2) {
                    try {hmapScope.put("etablissmt",((ConventionCentSynchro)synchroType).getEtablissement());}catch (ClassCastException e3) {try {hmapScope.put("etablissmt",((ConventionPeriSynchro)synchroType).getEtablissement());} catch (ClassCastException e4) {
                        try {hmapScope.put("type_mtrl",((MtrlSimpleCentSynchro)synchroType).getTypeMtrl());} catch (ClassCastException e5) {try {hmapScope.put("type_mtrl",((MtrlSimplePeriSynchro)synchroType).getTypeMtrl());} catch (ClassCastException e6) {
                            try {
                                EventCentSynchro event = (EventCentSynchro)synchroType;
                                hmapScope.put("type_mtrl",event.getTypeMtrl());hmapScope.put("etablissmt", event.getEtablissement());
                            } catch (ClassCastException e7) {
                                try {
                                    EventPeriSynchro event = (EventPeriSynchro)synchroType;
                                    hmapScope.put("type_mtrl",event.getTypeMtrl());hmapScope.put("etablissmt", event.getEtablissement());
                                } catch (ClassCastException e8) {
                                    /******************************************************convention_details*****Charge**************************/
                                    try {
                                        ConventionChargeCentSynchro event = (ConventionChargeCentSynchro)synchroType;
                                        hmapScope.put("convention",event.getConvention());hmapScope.put("etablissmt", event.getEtablissement());
                                    } catch (ClassCastException e9) {
                                        try {
                                            ConventionChargePeriSynchro event = (ConventionChargePeriSynchro)synchroType;
                                            hmapScope.put("convention",event.getConvention());hmapScope.put("etablissmt", event.getEtablissement());
                                        } catch (ClassCastException e10) {
                                            /***********************************************************Facture**************************/
                                            try {
                                                FactureCentSynchro event = (FactureCentSynchro)synchroType;
                                                hmapScope.put("convention",event.getConvention());hmapScope.put("etablissmt", event.getEtablissement());
                                            } catch (ClassCastException e11) {
                                                try {
                                                    FacturePeriSynchro event = (FacturePeriSynchro)synchroType;
                                                    hmapScope.put("convention",event.getConvention());hmapScope.put("etablissmt", event.getEtablissement());
                                                } catch (ClassCastException e12) {}
                                            }
                                            /***********************************************************Facture**************************/
                                        }
                                    }
                                    /******************************************************convention_details*************charge******************/
                                }
                            }
                        }}
                    }}
                }}
                /*******************************************ForFindBy*******************************************/
                /*********************is_in_etablissmt_context*********************/
                if((etablssmtId!= null) && (EventCentSynchro.class.isAssignableFrom(synchroTypeCls) || EventPeriSynchro.class.isAssignableFrom(synchroTypeCls) || (MtrlSimpleCentSynchro.class.isAssignableFrom(synchroTypeCls) || MtrlSimplePeriSynchro.class.isAssignableFrom(synchroTypeCls))) ){
                    if(!hmapScope.containsKey("etablissmt")) hmapScope.put("etablissmt", MtrlSimple.getLieuData(connection, etablssmtId));
                    if( (hmapScope.get("etablissmt") != null) && ( ((HashMap<String, Object>)hmapScope.get("etablissmt")).get("synchro_id") != null ) )
                        hmapScope.put("is_in_etablissmt_context", true);
                }
                /*********************is_in_etablissmt_context*********************/
                    
                
            }catch (PKNotFoundException e) {
                hmapScope.put("page_notification_message", "Element introuvable.");
                    hmapScope.put("exit_bttn", "OK");
                throw e;
            }
            synchro.put("ordered_indexe", synchroType.getOrderedDisplayableIndexs(Synchro.READ_DISPLAY_TYPE, connection, stationCibleType/*Helper.centraleStationType ou peri*/));
            synchro.put("ordered_data", synchroType.getOrderedDisplayableDatas(Synchro.READ_DISPLAY_TYPE, connection, stationCibleType));
            
            if(synchroType.getDateSuppr()!= null)
                synchro.put("date_suppr", SimpleDateFormatHelper.getSdfTimestampDisplay(timeZone).format(synchroType.getDateSuppr()));
            synchro.put("last_editor_email", synchroType.getUserEmail(connection));
            synchro.put("date_edition", SimpleDateFormatHelper.getSdfTimestampDisplay(timeZone).format(synchroType.getDateEdition()));
            if(synchroType instanceof PeriSynchro)
                synchro.put("synchronisation_state",((PeriSynchro)synchroType).getSynchronisationState());
            if(!isInStoryContext){
                synchro.put("recents_stories", 
                    HashHelper.newHashMap(new String[]{"count", "stories"}, 
                    new Object[]{RECENTS_STORIES_COUNT, synchroType.getRecentsStories(connection, RECENTS_STORIES_COUNT+1, null, null, null, new long[0])}
                ));
            }
            hmapResponse.put("status", true);
        } catch (Exception e) {
           if(e instanceof InvocationTargetException) e = (Exception) e.getCause();
            hmapResponse.put("status", false);
            if(!hmapScope.containsKey("page_notification_message")){
                if(e instanceof ParamException)
                    hmapScope.put("page_notification_message", e.getMessage());
                else
                    hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
            }
            hmapScope.put("page_notification_class", "danger");
            if(!hmapScope.containsKey("page_notification_title")){//default title
                hmapScope.put("page_notification_title", "Erreur");
            }

            throw e;//*******LLLLLLLL*******************
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
