/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autreWs;

import static autreWs.Function.getPagntFoot;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import synchronisable.param.exception.ParamException;
import synchronisable.peripherique.PeriSynchro;
import usefull.HashHelper;
import usefull.NumberFormatHelper;
import usefull.dao.CRUD;
import usefull.dao.Helper;
import usefull.dao.Key;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class ListeMateriel {
    private final static HashMap<String, Integer> mtrl_non_cnsmmbl_data_indice/*etblissmt_data_indice*/ = HashHelper.newHashMap(
        new String[] {"mtrl_id", "etat_mtrl", "lieu", "type_aff", "identifiant", "observation", "conflit_cent_synchro_id", "t_synchro_date_reception", "t_synchro_date_envoye", "t_synchro_date_edition"},/**/
        new Integer[]{0,1,2,3,4,5,6,7,8,9}/**/
    );
    
    private final static HashMap<String, Integer> mtrl_cnsmmbl_data_indice/*etblissmt_data_indice*/ = HashHelper.newHashMap(
        new String[] {"lieu_id", "lieu", "qtt"},
        new Integer[]{0,1,2}
    );
        
    public static List<HashMap<String,Object>[]> getMaterielData(String stationCibleType, Connection connection, String categorie, String unite, long typeMtrlId, long pagntStartNum, int pagntMaxSize, Long etablssmtId/*null by default*/, boolean deleted) throws ParamException, Exception{
        boolean isPeripheriqueStationType = Helper.peripheriqueStationType.equals(stationCibleType);/**/
        String tabName = (! isPeripheriqueStationType)? "t_cent_synchro_mtrl_simple": "t_peri_synchro_mtrl_simple";
        String materiel = "MaterielSimple";
        boolean isConsommable = false;
        if("ordinateur".equals(categorie)){
            materiel = "Ordinateur";
            tabName = (! isPeripheriqueStationType)? "t_cent_synchro_mtrl_ordi": "t_peri_synchro_mtrl_ordi";
        }else if("consommable".equals(categorie)){
            materiel = "MaterielConsommable";
            tabName = (! isPeripheriqueStationType)? "t_cent_synchro_consommation_event": "t_peri_synchro_consommation_event";
            isConsommable = true;
        }
        String whereLieuAffctId = "";
        String lieuCol = " t_lieu.identifiant||': '||t_lieu.nom AS lieu ";
        String lieuTab = (isConsommable)
                ? " JOIN\n t_cent_synchro_lieu_affectation t_lieu ON consomm_event.t_lieu_affectation_id = t_lieu.t_synchro_id \n"
                : "         JOIN t_cent_synchro_lieu_affectation t_lieu ON t_lieu.t_synchro_id = temp_mtrl.t_lieu_affectation_id \n "
        ;
        if(etablssmtId != null){
            lieuCol = " '' AS lieu";
            lieuTab = "";
            whereLieuAffctId = " AND t_lieu_affectation_id = "+etablssmtId+" ";
        }String deleteConstraint = deleted? "AND t_synchro_date_suppr IS NOT NULL" : "AND t_synchro_date_suppr IS NULL";
        
        /*synchronisation_state*/
        String SynhronisationCols = (! isPeripheriqueStationType)? "": " , conflit_cent_synchro_id, t_synchro_date_reception, t_synchro_date_envoye, t_synchro_date_edition ";/**/
        String prefixedSynhronisationCols = (! isPeripheriqueStationType)? "": " , temp_mtrl.conflit_cent_synchro_id, temp_mtrl.t_synchro_date_reception, temp_mtrl.t_synchro_date_envoye, temp_mtrl.t_synchro_date_edition ";/**/
        /*/synchronisation_state*/
        
        String sqlR = (!isConsommable)?
            "WITH temp_mtrl AS (\n" +
            "    SELECT t_synchro_id, t_etat_mtrl_id, t_lieu_affectation_id, t_type_affectation_id, identifiant, observation "+SynhronisationCols+" \n" + /**/
            "    FROM "+tabName+" \n" +
            "    WHERE t_type_mtrl_id = "+typeMtrlId+whereLieuAffctId+" "+deleteConstraint+" \n" +
            "    ORDER BY t_etat_mtrl_id, t_lieu_affectation_id, identifiant, t_synchro_id\n" +
            "    OFFSET "+pagntStartNum+" LIMIT "+pagntMaxSize+" \n" +
            ")   SELECT\n" +
            "        temp_mtrl.t_synchro_id, t_etat_mtrl.nom, "+lieuCol+", t_type_aff.nom, temp_mtrl.identifiant, temp_mtrl.observation \n" + /**/
            "       "+prefixedSynhronisationCols+" \n" + /**/
            "       , t_etat_mtrl_id, t_lieu_affectation_id, t_type_affectation_id\n" + /**/
            "FROM temp_mtrl\n" +
            "         JOIN t_cent_synchro_etat_mtrl t_etat_mtrl ON t_etat_mtrl.t_synchro_id = temp_mtrl.t_etat_mtrl_id\n" +
            lieuTab +
            "         JOIN t_cent_synchro_type_affectation t_type_aff on t_type_aff.t_synchro_id = temp_mtrl.t_type_affectation_id\n"+
            "ORDER BY t_etat_mtrl_id, t_lieu_affectation_id, identifiant\n"
                
                :
                
            "SELECT t_lieu.t_synchro_id AS lieu_id, "+lieuCol+", SUM(consomm_event.qtt) AS qtt\n" +
            "FROM "+
            "     (   SELECT t_lieu_affectation_id, CASE WHEN envent_type = 'retrait'::consommation_event_type THEN - qtt ELSE qtt END AS qtt\n" +
            "         FROM "+tabName+" WHERE t_type_mtrl_id = "+typeMtrlId+whereLieuAffctId+" "+deleteConstraint+" \n" +
            "     ) consomm_event \n"+
            lieuTab +
            "GROUP BY lieu_id, lieu\n" +
            "ORDER BY lieu_id\n" +
            "OFFSET "+pagntStartNum+" LIMIT "+pagntMaxSize
        ;
        if( isPeripheriqueStationType)
            sqlR = sqlR.replaceAll("t_cent_synchro_", "t_peri_synchro_").replaceAll("conflit_peri_synchro_id", "conflit_cent_synchro_id");
        Key[] orderedKeys = null; /**/
        if(!isConsommable){/**/
            orderedKeys = (isPeripheriqueStationType)?
                new Key[]{new Key<>("mtrl_id", String.class), new Key<>("etat_mtrl", String.class), new Key<>("lieu", String.class), new Key<>("type_aff", String.class), new Key<>("identifiant", String.class), new Key<>("observation", String.class), new Key<>("conflit_cent_synchro_id", String.class), new Key<>("t_synchro_date_reception", Timestamp.class), new Key<>("t_synchro_date_envoye", Timestamp.class), new Key<>("t_synchro_date_edition", Timestamp.class)}
            :   new Key[]{new Key<>("mtrl_id", String.class), new Key<>("etat_mtrl", String.class), new Key<>("lieu", String.class), new Key<>("type_aff", String.class), new Key<>("identifiant", String.class), new Key<>("observation", String.class)};
        }else{/**/
            orderedKeys = new Key[]{new Key<>("lieu_id", String.class), new Key<>("lieu", String.class), new Key<>("qtt", BigDecimal.class)};
        }List<Key[]> datas = CRUD.readToKeysList(sqlR, connection, orderedKeys);
        int size = datas.size();
        List<HashMap<String,Object>[]> final_datas = new ArrayList<>(size);
//            boolean isConsommable = (ArrayHelper.contains(categorie, "consommable"));
        DecimalFormat quantiteDecimalFormatInstance = NumberFormatHelper.getQuantiteDecimalFormatInstance();
        for (int i = 0; i < size; i++) {
            Key[] data = datas.get(i);
            if(!isConsommable){
                String mtrlId = ((String)data[mtrl_non_cnsmmbl_data_indice.get("mtrl_id")].getValue());
                String href = materiel+ "/" + mtrlId;
                if(etablssmtId != null)href = href+"?etablissmt="+etablssmtId;
//                String addUri = materiel+"/add?type="+mtrlId;
                String etat = (String)data[mtrl_non_cnsmmbl_data_indice.get("etat_mtrl")].getValue();
                String etatTolower = etat.toLowerCase();
                String classStatus = "";
                if(etatTolower.contains("en")&&etatTolower.contains("marche")) classStatus = "bg-success";
                if(etatTolower.contains("en")&&etatTolower.contains("panne")) classStatus = "bg-warning";
                if(etatTolower.contains("detruit") || etatTolower.contains("détruit")) classStatus = "bg-danger";
                /**/
                int finalDataSize = (etablssmtId == null)? 5: 4;
                if( isPeripheriqueStationType) 
                    finalDataSize ++;
                HashMap[] finalData = new HashMap[finalDataSize];
                int finalDataIndice = 0;
                finalData[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href", "class"},   new Object[]{etat, href, classStatus});
                if(etablssmtId == null){
                    finalData[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},            new Object[]{data[mtrl_non_cnsmmbl_data_indice.get("lieu")].getValue()        , href});
                }finalData[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},            new Object[]{data[mtrl_non_cnsmmbl_data_indice.get("type_aff")].getValue()        , href});
                finalData[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},            new Object[]{data[mtrl_non_cnsmmbl_data_indice.get("identifiant")].getValue()        , href});
                finalData[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},            new Object[]{data[mtrl_non_cnsmmbl_data_indice.get("observation")].getValue()        , href});
                if( isPeripheriqueStationType){
                    String synchronisationState = PeriSynchro.getSynchronisationState((String)data[mtrl_non_cnsmmbl_data_indice.get("conflit_cent_synchro_id")].getValue(), PeriSynchro.getDateSynchronisation((Timestamp)data[mtrl_non_cnsmmbl_data_indice.get("t_synchro_date_reception")].getValue(), (Timestamp)data[mtrl_non_cnsmmbl_data_indice.get("t_synchro_date_envoye")].getValue()), (Timestamp)data[mtrl_non_cnsmmbl_data_indice.get("t_synchro_date_edition")].getValue());
                    finalData[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},        new Object[]{synchronisationState, href});
                }/**/
                final_datas.add(finalData);
            }else{
                String leuId = ((String)data[mtrl_cnsmmbl_data_indice.get("lieu_id")].getValue());
                String href = "TypeMateriel/"+typeMtrlId+"/"+leuId;
                String addUri = materiel+"/add?"+String.join("&", "type="+typeMtrlId, "lieu="+leuId);
                BigDecimal qtt = ((BigDecimal)data[mtrl_cnsmmbl_data_indice.get("qtt")].getValue());
                String classStatus = (qtt.compareTo(BigDecimal.ZERO)<0)? "bg-danger": "";
                HashMap[] final_data =  (etablssmtId == null)
                ? new HashMap[]{
                    HashHelper.newHashMap(new String[]{"value", "href"},   new Object[]{((String)data[mtrl_cnsmmbl_data_indice.get("lieu")].getValue()), href}),
                    HashHelper.newHashMap(new String[]{"value", "href", "class"}, new Object[]{ NumberFormatHelper.getQuantiteFormat(qtt, unite, quantiteDecimalFormatInstance), href, classStatus}),
                    HashHelper.newHashMap(new String[]{"display_add_bttn"}, new Object[]{HashHelper.newHashMap(new String[]{"uri", "method"}, new String[]{addUri, "redirect"})})
                }
                : new HashMap[]{
                    HashHelper.newHashMap(new String[]{"value", "href", "class"}, new Object[]{ NumberFormatHelper.getQuantiteFormat(qtt, unite, quantiteDecimalFormatInstance), href, classStatus}),
                    HashHelper.newHashMap(new String[]{"display_add_bttn"}, new Object[]{HashHelper.newHashMap(new String[]{"uri", "method"}, new String[]{addUri, "redirect"})})
                };final_datas.add(final_data);
            }
        }
        return final_datas;
    }

    public static void findListOfMateriel(HashMap<String, Object> hmapResponse, String sationCibleType, Connection connection, long typeMtrlId, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize, Long etablssmtId/*null by default*/, boolean deleted) throws ParamException, SQLException, Exception{
        HashMap<String, Object> hmapScope = (HashMap<String, Object>) hmapResponse.get("scope");
        Object[][] nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlIdEtLieuData = getNomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlIdEtLieuData(sationCibleType, connection, typeMtrlId, etablssmtId, deleted);
        Object[] nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId = nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlIdEtLieuData[0];
        String categorie = (String)nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[1];
        HashMap<String, Object>  typeMtrl = HashHelper.newHashMap(new String[]{"id", "synchro_id", "nom","categorie"}, new Object[]{typeMtrlId, typeMtrlId, nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[0], categorie});
        if(nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[2] != null)
            typeMtrl.put("nbr_mtrl_en_marche"/*existant*/, nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[2]);
        String unite = (String)nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[4];
        if(unite != null)//unite
            typeMtrl.put("unite"/*existant*/, unite);
        typeMtrl.put("nbr_mtrl_total"/*existant*/, nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[3]);
        hmapScope.put("type_mtrl", typeMtrl);
        Object[] lieuIdIdtfNomTypeTpEtablssmtTpEnsNivEns = nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlIdEtLieuData[1];
        HashMap<String, Object> etablissmt = HashHelper.newHashMap(new String[]{"id", "identifiant", "nom", "type_lieu", "type_etablissmt", "type_enseignmt", "niveau_enseignmt"}, lieuIdIdtfNomTypeTpEtablssmtTpEnsNivEns);
        String  synchroName = ((etablissmt.get("type_lieu") != null) && ((String)etablissmt.get("type_lieu")).endsWith("cole"))? "EtablissementScolaire": "EtablissementAccesMad";
        etablissmt.put("synchro_id", etablissmt.get("id"));
        etablissmt.put("synchro_name", synchroName);
        hmapScope.put("etablissmt", etablissmt);
        HashMap<String, Object> pagntData = new HashMap<>(5);
        pagntData.put("body", getMaterielData(sationCibleType, connection, (String)nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[1], unite, typeMtrlId, pagntStartNum, pagntMaxSize, etablssmtId, deleted));
        long nbrTotalLigne = ("consommable".equals(categorie))?(Long)nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[5]/*NbrDeLieu*/: ((Number)nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[3]).longValue()/*NbrMtrlTotalExistant*/;
        
        String[][] keyValParams = null;
        if((etablssmtId != null) || (deleted == true)){
            int indice = 0; keyValParams = new String[2][];
            if(etablssmtId != null) keyValParams[indice++] = new String[]{"etablissmt", etablssmtId.toString()};
            if(deleted) keyValParams[indice] = new String[]{"deleted", ""+deleted};
        }pagntData.put("foot", getPagntFoot(pagntStartNum, pagntFootMaxSize, pagntMaxSize, nbrTotalLigne, "ListeMateriel/"+typeMtrlId, keyValParams));
        hmapScope.put("pagnt_data", pagntData);
    }

    public static Object[][] getNomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlIdEtLieuData(String sationCibleType, Connection connection, long typeMtrlId, Long etablssmtId, boolean deleted) throws SQLException{
        String functionName = deleted? " getNomCtgrieUniteTypeMtrlEtNbrMtrlEnMarcheTotalSupprime": " getNomCtgrieUniteTypeMtrlEtNbrMtrlEnMarcheTotalExistant";
        String suffixe = deleted? "supprime": "existant";
        String sqlR = ""+
            "SELECT le_nom, la_categorie, le_nbr_total_mtrl_en_marche_"+suffixe+" , le_nbr_total_mtrl_"+suffixe+", l_unite, le_nbr_lieu, \n" +
            " the_lieu_id, the_lieu_identifiant, the_lieu_nom, the_lieu_type, the_lieu_type_etablissmt, the_lieu_type_enseignmt, the_lieu_niveau_enseignmt \n" +
            " FROM "+functionName+"("+ CRUD.stringify(typeMtrlId) +", "+ CRUD.stringify(etablssmtId) +") \n"+
            "CROSS JOIN get_etblissmt_id_idtf_nom_tp_lieu_tp_etab_tp_ens_niv_ens("+CRUD.stringify(etablssmtId)+")"
        ;
        Key[] nbreTypeMaterielsExistant = new Key[]{
            new Key<>("le_nom", String.class), new Key<>("la_categorie", String.class), new Key<>("le_nbr_total_mtrl_en_marche_existant", Long.class), new Key<>("le_nbr_total_mtrl_existant", BigDecimal.class), new Key<>("l_unite", String.class), new Key<>("le_nbr_lieu", Long.class),
            new Key<>("the_lieu_id", BigInteger.class), new Key<>("the_lieu_identifiant", String.class), new Key<>("the_lieu_nom", String.class), new Key<>("the_lieu_type", String.class), new Key<>("the_lieu_type_etablissmt", String.class), new Key<>("the_lieu_type_enseignmt", String.class), new Key<>("the_lieu_niveau_enseignmt", String.class)
        };
        CRUD.scalar(sqlR, connection, nbreTypeMaterielsExistant);
        Object[] nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId = new Object[]{((Key<String>)nbreTypeMaterielsExistant[0]).getValue(), ((Key<String>)nbreTypeMaterielsExistant[1]).getValue(), ((Key<Long>)nbreTypeMaterielsExistant[2]).getValue(), ((Key<BigDecimal>)nbreTypeMaterielsExistant[3]).getValue(), ((Key<String>)nbreTypeMaterielsExistant[4]).getValue(), ((Key<Long>)nbreTypeMaterielsExistant[5]).getValue()};
        if(nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[3] == null)nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[3] = BigDecimal.ZERO;
        if(nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[5] == null)nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId[5] = new Long(0);
        Object[] lieuIdIdtfNomTypeTpEtablssmtTpEnsNivEns = new Object[]{((Key<BigInteger>)nbreTypeMaterielsExistant[6]).getValue(), ((Key<String>)nbreTypeMaterielsExistant[7]).getValue(), ((Key<String>)nbreTypeMaterielsExistant[8]).getValue(), ((Key<String>)nbreTypeMaterielsExistant[9]).getValue(), ((Key<String>)nbreTypeMaterielsExistant[10]).getValue(), ((Key<String>)nbreTypeMaterielsExistant[11]).getValue(), ((Key<String>)nbreTypeMaterielsExistant[12]).getValue()};
        return new Object[][]{nomCategorieTypeMtrlEtNbrMtrlEnMarcheEtTotalExistantEtUniteEtNbrDeLieuByTypeMtrlId, lieuIdIdtfNomTypeTpEtablssmtTpEnsNivEns};
    }

}
