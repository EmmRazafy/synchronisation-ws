/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisable;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import presynchronisation.PeriSation;
import synchronisable.admnstrtf.AdmnstrtfCentSynchro;
import synchronisable.admnstrtf.AdmnstrtfPeriSynchro;
import synchronisable.annee_scolaire.AnneeScolaire;
import synchronisable.annee_scolaire.AnneeScolaireCentSynchro;
import synchronisable.annee_scolaire.AnneeScolairePeriSynchro;
import synchronisable.annee_scolaire.exception.AnneeScolaireExcetion;
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
import synchronisable.param.ParamCentSynchro;
import synchronisable.param.ParamPeriSynchro;
import synchronisable.param.UsefulParam;
import synchronisation.SynchronisationConfig;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;
import synchroniseur.exception.UIAlreadyExistsException;
import usefull.ArrayHelper;
import usefull.HashHelper;
import usefull.StringHelper;
import usefull.dao.CRUD;
import usefull.dao.Helper;
import usefull.dao.exception.PKNotFoundException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class FunctionUpdate {
    
    public static <SynchroType extends Synchro> HashMap<String, Object> indexe(
            String synchroName, String synchroId, String stationCibleType,String userId,  
            String send, String[] orderedStringifiedIndexs, String[] orderedStringifiedDatas, 
            Long etablssmtId/*null by default*/,
            String[] isUpdatedOrderedIndexs, String[] isUpdatedOrderedDatas,
            HashMap<String, String> ...otherParams
    ) throws Exception {
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        HashMap<String, Object> hmapScope = new HashMap<>(50);
        hmapResponse.put("scope", hmapScope);
        String[] centSPeriSAndCentSStoryPeriSStoryClsName = null;
        SynchroType synchroType = null;
        Connection connection = null;
        String idStationSrc = null;
        HashMap<String,Object> synchro = null;
        HashMap<String, String> actionResult = null;
        try {
            /*******************************************ForConsommationEvent*******************************************/
            EventCentSynchro eventCentSynchro = null;EventPeriSynchro eventPeriSynchro = null;
            String typeMtrlIdInitiale = null;String lieuIdInitiale = null;String eventTypeInitiale = null;BigDecimal qtteInitiale = null;
            Object[][] typeMtrlOptionValue = null;Object[][] lieuAffectOptionValue = null;
            /*******************************************ForConsommationEvent*******************************************/

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
                    throw new Exception("path_error");
                }
                synchro = new HashMap<>(20);
                hmapScope.put("synchro", synchro);
                synchro.put("name", synchroName);
                synchro.put("id", synchroId);
                hmapScope.put("synchro_id",synchroId);
                try {
                    Class<SynchroType> synchroTypeCls =  (Class<SynchroType>) synchroType.getClass();
                    
                    
                    
                    synchroType = synchroType.findBy(synchroId, connection, synchroTypeCls);
                    
                    /*******************************************ForConsommationEvent*******************************************/
                    if((synchroType instanceof EventCentSynchro)||(synchroType instanceof EventPeriSynchro)){
                        if(Helper.centraleStationType.equals(stationCibleType)){
                            eventCentSynchro = (EventCentSynchro)synchroType;
                            typeMtrlIdInitiale = eventCentSynchro.getTypeMtrlId(); lieuIdInitiale = eventCentSynchro.getLieuAffectationId(); eventTypeInitiale = eventCentSynchro.getEnventType(); qtteInitiale = eventCentSynchro.getQtt();
                        }else{
                            eventPeriSynchro = (EventPeriSynchro)synchroType;
                            typeMtrlIdInitiale = eventPeriSynchro.getTypeMtrlId(); lieuIdInitiale = eventPeriSynchro.getLieuAffectationId(); eventTypeInitiale = eventPeriSynchro.getEnventType(); qtteInitiale = eventPeriSynchro.getQtt();
                        }                        
                    }
                    /*******************************************ForConsommationEvent*******************************************/
                
                    
                    
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
                
                    synchroType.checKIfSynchroIsNotDeleted();
                }catch (PKNotFoundException e) {
                    hmapScope.put("page_notification_message", "Element introuvable.");
                    throw e;
                }catch(InsertStoryOnSynchroAlreadyDeletedException e){
                    hmapScope.put("page_notification_message", e.getMessage());
                    throw e;
                }
            }catch (Exception e) {
                hmapScope.put("page_notification_class", "danger");
                if(!hmapScope.containsKey("page_notification_title")){//default title
                    hmapScope.put("page_notification_title", "Erreur");
                }
                throw e;
            }
            Object[] displayTypeAndConnectionAndSationCibleTypeAndOtherParams = ((otherParams.length>0)&&(! otherParams[0].isEmpty()))? new Object[]{Synchro.UPDATE_DISPLAY_TYPE, connection, stationCibleType, otherParams}: new Object[]{Synchro.UPDATE_DISPLAY_TYPE, connection, stationCibleType};
            HashMap<String, Object> orderedDisplayableIndexs = synchroType.getOrderedDisplayableIndexs(displayTypeAndConnectionAndSationCibleTypeAndOtherParams);
            HashMap<String, Object> orderedDisplayableDatas = synchroType.getOrderedDisplayableDatas(displayTypeAndConnectionAndSationCibleTypeAndOtherParams);
            
            /*******************************************ForConsommationEvent*******************************************/
            if((synchroType instanceof EventCentSynchro) || (synchroType instanceof EventPeriSynchro)){
                Object[][][] selectOptionValueTitle = (Object[][][])orderedDisplayableDatas.get("select_option_value_title");
                typeMtrlOptionValue = selectOptionValueTitle[0];
                lieuAffectOptionValue = selectOptionValueTitle[1];
            }
            /*******************************************ForConsommationEvent*******************************************/

            synchro.put("ordered_indexe", orderedDisplayableIndexs);
            synchro.put("ordered_data", orderedDisplayableDatas);
            hmapResponse.put("status", true);
            if(send == null)
                return hmapResponse;
            if(!synchroType.hasIndex())orderedStringifiedIndexs = new String[0];//au lieur de null
            if(!synchroType.hasData())orderedStringifiedDatas = new String[0];//au lieur de null
            
            orderedDisplayableIndexs.put("value", orderedStringifiedIndexs);
            orderedDisplayableDatas.put("value", orderedStringifiedDatas);
            boolean  updateExists = false;
                    
            //*****************************
            int length = orderedStringifiedIndexs.length;
            synchroType.setLastOrderedIndexs(CRUD.serialize(synchroType.getOrderedIndexs(Synchro.UPDATE_DISPLAY_TYPE, connection, stationCibleType)));
            synchroType.setCurrentUpdatedOrderedIndexs(new String[length]);
            String[] orderedIndexsError = new String[length];
            String[] orderedIndexsValidation = new String[length];
            for (int i = 0; i < length; i++){
                if(!"true".equals(isUpdatedOrderedIndexs[i]))continue;
                try {
                    synchroType.setOrderedStringifiedIndexs(orderedStringifiedIndexs[i],i);
                    if(synchroType.setCurrentUpdatedOrderedIndexs(i,synchroType.getOrderedIndexs(Synchro.UPDATE_DISPLAY_TYPE, connection, stationCibleType)[i])){
                        updateExists = true;
                        orderedIndexsValidation[i] = "is-valid";
                    }else{
                        orderedIndexsValidation[i] = "";
                    }
                } catch (Exception e) {
                    if(e instanceof InvocationTargetException) e = (Exception) e.getCause();
                    orderedIndexsError[i] = (StringHelper.isEmpty(e.getMessage()))? "Un problème est survenu.("+e.getClass().getName()+")" : e.getMessage();
                    orderedIndexsValidation[i] = "is-invalid";
                    hmapResponse.put("status", false);
                    if(actionResult == null)
                        actionResult = HashHelper.newHashMap(new String[]{"title", "bg_class", "detail"}, new String[]{"Échec de la Modification!", "bg-danger", "Erreur de saisie"});
                }
                
            }
            orderedDisplayableIndexs.put("validation",orderedIndexsValidation);
            if(hmapResponse.containsKey("status")){
                if(!(Boolean)hmapResponse.get("status")){//misy erreur
                    orderedDisplayableIndexs.put("error", orderedIndexsError);
                }
            }            
        
            length = orderedStringifiedDatas.length;
            synchroType.setLastOrderedDatas(CRUD.serialize(synchroType.getOrderedDatas(Synchro.UPDATE_DISPLAY_TYPE, connection, stationCibleType)));
            synchroType.setCurrentUpdatedOrderedDatas(new String[length]);
            String[] orderedDatasError = new String[length];
            String[] orderedDatasValidation = new String[length];
            boolean isSepecialSituation = false;
            for (int i = 0; i < length; i++){
                if( !("true".equals(isUpdatedOrderedDatas[i]) || isSepecialSituation) )continue;
                try {
                    synchroType.setOrderedStringifiedDatas(orderedStringifiedDatas[i],i);
                    if(synchroType.setCurrentUpdatedOrderedDatas(i,synchroType.getOrderedDatas(Synchro.UPDATE_DISPLAY_TYPE, connection, stationCibleType)[i])){
                        updateExists = true;
                        orderedDatasValidation[i] = "is-valid";
                    }else{
                        orderedDatasValidation[i] = "";
                    }
                    if( ((synchroType instanceof AdmnstrtfCentSynchro) || (synchroType instanceof AdmnstrtfPeriSynchro)) && ArrayHelper.contains(i, 0,6,12,18,24) )//setGenre
                        isSepecialSituation = true;// should setNom
                    else if( ((synchroType instanceof FactureCentSynchro) || (synchroType instanceof FacturePeriSynchro)) && (i==5) )//setFactAvrMtt
                        isSepecialSituation = true;// should setFactAvrDesc
                    else isSepecialSituation = false;
                    
                } catch (Exception e) {
                    if(e instanceof InvocationTargetException) e = (Exception) e.getCause();
                    orderedDatasError[i] = (StringHelper.isEmpty(e.getMessage()))? "Un problème est survenu.("+e.getClass().getName()+")" : e.getMessage();
                    orderedDatasValidation[i] = "is-invalid";
                    hmapResponse.put("status", false);
                    if(actionResult == null)
                        actionResult = HashHelper.newHashMap(new String[]{"title", "bg_class", "detail"}, new String[]{"Échec de la Modification!", "bg-danger", "Erreur de saisie"});

                    isSepecialSituation = false;
                }
            }
            orderedDisplayableDatas.put("validation",orderedDatasValidation);            
            if(!(Boolean)hmapResponse.get("status")){//misy erreur
                orderedDisplayableDatas.put("error", orderedDatasError);
            }else if(!updateExists){
                hmapScope.put("form_error", "Erreur: vous n'avez rien modifier.");
                hmapResponse.put("status", false);
                if(actionResult == null)
                    actionResult = HashHelper.newHashMap(new String[]{"title", "bg_class", "detail"}, new String[]{"Échec de la Modification!", "bg-warning", "Vous n'avez rien modifier."});
            }else{
                if(synchroType instanceof AnneeScolaireCentSynchro){
                    AnneeScolaireCentSynchro aS = (AnneeScolaireCentSynchro)synchroType;
                    AnneeScolaire.checkAmplitude(aS.getDebut(), aS.getFin());
                }else if(synchroType instanceof AnneeScolairePeriSynchro){
                    AnneeScolairePeriSynchro aS = (AnneeScolairePeriSynchro)synchroType;
                    AnneeScolaire.checkAmplitude(aS.getDebut(), aS.getFin());
                }

                Timestamp dateEdition = new Timestamp(new Date().getTime());
                String indexAlreadyExistErrMssg = "Les valeurs @ordered_indexValues sont déjà utilisé.".replaceFirst("@ordered_indexValues", String.join(", ", orderedStringifiedIndexs));
                
                /*******************************************ForConsommationEvent*******************************************/
                    String typeMtrlIdFinale;String lieuIdFinale;String eventTypeFinale;BigDecimal qtteFinale;
                    if((synchroType instanceof EventCentSynchro)||(synchroType instanceof EventPeriSynchro)){
                        if(Helper.centraleStationType.equals(stationCibleType)){
                            typeMtrlIdFinale = eventCentSynchro.getTypeMtrlId(); lieuIdFinale = eventCentSynchro.getLieuAffectationId(); eventTypeFinale = eventCentSynchro.getEnventType(); qtteFinale = eventCentSynchro.getQtt();
                        }else{
                            typeMtrlIdFinale = eventPeriSynchro.getTypeMtrlId(); lieuIdFinale = eventPeriSynchro.getLieuAffectationId(); eventTypeFinale = eventPeriSynchro.getEnventType(); qtteFinale = eventPeriSynchro.getQtt();
                        }
                        Event.cCEWhenUpdate(connection, stationCibleType, typeMtrlIdInitiale, typeMtrlIdFinale, lieuIdInitiale, lieuIdFinale, eventTypeInitiale, eventTypeFinale, qtteInitiale, qtteFinale, typeMtrlOptionValue, lieuAffectOptionValue);
                    }
                /*******************************************ForConsommationEvent*******************************************/
                
                synchroType.historisedSaveTransactly(connection, idStationSrc, dateEdition, userId, indexAlreadyExistErrMssg);
                //modifié avec success
                //reinitialisation
                String[] orderedIndexs = CRUD.serialize(synchroType.getOrderedIndexs(Synchro.UPDATE_DISPLAY_TYPE, connection, stationCibleType));
                orderedDisplayableIndexs.put("value", orderedIndexs);//tsy mila stringifiena fa tsy date/tmstmp(efa format printable)
                orderedDisplayableIndexs.put("initial_value", orderedIndexs);        
                orderedDisplayableIndexs.put("last_value", synchroType.getLastOrderedIndexs()); //LastOrderedIndexs: déjà m-a-j pendat le historisedSaveTransactly
                
                String[] orderedDatas = CRUD.serialize(synchroType.getOrderedDatas(Synchro.UPDATE_DISPLAY_TYPE, connection, stationCibleType));
                orderedDisplayableDatas.put("value", orderedDatas);//tsy mila stringifiena fa tsy date/tmstmp(efa format printable)
                orderedDisplayableDatas.put("initial_value", orderedDatas);        
                orderedDisplayableDatas.put("last_value", synchroType.getLastOrderedDatas());  //LastOrderedDatas: déjà m-a-j pendat le historisedSaveTransactly
                if(actionResult == null)
                    actionResult = HashHelper.newHashMap(new String[]{"title", "bg_class", "detail"}, new String[]{"Modification Réussie!", "bg-success", "L'élément a été modifié avec succès."});
            }
        } catch (Exception e) {
            if(e instanceof InvocationTargetException) e = (Exception) e.getCause();
            hmapResponse.put("status", false);
            if(!hmapScope.containsKey("page_notification_message")){
                String message = e.getMessage();
                if((e instanceof AnneeScolaireExcetion) || (e instanceof UIAlreadyExistsException) || (e instanceof QantiteInsuffisanteException) || (e instanceof InsertStoryOnSynchroAlreadyDeletedException)){
                    if(UsefulParam.PSEUDO_PARAMETRE.get(UsefulParam.PSEUDO_PARAMETRE.size()-1).equals(synchroName))//Paym_Min
                        message = "Le payment minimum éxiste déjà!";
                    else if(UsefulParam.PSEUDO_PARAMETRE.contains(synchroName)){
                        String name = "";String unite =  "élève";
                        if(UsefulParam.PSEUDO_PARAMETRE.get(0).equals(synchroName)){/*PrixAccompagnementAnnuel*/
                            name = "Le montant annuel de l'accompagnement";
                        }else if(UsefulParam.PSEUDO_PARAMETRE.get(1).equals(synchroName)){/*PrixUnitaireOrdiServeur*/
                            name = "Le montant annuel par serveur";
                        }else if(UsefulParam.PSEUDO_PARAMETRE.get(2).equals(synchroName)){/*ReductionUnitaireOrdiClient*/
                            name = "Le montant de la réduction par ordinateur (client) manquant"; unite =  "appareil";
                        }else if(UsefulParam.PSEUDO_PARAMETRE.get(3).equals(synchroName)){/*ReductionUnitaireOrdiServeur*/
                            name = "Le montant de la réduction par serveur manquant"; unite =  "appareil";
                        }
                        BigDecimal minimum = (stationCibleType.equals(Helper.centraleStationType))? ((ParamCentSynchro)synchroType).getMin(): ((ParamPeriSynchro)synchroType).getMin();
                        message = ( minimum == null )? name+"<br> par défaut éxiste déjà.": name+"<br> pour moins de "+minimum.longValue()+" "+unite+"(s) éxiste déjà";                    
                    }
                    hmapScope.put("form_error", message);
                }else{
                    message = "Un problème est survenu.<br>("+e.getMessage()+")";
                    hmapScope.put("form_error", message);
                }
                if(actionResult == null)
                    actionResult = HashHelper.newHashMap(new String[]{"title", "bg_class", "detail"}, new String[]{"Échec de la Modification!", "bg-danger", message});
            }else
                hmapScope.put("exit_bttn", "OK");
//            throw e;//*******LLLLLLLL*******************
        }finally{
            if(actionResult != null)
                hmapScope.put("action_result", actionResult);
            if(connection!=null){
                try {
                        connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(FunctionUpdate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }return hmapResponse;
    }
}
