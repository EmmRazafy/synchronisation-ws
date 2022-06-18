/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisable;

import autreWs.convention_details.Factures;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import presynchronisation.PeriSation;
import synchronisable.annee_scolaire.AnneeScolaire;
import synchronisable.annee_scolaire.AnneeScolaireCentSynchro;
import synchronisable.annee_scolaire.AnneeScolairePeriSynchro;
import synchronisable.annee_scolaire.exception.AnneeScolaireExcetion;
import synchronisable.convention.Convention;
import synchronisable.exception.NullArgumentException;
import synchronisable.facture.Facture;
import synchronisable.facture.FactureCentSynchro;
import synchronisable.facture.FacturePeriSynchro;
import synchronisable.mtrl_cnsmble.Event;
import synchronisable.mtrl_cnsmble.EventCentSynchro;
import synchronisable.mtrl_cnsmble.EventPeriSynchro;
import synchronisable.mtrl_cnsmble.exception.QantiteInsuffisanteException;
import synchronisable.param.ParamCentSynchro;
import synchronisable.param.ParamPeriSynchro;
import synchronisable.param.UsefulParam;
import synchronisable.param.exception.ParamException;
import synchronisation.SynchronisationConfig;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;
import synchroniseur.exception.UIAlreadyExistsException;
import usefull.ArrayHelper;
import usefull.HashHelper;
import usefull.StringHelper;
import usefull.dao.Helper;
import usefull.dao.exception.PKNotFoundException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class FunctionCreate {
    
    public static <SynchroType extends Synchro> HashMap<String, Object> indexe(
            String synchroName, String stationCibleType,String userId,  
            String send, String[] orderedStringifiedIndexs, String[] orderedStringifiedDatas, HashMap<String, String> ...otherParams
    ) throws Exception {
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        HashMap<String, Object> hmapScope = new HashMap<>(50);
        hmapResponse.put("scope", hmapScope);
        String[] centSPeriSAndCentSStoryPeriSStoryClsName = null;
        SynchroType synchroType = null;
        Connection connection = null;
        String idStationSrc = null;
        HashMap<String, String> actionResult = null;
        try {
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
                    hmapResponse.put("status", true);
                }else{
                    hmapResponse.put("path_error", true);
                    throw new Exception("path_error");
                }
            } catch (Exception e) {
//                if(!hmapResponse.containsKey("status"))
                    hmapResponse.put("status", false);
                if(!(Boolean)hmapResponse.get("status")){//misy erreur
                    hmapScope.put("page_notification_class", "danger");
                    if(!hmapScope.containsKey("page_notification_title")){//default title
                        hmapScope.put("page_notification_title", "Erreur");
                    }
                }else
                    hmapScope.put("page_notification_class", "info");
//                throw e;//*******LLLLLLLL*******************
            }
            if(hmapResponse.containsKey("status")){
                if(!(Boolean)hmapResponse.get("status"))
                    return hmapResponse;
            }
            HashMap<String,Object> synchro = new HashMap<>(20);
            hmapScope.put("synchro", synchro);
            synchro.put("name", synchroName);
            
            /******************************pre_selected value***************************************************/
            String synchroNameToLowerCase = synchroName.toLowerCase();
            List<List<String>> allSynchroName = SynchronisationConfig.getAllSynchroName();
            String mtrlSimplToLowerCase = allSynchroName.get(3).get(0).toLowerCase();
            String ordiToLowerCase = allSynchroName.get(3).get(1).toLowerCase();
            String consommEvtToLowerCase = allSynchroName.get(3).get(4).toLowerCase();
            if((send != null) && (ArrayHelper.contains(synchroNameToLowerCase,  mtrlSimplToLowerCase, ordiToLowerCase, consommEvtToLowerCase))){
                boolean isConsommEvent = consommEvtToLowerCase.equals(synchroNameToLowerCase);
                int size = 2;
                if(!StringHelper.isEmpty(orderedStringifiedDatas[0])){                    
                    otherParams = new HashMap[]{HashHelper.newHashMap(new String[]{"type"}, new String[]{orderedStringifiedDatas[0]}, size)};
                    if(isConsommEvent && (!StringHelper.isEmpty(orderedStringifiedDatas[1])))
                        otherParams[0].put("lieu", orderedStringifiedDatas[1]);
                    else if((synchroNameToLowerCase.equals(mtrlSimplToLowerCase)) &&  (!StringHelper.isEmpty(orderedStringifiedDatas[3])))
                        otherParams[0].put("lieu", orderedStringifiedDatas[3]);
                    else if((synchroNameToLowerCase.equals(ordiToLowerCase)) &&  (!StringHelper.isEmpty(orderedStringifiedDatas[4])))
                        otherParams[0].put("lieu", orderedStringifiedDatas[4]);
                }
            }
            
            synchro.put("ordered_indexe", synchroType.getOrderedDisplayableIndexs(Synchro.CREATE_DISPLAY_TYPE, connection, stationCibleType, otherParams));
            synchro.put("ordered_data", synchroType.getOrderedDisplayableDatas(Synchro.CREATE_DISPLAY_TYPE, connection, stationCibleType, otherParams));
            /**convention_details**/
            if(Convention.isConventionDetail(synchroType)){
                HashMap<String, String>  otherParam = ((HashMap<String, String>[])otherParams)[0];
                try {
                    Factures.setEtablismtAndCvtionDataByConvtionIdWhenCreate(otherParam.get("convention_id"), stationCibleType, connection, hmapScope);                    
                }catch (PKNotFoundException e) {
                    hmapScope.put("page_notification_message", e.getMessage());
                    hmapScope.put("exit_bttn", "OK");
                    throw e;
                }
            }
            /**convention_details**/
            /******************************pre_selected value***************************************************/

            hmapResponse.put("status", true);
            if(send == null)
                return hmapResponse;
            if(!synchroType.hasIndex())orderedStringifiedIndexs = new String[0];//au lieur de null
            if(!synchroType.hasData())orderedStringifiedDatas = new String[0];//au lieur de null
            
            HashMap<String, Object> orderedDisplayableIndexs = (( HashMap<String,Object>)synchro.get("ordered_indexe"));
            HashMap<String, Object> orderedDisplayableDatas = (( HashMap<String,Object>)synchro.get("ordered_data"));
            
            orderedDisplayableIndexs.put("value", orderedStringifiedIndexs);
            orderedDisplayableDatas.put("value", orderedStringifiedDatas);
            
            
            int length = orderedStringifiedIndexs.length;//*****************************
            String[] orderedIndexsValidation = new String[length];
            String[] orderedIndexsError = new String[length];
            for(int i = 0; i < length; i++){
                try {
                    if(i == 0 && (synchroType instanceof FactureCentSynchro || synchroType instanceof FacturePeriSynchro))//identi
                        orderedStringifiedIndexs[i] = Facture.getAutoGeneratedIdentifiant(Integer.parseInt(idStationSrc), connection);
                    synchroType.setOrderedStringifiedIndexs(orderedStringifiedIndexs[i],i);
                    synchroType.setCurrentUpdatedOrderedIndexs(i,synchroType.getOrderedIndexs(Synchro.CREATE_DISPLAY_TYPE, connection, stationCibleType, otherParams)[i]);
                    orderedIndexsValidation[i] = "is-valid";
                } catch (Exception e) {
                    if(e instanceof InvocationTargetException) e = (Exception) e.getCause();
                    orderedIndexsError[i] = (StringHelper.isEmpty(e.getMessage()))? "Un problème est survenu.("+e.getClass().getName()+")" : e.getMessage();
                    orderedIndexsValidation[i] = "is-invalid";
                    hmapResponse.put("status", false);
                    if(actionResult == null)
                        actionResult = HashHelper.newHashMap(new String[]{"title", "bg_class", "detail"}, new String[]{"Échec de la Création!", "bg-danger", "Erreur de saisie"});
                }
            }
            orderedDisplayableIndexs.put("validation",orderedIndexsValidation);  
            if(hmapResponse.containsKey("status")){
                if(!(Boolean)hmapResponse.get("status")){//misy erreur
                    orderedDisplayableIndexs.put("error", orderedIndexsError);
                }
            }
            
            length = orderedStringifiedDatas.length;
            String[] orderedDatasValidation = new String[length];
            String[] orderedDatasError = new String[length];
                
                    
            for(int i = 0; i < length; i++){
                try {
                    synchroType.setOrderedStringifiedDatas(orderedStringifiedDatas[i],i);
                    synchroType.setCurrentUpdatedOrderedDatas(i, synchroType.getOrderedDatas(Synchro.CREATE_DISPLAY_TYPE, connection, stationCibleType, otherParams)[i]);
                    orderedDatasValidation[i] = "is-valid";
                    
                    //*******************efa avy nisetConventionId successfully****************************************
                    if(i == 0 && (synchroType instanceof FactureCentSynchro || synchroType instanceof FacturePeriSynchro)){
                        try {
                            FactureCentSynchro factureCentSynchro = (FactureCentSynchro)synchroType;
                            BigDecimal resteAPayer = null;
                            try {
                                resteAPayer = Convention.getResteApayer(connection, factureCentSynchro.getConventionId());
                            } catch (NullArgumentException |ParamException ex) {
                                hmapScope.put("page_notification_title", "Erreur.");
                                hmapScope.put("page_notification_message", ex.getMessage());
                                hmapScope.put("page_notification_class", "danger"); 
                                return hmapResponse;
                            }
                            try {
                                if(resteAPayer.compareTo(BigDecimal.ZERO)==0)
                                    throw new NullArgumentException("Le total des versments est déjà suffisant.");
                                if(resteAPayer.compareTo(BigDecimal.ZERO)<0)
                                    throw new NullArgumentException("Le total des versments est déjà plus que suffisant.");
                            } catch (NullArgumentException nullArgumentException) {
                                hmapScope.put("page_notification_title", "Désolé.");
                                hmapScope.put("page_notification_message", nullArgumentException.getMessage());
                                hmapScope.put("page_notification_class", "info");
                                return hmapResponse;
                            }
                            factureCentSynchro.setResteApayer(resteAPayer);
                        } catch (ClassCastException e) {
                            FacturePeriSynchro facturePeriSynchro = (FacturePeriSynchro)synchroType;
                            BigDecimal resteAPayer = null;
                            try {
                                resteAPayer = Convention.getResteApayer(connection, facturePeriSynchro.getConventionId());
                            } catch (NullArgumentException |ParamException ex) {
                                hmapScope.put("page_notification_title", "Erreur.");
                                hmapScope.put("page_notification_message", ex.getMessage());
                                hmapScope.put("page_notification_class", "danger"); 
                                return hmapResponse;
                            }
                            try {
                                if(resteAPayer.compareTo(BigDecimal.ZERO)==0)
                                    throw new NullArgumentException("Le total des versments est déjà suffisant.");
                                if(resteAPayer.compareTo(BigDecimal.ZERO)<0)
                                    throw new NullArgumentException("Le total des versments est déjà plus que suffisant.");
                            } catch (NullArgumentException nullArgumentException) {
                                hmapScope.put("page_notification_title", "Désolé.");
                                hmapScope.put("page_notification_message", nullArgumentException.getMessage());
                                hmapScope.put("page_notification_class", "info");
                                return hmapResponse;
                            }
                            facturePeriSynchro.setResteApayer(resteAPayer);
                        }
                    }
                } catch (Exception e) {
                    if(e instanceof InvocationTargetException) e = (Exception) e.getCause();
                    orderedDatasError[i] = (StringHelper.isEmpty(e.getMessage()))? "Un problème est survenu.("+e.getClass().getName()+")" : e.getMessage();
                    orderedDatasValidation[i] = "is-invalid";
                    hmapResponse.put("status", false);
                    if(actionResult == null)
                        actionResult = HashHelper.newHashMap(new String[]{"title", "bg_class", "detail"}, new String[]{"Échec de la Création!", "bg-danger", "Erreur de saisie"});
                }
            }
            orderedDisplayableDatas.put("validation",orderedDatasValidation);
            if(!hmapResponse.containsKey("status")){
                hmapResponse.put("status", true);
            }
            if(!(Boolean)hmapResponse.get("status")){//misy erreur
                orderedDisplayableDatas.put("error", orderedDatasError);
            }else{
                if(synchroType instanceof AnneeScolaireCentSynchro){
                    AnneeScolaireCentSynchro aS = (AnneeScolaireCentSynchro)synchroType;
                    AnneeScolaire.checkAmplitude(aS.getDebut(), aS.getFin());
                }else if(synchroType instanceof AnneeScolairePeriSynchro){
                    AnneeScolairePeriSynchro aS = (AnneeScolairePeriSynchro)synchroType;
                    AnneeScolaire.checkAmplitude(aS.getDebut(), aS.getFin());
                }
                Timestamp dateEdition = new Timestamp(new Date().getTime());
                String indexAlreadyExistErrMssg = "Les valeurs @ordered_indexValues sont déjà utilisés.".replaceFirst("@ordered_indexValues", String.join(", ", orderedStringifiedIndexs));
                
                /*******************************************ForConsommationEvent*******************************************/
                if((synchroType instanceof EventCentSynchro)||(synchroType instanceof EventPeriSynchro)){
                    Object eventInstance = (Helper.centraleStationType.equals(stationCibleType))? (EventCentSynchro)synchroType : (EventPeriSynchro)synchroType;
                    Event.cCEWhenCreate(connection, stationCibleType, eventInstance);
                }
                /*******************************************ForConsommationEvent*******************************************/
                
                synchroType.historisedSaveTransactly(connection, idStationSrc, dateEdition, userId, indexAlreadyExistErrMssg);

                //insérer avec success
                orderedDisplayableIndexs.remove("value");
                orderedDisplayableDatas.remove("value");
                orderedDisplayableIndexs.remove("validation");
                orderedDisplayableDatas.remove("validation");
                if(actionResult == null)
                    actionResult = HashHelper.newHashMap(new String[]{"title", "bg_class", "detail"}, new String[]{"Création Réussie!", "bg-success", "L'élément a été créé avec succès."});
            }       
        } catch (Exception e) {
            if(e instanceof InvocationTargetException) e = (Exception) e.getCause();
            String message = e.getMessage();
            if((e instanceof AnneeScolaireExcetion) || (e instanceof UIAlreadyExistsException) || (e instanceof QantiteInsuffisanteException)){
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
                actionResult = HashHelper.newHashMap(new String[]{"title", "bg_class", "detail"}, new String[]{"Échec de la Création!", "bg-danger", message});
            if(hmapScope.containsKey("page_notification_message")){
                if(!hmapScope.containsKey("page_notification_class"))
                    hmapScope.put("page_notification_class", "danger");
                if(!hmapScope.containsKey("page_notification_title"))//default title
                    hmapScope.put("page_notification_title", "Erreur");
            }
            hmapResponse.put("status", false);
                
            throw e;//*******LLLLLLLL*******************
        }finally{
            if(actionResult != null)
                hmapScope.put("action_result", actionResult);
            if(connection!=null){
                try {
                        connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(FunctionCreate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }return hmapResponse;
    }
}
