/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisation.u_i_conflict.solve;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import synchronisable.Synchro;
import synchronisable.SynchroStory;
import synchronisable.annee_scolaire.AnneeScolaire;
import synchronisable.annee_scolaire.AnneeScolaireCentSynchro;
import synchronisable.annee_scolaire.AnneeScolairePeriSynchro;
import synchronisable.annee_scolaire.exception.AnneeScolaireExcetion;
import synchronisable.centrale.CentSynchro;
import synchronisable.centrale.CentSynchroStory;
import synchronisable.etat_mtrl.EtatMtrlCentSynchro;
import synchronisable.etat_mtrl.EtatMtrlPeriSynchro;
import synchronisable.peripherique.PeriSynchro;
import synchronisable.peripherique.PeriSynchroStory;
import synchronisation.SynchronisationConfig;
import synchroniseur.SynchroniseurManager;
import synchroniseur.exception.UIAlreadyExistsException;
import synchroniseur.uiconflict_option.UIConflictOption;
import synchroniseur.uiconflict_option.UIConflictOptionDelete;
import synchroniseur.uiconflict_option.UIConflictOptionFusion;
import synchroniseur.uiconflict_option.UIConflictOptionUpdate;
import synchronisation.u_i_conflict.solve.exception.ConflictNotFoundException;
import synchronisation.u_i_conflict.solve.exception.ConflictUpdateException;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;
import synchroniseur.SynchroniseurUpdateManager;
import usefull.dao.CRUD;
import usefull.dao.Helper;
import usefull.dao.Key;
import usefull.dao.exception.PKNotFoundException;
import usefull.dao.exception.UnsupportedKeyTypeException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class Function {
    public static <PeriS extends PeriSynchro,CentS extends CentSynchro> Object[] getCentSPeriSToSolve(
                Connection centConnection, Connection periConnection,
                Class<CentS>  centCls, Class<PeriS> periCls, 
                String periId,
                Timestamp dateSynchronisation
    ) throws PKNotFoundException,ConflictNotFoundException, SQLException,UnsupportedKeyTypeException,InstantiationException, IllegalAccessException, Exception{
        PeriS periSWithoutRestitution = periCls.newInstance().findBy(periId, periConnection, periCls);//****
        //transfert na modification sao de hisy update na save
            periSWithoutRestitution.setCurrentUpdatedOrderedIndexs(periSWithoutRestitution.getLastOrderedIndexs());//****
            periSWithoutRestitution.setCurrentUpdatedOrderedDatas(periSWithoutRestitution.getLastOrderedDatas());//****
        //transfert na modification sao de hisy update na save
        PeriS periS = periSWithoutRestitution; 
        //verifier farany
        //tsy misy afaka manao update tsony(mandritra le restitution) sy le find conflict
        centConnection.setAutoCommit(false);
        periConnection.setAutoCommit(false);
       if(periSWithoutRestitution.getUIConflitMessage().equals("peri_restt_conflct")){
            String idSynchroCommun = periS.getId();
            periS = getPeriRestitution(//zay vao afaka mitady le cent Id en conflit
                        idSynchroCommun,
                        centConnection,
                        periConnection,
                        centCls,
                        periCls,
                        dateSynchronisation
                    );
        }

        String uiConflitCentSynchroId = periSWithoutRestitution.getUIConflitCentSynchroId();
        
        CentS centS = null;PeriSynchro periRestitution = null;
        if(periSWithoutRestitution.getUIConflitMessage().equals("cent_restt_conflct")){
            String idSynchroCommun = uiConflitCentSynchroId;//uiConflitCentSynchroId=>idSynchroCommu
            periRestitution =  getPeriRestitution(//zay vao afaka mitady le cent Id en conflit;
                                    idSynchroCommun,
                                    centConnection,
                                    periConnection,
                                    centCls,
                                    periCls,
                                    dateSynchronisation
                                );
            //replication
            
            centS = periRestitution.getRestitutionCentSynchro(centCls);
    //        //transfert last value avy direct any @source alohan' ny resaka affichage(this.setCurrentOrdered(this.getLastOrdered))
            centS.setLastOrderedIndexs(periRestitution.getLastOrderedIndexs());
            centS.setLastOrderedDatas(periRestitution.getLastOrderedDatas());
    //        //transfert last value
            //replication
        }
            

        if(periSWithoutRestitution.getUIConflitMessage().equals("cent_restt_conflct")){
            //refa hisolve conflict de le restitution centrale(centrale) no hitadiavana 
            //le peri ID conflict de verifiena hoe mitovy @ le teo aloha ve? si non annulerna na transferena le centId UI conflict ny le teo aloha de petahana  @ pery synchro hafa
            List<Long> allMostRecentUIConflitPeriSynchroId = null;
            if(centS instanceof AnneeScolaireCentSynchro){
                allMostRecentUIConflitPeriSynchroId = ((AnneeScolaireCentSynchro)centS).findAllUIConflitPeriSynchroId(periConnection);
            }else{
                try {
                    Long mostRecentUIConflitPeriSynchroId = Long.parseLong(centS.findUIConflitPeriSynchroId(periConnection));
                    allMostRecentUIConflitPeriSynchroId = new ArrayList<>(1);
                    allMostRecentUIConflitPeriSynchroId.add(mostRecentUIConflitPeriSynchroId);
                } catch (Exception e) {allMostRecentUIConflitPeriSynchroId = new ArrayList<>(0);}
            }
            
            boolean libererUIConflitCentSynchroId = false;
            boolean transfererUIConflitCentSynchroId = false;
            if(allMostRecentUIConflitPeriSynchroId.isEmpty()){//annulation
                libererUIConflitCentSynchroId = true;
            }else if(!allMostRecentUIConflitPeriSynchroId.contains(new Long(periSWithoutRestitution.getId()))){//transfert
                transfererUIConflitCentSynchroId = true;
            }
            
            if(transfererUIConflitCentSynchroId || libererUIConflitCentSynchroId){//raha efa voavaha le probleme
                periSWithoutRestitution.setUIConflitCentSynchroId(null);
                periSWithoutRestitution.setUIConflitMessage(null);
                periSWithoutRestitution.update(periConnection);
                if(libererUIConflitCentSynchroId){
                    //mamita le Synchronisation fa tsy misy conflit tsony*******************************************
                    centS.update(centConnection);
                    periRestitution.update(periConnection);
                    centConnection.commit();
                    periConnection.commit();//******
                    throw new ConflictNotFoundException();
                }
                //*transfert
                //annuler le restituion(passe passe na cent peri SStory)
                periConnection.rollback();
                centConnection.rollback();
                
                PeriS newPeriSWithUIConflit = periCls.newInstance().findBy(allMostRecentUIConflitPeriSynchroId.get(0).toString(), periConnection, periCls);//****
                //transfert na modification sao de hisy update na save
                    newPeriSWithUIConflit.setCurrentUpdatedOrderedIndexs(newPeriSWithUIConflit.getLastOrderedIndexs());//****
                    newPeriSWithUIConflit.setCurrentUpdatedOrderedDatas(newPeriSWithUIConflit.getLastOrderedDatas());//****
                //transfert na modidification
                newPeriSWithUIConflit.setUIConflitCentSynchroId(centS.getId());
                newPeriSWithUIConflit.update(periConnection);
                periConnection.commit();//*****
                throw new ConflictUpdateException();
            }
        }else{
            List<Long> allMostRecentUIConflitCentSynchroId = null;
            if(periS instanceof AnneeScolairePeriSynchro){
                allMostRecentUIConflitCentSynchroId = ((AnneeScolairePeriSynchro)periS).findAllUIConflitCentSynchroId(centConnection);
            }else{
                try {
                    long mostRecentUIConflitCentSynchroId = Long.parseLong(periS.findUIConflitCentSynchroId(centConnection));//*****
                    allMostRecentUIConflitCentSynchroId = new ArrayList<>(1);
                    allMostRecentUIConflitCentSynchroId.add(mostRecentUIConflitCentSynchroId);
                } catch (Exception e) {allMostRecentUIConflitCentSynchroId = new ArrayList<>(0);}
            }
            if(allMostRecentUIConflitCentSynchroId.isEmpty()){//liberation: raha efa voavaha le probleme
                periS.setUIConflitCentSynchroId(null);
                periS.setUIConflitMessage(null);
                if(periSWithoutRestitution.getUIConflitMessage().equals("peri_restt_conflct")){//efa vita ho azy le transfert
                    //mamita le Synchronisation fa tsy misy conflit tsony
                        CentS centSynchroRestitution = periS.getRestitutionCentSynchro(centCls);
                        centSynchroRestitution.update(centConnection);
                        centConnection.commit();
                }
                periS.update(periConnection);
                periConnection.commit();//******
                throw new ConflictNotFoundException();
            }
            if(!allMostRecentUIConflitCentSynchroId.contains(new Long(uiConflitCentSynchroId))){//transfert::modification ny le uiConflitCentSynchroId fa tsy le uiConflitMessage(type)
                if(periSWithoutRestitution.getUIConflitMessage().equals("peri_restt_conflct")){//*transfert:
                    //annuler le restituion(passe passe na cent peri SStory)
                    periConnection.rollback();
                    centConnection.rollback();
                }
                periSWithoutRestitution.setUIConflitCentSynchroId(allMostRecentUIConflitCentSynchroId.get(0).toString());
                periSWithoutRestitution.update(periConnection);
                periConnection.commit();//*****
                throw new ConflictUpdateException();
            }
        }
            
        if(!periSWithoutRestitution.getUIConflitMessage().equals("cent_restt_conflct"))
            centS = centCls.newInstance().findBy(uiConflitCentSynchroId, centConnection, centCls);//*****
        return new Object[]{centS,periS};
    }
    
    public static <CentS extends CentSynchro, PeriS extends PeriSynchro> HashMap<String, Object> solve(String synchroName) throws SQLException, Exception{
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        HashMap<String, Object> hmapScope = new HashMap<>(50);
        hmapResponse.put("scope", hmapScope);
        String periClsName = null;
        Connection periConnection = null;
        try {
            try {
                periClsName = SynchronisationConfig.getCentSPeriSAndCentSStoryPeriSStoryClsName(synchroName)[1]; 
            } catch (SynchroNameNotFoundException e) {
                hmapResponse.put("path_error", true);
                throw e;
            }
            
            Class<PeriS> periCls = null;
            try {
                periCls = (Class<PeriS>) Class.forName(periClsName);
            } catch (ClassNotFoundException e) {
                hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                throw e;
            }
        
            try {
                periConnection = Helper.getPeriConn();
            } catch (SQLException e) {
                hmapScope.put("page_notification_message", "problème de connection locale.");
                throw e;
            }catch(Exception e){
                hmapScope.put("page_notification_message", "un problème est survenu <br> pendant la connection en locale.<br>("+e.getMessage()+")");
                throw e;
            }
            String sqlRFormat = ""
                + "WITH temp_t_peri_synchro_name_u_i_conflit AS(\n" +
                "    SELECT t_synchro_id,t_synchro_date_edition FROM  @t_peri_synchro_name WHERE conflit_cent_synchro_id IS NOT NULL\n" +
                ")\n" +
                "SELECT conflict_count,next_periId FROM (\n" +
                "    SELECT count(*) AS conflict_count FROM temp_t_peri_synchro_name_u_i_conflit\n" +
                ") AS temp_conflict_count\n" +
                " CROSS JOIN\n" +
                "(\n" +
                "    SELECT t_synchro_id AS next_periId from temp_t_peri_synchro_name_u_i_conflit\n" +
                "    ORDER BY t_synchro_date_edition ASC, t_synchro_id ASC\n" +
                "    LIMIT 1\n" +
                ") AS temp_next_periId"
            ;
            String sqlR = sqlRFormat.replaceFirst("@t_peri_synchro_name", periCls.newInstance().getTabName());
            Key[] OrederedKeys = new Key[]{
                new Key<Integer>("conflict_count", Integer.class), new Key<Integer>("next_periid", Integer.class)
            };
            CRUD.scalar(sqlR, periConnection, OrederedKeys);
            if(OrederedKeys[0].getValue()==null){//0: ligne: aucun next_periId 
                hmapResponse.put("conflict_count", 0);
            }else{
                hmapResponse.put("conflict_count", OrederedKeys[0].getValue());
                hmapResponse.put("next_peri_id", OrederedKeys[1].getValue());
            }
            hmapResponse.put("status", true);
        } catch (Exception e) {
//            if(!hmapResponse.containsKey("status"))
                hmapResponse.put("status", false);
            if(!(Boolean)hmapResponse.get("status")){//misy erreur
                if(!hmapResponse.containsKey("page_notification_message")){
                    hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                }
                hmapScope.put("page_notification_class", "danger");
                if(!hmapScope.containsKey("page_notification_title")){//default title
                    hmapScope.put("page_notification_title", "Erreur");
                }
            }else
                hmapScope.put("page_notification_class", "info");
            hmapScope.put("exit_bttn", "OK");
            //throw e;
        }finally{
            if(periConnection!=null){
                periConnection.close();
            }
        }
        return hmapResponse;
    }
    
    public static <CentS extends CentSynchro, PeriS extends PeriSynchro> HashMap<String, Object> solve(
            String synchroName, String conflictCount, String periSynchroId, String userId,
            String option,String deleteLocaleOptionError,
            String[] centOrderedStringifiedIndexes, String[] periOrderedStringifiedIndexes,
            String[] centIsUpdatedOrderedIndexs, String[] periIsUpdatedOrderedIndexs
    ) throws Exception{        
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        HashMap<String, Object> hmapScope = new HashMap<>(50);
        hmapResponse.put("scope", hmapScope);
        hmapScope.put("synchro_name", synchroName);
        String[] centSPeriSAndCentSStoryPeriSStoryClsName = null;
        int periSynchroId_Int = 0;
        try {
            try {
                centSPeriSAndCentSStoryPeriSStoryClsName = SynchronisationConfig.getCentSPeriSAndCentSStoryPeriSStoryClsName(synchroName); 
                int parseIntConflict_count = Integer.parseInt(conflictCount);
                if(parseIntConflict_count <0 )//>=0
                    throw new NumberFormatException();
                periSynchroId_Int = Integer.parseInt(periSynchroId);
                Integer.parseInt(userId);
                hmapScope.put("peri_synchro_id", periSynchroId);
                hmapScope.put("conflict_count", conflictCount);
                hmapScope.put("current_user_id", userId);
            } catch (SynchroNameNotFoundException| NumberFormatException e) {
                hmapResponse.put("path_error", true);
                throw e;
            }
            
            Class<PeriS> periCls = null; Class<CentS>  centCls = null;
            try {
                centCls = (Class<CentS>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[0]);
                periCls = (Class<PeriS>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[1]);
            } catch (ClassNotFoundException e) {
                hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                hmapScope.put("exit_bttn", "OK");
                throw e;
            }
        
            Connection centConnection = null;
            Connection periConnection = null;
            try {
                try {
                    centConnection = Helper.getCentConn();
                } catch (SQLException e) {
                    hmapScope.put("page_notification_message", "problème de connection en ligne.");
                    hmapScope.put("retry_bttn", "RECOMMENCER");
                    throw e;
                }catch(Exception e){
                    hmapScope.put("page_notification_message", "un problème est survenu <br> pendant la connection en ligne.<br>("+e.getMessage()+")");
                    hmapScope.put("retry_bttn", "RECOMMENCER");
                    throw e;
                }

                try {
                    periConnection = Helper.getPeriConn();
                } catch (SQLException e) {
                    hmapScope.put("page_notification_message", "problème de connection locale.");
                    throw e;
                }catch(Exception e){
                    hmapScope.put("page_notification_message", "un problème est survenu <br> pendant la connection en locale.<br>("+e.getMessage()+")");
                    throw e;
                }
                PeriS periS = null;
                CentS centS = null;
                Timestamp dateSynchronisation = new Timestamp(new Date().getTime());
                try {
                    Object[] centSPeriSToSolve= getCentSPeriSToSolve(
                        centConnection, periConnection, centCls, periCls,periSynchroId,dateSynchronisation
                    );
                    
                    centS = (CentS) centSPeriSToSolve[0];
                    periS = (PeriS) centSPeriSToSolve[1];
                }catch (PKNotFoundException e) {
                    hmapScope.put("page_notification_message", "Element introuvable.");
                    hmapScope.put("exit_bttn", "OK");
                    throw e;
                }catch (ConflictNotFoundException e){
                    hmapResponse.put("status", true);//tsy misy erreur nefa tsy misy conflict à traiter
                    hmapScope.put("page_notification_title", "Aucun conflit détecté.");
                    hmapScope.put("page_notification_message", "Le conflit n'existe pas ou a été déjà résolu.");
                    hmapScope.put("exit_bttn", "OK");
                    throw e;
                }catch (ConflictUpdateException e){
                    hmapScope.put("page_notification_message", e.getMessage());
                    hmapScope.put("retry_bttn", "RECOMMENCER");
                    throw e;
                }
                        
                HashMap<String,Object> peri = new HashMap<>(20);
                HashMap<String,Object> cent = new HashMap<>(20);
                HashMap<String,Object> periWhenRead = new HashMap<>(20);
                HashMap<String,Object> centWhenRead = new HashMap<>(20);
                hmapScope.put("peri", peri);
                hmapScope.put("cent", cent);
                hmapScope.put("peri_when_read", periWhenRead);
                hmapScope.put("cent_when_read", centWhenRead);
                hmapScope.put("cent_synchro_id", centS.getId());
                
                try {
                    EtatMtrlPeriSynchro etatMtrl = (EtatMtrlPeriSynchro)periS;
                    String nomToLowerCase = etatMtrl.getNom().toLowerCase();
                    if(nomToLowerCase.contains("en")&&nomToLowerCase.contains("marche")){
                        hmapScope.put("fusion_only", true);
                    }
                } catch (ClassCastException e) {}
                
                peri.put("ordered_indexe", periS.getOrderedDisplayableIndexs(Synchro.UPDATE_DISPLAY_TYPE, periConnection, Helper.peripheriqueStationType));//**********************le départ
                cent.put("ordered_indexe", centS.getOrderedDisplayableIndexs(Synchro.UPDATE_DISPLAY_TYPE, centConnection, Helper.centraleStationType));//**********************le départ
                
                periWhenRead.put("ordered_indexe", periS.getOrderedDisplayableIndexs(Synchro.READ_DISPLAY_TYPE, periConnection, Helper.peripheriqueStationType));//**********************le départ
                centWhenRead.put("ordered_indexe", centS.getOrderedDisplayableIndexs(Synchro.READ_DISPLAY_TYPE, centConnection, Helper.centraleStationType));//**********************le départ
                periWhenRead.put("ordered_data", periS.getOrderedDisplayableDatas(Synchro.READ_DISPLAY_TYPE, periConnection, Helper.peripheriqueStationType));
                centWhenRead.put("ordered_data", centS.getOrderedDisplayableDatas(Synchro.READ_DISPLAY_TYPE, centConnection, Helper.centraleStationType));
                periWhenRead.put("last_editor_email", periS.getUserEmail(periConnection));
                centWhenRead.put("last_editor_email", centS.getUserEmail(centConnection));
                
                String[] centPeriTimeZone = Helper.getCentPeriTimeZone(periConnection, Helper.peripheriqueStationType);
                TimeZone timeZone = TimeZone.getTimeZone(centPeriTimeZone[1]);//par % @ Fus Hrr ny Periphrque
                
                SimpleDateFormat sdfTimestampDisplay = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                sdfTimestampDisplay.setTimeZone(timeZone);
                
                periWhenRead.put("date_edition", sdfTimestampDisplay.format(periS.getDateEdition()));
                centWhenRead.put("date_edition", sdfTimestampDisplay.format(centS.getDateEdition()));
                if(periS.getDateSuppr()!= null)
                    periWhenRead.put("date_suppr", sdfTimestampDisplay.format(periS.getDateSuppr()));
                if(centS.getDateSuppr()!= null)
                    centWhenRead.put("date_suppr", sdfTimestampDisplay.format(centS.getDateSuppr()));

                
                hmapResponse.put("status", true);
                if(option!=null){
                    hmapResponse.put("option", option);//mba halalana hoe hi load solve view ve sa hanao redirection hitady next periId to solve
                    hmapScope.put("option", option);
                    (( HashMap<String,Object>)cent.get("ordered_indexe")).put("value", centOrderedStringifiedIndexes);
                    (( HashMap<String,Object>)peri.get("ordered_indexe")).put("value", periOrderedStringifiedIndexes);
                    
                    try {
                        checkAvailableOption(option, deleteLocaleOptionError, periS);                        
                    } catch (Exception e) {
                        hmapScope.put("option_error", e.getMessage());
                        hmapResponse.put("status", false);
                        throw e;
                    }
                    SynchroniseurManager<CentS, PeriS> instance = new SynchroniseurManager<>(
                        centConnection, centCls, periConnection, periCls, dateSynchronisation
                    );
                    UIConflictOption theUIConflictOption = null;
                    String indexAlreadyExistErrMssg = "Les valeurs @ordered_indexValues sont déjà utilisés.";
                    String centSynchroId = (String)hmapScope.get("cent_synchro_id");
                    if(!option.equalsIgnoreCase("fusion")) {
                        String stationCible = null;
                        if(option.endsWith("locale"))
                            stationCible = UIConflictOption.PERIPHERIQUE;
                        else
                            stationCible = UIConflictOption.CENTRALE;
                        if(option.startsWith("delete")){
                            theUIConflictOption = new UIConflictOptionDelete(centSynchroId, periSynchroId, stationCible);
                        }else if(option.startsWith("update")){
                            Synchro synchro = centS;
                            String[] orderedStringifiedIndexs = centOrderedStringifiedIndexes;
                            String[] isUpdatedOrderedIndexs = centIsUpdatedOrderedIndexs;
                            HashMap<String, Object> orderedDisplayableIndexs = (( HashMap<String,Object>)cent.get("ordered_indexe"));
                            Connection connection = centConnection;
                            String stationCibleType = Helper.centraleStationType;
                            if(stationCible.equals(UIConflictOption.PERIPHERIQUE)){
                                synchro = periS;
                                orderedStringifiedIndexs = periOrderedStringifiedIndexes;
                                isUpdatedOrderedIndexs = periIsUpdatedOrderedIndexs;
                                orderedDisplayableIndexs = (( HashMap<String,Object>)peri.get("ordered_indexe"));
                                connection = periConnection;
                                stationCibleType = Helper.peripheriqueStationType;
                            }
                            int length = orderedStringifiedIndexs.length;
                            String[] orderedIndexsValidation = new String[length];
                            String[] orderedIndexsError = new String[length];
                            
                                                        
                            boolean  updateExists = false;
                            synchro.setLastOrderedIndexs(CRUD.serialize(synchro.getOrderedIndexs()));
                            synchro.setCurrentUpdatedOrderedIndexs(new String[length]);
                            
                            for (int i = 0; i < length; i++){
                                if(!"true".equals(isUpdatedOrderedIndexs[i]))continue;
                                try {
                                    synchro.setOrderedStringifiedIndexs(orderedStringifiedIndexs[i],i);
                                    if(synchro.setCurrentUpdatedOrderedIndexs(i,synchro.getOrderedIndexs(Synchro.UPDATE_DISPLAY_TYPE, connection, stationCibleType)[i])){
                                        updateExists = true;
                                        orderedIndexsValidation[i] = "is-valid";
                                    }else{
                                        orderedIndexsValidation[i] = "";
                                    }
                                } catch (Exception e) {
                                    if(e instanceof InvocationTargetException) e = (Exception) e.getCause();
                                    orderedIndexsError[i] = e.getMessage();
                                    orderedIndexsValidation[i] = "is-invalid";
                                    hmapResponse.put("status", false);
                                }
                            }
                            orderedDisplayableIndexs.put("validation",orderedIndexsValidation);
                            if(!hmapResponse.containsKey("status")){
                                hmapResponse.put("status", true);
                            }
                            if(!(Boolean)hmapResponse.get("status")){//misy erreur
                                orderedDisplayableIndexs.put("error", orderedIndexsError);
                            }else if(!updateExists){
                                hmapScope.put("form_error", "Erreur: vous n'avez rien modifier.");
                                hmapResponse.put("status", false);
                            }else{
                                try {
                                    if(synchro instanceof AnneeScolaireCentSynchro){
                                        AnneeScolaireCentSynchro aS = (AnneeScolaireCentSynchro)synchro;
                                        AnneeScolaire.checkAmplitude(aS.getDebut(), aS.getFin());
                                    }else if(synchro instanceof AnneeScolairePeriSynchro){
                                        AnneeScolairePeriSynchro aS = (AnneeScolairePeriSynchro)synchro;
                                        AnneeScolaire.checkAmplitude(aS.getDebut(), aS.getFin());
                                    }
                                } catch (AnneeScolaireExcetion e) {
                                    hmapScope.put("form_error", e.getMessage());
                                    hmapResponse.put("status", false);
                                }
                                indexAlreadyExistErrMssg = indexAlreadyExistErrMssg.replaceFirst("@ordered_indexValues", String.join(", ", orderedStringifiedIndexs));
                                theUIConflictOption = new UIConflictOptionUpdate(centSynchroId, periSynchroId, stationCible, synchro.getCurrentUpdatedOrderedIndexs());//***************************************************
                            }
                        }
                    }else{
                        theUIConflictOption = new UIConflictOptionFusion(centSynchroId, periSynchroId);
                    }
                    if((Boolean)hmapResponse.get("status")){
                        try {
                            instance.solveUIConflictTransactly(theUIConflictOption, userId, indexAlreadyExistErrMssg);//commit ou rollback automatique
                        }catch (SQLException e) {//@ le delete
                            if(e.getSQLState().equals("23503")){//test on delete no action
                                hmapScope.put("delete_option_error", true);//status =  true :tsy erreur fa juste efa tsy afaka deletena fotsiny
                                hmapScope.put("option_error", "Désolé, la suppression n'est plus possible");
                            }else{
                                hmapResponse.put("status", false);
                                throw e;
                            }
                        }catch (UIAlreadyExistsException e) {//@ update
                            hmapScope.put("form_error", e.getMessage());
                            hmapResponse.put("status", false);
                        }
                    }
                }
            } catch (Exception e) {
                if(!(hmapScope.containsKey("form_error")||hmapScope.containsKey("option_error")))
                    hmapScope.put("form_error", "Un problème est survenu.<br>("+e.getMessage()+")");
                throw e;
            }finally{
                if(centConnection!=null){
                    if(!centConnection.getAutoCommit())
                        centConnection.rollback();//fafana zay cent sy peri synchroStory efa napasypasy nefa tsy valide(commité) le izy
                    centConnection.close();
                }
                if(periConnection!=null){
                    if(!periConnection.getAutoCommit())
                        periConnection.rollback();//fafana zay cent sy peri synchroStory efa napasypasy nefa tsy valide(commité) le izy
                    periConnection.close();
                }
            }
            
        }catch (Exception e){
//            if(!hmapResponse.containsKey("status"))
                hmapResponse.put("status", false);
            if(!(Boolean)hmapResponse.get("status")){//misy erreur
                hmapScope.put("page_notification_class", "danger");
                if(!hmapScope.containsKey("page_notification_title")){//default title
                    hmapScope.put("page_notification_title", "Erreur");
                }
            }else
                hmapScope.put("page_notification_class", "info");
            hmapScope.put("exit_bttn", "OK");
            //throw e;
        }
        return hmapResponse;
    }

    private static void checkAvailableOption(String options, String deleteLocaleOptionError, Synchro synchronisable) throws Exception{
        if(synchronisable instanceof EtatMtrlCentSynchro){
            EtatMtrlCentSynchro etatMtrl = (EtatMtrlCentSynchro)synchronisable;
            String nomToLowerCase = etatMtrl.getNom().toLowerCase();
            if(nomToLowerCase.contains("en")&&nomToLowerCase.contains("marche")){
                if(!("fusion".equals(options)))
                    throw new Exception("option invalide.(Il faut fusionner)");
            }
        }else if(synchronisable instanceof EtatMtrlPeriSynchro){
            EtatMtrlPeriSynchro etatMtrl = (EtatMtrlPeriSynchro)synchronisable;
            String nomToLowerCase = etatMtrl.getNom().toLowerCase();
            if(nomToLowerCase.contains("en")&&nomToLowerCase.contains("marche")){
                if(!("fusion".equals(options)))
                    throw new Exception("option invalide.(Il faut fusionner)");
            }
        }
        int periSynchroId = Integer.parseInt(synchronisable.getId());
        Set<String> allAvailableOption = new HashSet<>(5);
        allAvailableOption.add("update_locale");
        allAvailableOption.add("update_centrale");
        if(periSynchroId==0)
            throw new Exception("invalide element id");
        if(periSynchroId>0){
            allAvailableOption.add("fusion");
            if(deleteLocaleOptionError == null)
                allAvailableOption.add("delete_locale");
        }
        if(!allAvailableOption.contains(options))
            throw new Exception("option invalide");
    }

    private static <PeriS extends PeriSynchro, CentS extends CentSynchro> PeriS getPeriRestitution(
            String idSynchroCommun,
            Connection centConnection,
            Connection periConnection,
            Class<CentS> centSCls,
            Class<PeriS> periSCls,
            Timestamp dateSynchronisation
        ) throws Exception {
            String periStationId = Helper.getPERI_ID(periConnection);//station peripherique anaovana restitution
            CentSynchroStory centSStoryInst = new CentSynchroStory(centSCls.newInstance());
            String[] centSStoryOrderedCols = centSStoryInst.getOrderedColsNames();
            Key[] centSStorySortedKeys = centSStoryInst.getAllOrderedKey();/*setterName,type&value*/
            String centSStoryTab = centSStoryInst.getTabName();
            String centSStoryRecepteurTab = centSStoryInst.getRecepteurTabName();
        
            PeriSynchroStory periSStoryInst = new PeriSynchroStory(periSCls.newInstance());
            String[] periSStoryOrderedCols = periSStoryInst.getOrderedColsNames();
            Key[] periSStorySortedKeys = periSStoryInst.getAllOrderedKey();
            String periSStoryTab = periSStoryInst.getTabName();
            
            List<CentSynchroStory> liCentOrderedSynchronisableStory = CentSynchroStory.getOrderedSynchronisableStory(
                    centConnection,
                    centSStoryOrderedCols,
                    centSStoryTab,
                    centSStoryRecepteurTab,
                    idSynchroCommun,
                    periStationId,
                    centSStorySortedKeys/*setterName,type&value*/
            );
                    
            List<PeriSynchroStory> liPeriOrderedSynchronisableStory = PeriSynchroStory.getOrderedSynchronisableStory(
                    periConnection,
                    periSStoryOrderedCols,
                    periSStoryTab,
                    idSynchroCommun,
                    periSStorySortedKeys/*setterName,type&value*/
            );
            int liSynchroStorySize = liCentOrderedSynchronisableStory.size()+liPeriOrderedSynchronisableStory.size();
            List<SynchroStory> liSynchroStory = new ArrayList<SynchroStory>(liSynchroStorySize);
            liSynchroStory.addAll(liCentOrderedSynchronisableStory);liSynchroStory.addAll(liPeriOrderedSynchronisableStory);

            SynchroStory firstDeleter = SynchroniseurUpdateManager.getFirstDeleter(liSynchroStory);//getFirstDeleter
            if(firstDeleter!=null){ //transaction
                //throwsAfterFirstDeleter
                String causeRejet="L'element que vous avez modifié à été déjà supprimié en avance.";
                SynchroniseurUpdateManager.throwsAfterFirstDeleter(
                        firstDeleter,
                        liSynchroStory, liCentOrderedSynchronisableStory,liPeriOrderedSynchronisableStory,
                        centConnection,  periConnection, causeRejet, centSCls, periSCls
                );
            }
            //Synchronisation not rejected story
            //P=>C
            int liPeriOrderedSynchronisableStorySize = liPeriOrderedSynchronisableStory.size();
            for (int i = 0; i <liPeriOrderedSynchronisableStorySize ; i++) {
                liPeriOrderedSynchronisableStory.get(i).insertEquivalentCentSynchroStory(centConnection, periConnection,
                        periStationId, centSCls, periSCls,idSynchroCommun,dateSynchronisation
                    );
            }

            //C=>P
            int liCentOrderedSynchronisableStorySize = liCentOrderedSynchronisableStory.size();
            for (int i = 0; i < liCentOrderedSynchronisableStorySize; i++) {
                liCentOrderedSynchronisableStory.get(i).insertEquivalentPeriSynchroStory(centConnection, periConnection,
                    periStationId, centSCls, periSCls, dateSynchronisation, centSStoryRecepteurTab
                );
            }
            List<PeriSynchroStory>  orderedLastNotThrwsedNotNullValue = PeriSynchroStory.getOrderedLastNotThrwsedNotNullValue(periConnection, periSStoryInst, idSynchroCommun);
            
            PeriS periSynchroRestitution = PeriSynchroStory.getPeriSynchroRestitution(idSynchroCommun, orderedLastNotThrwsedNotNullValue, periSCls);
            return periSynchroRestitution;
    }
}
