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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import synchronisable.param.exception.ParamException;
import synchronisable.peripherique.PeriSynchro;
import usefull.ArrayHelper;
import usefull.HashHelper;
import usefull.NumberFormatHelper;
import usefull.dao.CRUD;
import usefull.dao.Helper;
import usefull.dao.Key;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class Materiel {
    private final static HashMap<String, Integer> type_mtrl_data_indice/*etblissmt_data_indice*/ = HashHelper.newHashMap(
        new String[] { "type_mtrl_id", "categorie", "type_mtrl_nom", "type_mtrl_qtte_en_marche","type_mtrl_qtte_total", "unite", "qtte_min_crtq", "conflit_cent_synchro_id", "t_synchro_date_reception", "t_synchro_date_envoye", "t_synchro_date_edition"},/**/
        new Integer[]{ 0,1,2,3,4,5,6, 7,8,9,10}/**/
    );
    
    public static List<HashMap<String,Object>[]> getMaterielData(String stationCibleType, Connection connection, long pagntStartNum, int pagntMaxSize, Long etablssmtId/*null by default*/) throws ParamException, Exception{
        boolean isPeripheriqueStationType = Helper.peripheriqueStationType.equals(stationCibleType);/**/
        /*synchronisation_state*/
        String SynhronisationCols = (!isPeripheriqueStationType)? "": " , le_conflit_cent_synchro_id, la_date_reception, la_date_envoye, la_date_edition ";/**/
        /*/synchronisation_state*/
        String sqlR = "SELECT le_type_mtrl_id, la_categorie, le_nom, la_quantie_en_marche, la_quantie_total, l_unite, la_qtte_min_crtq "+SynhronisationCols+" FROM get_type_mtrl_data("+pagntStartNum+", "+pagntMaxSize+", "+CRUD.stringify(etablssmtId)+")";
        Key[] orderedKeys = (isPeripheriqueStationType)?/**/
                new Key[]{new Key<>("type_mtrl_id", String.class), new Key<>("categorie", String.class), new Key<>("type_mtrl_nom", String.class), new Key<>("type_mtrl_qtte_en_marche", BigDecimal.class), new Key<>("type_mtrl_qtte_total", BigDecimal.class), new Key<>("unite", String.class), new Key<>("qtte_min_crtq", BigDecimal.class), new Key<>("conflit_cent_synchro_id", String.class), new Key<>("t_synchro_date_reception", Timestamp.class), new Key<>("t_synchro_date_envoye", Timestamp.class), new Key<>("t_synchro_date_edition", Timestamp.class)}
            :   new Key[]{new Key<>("type_mtrl_id", String.class), new Key<>("categorie", String.class), new Key<>("type_mtrl_nom", String.class), new Key<>("type_mtrl_qtte_en_marche", BigDecimal.class), new Key<>("type_mtrl_qtte_total", BigDecimal.class), new Key<>("unite", String.class), new Key<>("qtte_min_crtq", BigDecimal.class)};
        List<Key[]> datas = CRUD.readToKeysList(sqlR, connection, orderedKeys);
        int size = datas.size();  

        boolean isInEtablssmtContext = etablssmtId != null;
        List<HashMap<String,Object>[]> final_datas = new ArrayList<>(size);
        DecimalFormat quantiteDecimalFormat = NumberFormatHelper.QUANTITE_DECIMAL_FORMAT();
        String displayMethod = (etablssmtId == null)? "redirect" : "open_in_new_tab";
        for (int i = 0; i < size; i++) {
            Key[] data = datas.get(i);
            String typeMtrlId = ((String)data[type_mtrl_data_indice.get("type_mtrl_id")].getValue());
            String href = "TypeMateriel"+ "/" + typeMtrlId;
            String categorie = (String) data[type_mtrl_data_indice.get("categorie")].getValue();
            boolean isConsommable = "consommable".equals(categorie);
            String quantite = isConsommable?NumberFormatHelper.getQuantiteFormat((BigDecimal)data[type_mtrl_data_indice.get("type_mtrl_qtte_total")].getValue(), (String)data[type_mtrl_data_indice.get("unite")].getValue(), quantiteDecimalFormat): quantiteDecimalFormat.format((BigDecimal)data[type_mtrl_data_indice.get("type_mtrl_qtte_en_marche")].getValue()) +"/"+ quantiteDecimalFormat.format((BigDecimal)data[type_mtrl_data_indice.get("type_mtrl_qtte_total")].getValue());
            String quantiteStatus = "";
            BigDecimal quantiteDispo = isConsommable?(BigDecimal)data[type_mtrl_data_indice.get("type_mtrl_qtte_total")].getValue(): (BigDecimal)data[type_mtrl_data_indice.get("type_mtrl_qtte_en_marche")].getValue();
            if(quantiteDispo.compareTo(BigDecimal.ZERO)<0){//mtrl consommable avec perte de stablite pendant une transaction
                quantiteStatus = "bg-danger";
            }else if(data[type_mtrl_data_indice.get("qtte_min_crtq")].getValue() != null){
                if(quantiteDispo.compareTo(BigDecimal.ZERO) == 0)
                    quantiteStatus = "bg-danger";
                else{
                    BigDecimal qtteMinCrtq = (BigDecimal)data[type_mtrl_data_indice.get("qtte_min_crtq")].getValue();
                    if(!(quantiteDispo.compareTo(qtteMinCrtq)>0))
                        quantiteStatus = "bg-warning";
                }
            }            
            String addUri = "MaterielSimple";
            if((ArrayHelper.contains(categorie, "ordinateur")))
                addUri = "Ordinateur";
            else if((ArrayHelper.contains(categorie, "consommable")))
                addUri = "MaterielConsommable";
            addUri = addUri+"/add?type="+typeMtrlId;
            if(isInEtablssmtContext){
                href = (isConsommable)? href+"/"+etablssmtId+"?etablissmt="+etablssmtId: href+"/Materiel?etablissmt="+etablssmtId;
                addUri = addUri+"&lieu="+etablssmtId+"&etablissmt="+etablssmtId;
            }
            /**/
            HashMap[] final_data = ( isPeripheriqueStationType)? new HashMap[5]: new HashMap[4];
            int finalDataIndice = 0;
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},            new Object[]{categorie, href});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},            new Object[]{data[type_mtrl_data_indice.get("type_mtrl_nom")].getValue()   , href});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href", "class"},   new Object[]{quantite, href, quantiteStatus});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"display_add_bttn"}, new Object[]{HashHelper.newHashMap(new String[]{"uri", "method"}, new String[]{addUri, displayMethod})});
            if( isPeripheriqueStationType){
                String synchronisationState = PeriSynchro.getSynchronisationState((String)data[type_mtrl_data_indice.get("conflit_cent_synchro_id")].getValue(), PeriSynchro.getDateSynchronisation((Timestamp)data[type_mtrl_data_indice.get("t_synchro_date_reception")].getValue(), (Timestamp)data[type_mtrl_data_indice.get("t_synchro_date_envoye")].getValue()), (Timestamp)data[type_mtrl_data_indice.get("t_synchro_date_edition")].getValue());
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},        new Object[]{synchronisationState, href});
            }/**/
            final_datas.add(final_data);
        }return final_datas;
    }

    public static void findMateriels(HashMap<String, Object> hmapResponse, String sationCibleType, Connection connection, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize, Long etablssmtId/*null by default*/) throws ParamException, SQLException, Exception{
        HashMap<String, Object> hmapScope = (HashMap<String, Object>) hmapResponse.get("scope");
        Object[] nbreTypeMaterielsExistantAndLieuData = getNbreTypeMaterielsExistantAndLieuData(sationCibleType, connection, etablssmtId);
        hmapScope.put("nbre_type_materiels_existant", nbreTypeMaterielsExistantAndLieuData[0]);
        if(nbreTypeMaterielsExistantAndLieuData[1] != null){
            String typeLieu = (String)nbreTypeMaterielsExistantAndLieuData[4];
            String synchroName = ((typeLieu != null) && typeLieu.endsWith("cole"))? "EtablissementScolaire": "EtablissementAccesMad";
            hmapScope.put("etablissmt", HashHelper.newHashMap(
               new String[]{"id",                                    "synchro_id",                              "identifiant",                          "nom",                                  "type_lieu", "synchro_name", "type_etablissmt",                         "type_enseignmt",                        "niveau_enseignmt"}, 
               new Object[]{nbreTypeMaterielsExistantAndLieuData[1], nbreTypeMaterielsExistantAndLieuData[1], nbreTypeMaterielsExistantAndLieuData[2],  nbreTypeMaterielsExistantAndLieuData[3], typeLieu,   synchroName,    nbreTypeMaterielsExistantAndLieuData[5],   nbreTypeMaterielsExistantAndLieuData[6], nbreTypeMaterielsExistantAndLieuData[7]}
            ));
        }
        HashMap<String, Object> pagntData = new HashMap<>(5);
        hmapScope.put("pagnt_data", pagntData);
        pagntData.put("body", getMaterielData(sationCibleType, connection, pagntStartNum, pagntMaxSize, etablssmtId));
        long nbrTotalLigne = (Long)nbreTypeMaterielsExistantAndLieuData[0];
        String pagntFootSynchroName = (etablssmtId == null)? "Materiel": "Materiel/etablssmt_"+etablssmtId;
        pagntData.put("foot", getPagntFoot(pagntStartNum, pagntFootMaxSize, pagntMaxSize, nbrTotalLigne, pagntFootSynchroName));
    }

    public static Object[] getNbreTypeMaterielsExistantAndLieuData(String stationCibleType, Connection connection, Long etablssmtId) throws SQLException{
        String sqlR = ""
        + "SELECT the_nbr_tp_mtrl, the_lieu_id,  the_lieu_identifiant,  the_lieu_nom,  the_lieu_type, the_lieu_type_etablissmt,  the_lieu_type_enseignmt,  the_lieu_niveau_enseignmt \n" +
        " FROM get_nbrtpmtrl_etbl_id_idtf_nom_tp_lieu_tp_etab_tp_ens_niv_ens("+CRUD.stringify(etablssmtId)+")";
      Key[] keys = new Key[]{ new Key<>("the_nbr_tp_mtrl", Long.class), new Key<>("the_lieu_id", Long.class), new Key<>("the_lieu_identifiant", String.class), new Key<>("the_lieu_nom", String.class), new Key<>("the_lieu_type", String.class), new Key<>("the_lieu_type_etablissmt", String.class), new Key<>("the_lieu_type_enseignmt", String.class), new Key<>("the_lieu_niveau_enseignmt", String.class)};
      CRUD.scalar(sqlR, connection, keys);
      return new Object[]{((Key<Long>)keys[0]).getValue(), ((Key<Long>)keys[1]).getValue(), ((Key<String>)keys[2]).getValue(), ((Key<String>)keys[3]).getValue(), ((Key<String>)keys[4]).getValue(), ((Key<String>)keys[5]).getValue(), ((Key<String>)keys[6]).getValue(), ((Key<String>)keys[7]).getValue()};
    }

}
