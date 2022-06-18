/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autreWs;

import static autreWs.Function.getPagntFoot;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import synchronisable.convention.Convention;
import synchronisable.param.UsefulParam;
import synchronisable.param.exception.ParamException;
import synchronisable.peripherique.PeriSynchro;
import static synchronisable.peripherique.PeriSynchro.SYNCHRONISATION_STATE_CONFLICT;
import usefull.ArrayHelper;
import usefull.BigDecimalHelper;
import usefull.HashHelper;
import usefull.StringHelper;
import usefull.dao.CRUD;
import usefull.dao.Helper;
import usefull.dao.Key;
import usefull.dao.type.DBEnumType;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class Etablissement {
    private final static HashMap<String, Integer> et_data_indice/*etblissmt_data_indice*/ = HashHelper.newHashMap(
        new String[] {
            "nom", "identifiant", "etblssmt_id", "nbr_eleve", "type_lieu", "t_region_id", "ville", "region", "t_type_etablissmt_id", "type_etablissement", "t_type_enseignmt_id", "type_enseignement", "t_niveau_enseignmt_id", "niveau_enseignement", "nbr_ordi_client", "nbr_ordi_serveur", "convention_id", "convention_debut", "convention_fin", "convention_nbr_eleve", "convention_ordi_client_nbr", "convention_ordi_client_prx_u", "convention_ordi_client_red_u", "convention_ordi_client_red_u_type_val", "convention_ordi_servr_nbr", "convention_ordi_servr_prx_u", "convention_ordi_servr_red_u", "convention_ordi_servr_red_u_type_val", "paymt_mtt",      "ordi_client_red_marge",  "ordi_servr_red_marge",  "red_finale",  "red_finale_type_val",  /*"description",*/  "autres_charges"
            , "l_etblssmt_conflit_cent_synchro_id", "l_etblssmt_t_synchro_date_reception", "l_etblssmt_t_synchro_date_envoye", "l_etblssmt_t_synchro_date_edition"
            , "l_admnstrtf_conflit_cent_synchro_id", "l_admnstrtf_t_synchro_date_reception", "l_admnstrtf_t_synchro_date_envoye", "l_admnstrtf_t_synchro_date_edition"    
        },
        new Integer[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,   29, 30, 31, 32, 33/*, 34*/,   
            33 + 1, 33 + 2, 33 + 3, 33 + 4, 
            33 + 5, 33 + 6, 33 + 7, 33 + 8/**/
        }
    );
    
    public static List<HashMap<String,Object>[]> getEtblssmtData(String stationCibleType, Connection connection, long pagntStartNum, int pagntMaxSize) throws ParamException, Exception{
        String sqlR = "SELECT * FROM get_etablissemts_data("+pagntStartNum+", "+pagntMaxSize+")";
        boolean isPeripheriqueStationType = Helper.peripheriqueStationType.equals(stationCibleType);/**/
        Key[] orderedKeys = (isPeripheriqueStationType)?/**/
            new Key[]{
                new Key<>("nom", String.class), new Key<>("identifiant", String.class), new Key<>("etblssmt_id", String.class), new Key<>("nbr_eleve", Integer.class), new Key<>("type_lieu", String.class), new Key<>("t_region_id", String.class), new Key<>("ville", String.class), new Key<>("region", String.class), new Key<>("t_type_etablissmt_id", String.class), new Key<>("type_etablissement", String.class), new Key<>("t_type_enseignmt_id", String.class), new Key<>("type_enseignement", String.class), new Key<>("t_niveau_enseignmt_id", String.class), new Key<>("niveau_enseignement", String.class), new Key<>("nbr_ordi_client", Long.class), new Key<>("nbr_ordi_serveur", Long.class), new Key<>("convention_id", String.class), new Key<>("convention_debut", Date.class), new Key<>("convention_fin", Date.class), new Key<>("convention_nbr_eleve", Integer.class), new Key<>("convention_ordi_client_nbr", Long.class), new Key<>("convention_ordi_client_prx_u", BigDecimal.class), new Key<>("convention_ordi_client_red_u", BigDecimal.class), new Key<>("convention_ordi_client_red_u_type_val", String.class), new Key<>("convention_ordi_servr_nbr", Long.class), new Key<>("convention_ordi_servr_prx_u", BigDecimal.class), new Key<>("convention_ordi_servr_red_u", BigDecimal.class), new Key<>("convention_ordi_servr_red_u_type_val", String.class), new Key<>("paymt_mtt", BigDecimal.class)

                ,new Key<>("ordi_client_red_marge", Long.class), new Key<>("ordi_servr_red_marge", Long.class), new Key<>("red_finale", BigDecimal.class), new Key<>("red_finale_type_val", String.class)/*, new Key<>("description", String.class)*/, new Key<>("autres_charges", BigDecimal.class)
            
                    
                , new Key<>("l_etblssmt_conflit_cent_synchro_id", String.class), new Key<>("l_etblssmt_t_synchro_date_reception", Timestamp.class), new Key<>("l_etblssmt_t_synchro_date_envoye", Timestamp.class), new Key<>("l_etblssmt_t_synchro_date_edition", Timestamp.class)

                , new Key<>("l_admnstrtf_conflit_cent_synchro_id", String.class), new Key<>("l_admnstrtf_t_synchro_date_reception", Timestamp.class), new Key<>("l_admnstrtf_t_synchro_date_envoye", Timestamp.class), new Key<>("l_admnstrtf_t_synchro_date_edition", Timestamp.class)
            }
        :   new Key[]{
                new Key<>("nom", String.class), new Key<>("identifiant", String.class), new Key<>("etblssmt_id", String.class), new Key<>("nbr_eleve", Integer.class), new Key<>("type_lieu", String.class), new Key<>("t_region_id", String.class), new Key<>("ville", String.class), new Key<>("region", String.class), new Key<>("t_type_etablissmt_id", String.class), new Key<>("type_etablissement", String.class), new Key<>("t_type_enseignmt_id", String.class), new Key<>("type_enseignement", String.class), new Key<>("t_niveau_enseignmt_id", String.class), new Key<>("niveau_enseignement", String.class), new Key<>("nbr_ordi_client", Long.class), new Key<>("nbr_ordi_serveur", Long.class), new Key<>("convention_id", String.class), new Key<>("convention_debut", Date.class), new Key<>("convention_fin", Date.class), new Key<>("convention_nbr_eleve", Integer.class), new Key<>("convention_ordi_client_nbr", Long.class), new Key<>("convention_ordi_client_prx_u", BigDecimal.class), new Key<>("convention_ordi_client_red_u", BigDecimal.class), new Key<>("convention_ordi_client_red_u_type_val", String.class), new Key<>("convention_ordi_servr_nbr", Long.class), new Key<>("convention_ordi_servr_prx_u", BigDecimal.class), new Key<>("convention_ordi_servr_red_u", BigDecimal.class), new Key<>("convention_ordi_servr_red_u_type_val", String.class), new Key<>("paymt_mtt", BigDecimal.class)

                ,new Key<>("ordi_client_red_marge", Long.class), new Key<>("ordi_servr_red_marge", Long.class), new Key<>("red_finale", BigDecimal.class), new Key<>("red_finale_type_val", String.class)/*, new Key<>("description", String.class)*/, new Key<>("autres_charges", BigDecimal.class)
            }
        ;
        List<Key[]> datas = CRUD.readToKeysList(sqlR, connection, orderedKeys);
        int size = datas.size();  
        List<HashMap<String,Object>[]> final_datas = new ArrayList<>(size);
        List<Key[]>  allParamsOrederedByPlafondAsc = UsefulParam.getAllParamsOrederedByPlafondAsc(stationCibleType, connection);
        for (int i = 0; i < size; i++) {
            Key[] data = datas.get(i);
            boolean isEcole = "ecole".equals((String)data[et_data_indice.get("type_lieu")].getValue());
            String href = (isEcole)? "EtablissementScolaire": "EtablissementAccesMad";
            href = href+ "/" +((String)data[et_data_indice.get("etblssmt_id")].getValue());
            String type = "";
            if(isEcole){
                type = StringHelper.isEmpty((String)data[et_data_indice.get("niveau_enseignement")].getValue())? "École": (String)data[et_data_indice.get("niveau_enseignement")].getValue();
                if(!StringHelper.isEmpty((String)data[et_data_indice.get("type_enseignement")].getValue()))type = type +" "+ ((String)data[et_data_indice.get("type_enseignement")].getValue());
                if(!StringHelper.isEmpty(((String)data[et_data_indice.get("type_etablissement")].getValue())))type = type + "("+((String)data[et_data_indice.get("type_etablissement")].getValue())+")";
            }else type = "Centre accesmad";
            String anneeSclr = (!isEcole)?null: (((Date)data[et_data_indice.get("convention_debut")].getValue()).getYear()+1900)+" - "+(((Date)data[et_data_indice.get("convention_fin")].getValue()).getYear()+1900);
            Integer conventionNbrEleve = ((Integer)data[et_data_indice.get("convention_nbr_eleve")].getValue());
            Boolean isConventionFinished = null;
            String deroulemnt = "";
            if(isEcole){
                isConventionFinished = conventionNbrEleve != null;
                deroulemnt = isConventionFinished?"Términée" : "En cours.";
            }

            Integer nbrEleve = (conventionNbrEleve != null)? conventionNbrEleve : (Integer)data[et_data_indice.get("nbr_eleve")].getValue();
            Long nbrOrdiClient = ((Long)data[et_data_indice.get("convention_ordi_client_nbr")].getValue() != null)? (Long)data[et_data_indice.get("convention_ordi_client_nbr")].getValue() : (Long)data[et_data_indice.get("nbr_ordi_client")].getValue();
            Long nbrOrdiServeur = ((Long)data[et_data_indice.get("convention_ordi_servr_nbr")].getValue() != null)? (Long)data[et_data_indice.get("convention_ordi_servr_nbr")].getValue() : (Long)data[et_data_indice.get("nbr_ordi_serveur")].getValue();

            BigDecimal ordiClientPrxU = null;
            BigDecimal ordiClientRedU = null;
            String ordiClientRedUTypeVal = null;
            BigDecimal ordiServrPrxU = null;
            BigDecimal ordiServrRedU = null;
            String ordiServrRedUTypeVal = null;
            BigDecimal factureMtt = null;
            BigDecimal totalVesment = null;
            BigDecimal montantTotalApayer = null;
            BigDecimal reste = null;
            String paymentStatus = null;
            String paymentValidation = null;
            
            Long ordiClientRedMarge = null;
            Long ordiServrRedMarge = null;
            BigDecimal redFinale = null;
            String redFinaleTypeVal = null;
            BigDecimal autresCharges = null;
            if(isEcole){
                if(isConventionFinished){
                    ordiClientPrxU = ((BigDecimal)data[et_data_indice.get("convention_ordi_client_prx_u")].getValue());
                    ordiClientRedU = ((BigDecimal)data[et_data_indice.get("convention_ordi_client_red_u")].getValue());
                    ordiClientRedUTypeVal = ((String)data[et_data_indice.get("convention_ordi_client_red_u_type_val")].getValue());
                    ordiServrPrxU = ((BigDecimal)data[et_data_indice.get("convention_ordi_servr_prx_u")].getValue());
                    ordiServrRedU = ((BigDecimal)data[et_data_indice.get("convention_ordi_servr_red_u")].getValue());
                    ordiServrRedUTypeVal = ((String)data[et_data_indice.get("convention_ordi_servr_red_u_type_val")].getValue());
                    
                    ordiClientRedMarge = ((Long)data[et_data_indice.get("ordi_client_red_marge")].getValue());
                    ordiServrRedMarge = ((Long)data[et_data_indice.get("ordi_servr_red_marge")].getValue());
                    redFinale = ((BigDecimal)data[et_data_indice.get("red_finale")].getValue());
                    redFinaleTypeVal = ((String)data[et_data_indice.get("red_finale_type_val")].getValue());
                    autresCharges = ((BigDecimal)data[et_data_indice.get("autres_charges")].getValue());
                }else{
                    ordiClientPrxU = (BigDecimal)UsefulParam.getValAndTypeValAndPlafondMajorant(allParamsOrederedByPlafondAsc, UsefulParam.PRX_U_ORDI_CLIENT_PARAM_NAME, new BigDecimal(nbrEleve.intValue()), "Veuillez paramètrer le montant annuel de l'accompagnement <br> au cas où le nombre d'élèves = "+ nbrEleve+" .")[0];
                    Object[] ordiClientRedUPrxTypeValAndMarge = null;
                    try {
                        ordiClientRedUPrxTypeValAndMarge = UsefulParam.getValAndTypeValAndPlafondMajorant(allParamsOrederedByPlafondAsc, UsefulParam.RED_U_ORDI_CLIENT_PARAM_NAME, new BigDecimal(nbrOrdiClient.longValue()));
                    } catch (ParamException e) {
                        ordiClientRedUPrxTypeValAndMarge = new Object[]{BigDecimal.ZERO, DBEnumType.PARAM_TYPE_VAL_MONTANT, BigDecimal.ZERO};
                    }
                    ordiClientRedU = (BigDecimal)ordiClientRedUPrxTypeValAndMarge[0];
                    ordiClientRedUTypeVal = (String)ordiClientRedUPrxTypeValAndMarge[1];
                    ordiClientRedMarge = ((BigDecimal)ordiClientRedUPrxTypeValAndMarge[2]).longValue();
                    ordiServrPrxU = (BigDecimal)UsefulParam.getValAndTypeValAndPlafondMajorant(allParamsOrederedByPlafondAsc, UsefulParam.PRX_U_ORDI_SERVEUR_PARAM_NAME, new BigDecimal(nbrEleve.intValue()), "Veuillez paramètrer le montant annuel par serveur<br> au cas où le nombre d'élèves = "+ nbrEleve+" .")[0];
                    Object[] ordiServrRedUPrxTypeValAndMarge = null;
                    try {
                        ordiServrRedUPrxTypeValAndMarge = UsefulParam.getValAndTypeValAndPlafondMajorant(allParamsOrederedByPlafondAsc, UsefulParam.RED_U_ORDI_SERVEUR_PARAM_NAME, new BigDecimal(nbrOrdiServeur.longValue()));
                    } catch (ParamException e) {
                        ordiServrRedUPrxTypeValAndMarge = new Object[]{BigDecimal.ZERO, DBEnumType.PARAM_TYPE_VAL_MONTANT, BigDecimal.ZERO};
                    }
                    ordiServrRedU = (BigDecimal)ordiServrRedUPrxTypeValAndMarge[0];
                    ordiServrRedUTypeVal = (String)ordiServrRedUPrxTypeValAndMarge[1];
                    ordiServrRedMarge = ((BigDecimal)ordiServrRedUPrxTypeValAndMarge[2]).longValue();
                    redFinale = BigDecimal.ZERO;
                    redFinaleTypeVal = DBEnumType.PARAM_TYPE_VAL_MONTANT;
                    autresCharges = BigDecimal.ZERO;
                }
                montantTotalApayer = Convention.getMontantTotalApayer(nbrOrdiClient, ordiClientPrxU, ordiClientRedU, ordiClientRedMarge,
                    nbrOrdiServeur,  ordiServrPrxU,  ordiServrRedU, ordiServrRedMarge,
                    redFinale, redFinaleTypeVal, 
                    autresCharges
                );
                montantTotalApayer = BigDecimalHelper.max(montantTotalApayer, BigDecimal.ZERO);
                totalVesment = (BigDecimal)data[et_data_indice.get("paymt_mtt")].getValue();
                paymentStatus = Convention.getPaymentStatus(montantTotalApayer, totalVesment);
                paymentValidation = Convention.getPaymentValidation(montantTotalApayer, totalVesment, isConventionFinished);
                
                reste = montantTotalApayer.subtract(totalVesment);
            }
    //        (String) data[et_data_indice.get("etblssmt_id")].getValue();
            /**/
            HashMap[] final_data = ( isPeripheriqueStationType)? new HashMap[15]: new HashMap[14];
                int finalDataIndice = 0; 
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{data[et_data_indice.get("identifiant")].getValue()            , href});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{type                                                          , href});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{data[et_data_indice.get("nom")].getValue()                    , href});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{data[et_data_indice.get("ville")].getValue()                  , href});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{data[et_data_indice.get("region")].getValue()                 , href});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{anneeSclr                                                     , href});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{deroulemnt                                                    , href});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{nbrEleve                                                      , href});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{nbrOrdiClient                                                 , href});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{nbrOrdiServeur                                                , href});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href", "class"}, new Object[]{paymentStatus                                                 , href, paymentValidation, "money"});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href", "type"}, new Object[]{montantTotalApayer                                            , href, "money"});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href", "type"}, new Object[]{totalVesment                                                  , href, "money"});
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href", "type"}, new Object[]{reste                                                         , href, "money"});
            if( isPeripheriqueStationType){
                String etablissmtSynchronisationState = PeriSynchro.getSynchronisationState((String)data[et_data_indice.get("l_etblssmt_conflit_cent_synchro_id")].getValue(), PeriSynchro.getDateSynchronisation((Timestamp)data[et_data_indice.get("l_etblssmt_t_synchro_date_reception")].getValue(), (Timestamp)data[et_data_indice.get("l_etblssmt_t_synchro_date_envoye")].getValue()), (Timestamp)data[et_data_indice.get("l_etblssmt_t_synchro_date_edition")].getValue());
                String admnstrtfSynchronisationState = PeriSynchro.getSynchronisationState((String)data[et_data_indice.get("l_admnstrtf_conflit_cent_synchro_id")].getValue(), PeriSynchro.getDateSynchronisation((Timestamp)data[et_data_indice.get("l_admnstrtf_t_synchro_date_reception")].getValue(), (Timestamp)data[et_data_indice.get("l_admnstrtf_t_synchro_date_envoye")].getValue()), (Timestamp)data[et_data_indice.get("l_admnstrtf_t_synchro_date_edition")].getValue());
                String synchronisationState = PeriSynchro.SYNCHRONISATION_STATE_PERFORMED;
                if(ArrayHelper.contains(PeriSynchro.SYNCHRONISATION_STATE_CONFLICT, etablissmtSynchronisationState, admnstrtfSynchronisationState))
                    synchronisationState = PeriSynchro.SYNCHRONISATION_STATE_CONFLICT;
                else if(ArrayHelper.contains(PeriSynchro.SYNCHRONISATION_STATE_NOT_YET_PERFORMED, etablissmtSynchronisationState, admnstrtfSynchronisationState))
                    synchronisationState = PeriSynchro.SYNCHRONISATION_STATE_NOT_YET_PERFORMED;
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},        new Object[]{synchronisationState, href});
            }/**/
            final_datas.add(final_data);
        }
        return final_datas;
    }

    public static void findEtablissements(HashMap<String, Object> hmapResponse, String sationCibleType, Connection connection, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize) throws ParamException, SQLException, Exception{
        HashMap<String, Object> hmapScope = (HashMap<String, Object>) hmapResponse.get("scope");
        long[] nombreEtbssmtEcolesAccsmdExistants = getNombreEtbssmtEcolesAccsmdExistants(sationCibleType, connection);
        hmapScope.put("nbre_etbssmt_existants", nombreEtbssmtEcolesAccsmdExistants[0]);
        hmapScope.put("nbre_ecole_existantes", nombreEtbssmtEcolesAccsmdExistants[1]);
        hmapScope.put("nbre_locax_accsmd_existants", nombreEtbssmtEcolesAccsmdExistants[2]);
        HashMap<String, Object> pagntData = new HashMap<>(5);
        hmapScope.put("pagnt_data", pagntData);
        pagntData.put("body", getEtblssmtData(sationCibleType, connection, pagntStartNum, pagntMaxSize));
        long nbrTotalLigne = nombreEtbssmtEcolesAccsmdExistants[0];
        pagntData.put("foot", getPagntFoot(pagntStartNum, pagntFootMaxSize, pagntMaxSize, nbrTotalLigne, "Etablissement"));
    }

    public static long[] getNombreEtbssmtEcolesAccsmdExistants(String sationCibleType, Connection connection) throws SQLException{
        String tabName = (Helper.centraleStationType.equals(sationCibleType))? "t_cent_synchro_lieu_affectation" : "t_peri_synchro_lieu_affectation";
        String sqlR = ""
              + "SELECT\n" +
              "       nbr_ecole_existantes, nbr_locax_accsmd_existants\n" +
              "FROM\n" +
              "     (SELECT count(*) AS nbr_ecole_existantes FROM "+tabName+" WHERE t_synchro_date_suppr IS NULL AND type_lieu = 'ecole') t_ecole\n" +
              "CROSS JOIN\n" +
              "     (SELECT count(*) AS nbr_locax_accsmd_existants FROM "+tabName+" WHERE t_synchro_date_suppr IS NULL AND type_lieu = 'acces_mad') t_locax_accsmd\n"
      ;
      Key[] nbrEcoleAndLocaxAccsmdExistantesKeys = new Key[]{new Key<>("nbr_ecole_existantes", Long.class), new Key<>("nbr_locax_accsmd_existants", Long.class)};
      CRUD.scalar(sqlR, connection, nbrEcoleAndLocaxAccsmdExistantesKeys);
      return new long[]{(Long)nbrEcoleAndLocaxAccsmdExistantesKeys[0].getValue()+(Long)nbrEcoleAndLocaxAccsmdExistantesKeys[1].getValue(), (Long)nbrEcoleAndLocaxAccsmdExistantesKeys[0].getValue(), (Long)nbrEcoleAndLocaxAccsmdExistantesKeys[1].getValue()};
    }

}
