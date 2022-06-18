/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autreWs;

import static autreWs.Function.getPagntFoot;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.hibernate.validator.internal.util.StringHelper;
import synchronisable.Synchro;
import synchronisable.centrale.CentSynchro;
import synchronisable.param.exception.ParamException;
import synchronisable.peripherique.PeriSynchro;
import synchronisation.SynchronisationConfig;
import synchronisation.u_i_conflict.solve.exception.SynchroNameNotFoundException;
import usefull.ArrayHelper;
import usefull.HashHelper;
import usefull.dao.CRUD;
import usefull.dao.Helper;
import usefull.dao.Key;
import usefull.dao.exception.IndefinedStaionTypeException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class Autres {
    public static<SynchroType extends Synchro> List<HashMap<String,Object>[]> getMaterielData(Connection connection, long pagntStartNum, int pagntMaxSize, HashMap<String, Object> data, String synchroName) throws ParamException, Exception{
        SynchroType synchroType = (SynchroType) data.get("synchro");
        ArrayList<String> orderedColsNames = ArrayHelper.arrayFusion(synchroType.getOrderedIndexsNames(), synchroType.getOrderedDatasNames());
        orderedColsNames.add(0, "t_synchro_id");
        boolean isPeripheriqueStationType = synchroType instanceof PeriSynchro;/**/
        /**/
        if(isPeripheriqueStationType){
            orderedColsNames.add("conflit_cent_synchro_id");
            orderedColsNames.add("t_synchro_date_reception");
            orderedColsNames.add("t_synchro_date_envoye");
            orderedColsNames.add("t_synchro_date_edition");
        }/**/ 
        String sqlR = ""
            + "SELECT "+StringHelper.join(orderedColsNames, ", ")+" FROM "+synchroType.getTabName()+" "+"WHERE t_synchro_date_suppr IS NULL \n"
            + "ORDER BY "+StringHelper.join(ArrayHelper.arrayFusion(synchroType.getOrderedIndexsNames(), "t_synchro_id"), ", ")+" \n"
            + "OFFSET "+pagntStartNum+" LIMIT "+pagntMaxSize
        ;
        int colsCount = orderedColsNames.size();
        if(synchroName.equalsIgnoreCase(SynchronisationConfig.getAllSynchroName().get(1).get(0))){ /*Region*/ 
            colsCount--;
            orderedColsNames.remove(3);
            for (int i = 0; i < colsCount; i++) 
                orderedColsNames.set(i, "t_reg."+orderedColsNames.get(i));
            orderedColsNames.add(3, "t_prov.nom||': '||t_prov.identifiant");
            String tabProvince = (synchroType instanceof CentSynchro)? "t_cent_synchro_province": "t_peri_synchro_province";
            sqlR = "SELECT "+StringHelper.join(orderedColsNames, ", ")+" FROM ("+sqlR+") t_reg \n"
                   + " JOIN "+tabProvince+" t_prov ON t_reg.t_province_id = t_prov.t_synchro_id";
            colsCount++;
        }
        Key[] orderedKeys =new Key[colsCount];
        if(isPeripheriqueStationType)/**/
            colsCount -= 4;
        int keyIndice = 0;
        for (keyIndice = 0; keyIndice < colsCount; keyIndice++)
            orderedKeys[keyIndice] = new Key<>(String.class);
        /**/
        if(isPeripheriqueStationType){
            orderedKeys[keyIndice++] = new Key<>("conflit_cent_synchro_id", String.class);
            orderedKeys[keyIndice++] = new Key<>("t_synchro_date_reception", Timestamp.class);
            orderedKeys[keyIndice++] = new Key<>("t_synchro_date_envoye", Timestamp.class);
            orderedKeys[keyIndice++] = new Key<>("t_synchro_date_edition", Timestamp.class);
        }/**/
        List<Key[]> datas = CRUD.readToKeysList(sqlR, connection, orderedKeys);
        int size = datas.size();  
        List<HashMap<String,Object>[]> finalDatas = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Key[] adata = datas.get(i);
            String id = (String)adata[0].getValue();
            HashMap[] finalData = (isPeripheriqueStationType)? new HashMap[colsCount-1+1]: new HashMap[colsCount-1];/**/
            for (int j = 1; j < colsCount; j++)
                finalData[j-1] = HashHelper.newHashMap(new String[]{"value", "href"}, new Object[]{adata[j].getValue(), synchroName+ "/" + id});
            /**/
            int keyIndice1 = colsCount;
            if(isPeripheriqueStationType){
                String synchronisationState = PeriSynchro.getSynchronisationState((String)adata[keyIndice1++].getValue(), PeriSynchro.getDateSynchronisation((Timestamp)adata[keyIndice1++].getValue(), (Timestamp)adata[keyIndice1++].getValue()), (Timestamp)adata[keyIndice1++].getValue());
                finalData[colsCount-1] = HashHelper.newHashMap(new String[]{"value", "href"},        new Object[]{synchronisationState, synchroName+ "/" + id});
            }/**/
            finalDatas.add(finalData);
        }
        return finalDatas;
    }

    public static void pagine(String synchroName, HashMap<String, Object> hmapResponse, String stationCibleType, Connection connection, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize) throws ParamException, SQLException, Exception{
        HashMap<String, Object> hmapScope = (HashMap<String, Object>) hmapResponse.get("scope");
        HashMap<String, Object> data = getData(hmapResponse, synchroName, stationCibleType, connection);
        if(Boolean.FALSE.equals(hmapResponse.get("status"))) return;
        HashMap<String, Object> pagntData = new HashMap<>(6);
        hmapScope.put("pagnt_data", pagntData);
        pagntData.put("head", data.get("title"));
        pagntData.put("body", getMaterielData(connection, pagntStartNum, pagntMaxSize, data, synchroName));
        long nbrTotalLigne = (data.get("nbr_ligne") == null)? 0: (Long)data.get("nbr_ligne");
        pagntData.put("foot", getPagntFoot(pagntStartNum, pagntFootMaxSize, pagntMaxSize, nbrTotalLigne, synchroName));
    }
    
    public static <SynchroType extends Synchro> HashMap<String, Object> getData(HashMap<String, Object> hmapResponse, String synchroName, String stationCibleType, Connection connection) throws InstantiationException, IllegalAccessException, SQLException, SynchroNameNotFoundException, ClassNotFoundException, IndefinedStaionTypeException {
        HashMap<String, Object> hmapScope = (HashMap<String, Object>) hmapResponse.get("scope");
        HashMap<String, Object> data = new HashMap<>(3);
        String[] centSPeriSAndCentSStoryPeriSStoryClsName = null;
        SynchroType synchroType = null;
        try {
            centSPeriSAndCentSStoryPeriSStoryClsName = centSPeriSAndCentSStoryPeriSStoryClsName = SynchronisationConfig.getCentSPeriSAndCentSStoryPeriSStoryClsName(synchroName); 
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
        }else if(Helper.peripheriqueStationType.equals(stationCibleType)){
                try {
                    synchroType = ((Class<SynchroType>) Class.forName(centSPeriSAndCentSStoryPeriSStoryClsName[1])).newInstance();
                } catch (ClassNotFoundException e) {
                    hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
                    throw e;
                }
        }else{
            hmapResponse.put("path_error", true);
            throw new IndefinedStaionTypeException();
        }
        String tabName = synchroType.getTabName();
        String sqlR = "SELECT count(*) from "+tabName+" WHERE t_synchro_date_suppr IS NULL";
        Key[] keys = new Key[]{new Key<>(Long.class)};
        CRUD.scalar(sqlR, connection, keys);
        long nbrTotalLigne = ((Key<Long>)keys[0]).getValue();
        List<HashMap<String, String>> title = new ArrayList<>(5);
        try {
            String[] titles = (String[])synchroType.getOrderedDisplayableIndexs(Synchro.READ_DISPLAY_TYPE, connection, stationCibleType).get("title");
            for (String atitle : titles)
                title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{atitle}));
        } catch (Exception e) {}
        try {
            String[] titles = (String[])synchroType.getOrderedDisplayableDatas(Synchro.READ_DISPLAY_TYPE, connection, stationCibleType).get("title");
            for (String atitle : titles)
                title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{atitle}));
        } catch (Exception e) {}
        data.put("nbr_ligne", nbrTotalLigne);//2
        data.put("title", title);
        data.put("synchro", synchroType);
        return data;
    }
}
