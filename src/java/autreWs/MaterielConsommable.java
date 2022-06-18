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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import synchronisable.param.exception.ParamException;
import synchronisable.peripherique.PeriSynchro;
import usefull.HashHelper;
import usefull.NumberFormatHelper;
import usefull.SimpleDateFormatHelper;
import usefull.dao.CRUD;
import usefull.dao.Helper;
import usefull.dao.Key;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class MaterielConsommable {
    private final static HashMap<String, Integer> mtrl_cnsmmbl_data_indice/*etblissmt_data_indice*/ = HashHelper.newHashMap(
        new String[] {"synchro_id", "date_edition", "user_id", "login_email", "envent_type", "qtt", "description",  "conflit_cent_synchro_id", "t_synchro_date_reception", "t_synchro_date_envoye"},/**/
        new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}/**/
    );
    
        
    public static List<HashMap<String,Object>[]> getConsmmationEventData(String stationCibleType, Connection connection, String categorie, long typeMtrlId, long lieuId, String timeZone, String unite, long pagntStartNum, int pagntMaxSize, boolean isInEtablissmtContext) throws ParamException, Exception{
        boolean isPeripheriqueStationType = Helper.peripheriqueStationType.equals(stationCibleType);
        String materiel = "MaterielConsommable";
        /*synchronisation_state*/
        String SynhronisationCols = (! isPeripheriqueStationType)? "": " , conflit_cent_synchro_id, t_synchro_date_reception, t_synchro_date_envoye";/**/
        String prefixedSynhronisationCols = (! isPeripheriqueStationType)? "": " , t_event.conflit_cent_synchro_id, t_event.t_synchro_date_reception, t_event.t_synchro_date_envoye";/**/
        /*/synchronisation_state*/
        String sqlR = (Helper.centraleStationType.equals(stationCibleType))
            ? 
                "SELECT\n" +
                "    t_synchro_id, t_synchro_date_edition, t_event.t_user_id, t_login.t_cent_login_email, envent_type, qtt, description\n" +
                "FROM\n" +
                "     (  SELECT t_synchro_id, t_synchro_date_edition, t_user_id, envent_type, qtt, description\n" +
                "        FROM t_cent_synchro_consommation_event \n" +
                "        WHERE t_synchro_date_suppr IS NULL AND t_type_mtrl_id = "+typeMtrlId+" AND t_lieu_affectation_id = "+lieuId+" \n" +
                "        ORDER BY t_synchro_date_edition DESC , t_synchro_id ASC\n" +
                "        OFFSET "+pagntStartNum+" LIMIT "+pagntMaxSize+" \n" +
                "     ) t_event\n" +
                "     JOIN t_cent_user  t_user ON t_user.t_cent_user_id = t_event.t_user_id\n" +
                "     JOIN t_cent_login  t_login on t_user.t_cent_login_id = t_login.t_cent_login_id\n"
            :
                "SELECT\n" +
                "    t_synchro_id, t_synchro_date_edition, t_event.t_user_id, t_user.t_login_email, envent_type, qtt, description "+prefixedSynhronisationCols+" \n" + /**/
                "FROM\n" +
                "    (  SELECT t_synchro_id, t_synchro_date_edition, t_user_id, envent_type, qtt, description "+SynhronisationCols+" \n" + /**/
                "       FROM t_peri_synchro_consommation_event\n" +
                "        WHERE t_synchro_date_suppr IS NULL AND t_type_mtrl_id = "+typeMtrlId+" AND t_lieu_affectation_id = "+lieuId+" \n" +
                "       ORDER BY t_synchro_date_edition DESC , t_synchro_id ASC\n" +
                "       OFFSET "+pagntStartNum+" LIMIT "+pagntMaxSize+" \n" +
                "    ) t_event JOIN t_user_data t_user ON t_user.t_user_id = t_event.t_user_id"
        ;
        Key[] orderedKeys = (isPeripheriqueStationType)? /**/
                new Key[]{new Key<>("synchro_id", String.class), new Key<>("date_edition", Timestamp.class), new Key<>("user_id", String.class), new Key<>("login_email", String.class), new Key<>("envent_type", String.class), new Key<>("qtt", BigDecimal.class), new Key<>("description", String.class),  new Key<>("conflit_cent_synchro_id", String.class), new Key<>("t_synchro_date_reception", Timestamp.class), new Key<>("t_synchro_date_envoye", Timestamp.class)}
            :   new Key[]{new Key<>("synchro_id", String.class), new Key<>("date_edition", Timestamp.class), new Key<>("user_id", String.class), new Key<>("login_email", String.class), new Key<>("envent_type", String.class), new Key<>("qtt", BigDecimal.class), new Key<>("description", String.class)};
        List<Key[]> datas = CRUD.readToKeysList(sqlR, connection, orderedKeys);
        int size = datas.size();  
        List<HashMap<String,Object>[]> final_datas = new ArrayList<>(size);
        SimpleDateFormat sdfTimestampDisplay = SimpleDateFormatHelper.getSdfTimestampDisplay(timeZone);
        DecimalFormat quantiteDecimalFormatInstance = NumberFormatHelper.QUANTITE_DECIMAL_FORMAT();
        for (int i = 0; i < size; i++) {
            Key[] data = datas.get(i);
            String mtrlId = ((String)data[mtrl_cnsmmbl_data_indice.get("synchro_id")].getValue());
            String href = materiel+ "/" + mtrlId;
            if(isInEtablissmtContext)href = href+"?etablissmt="+lieuId;
            String qtt = NumberFormatHelper.getQuantiteFormat((BigDecimal)data[mtrl_cnsmmbl_data_indice.get("qtt")].getValue(), unite, quantiteDecimalFormatInstance);
            qtt = ("retrait".equals(data[mtrl_cnsmmbl_data_indice.get("envent_type")].getValue())) ? "- "+qtt: "+ "+qtt;
            Timestamp dateGMT = (Timestamp)data[mtrl_cnsmmbl_data_indice.get("date_edition")].getValue();
            String dateLocale = sdfTimestampDisplay.format(dateGMT);
            /**/
            HashMap[] final_data = ( isPeripheriqueStationType)? new HashMap[5]: new HashMap[4];
            int finalDataIndice = 0;
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{dateLocale , href});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{qtt, href});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{data[mtrl_cnsmmbl_data_indice.get("login_email")].getValue(), href});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{data[mtrl_cnsmmbl_data_indice.get("description")].getValue(), href});
            if( isPeripheriqueStationType){
                String synchronisationState = PeriSynchro.getSynchronisationState((String)data[mtrl_cnsmmbl_data_indice.get("conflit_cent_synchro_id")].getValue(), PeriSynchro.getDateSynchronisation((Timestamp)data[mtrl_cnsmmbl_data_indice.get("t_synchro_date_reception")].getValue(), (Timestamp)data[mtrl_cnsmmbl_data_indice.get("t_synchro_date_envoye")].getValue()), (Timestamp)data[mtrl_cnsmmbl_data_indice.get("date_edition")].getValue());
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},        new Object[]{synchronisationState, href});
            }/**/
            final_datas.add(final_data);
        }
        return final_datas;
    }

    public static void findListOfEvent(HashMap<String, Object> hmapResponse, String sationCibleType, Connection connection, long typeMtrlId, long lieuId, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize, boolean isInEtablissmtContext) throws ParamException, SQLException, Exception{
        HashMap<String, Object> hmapScope = (HashMap<String, Object>) hmapResponse.get("scope");
        Object[] nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone = getNomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone(sationCibleType, connection, typeMtrlId, lieuId);
        String categorie = (String)nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[1];
        HashMap<String, Object>  typeMtrl = HashHelper.newHashMap(new String[]{"synchro_id", "id", "nom","categorie", "qtt"}, new Object[]{typeMtrlId, typeMtrlId, nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[0], categorie, nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[9]});
        if(nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[2] != null)//unite
            typeMtrl.put("unite"/*existant*/, nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[2]);
        typeMtrl.put("nbr_mtrl_total"/*existant*/, nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[9]);
        hmapScope.put("type_mtrl", typeMtrl);
        String typeLieu = (String) nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[5];
        String synchroName = "ecole".equals(typeLieu)? "EtablissementScolaire": "EtablissementAccesMad";
        hmapScope.put("etablissmt", HashHelper.newHashMap(new String[]{"synchro_id", "id", "nom", "identifiant", "type_lieu", "synchro_name", "type_etablissmt", "type_enseignmt", "niveau_enseignmt"}, 
            new Object[]{
                lieuId, lieuId, 
                nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[3], 
                nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[4], 
                typeLieu, synchroName, 
                nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[6], 
                nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[7], 
                nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[8]
        }));
        HashMap<String, Object> pagntData = new HashMap<>(5);
        hmapScope.put("pagnt_data", pagntData);
        String timeZone = (String)nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[11];
        String unite = (String)nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[2];
        pagntData.put("body", getConsmmationEventData(sationCibleType, connection, categorie, typeMtrlId, lieuId, timeZone, unite, pagntStartNum, pagntMaxSize, isInEtablissmtContext));
        long nbrTotalLigne = (Long) nomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone[10];
        if(isInEtablissmtContext) pagntData.put("foot", getPagntFoot(pagntStartNum, pagntFootMaxSize, pagntMaxSize, nbrTotalLigne, "ConsommEventList/"+typeMtrlId+"/"+lieuId, new String[]{"etablissmt", ""+lieuId}));
        else pagntData.put("foot", getPagntFoot(pagntStartNum, pagntFootMaxSize, pagntMaxSize, nbrTotalLigne, "ConsommEventList/"+typeMtrlId+"/"+lieuId));
    }

    public static Object[] getNomCategorieUniteTypeMtrlEtIdtfNomLieuAutreDesignationEtQtteDispoEtNbrTotalEventExistantTimezone(String stationCibleType, Connection connection, long typeMtrlId, long lieuId) throws SQLException{
        String sqlR =
        "   SELECT le_type_mtrl_nom, le_type_mtrl_categorie, le_type_mtrl_unite, le_lieu_nom, le_lieu_identifiant, le_lieu_type, le_lieu_type_etablissmt, le_lieu_type_enseignmt, le_lieu_niveau_enseignmt, la_quantie_dispo, le_nbr_event, le_timezone \n"
        + " FROM get_cnsmmbl_tpmtrldata_and_lieudata_andqttdisponbreventtimezone("+CRUD.stringify(typeMtrlId)+", "+CRUD.stringify(lieuId)+")"
        ;
        Key[] keys = new Key[]{ new Key<>("le_type_mtrl_nom", String.class), new Key<>("le_type_mtrl_categorie", String.class), new Key<>("le_type_mtrl_unite", String.class), new Key<>("le_lieu_nom", String.class), new Key<>("le_lieu_identifiant", String.class), new Key<>("le_lieu_type", String.class), new Key<>("le_lieu_type_etablissmt", String.class), new Key<>("le_lieu_type_enseignmt", String.class), new Key<>("le_lieu_niveau_enseignmt", String.class), new Key<>("la_quantie_dispo", BigDecimal.class), new Key<>("le_nbr_event", Long.class), new Key<>("le_timezone", String.class)};
        CRUD.scalar(sqlR, connection, keys);
        return new Object[]{ ((Key<String>)keys[0]).getValue(), ((Key<String>)keys[1]).getValue(), ((Key<String>)keys[2]).getValue(), ((Key<String>)keys[3]).getValue(), ((Key<String>)keys[4]).getValue(), ((Key<String>)keys[5]).getValue(), ((Key<String>)keys[6]).getValue(), ((Key<String>)keys[7]).getValue(), ((Key<String>)keys[8]).getValue(), ((Key<BigDecimal>)keys[9]).getValue(), ((Key<Long>)keys[10]).getValue(), ((Key<String>)keys[11]).getValue()};
    }

}
