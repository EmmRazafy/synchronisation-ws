/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.gestion_login;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import synchronisable.FunctionRead;
import usefull.ArrayHelper;
import usefull.HashHelper;
import usefull.StringHelper;
import usefull.dao.CRUD;
import usefull.dao.Helper;
import usefull.dao.Key;
import static autreWs.Function.getPagntFoot;
import java.sql.Timestamp;
import java.time.Instant;
import synchronisable.exception.NullArgumentException;
import usefull.user.EmailHelper;
import user.Function;


/**
 *
 * @author P12A-92-Emmanuel
 */
public class GestionLoginFunction {
    private final static String SYNCHRO_NAME = "GestionLogIn";
    private final static String LOGIN_AUTORISÉ = "autorise";
    private final static String LOGIN_EN_LISTE_D_ATTENTE = "en_attente";
    private final static String LOGIN_BLOQUÉ = "bloque";
    
    private final static String ACTION_AUTORISER = "Autoriser";
    private final static String ACTION_MODIFIER = "Modifier le profil";
    private final static String ACTION_BLOQUER = "Bloquer";

    private final static String NOT_AUTORISED_USER_PROFIL_SQL_STATE = "100YY"/*editor_profil non authorisé (profil ni dev ni admin)*/;
    
    public static List<HashMap<String,Object>[]> getData(String onglet, Connection connectionCentrale, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize, long[] nbrTotalLigne) throws SQLException, Exception{
        String sqlR = null;Key[] keys = null;List<Key[]> datas = null;int size = 0;List<HashMap<String,Object>[]> finalDatas = null;
        switch (onglet){
            case LOGIN_AUTORISÉ:
                sqlR = "SELECT le_login_id, l_email, le_profil_id, le_profil_nom FROM get_gestion_login_data("+CRUD.stringify(onglet)+", "+CRUD.stringify(pagntStartNum)+", "+CRUD.stringify(pagntMaxSize)+")";
                keys = new Key[]{new Key<>("login_id", Long.class), new Key<>("email", String.class), new Key<>("profil_id", String.class), new Key<>("profil_nom", String.class) };
                datas = CRUD.readToKeysList(sqlR, connectionCentrale, keys);
                nbrTotalLigne[0] = ((Key<Long>)datas.get(0)[0]).getValue();
                size = datas.size();  
                finalDatas = new ArrayList<>(size);
                for (int i = 1; i < size; i++) {
                    Key[] adata = datas.get(i);
                    long loginId = ((Key<Long>)adata[0]).getValue();
                    String profilId = ((Key<String>)adata[2]).getValue();
                    HashMap[] finalData = new HashMap[]{
                        HashHelper.newHashMap(new String[]{"value"}, new Object[]{adata[1].getValue()}),
                        HashHelper.newHashMap(new String[]{"value"}, new Object[]{adata[3].getValue()}),
                        HashHelper.newHashMap(new String[]{"value"}, new Object[]{
                            "<button class=\"btn btn-primary border rounded\" type=\"button\" data-toggle=\"modal\" data-target=\"#modal_update_login_profil\" login-id=\""+loginId+"\" profil-id=\""+profilId+"\"  login-email=\""+adata[1].getValue()+"\" ><i class=\"far fa-edit\"></i>"+ACTION_MODIFIER+"</button>"+                       
                            "<button class=\"btn btn-danger border rounded ml-4\" type=\"button\" login-id=\""+loginId+"\" role=\"bloquer_login\" ><i class=\"fas fa-user-alt-slash\"></i>"+ACTION_BLOQUER+"</button>"
                        })                        
                    };
                    finalDatas.add(finalData);
                }return finalDatas;
            case LOGIN_EN_LISTE_D_ATTENTE:
                sqlR = "SELECT le_login_id, l_email FROM get_gestion_login_data("+CRUD.stringify(onglet)+", "+CRUD.stringify(pagntStartNum)+", "+CRUD.stringify(pagntMaxSize)+")";
                keys = new Key[]{new Key<>("login_id", Long.class), new Key<>("email", String.class) };
                datas = CRUD.readToKeysList(sqlR, connectionCentrale, keys);
                nbrTotalLigne[0] = ((Key<Long>)datas.get(0)[0]).getValue();
                size = datas.size();
                finalDatas = new ArrayList<>(size);
                for (int i = 1; i < size; i++) {
                    Key[] adata = datas.get(i);
                    long loginId = ((Key<Long>)adata[0]).getValue();
                    HashMap[] finalData = new HashMap[]{
                        HashHelper.newHashMap(new String[]{"value"}, new Object[]{adata[1].getValue()}),
                        HashHelper.newHashMap(new String[]{"value"}, new Object[]{
                            "<button class=\"btn btn-success border rounded\" type=\"button\" data-toggle=\"modal\" data-target=\"#modal_autoriser_login\" login-id=\""+loginId+"\" login-email=\""+adata[1].getValue()+"\" ><i class=\"fas fa-user-check\"></i>"+ACTION_AUTORISER+"</button>"+                        
                            "<button class=\"btn btn-danger border rounded ml-4\" type=\"button\" login-id=\""+loginId+"\" role=\"bloquer_login\" ><i class=\"fas fa-user-alt-slash\"></i>"+ACTION_BLOQUER+"</button>"
                        })                        
                    };
                    finalDatas.add(finalData);
                }return finalDatas;
            case LOGIN_BLOQUÉ:
                sqlR = "SELECT le_login_id, l_email, le_profil_id, le_profil_nom FROM get_gestion_login_data("+CRUD.stringify(onglet)+", "+CRUD.stringify(pagntStartNum)+", "+CRUD.stringify(pagntMaxSize)+")";
                keys = new Key[]{new Key<>("login_id", Long.class), new Key<>("email", String.class), new Key<>("profil_id", String.class), new Key<>("profil_nom", String.class) };
                datas = CRUD.readToKeysList(sqlR, connectionCentrale, keys);
                nbrTotalLigne[0] = ((Key<Long>)datas.get(0)[0]).getValue();
                size = datas.size();  
                finalDatas = new ArrayList<>(size);
                for (int i = 1; i < size; i++) {
                    Key[] adata = datas.get(i);
                    long loginId = ((Key<Long>)adata[0]).getValue();
                    String profilId = ((Key<String>)adata[2]).getValue();
                    HashMap[] finalData = new HashMap[3];
                    finalData[0] = HashHelper.newHashMap(new String[]{"value"}, new Object[]{adata[1].getValue()});
                    finalData[1] = HashHelper.newHashMap(new String[]{"value"}, new Object[]{adata[3].getValue()});
                    finalData[2] = StringHelper.isEmpty(profilId)
                        ? HashHelper.newHashMap(new String[]{"value"}, new Object[]{"<button class=\"btn btn-success border rounded autoriser_login\" type=\"button\" data-toggle=\"modal\" data-target=\"#modal_autoriser_login\" login-id=\""+loginId+"\" login-email=\""+adata[1].getValue()+"\" ><i class=\"fas fa-user-check\"></i>"+ACTION_AUTORISER+"</button>"})
                        : HashHelper.newHashMap(new String[]{"value"}, new Object[]{"<button class=\"btn btn-success border rounded\" type=\"button\" login-id=\""+loginId+"\" profil-id=\""+profilId+"\" role=\"autoriser_login\" ><i class=\"fas fa-user-check\"></i>"+ACTION_AUTORISER+"</button>"})
                    ;finalDatas.add(finalData);
                }return finalDatas;
            default:
                throw new Exception("Oglet Inconnue!");
        }
    }

    public static List<HashMap<String, String>> getTitle(String onglet){
        boolean estOngletListeDattente = LOGIN_EN_LISTE_D_ATTENTE.equals(onglet);
        List<HashMap<String, String>> title = (estOngletListeDattente)? new ArrayList<>(2): new ArrayList<>(3);
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"E-mail"}));
        if(!estOngletListeDattente)
            title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Profil"}));
        title.add(HashHelper.newHashMap(new String[]{"title"}, new String[]{"Option(s)"}));
        return title;
    }
    
    public static void pagine(HashMap<String, Object> hmapResponse, String onglet, Connection connectionCentrale, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize) throws SQLException, Exception{
        if(Boolean.FALSE.equals(hmapResponse.get("status"))) return;
        HashMap<String, Object> hmapScope = (HashMap<String, Object>) hmapResponse.get("scope");
        HashMap<String, Object> pagntData = new HashMap<>(6);
        hmapScope.put("pagnt_data", pagntData);
        pagntData.put("head", getTitle(onglet));
        long[] nbrTotalLigne = new long[]{0};
        pagntData.put("body", getData(onglet, connectionCentrale, pagntStartNum, pagntFootMaxSize, pagntMaxSize, nbrTotalLigne));
        pagntData.put("foot", getPagntFoot(pagntStartNum, pagntFootMaxSize, pagntMaxSize, nbrTotalLigne[0], SYNCHRO_NAME+"/"+onglet));
        
        String options = "";
        String sqlR = "SELECT t_cent_profil_id, t_cent_profil_nom FROM t_cent_profil WHERE t_cent_profil_date_suppr IS NULL ORDER BY t_cent_profil_autority DESC, t_cent_profil_nom ASC";
        Key[] keys = new Key[]{new Key<>("id", String.class), new Key<>("nom", String.class)};
        List<Key[]> datas = CRUD.readToKeysList(sqlR, connectionCentrale, keys);
        int size = datas.size();
        for (int i = 0; i < size; i++) {
            Key[] adata = datas.get(i);
            options = options+"<option value=\""+adata[0].getValue()+"\" >"+adata[1].getValue()+"</option>";
        }hmapScope.put("profil_options", options);
    }
    
    public static HashMap<String, Object> pagine(String onglet, long pagntStartNum, int pagntFootMaxSize, int pagntMaxSize, HashMap<String,Object>... args) throws Exception{
        HashMap<String, Object> hmapResponse = new HashMap<>(4);
        HashMap<String, Object> hmapScope = new HashMap<>(20);
        hmapResponse.put("scope", hmapScope);
        Connection connectionCentrale = null;
        try {
            if(!ArrayHelper.contains(onglet,  LOGIN_AUTORISÉ, LOGIN_EN_LISTE_D_ATTENTE, LOGIN_BLOQUÉ)){
                hmapResponse.put("path_error", true);
                throw new Exception("Oglet Inconnue!"); 
            }
            try {
                connectionCentrale = Helper.getCentConn();
            } catch (SQLException e) {
                hmapScope.put("page_notification_message", "Problème de connection.");
                throw e;
            } catch(Exception e){
                hmapScope.put("page_notification_message", "Un problème est survenu <br> pendant la connection.<br>("+e.getMessage()+")");
                throw e;
            }
            /*code*/
                pagine(hmapResponse, onglet, connectionCentrale, pagntStartNum, pagntFootMaxSize, pagntMaxSize);
            /*code*/
            if(!hmapResponse.containsKey("status"))
                hmapResponse.put("status", true);
        } catch (Exception e) {
            hmapResponse.put("status", false);
            if(!hmapScope.containsKey("page_notification_message")){
                /*if(e instanceof ParamException){
                    hmapScope.put("page_notification_message", e.getMessage());
                    hmapScope.put("primary_bttn",HashHelper.newHashMap(new String[]{"value","uri"}, new String[]{"Paramètre","Parametres"}));
                }else*/
                    hmapScope.put("page_notification_message", "Un problème est survenu.<br>("+e.getMessage()+")");
            }
            hmapScope.put("page_notification_class", "danger");
            if(!hmapScope.containsKey("page_notification_title")){//default title
                hmapScope.put("page_notification_title", "Erreur");
            }
            //            throw e;//*******LLLLLLLL*******************
        }finally{
            if(connectionCentrale!=null){
                try {
                    connectionCentrale.close();
                } catch (SQLException ex) {
                    Logger.getLogger(FunctionRead.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }return hmapResponse;
    }

    public static void autoriserNewLogin(HashMap<String, Object> hmapResponse, HashMap<String, Object> hmapScope, HashMap<String, Object> data, Connection connectionCentrale) throws SQLException{
        HashMap<String, Object> field = new HashMap<String, Object>(2);
        hmapScope.put("fields", field);
        String email = (String) data.get("email");
        long profil = 0; long userId = 0;String formError = "";
        try{
            EmailHelper.checkEmail(email);
            field.put("email", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("email", HashHelper.newHashMap(new String[]{"status"}, new Object[]{false}) );
            hmapResponse.put("status", false); formError += StringHelper.coalesce(e.getMessage(), e.getClass().getName()) + "<br>";
        }
        try{
            profil = Long.parseLong(data.get("profil").toString());
            field.put("profil", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("profil", HashHelper.newHashMap(new String[]{"status"}, new Object[]{false}) );
            hmapResponse.put("status", false);formError += "Profil invalide!" + "<br>";
        }
        if(hmapResponse.containsKey("status") && (!(Boolean)hmapResponse.get("status"))){
            formError = "Erreur de saisie!<br>"+formError;
            hmapScope.put("form_error", formError);
            return;
        }
        
        
        try{
            userId = Long.parseLong(data.get("user_id").toString());
        } catch (Exception e) {
            hmapResponse.put("status", false); hmapScope.put("form_error", "Un problème est survenu!<br>(indefined user_id)");
            return ;
        }
        
        try {
            String sqlI = ""+
                "INSERT INTO t_cent_login (t_cent_login_email, t_cent_profil_id, t_cent_login_date_edition, t_user_id) \n" +
                "values ("+CRUD.stringify(email)+", "+CRUD.stringify(profil)+", now(), "+CRUD.stringify(userId)+")"
            ;CRUD.execute(sqlI, connectionCentrale);
        } catch (SQLException e) {
            if(CRUD.UNIQUE_INDEX_VIOLATION_SQL_STATE.equals(e.getSQLState())){
                field.put("email", HashHelper.newHashMap(new String[]{"status"}, new Object[]{false}) );hmapScope.put("form_error", "L'e-mail existe déjà!");
                hmapResponse.put("status", false);
            }else throw e;
        }
    }
    
    private static void bloquerNewLogin(HashMap<String, Object> hmapResponse, HashMap<String, Object> hmapScope, HashMap<String, Object> data, Connection connectionCentrale) throws SQLException {
        HashMap<String, Object> field = new HashMap<String, Object>(1);
        hmapScope.put("fields", field);
        String email = (String) data.get("email");
        long userId = 0;String formError = "";
        try{
            EmailHelper.checkEmail(email);
            field.put("email", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("email", HashHelper.newHashMap(new String[]{"status"}, new Object[]{false}) );
            hmapResponse.put("status", false); formError += StringHelper.coalesce(e.getMessage(), e.getClass().getName()) + "<br>";
        }
        if(hmapResponse.containsKey("status") && (!(Boolean)hmapResponse.get("status"))){
            formError = "Erreur de saisie!<br>"+formError;
            hmapScope.put("form_error", formError);
            return;
        }
        
        
        try{
            userId = Long.parseLong(data.get("user_id").toString());
        } catch (Exception e) {
            hmapResponse.put("status", false); hmapScope.put("form_error", "Un problème est survenu!<br>(indefined user_id)");
            return ;
        }
        
        try {
            String now = CRUD.stringify(Timestamp.from(Instant.now()));
            String sqlI = ""+
                "INSERT INTO t_cent_login (t_cent_login_email, t_cent_login_date_edition, t_cent_login_date_suppr, t_user_id) \n" +
                "values ("+CRUD.stringify(email)+", "+now+", "+now+", "+CRUD.stringify(userId)+")"
            ;CRUD.execute(sqlI, connectionCentrale);
        } catch (SQLException e) {
            if(CRUD.UNIQUE_INDEX_VIOLATION_SQL_STATE.equals(e.getSQLState())){
                hmapResponse.put("status", false);
                field.put("email", HashHelper.newHashMap(new String[]{"status"}, new Object[]{false}) );hmapScope.put("form_error", "L'e-mail existe déjà!");
            }else throw e;
        }
    }

    private static void autoriserOldLoginEnLstDAttente(HashMap<String, Object> hmapResponse, HashMap<String, Object> hmapScope, HashMap<String, Object> data, Connection connectionCentrale) throws SQLException {
        HashMap<String, Object> field = new HashMap<String, Object>(1);
        hmapScope.put("fields", field);
        long profil = 0; long userId = 0;long loginId = 0; String formError = "";
        try{
            profil = Long.parseLong(data.get("profil").toString());
            field.put("profil", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("profil", HashHelper.newHashMap(new String[]{"status"}, new Object[]{false}) );
            hmapResponse.put("status", false);formError += "Profil invalide!" + "<br>";
        }
        if(hmapResponse.containsKey("status") && (!(Boolean)hmapResponse.get("status"))){
            formError = "Erreur de saisie!<br>"+formError;
            hmapScope.put("form_error", formError);
            return;
        }
        
        
        try{
            userId = Long.parseLong(data.get("user_id").toString());
        } catch (Exception e) {
            hmapResponse.put("status", false); hmapScope.put("form_error", "Un problème est survenu!<br>(indefined user_id)");
            return ;
        }
        try{
            loginId = Long.parseLong(data.get("login_id").toString());
        } catch (Exception e) {
            hmapResponse.put("status", false); hmapScope.put("form_error", "Un problème est survenu!<br>(indefined login_id)");
            return ;
        }        
        String sqlU = "UPDATE t_cent_login \n"
        + " SET t_user_id = "+CRUD.stringify(userId)+", t_cent_login_date_edition = now(), t_cent_profil_id = "+CRUD.stringify(profil)+", t_cent_login_date_suppr = NULL \n "
        + " WHERE t_cent_login_id = "+CRUD.stringify(loginId)
        ;CRUD.execute(sqlU, connectionCentrale);
    }
    
    private static void updateAutorisedOldLoginProfil(HashMap<String, Object> hmapResponse, HashMap<String, Object> hmapScope, HashMap<String, Object> data, Connection connectionCentrale) throws SQLException {
        HashMap<String, Object> field = new HashMap<String, Object>(1);
        hmapScope.put("fields", field);
        long profil = 0; long userId = 0;long loginId = 0; String formError = "";
        try{
            profil = Long.parseLong(data.get("profil").toString());
            field.put("profil", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("profil", HashHelper.newHashMap(new String[]{"status"}, new Object[]{false}) );
            hmapResponse.put("status", false);formError += "Profil invalide!" + "<br>";
        }
        if(hmapResponse.containsKey("status") && (!(Boolean)hmapResponse.get("status"))){
            formError = "Erreur de saisie!<br>"+formError;
            hmapScope.put("form_error", formError);
            return;
        }
        
        
        try{
            userId = Long.parseLong(data.get("user_id").toString());
        } catch (Exception e) {
            hmapResponse.put("status", false); hmapScope.put("form_error", "Un problème est survenu!<br>(indefined user_id)");
            return ;
        }
        try{
            loginId = Long.parseLong(data.get("login_id").toString());
        } catch (Exception e) {
            hmapResponse.put("status", false); hmapScope.put("form_error", "Un problème est survenu!<br>(indefined login_id)");
            return ;
        }
        
        String sqlU = "UPDATE t_cent_login \n"
        + " SET t_user_id = "+CRUD.stringify(userId)+", t_cent_login_date_edition = now(), t_cent_profil_id = "+CRUD.stringify(profil)+" \n "
        + " WHERE t_cent_login_date_suppr IS NULL AND t_cent_login_id = "+CRUD.stringify(loginId)
        ;int nbLligneTouché = CRUD.execute(sqlU, connectionCentrale);
        if(nbLligneTouché == 0)
            throw new NullArgumentException("Login introuvable!<br>Peut-être qu'il a déjà été bloqué.");
    }
   
    private static void autoriserOldLoginDUnUser(HashMap<String, Object> hmapResponse, HashMap<String, Object> hmapScope, HashMap<String, Object> data, Connection connectionCentrale) throws SQLException {
        HashMap<String, Object> field = new HashMap<String, Object>(1);
        hmapScope.put("fields", field);
        long userId = 0;long loginId = 0;        
        try{
            userId = Long.parseLong(data.get("user_id").toString());
        } catch (Exception e) {
            hmapResponse.put("status", false); hmapScope.put("form_error", "Un problème est survenu!<br>(indefined user_id)");
            return ;
        }
        try{
            loginId = Long.parseLong(data.get("login_id").toString());
        } catch (Exception e) {
            hmapResponse.put("status", false); hmapScope.put("form_error", "Un problème est survenu!<br>(indefined login_id)");
            return ;
        }
        
        String sqlU = "UPDATE t_cent_login \n"
        + " SET t_user_id = "+CRUD.stringify(userId)+", t_cent_login_date_edition = now(), t_cent_login_date_suppr = NULL \n "
        + " WHERE t_cent_login_date_suppr IS NOT NULL AND t_cent_login_id = "+CRUD.stringify(loginId)
        ;int nbLligneTouché = CRUD.execute(sqlU, connectionCentrale);
        if(nbLligneTouché == 0)
            throw new NullArgumentException("Login introuvable!<br>Peut-être qu'il a déjà été autorisé.");
    }

    private static void bloquerOldLoginDUnUser(HashMap<String, Object> hmapResponse, HashMap<String, Object> hmapScope, HashMap<String, Object> data, Connection connectionCentrale) throws SQLException {
        HashMap<String, Object> field = new HashMap<String, Object>(1);
        hmapScope.put("fields", field);
        long userId = 0;long loginId = 0;        
        try{
            userId = Long.parseLong(data.get("user_id").toString());
        } catch (Exception e) {
            hmapResponse.put("status", false); hmapScope.put("form_error", "Un problème est survenu!<br>(indefined user_id)");
            return ;
        }
        try{
            loginId = Long.parseLong(data.get("login_id").toString());
        } catch (Exception e) {
            hmapResponse.put("status", false); hmapScope.put("form_error", "Un problème est survenu!<br>(indefined login_id)");
            return ;
        }
        String now = CRUD.stringify(Timestamp.from(Instant.now()));
        String sqlU = "UPDATE t_cent_login \n"
        + " SET t_user_id = "+CRUD.stringify(userId)+", t_cent_login_date_edition = "+now+", t_cent_login_date_suppr = "+now+" \n "
        + " WHERE t_cent_login_date_suppr IS NULL AND t_cent_login_id = "+CRUD.stringify(loginId)
        ;int nbLligneTouché = CRUD.execute(sqlU, connectionCentrale);
        if(nbLligneTouché == 0)
            throw new NullArgumentException("Login introuvable!<br>Peut-être qu'il a déjà été bloqué.");
    }

    public static HashMap<String, Object> executeWithConnection(String actionUri, HashMap<String, Object> data){
        HashMap<String, Object> hmapResponse = new HashMap<>(2);
        HashMap<String, Object> hmapScope = new HashMap<>(2);
        hmapResponse.put("scope", hmapScope);
        Connection connectionCentrale = null;
        try {
            try {
                connectionCentrale = Helper.getCentConn();
            } catch (SQLException e) {
                hmapScope.put("form_error", "Problème de connection.");
                throw e;
            } catch(Exception e){
                hmapScope.put("form_error", "Un problème est survenu <br> pendant la connection.<br>("+e.getMessage()+")");
                throw e;
            }
            /*code*/
            switch (actionUri){
                case "autoriser_new_login": autoriserNewLogin(hmapResponse, hmapScope, data, connectionCentrale); break;
                case "bloquer_new_login": bloquerNewLogin(hmapResponse, hmapScope, data, connectionCentrale); break;
                case "autoriser_old_login_en_lst_d_attente": autoriserOldLoginEnLstDAttente(hmapResponse, hmapScope, data, connectionCentrale); break;
                case "update_autorised_old_login_profil": updateAutorisedOldLoginProfil(hmapResponse, hmapScope, data, connectionCentrale); break;
                case "autoriser_old_login_d_un_user": autoriserOldLoginDUnUser(hmapResponse, hmapScope, data, connectionCentrale); break;
                case "bloquer_old_login": bloquerOldLoginDUnUser(hmapResponse, hmapScope, data, connectionCentrale); break;
            }
            /*code*/
            if(!hmapResponse.containsKey("status"))
                hmapResponse.put("status", true);
        }catch (Exception e) {
            if( (e instanceof SQLException) && (NOT_AUTORISED_USER_PROFIL_SQL_STATE.equals(((SQLException)e).getSQLState())) ){
                hmapScope.put("form_error", "Votre profil n'est pas autorisé à faire ça!");
            }else if(!hmapScope.containsKey("form_error"))
                hmapScope.put("form_error", "Un problème est survenu.<br>("+StringHelper.coalesce(e.getMessage(), e.getClass().getName())+")");
            hmapResponse.put("status", false);
        }finally {
            if(connectionCentrale!=null){
                try {
                    connectionCentrale.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Function.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }return hmapResponse;
    }

}
