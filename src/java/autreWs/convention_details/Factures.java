/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autreWs.convention_details;

import static autreWs.Function.getPagntFoot;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import synchronisable.Synchro;
import synchronisable.param.exception.ParamException;
import synchronisable.peripherique.PeriSynchro;
import synchronisation.SynchronisationConfig;
import usefull.HashHelper;
import usefull.NumberFormatHelper;
import usefull.SimpleDateFormatHelper;
import usefull.StringHelper;
import usefull.dao.CRUD;
import usefull.dao.Helper;
import usefull.dao.Key;
import usefull.dao.exception.PKNotFoundException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class Factures {
    final static List<List<String>> ALL_SYNCHRO_NAME = SynchronisationConfig.getAllSynchroName();
    final static String CONVENTION_SYNCHRO_NAME = ALL_SYNCHRO_NAME.get(3).get(3);//Convention
    private final static String SYNCHRO_NAME = ALL_SYNCHRO_NAME.get(4).get(0);//Facture
    private final static HashMap<String, Integer> DATA_INDICE/*etblissmt_data_indice*/ = HashHelper.newHashMap(
        new String[] {"synchro_id", "date", "desc", "total", "email", "last_edit_date", "identifiant",  "conflit_cent_synchro_id", "t_synchro_date_reception", "t_synchro_date_envoye"},/**/
        new Integer[]{0, 1, 2, 3, 4, 5, 6, 7,8,9}/**/
    );
    
    private final static NumberFormat MONEY_DECIMAL_FORMAT = NumberFormatHelper.MONEY_DECIMAL_FORMAT();

    public static List<HashMap<String,Object>[]> getPageData(long conventionId, HashMap<String, Object> neededData, String stationCibleType, Connection connection, long pagntStartNum, int pagntMaxSize) throws ParamException, Exception{
        boolean isPeripheriqueStationType = Helper.peripheriqueStationType.equals(stationCibleType);/**/
        /*synchronisation_state*/
        String SynhronisationCols = (! isPeripheriqueStationType)? "": " , conflit_cent_synchro_id, t_synchro_date_reception, t_synchro_date_envoye ";/**/
        String prefixedSynhronisationCols = (! isPeripheriqueStationType)? "": " , tab.conflit_cent_synchro_id, tab.t_synchro_date_reception, tab.t_synchro_date_envoye ";/**/
        /*/synchronisation_state*/
        String sqlR = (!isPeripheriqueStationType)
            ? "SELECT\n" +
            "       tab.t_synchro_id AS synchro_id,\n" +
            "       date,\n" +
            "       vrsmt_desc,\n" +
            "       total_vesrment,\n" +
            "       t_login.t_cent_login_email,\n" +
            "       last_edit_date\n" +
            "       , tab.identifiant\n" +
            "    from (\n" +
            "            SELECT identifiant, date, vrsmt_desc, t_user_id, t_synchro_date_edition AS last_edit_date, t_synchro_id, COALESCE(SUM(VRSMT_MTT - coalesce(FACT_AVR_MTT, 0)), 0)::MONTANT AS total_vesrment\n" +
            "            FROM t_cent_synchro_facture\n" +
            "            WHERE t_convention_id = "+conventionId+" AND t_synchro_date_suppr IS NULL \n" +
            "            GROUP BY  identifiant, date, vrsmt_desc, t_user_id, t_synchro_date_edition, t_synchro_id \n" +
            "            ORDER BY date DESC, identifiant ASC, t_synchro_date_edition DESC, t_synchro_id ASC \n" +
            "            OFFSET "+pagntStartNum+" LIMIT "+pagntMaxSize+" \n" +
            "    ) tab\n" +
            "    JOIN t_cent_user  t_user ON t_user.t_cent_user_id = tab.t_user_id\n" +
            "    JOIN t_cent_login  t_login on t_user.t_cent_login_id = t_login.t_cent_login_id"

            : "SELECT\n" +
            "    tab.t_synchro_id AS synchro_id,\n" +
            "    date,\n" +
            "    vrsmt_desc,\n" +
            "    total_vesrment,\n" +
            "    t_user.t_login_email,\n" +
            "    last_edit_date,\n" +
            "    tab.identifiant\n" +
            "  "+prefixedSynhronisationCols+ " \n" +/**/
            "from (\n" +
            "         SELECT identifiant, date, vrsmt_desc, t_user_id, t_synchro_date_edition AS last_edit_date, t_synchro_id, COALESCE(SUM(VRSMT_MTT - coalesce(FACT_AVR_MTT, 0)), 0)::MONTANT AS total_vesrment "+SynhronisationCols+" \n" +/**/
            "         FROM t_peri_synchro_facture\n" +
            "         WHERE t_convention_id = "+conventionId+" AND t_synchro_date_suppr IS NULL\n" +
            "         GROUP BY  identifiant, date, vrsmt_desc, t_user_id, t_synchro_date_edition, t_synchro_id \n" +
            "         ORDER BY date DESC, identifiant ASC, t_synchro_date_edition DESC, t_synchro_id ASC \n" +
            "            OFFSET "+pagntStartNum+" LIMIT "+pagntMaxSize+" \n" +
            "     ) tab\n" +
            "     JOIN t_user_data t_user ON t_user.t_user_id = tab.t_user_id"
        ;
        Key[] orderedKeys = (isPeripheriqueStationType)?/**/
                new Key[]{new Key<>("synchro_id", Long.class), new Key<>("date", Date.class), new Key<>("vrsmt_desc", String.class), new Key<>("total_vesrment", BigDecimal.class, BigDecimal.ZERO), new Key<>("email", String.class), new Key<>("last_edit_date", Timestamp.class), new Key<>("tab.identifiant", String.class)     , new Key<>("conflit_cent_synchro_id", String.class), new Key<>("t_synchro_date_reception", Timestamp.class), new Key<>("t_synchro_date_envoye", Timestamp.class)}
            :   new Key[]{new Key<>("synchro_id", Long.class), new Key<>("date", Date.class), new Key<>("vrsmt_desc", String.class), new Key<>("total_vesrment", BigDecimal.class, BigDecimal.ZERO), new Key<>("email", String.class), new Key<>("last_edit_date", Timestamp.class), new Key<>("tab.identifiant", String.class)};
        List<Key[]> datas = CRUD.readToKeysList(sqlR, connection, orderedKeys);
        SimpleDateFormat sdfTimestampDisplayLocale = SimpleDateFormatHelper.getSdfTimestampDisplay((TimeZone) neededData.get("timezone"));
        SimpleDateFormat sdfDateDisplayGMT = SimpleDateFormatHelper.getSdfDateDisplay(CRUD.GMTTimezone);

        int size = datas.size();  
        List<HashMap<String,Object>[]> final_datas = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Key[] data = datas.get(i);
            Long synchroId = ((Long)data[DATA_INDICE.get("synchro_id")].getValue());
            String href = SYNCHRO_NAME+ "/" + synchroId;
            BigDecimal montant = (BigDecimal) data[DATA_INDICE.get("total")].getValue();
            /**/
            HashMap[] final_data = (isPeripheriqueStationType)? new HashMap[7]: new HashMap[6];
            int finalDataIndice = 0;
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{sdfDateDisplayGMT.format((Date)data[DATA_INDICE.get("date")].getValue()), href});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{(String)data[DATA_INDICE.get("identifiant")].getValue(), href});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{ MONEY_DECIMAL_FORMAT.format(montant) , href});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{data[DATA_INDICE.get("desc")].getValue(), href});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{data[DATA_INDICE.get("email")].getValue(), href});
            final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{sdfTimestampDisplayLocale.format((Timestamp)data[DATA_INDICE.get("last_edit_date")].getValue()), href});
            if( isPeripheriqueStationType){
                String synchronisationState = PeriSynchro.getSynchronisationState((String)data[DATA_INDICE.get("conflit_cent_synchro_id")].getValue(), PeriSynchro.getDateSynchronisation((Timestamp)data[DATA_INDICE.get("t_synchro_date_reception")].getValue(), (Timestamp)data[DATA_INDICE.get("t_synchro_date_envoye")].getValue()), (Timestamp)data[DATA_INDICE.get("last_edit_date")].getValue());
                final_data[finalDataIndice++] = HashHelper.newHashMap(new String[]{"value", "href"},        new Object[]{synchronisationState, href});
            }/**/
            final_datas.add(final_data);
        }
        return final_datas;
    }

    public static void pagine(long conventionId, HashMap<String, Object> hmapResponse, String stationCibleType, Connection connection, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize) throws SQLException, Exception{
        HashMap<String, Object> hmapScope = (HashMap<String, Object>) hmapResponse.get("scope");
        HashMap<String, Object> data = getData(conventionId, hmapResponse, stationCibleType, connection);
        if(Boolean.FALSE.equals(hmapResponse.get("status"))) return;
        HashMap<String, Object> pagntData = new HashMap<>(6);
        hmapScope.put("pagnt_data", pagntData);
        pagntData.put("head", data.get("title"));
        pagntData.put("body", getPageData(conventionId, data, stationCibleType, connection, pagntStartNum, pagntMaxSize));
        long nbrTotalLigne = (data.get("nbr_ligne") == null)? 0: (Long)data.get("nbr_ligne");
        pagntData.put("foot", getPagntFoot(pagntStartNum, pagntFootMaxSize, pagntMaxSize, nbrTotalLigne, CONVENTION_SYNCHRO_NAME+"/"+conventionId+"/"+SYNCHRO_NAME));
    }
    
    public static <SynchroType extends Synchro> HashMap<String, Object> getData(long conventionId, HashMap<String, Object> hmapResponse, String stationCibleType, Connection connection) throws SQLException {
        HashMap<String, Object> hmapScope = (HashMap<String, Object>) hmapResponse.get("scope");
        HashMap<String, Object> data = new HashMap<>(3);
        String[] tabName = new String[]{"the_cent_timezone" , "t_cent_synchro_facture", "t_cent_synchro_convention", "t_cent_synchro_ann_sclr", "t_cent_synchro_lieu_affectation", "t_cent_synchro_type_etablissmt", "t_cent_synchro_type_enseignmt", "t_cent_synchro_niveau_enseignmt"};
        if(Helper.peripheriqueStationType.equals(stationCibleType)){
            int length = tabName.length;
            for (int i = 0; i < length; i++) 
                tabName[i] = tabName[i].replaceAll("_cent_", "_peri_");
        }
        int i = 0;
        String sqlR = ""
            + "SELECT\n" +
            "    "+tabName[i++]+" AS timezone, COALESCE(nbr_ligne, 0) AS nbr_ligne,\n" +
            "    convention_id, ann_sclr.debut, ann_sclr.fin,\n" +
            "    convention.t_lieu_affectation_id AS etablismt_id, etablismt.nom, etablismt.identifiant, etablismt.type_lieu,\n" +
            "    t_type_etablissmt.nom, t_type_enseignmt.nom, t_niveau_enseignmt.nom, \n" +
            "    COALESCE(total_vesrment, 0)::MONTANT AS total_vesrment \n" +
            "FROM\n" +
            "     get_this_cent_peri_station_timezone() t_station_timezone CROSS JOIN\n" +
            "    (SELECT "+conventionId+" AS convention_id, SUM(VRSMT_MTT - coalesce(FACT_AVR_MTT, 0)) AS total_vesrment, count(*) AS nbr_ligne from "+tabName[i++]+" where t_synchro_date_suppr IS NULL AND t_convention_id = "+conventionId+") t1\n" +
            "    JOIN "+tabName[i++]+" convention ON convention.t_synchro_id = convention_id\n" +
            "    JOIN "+tabName[i++]+" ann_sclr ON ann_sclr.t_synchro_id = convention.t_ann_sclr_id\n" +
            "    JOIN "+tabName[i++]+" etablismt ON etablismt.t_synchro_id = convention.t_lieu_affectation_id\n" +
            "    LEFT JOIN "+tabName[i++]+" t_type_etablissmt ON etablismt.t_type_etablissmt_id = t_type_etablissmt.t_synchro_id\n" +
            "    LEFT JOIN "+tabName[i++]+" t_type_enseignmt ON etablismt.t_type_enseignmt_id = t_type_enseignmt.t_synchro_id\n" +
            "    LEFT JOIN "+tabName[i++]+" t_niveau_enseignmt ON etablismt.t_niveau_enseignmt_id = t_niveau_enseignmt.t_synchro_id"
        ;
        Key[] keys = new Key[]{
            new Key<>("timezone", String.class),//0
            new Key<>("nbr_ligne", Long.class, new Long(0)),//1
            new Key<>("convention_id", Long.class),//2
            new Key<>("ann_sclr.debut", Date.class),//3
            new Key<>("ann_sclr.fin", Date.class),//4
            new Key<>("etablismt_id", Long.class),//5
            new Key<>("etablismt.nom", String.class),//6
            new Key<>("etablismt.identifiant", String.class),//7
            new Key<>("etablismt.type_lieu", String.class),//8
            new Key<>("t_type_etablissmt.nom", String.class),//9
            new Key<>("t_type_enseignmt.nom", String.class),//10
            new Key<>("t_niveau_enseignmt.nom", String.class),//11
            new Key<>("total_vesrment", BigDecimal.class, BigDecimal.ZERO)//12
        };
        CRUD.scalar(sqlR, connection, keys);
        long nbrTotalLigne = ((Key<Long>)keys[1]).getValue();
        List<HashMap<String, String>> title = new ArrayList<>(5);
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Date"}));
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Code"}));
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Montant"}));
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Description"}));
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Éditeur"}));
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Date d'édition"}));

        String anneeScolaire = ((keys[3].getValue() != null) && (keys[4].getValue()!=null))? ""+(((Date)keys[3].getValue()).getYear()+1900) +"-"+ (((Date)keys[4].getValue()).getYear()+1900): "";
        hmapScope.put("valeur", MONEY_DECIMAL_FORMAT.format(keys[12].getValue()));
        hmapScope.put("convention", HashHelper.newHashMap(
                new String[]{"id",               "synchro_id",      "annee_scolaire"}, 
                new Object[]{keys[2].getValue(), keys[2].getValue(), anneeScolaire}
        ));
        String typeLieu = (String)keys[8].getValue();

        String EtablissmtSynchroName = ALL_SYNCHRO_NAME.get(2).get(1)/*Ecole*/;
        hmapScope.put("etablissmt", HashHelper.newHashMap(new String[]{"synchro_name", "synchro_id",      "nom",              "identifiant",      "type_lieu",  "type_etablissmt",  "type_enseignmt",     "niveau_enseignmt"  }, 
            new Object[]{EtablissmtSynchroName, keys[5].getValue(),   keys[6].getValue(), keys[7].getValue(), typeLieu,     keys[9].getValue(), keys[10].getValue(),  keys[11].getValue() }
        ));
        TimeZone timezone = StringHelper.isEmpty(((Key<String>)keys[0]).getValue())? CRUD.GMTTimezone : TimeZone.getTimeZone(((Key<String>)keys[0]).getValue());
        data.put("timezone", timezone);
        data.put("nbr_ligne", nbrTotalLigne);//2
        data.put("title", title);
        return data;
    }
    
    public static void setEtablismtAndCvtionDataByConvtionIdWhenCreate(String conventionId, String stationCibleType, Connection connection, HashMap<String, Object> hmapScope) throws SQLException{
        String[] tabName = new String[]{"t_cent_synchro_convention", "t_cent_synchro_ann_sclr", "t_cent_synchro_lieu_affectation", "t_cent_synchro_type_etablissmt", "t_cent_synchro_type_enseignmt", "t_cent_synchro_niveau_enseignmt"};
        if(Helper.peripheriqueStationType.equals(stationCibleType)){
            int length = tabName.length;
            for (int i = 0; i < length; i++) 
                tabName[i] = tabName[i].replaceAll("_cent_", "_peri_");
        }
        int i = 0;
        String sqlR = ""+
                "SELECT\n" +
                "    convention_id, ann_sclr.debut, ann_sclr.fin,\n" +
                "    convention.t_lieu_affectation_id AS etablismt_id, etablismt.nom, etablismt.identifiant, etablismt.type_lieu,\n" +
                "    t_type_etablissmt.nom, t_type_enseignmt.nom, t_niveau_enseignmt.nom\n" +
                "FROM\n" +
                "    (SELECT t_synchro_id AS convention_id, t_ann_sclr_id, t_lieu_affectation_id FROM "+tabName[i++]+" WHERE t_synchro_id = "+conventionId+") convention\n" +
                "    JOIN "+tabName[i++]+" ann_sclr ON ann_sclr.t_synchro_id = convention.t_ann_sclr_id\n" +
                "    JOIN "+tabName[i++]+" etablismt ON etablismt.t_synchro_id = convention.t_lieu_affectation_id\n" +
                "    LEFT JOIN "+tabName[i++]+" t_type_etablissmt ON etablismt.t_type_etablissmt_id = t_type_etablissmt.t_synchro_id\n" +
                "    LEFT JOIN "+tabName[i++]+" t_type_enseignmt ON etablismt.t_type_enseignmt_id = t_type_enseignmt.t_synchro_id\n" +
                "    LEFT JOIN "+tabName[i++]+" t_niveau_enseignmt ON etablismt.t_niveau_enseignmt_id = t_niveau_enseignmt.t_synchro_id"
        ;
        Key[] keys = new Key[]{
            new Key<>("convention_id", Long.class),//0
            new Key<>("ann_sclr.debut", Date.class),//1
            new Key<>("ann_sclr.fin", Date.class),//2
            new Key<>("etablismt_id", Long.class),//3
            new Key<>("etablismt.nom", String.class),//4
            new Key<>("etablismt.identifiant", String.class),//5
            new Key<>("etablismt.type_lieu", String.class),//6
            new Key<>("t_type_etablissmt.nom", String.class),//7
            new Key<>("t_type_enseignmt.nom", String.class),//8
            new Key<>("t_niveau_enseignmt.nom", String.class),//9
        };
        CRUD.scalar(sqlR, connection, keys);
        if(keys[0].getValue() == null)throw new PKNotFoundException("Convention introuvable.");
        if(keys[3].getValue() == null)throw new PKNotFoundException("Etablissement introuvable.");

        String typeLieu = (String)keys[6].getValue();
        String anneeScolaire = ((keys[1].getValue() != null) && (keys[2].getValue()!=null))? ""+(((Date)keys[1].getValue()).getYear()+1900) +"-"+ (((Date)keys[2].getValue()).getYear()+1900): "";
        hmapScope.put("convention", HashHelper.newHashMap(
                new String[]{"id",               "synchro_id",      "annee_scolaire"}, 
                new Object[]{keys[0].getValue(), keys[0].getValue(), anneeScolaire}
        ));
        String EtablissmtSynchroName = ALL_SYNCHRO_NAME.get(2).get(1)/*Ecole*/;
        hmapScope.put("etablissmt", HashHelper.newHashMap(
            new String[]{"synchro_name",        "synchro_id",      "nom",               "identifiant",      "type_lieu",  "type_etablissmt",  "type_enseignmt",     "niveau_enseignmt"  }, 
            new Object[]{EtablissmtSynchroName, keys[3].getValue(), keys[4].getValue(), keys[5].getValue(), typeLieu,     keys[7].getValue(), keys[8].getValue(),  keys[9].getValue() }
        ));
    }

}
/*
valeur
convention{
    id
    synchro_id
    annee_scolaire
}
etablissmt{
    synchro_name
    synchro_id
    identifiant
    nom

    type_lieu
    type_etablissmt
    type_enseignmt
    niveau_enseignmt
}
data{
    {{date if exists}}, mtt, descrption, user, edit editDate
    order by date if exists, Editdate
}
*/