/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import presynchronisation.PeriSation;
import synchronisable.admnstrtf.AdmnstrtfCentSynchro;
import synchronisable.admnstrtf.AdmnstrtfPeriSynchro;
import synchronisable.convention.ConventionCentSynchro;
import synchronisable.convention.ConventionPeriSynchro;
import synchronisable.convention_charge.ConventionChargeCentSynchro;
import synchronisable.convention_charge.ConventionChargePeriSynchro;
import synchronisable.exception.InsertStoryOnSynchroAlreadyDeletedException;
import synchronisable.facture.FactureCentSynchro;
import synchronisable.facture.FacturePeriSynchro;
import synchronisable.mtrl_cnsmble.Event;
import synchronisable.mtrl_cnsmble.EventCentSynchro;
import synchronisable.mtrl_cnsmble.EventPeriSynchro;
import synchronisable.mtrl_cnsmble.exception.QantiteInsuffisanteException;
import synchronisable.mtrl_simple.MtrlSimple;
import synchronisable.mtrl_simple.MtrlSimpleCentSynchro;
import synchronisable.mtrl_simple.MtrlSimplePeriSynchro;
import synchronisable.peripherique.PeriSynchro;
import synchronisation.SynchronisationConfig;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;
import synchroniseur.exception.UIAlreadyExistsException;
import usefull.ArrayHelper;
import usefull.HashHelper;
import static usefull.SimpleDateFormatHelper.getSdfTimestampDisplay;
import usefull.dao.Helper;
import usefull.dao.exception.IndefinedStaionTypeException;
import usefull.dao.exception.PKNotFoundException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class FunctionDelete {
    
    public static <SynchroType extends Synchro> HashMap<String, Object> indexe(
            String synchroName, String synchroId, String stationCibleType,String userId,  Long etablssmtId/*null by default*/
    ) throws Exception {
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        HashMap<String, Object> hmapScope = new HashMap<>(50);
        hmapResponse.put("scope", hmapScope);
        String[] centSPeriSAndCentSStoryPeriSStoryClsName = null;
        SynchroType synchroType = null;
        Connection connection = null;
        String idStationSrc = null;
        hmapScope.put("is_in_story_context", false);
        try {        
            try {
                if(ArrayHelper.contains(synchroName, SynchronisationConfig.getAllSynchroName().get(3).get(2)))/*administrtf tsy afaka deletena solo*/
                    throw new SynchroNameNotFoundException(synchroName+" ne peut pas être supprimer!");
                centSPeriSAndCentSStoryPeriSStoryClsName = SynchronisationConfig.getCentSPeriSAndCentSStoryPeriSStoryClsName(synchroName); 
            } catch (SynchroNameNotFoundException e) {
                hmapResponse.put("path_error", true);
                throw e;
            }

            if(Helper.centraleStationType.equals(stationCibleType)){
                try {
                    synchroType = ((Class<SynchroType>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[0])).newInstance();
                } catch (ClassNotFoundException e) {
                    hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                    throw e;
                }

                try {
                    connection = Helper.getCentConn();
                    idStationSrc = PeriSation.getCentId(connection, PeriSation.CENT_STATION_NOM);
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
                } catch (ClassNotFoundException e) {
                    hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                    throw e;
                }
                try {
                    connection = Helper.getPeriConn();
                    idStationSrc = Helper.getPERI_ID(connection);
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
            HashMap<String,Object> synchro = new HashMap<>(20);
            hmapScope.put("synchro", synchro);
            synchro.put("name", synchroName);
            synchro.put("id", synchroId);
            hmapScope.put("synchro_id",synchroId);
            
            try {
                Class<SynchroType> synchroTypeCls =  (Class<SynchroType>) synchroType.getClass();
                synchroType = synchroType.findBy(synchroId, connection, synchroTypeCls);
                
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
                throw e;
            }
            Timestamp dateEdition = new Timestamp(new Date().getTime());
            String indexAlreadyExistErrMssg = "";
            try {
                synchroType.checKIfSynchroIsNotDeleted();
            } catch(InsertStoryOnSynchroAlreadyDeletedException e) {
                hmapScope.put("page_notification_message", e.getMessage());
                throw e;
            }
            synchroType.setDateSuppr(dateEdition);
                            
            /*******************************************ForConsommationEvent*******************************************/
            if((synchroType instanceof EventCentSynchro)||(synchroType instanceof EventPeriSynchro)){
                Object eventInstance = (Helper.centraleStationType.equals(stationCibleType))? (EventCentSynchro)synchroType : (EventPeriSynchro)synchroType;
                Event.cCEWhenDelete(connection, stationCibleType, eventInstance);
            }
            /*******************************************ForConsommationEvent*******************************************/
                
            synchroType.historisedSaveTransactly(connection, idStationSrc, dateEdition, userId, indexAlreadyExistErrMssg);
            
            TimeZone timeZone = FunctionRead.getTimeZone(stationCibleType, connection);
            synchro.put("ordered_indexe", synchroType.getOrderedDisplayableIndexs(Synchro.READ_DISPLAY_TYPE, connection, stationCibleType));
            synchro.put("ordered_data", synchroType.getOrderedDisplayableDatas(Synchro.READ_DISPLAY_TYPE, connection, stationCibleType));
            if(synchroType.getDateSuppr()!= null)
                synchro.put("date_suppr", getSdfTimestampDisplay(timeZone).format(synchroType.getDateSuppr()));
            synchro.put("last_editor_email", synchroType.getUserEmail(connection));
            synchro.put("date_edition", getSdfTimestampDisplay(timeZone).format(synchroType.getDateEdition()));
            if(synchroType instanceof PeriSynchro)
                synchro.put("synchronisation_state",((PeriSynchro)synchroType).getSynchronisationState());
            /*historique*/
            synchro.put("recents_stories", 
                HashHelper.newHashMap(new String[]{"count", "stories"}, 
                new Object[]{FunctionRead.RECENTS_STORIES_COUNT, synchroType.getRecentsStories(connection, FunctionRead.RECENTS_STORIES_COUNT+1, null, null, null, new long[0])}
            ));
            hmapResponse.put("status", true);
            //supprimé avec success    
            hmapScope.put("action_result", HashHelper.newHashMap(new String[]{"title", "bg_class", "detail"}, new String[]{"Suppression Réussie!", "bg-success", "L'élément a été supprimé avec succès."}));
        } catch (Exception e) {
            hmapResponse.put("status", false);
            if(!hmapScope.containsKey("page_notification_message")){
                String message = ((e instanceof UIAlreadyExistsException) || (e instanceof QantiteInsuffisanteException) || (e instanceof InsertStoryOnSynchroAlreadyDeletedException))
                                    ? e.getMessage() :  "Un problème est survenu.<br>("+e.getMessage()+")";
                hmapScope.put("page_notification_message", message);
            }
            if(!(Boolean)hmapResponse.get("status")){//misy erreur
                hmapScope.put("page_notification_class", "danger");
                if(!hmapScope.containsKey("page_notification_title")){//default title
                    hmapScope.put("page_notification_title", "Erreur");
                }
            }else
                hmapScope.put("page_notification_class", "info");
            hmapScope.put("exit_bttn", "OK");
//            throw e;//*******LLLLLLLL*******************
        }finally{
            if(connection!=null){
                try {
                        connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(FunctionDelete.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }return hmapResponse;
    }
}
