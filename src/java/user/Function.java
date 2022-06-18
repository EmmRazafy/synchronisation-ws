package user;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.internet.AddressException;
import synchronisable.exception.NullArgumentException;
import usefull.HashHelper;
import usefull.StringHelper;
import usefull.dao.CRUD;
import usefull.user.EmailHelper;
import usefull.dao.Helper;
import usefull.dao.Key;
import usefull.dao.exception.IndefinedStaionTypeException;
import usefull.user.PassWordHelper;
import usefull.user.Person;
import usefull.user.exception.EmailNotFoundException;
import usefull.user.exception.PasswordException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author P12A-92-Emmanuel
 * 
 */
public class Function {
    private final static String NOT_OTORISED_EMAIL_SING_UP_ERROR_STATE = "235YY";
    private final static String USER_EMAIL_SING_UP_ERROR_STATE = "235Z1";
    private final static String STAND_BY_USER_EMAIL_ERROR_STATE = "235Z2";
    
    private final static String PRE_AUTORISED_LOGIN_CONTEXT = "pre-autorised_email";
    private final static String LOGIN_WAITING_VALIDATION_BY_E_MAIL_CONTEXT = "new_email_validable_by_e-mail";
    
    
    static HashMap<String, Object> checkField(String name, String value) {
        FuturUser futurUser = new FuturUser();
        try{
            futurUser.set(name, value);
            return  HashHelper.newHashMap(new String[]{"status"}, new Object[]{true});
        } catch (Exception e) {
            return HashHelper.newHashMap(new String[]{"status", "message"}, new Object[]{false, StringHelper.coalesce(e.getMessage(), e.getClass().getName())});
        }
    }
    
    public static HashMap<String, Object> singUp(
        String genre, String nom, String email, 
        String tel1, String tel2, String tel3, String desc, 
        String inputPwd, String pwdConfirmation
    ){
        HashMap<String, Object> hmapResponse = new HashMap<>(3);
        HashMap<String, Object> hmapScope = new HashMap<>(5);
        hmapResponse.put("scope", hmapScope);
        HashMap<String, Object> field = new HashMap<String, Object>(10);
        hmapScope.put("fields", field);
        FuturUser futurUser = new FuturUser();

        try{
            futurUser.setGenre(genre);
            field.put("nom", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("nom", HashHelper.newHashMap(new String[]{"status", "message"}, new Object[]{false, StringHelper.coalesce(e.getMessage(), e.getClass().getName())}) );
            hmapResponse.put("status", false);
        }
        HashMap<String, Object> nomValidationResult = (HashMap<String, Object>) field.get("nom");
        if((Boolean)nomValidationResult.get("status")){
            try{
                futurUser.setNom(nom);
            } catch (Exception e) {
                nomValidationResult.put("status", false);
                nomValidationResult.put("message", StringHelper.coalesce(e.getMessage(), e.getClass().getName()));
                hmapResponse.put("status", false);
            }
        }
        try{
            futurUser.setEmail(email);
            field.put("email", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("email", HashHelper.newHashMap(new String[]{"status", "message"}, new Object[]{false, StringHelper.coalesce(e.getMessage(), e.getClass().getName())}) );
            hmapResponse.put("status", false);
        }
        try{
            futurUser.setTel1(tel1);
            field.put("tel1", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("tel1", HashHelper.newHashMap(new String[]{"status", "message"}, new Object[]{false, StringHelper.coalesce(e.getMessage(), e.getClass().getName())}) );
            hmapResponse.put("status", false);
        }
        try{
            futurUser.setTel2(tel2);
            field.put("tel2", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("tel2", HashHelper.newHashMap(new String[]{"status", "message"}, new Object[]{false, StringHelper.coalesce(e.getMessage(), e.getClass().getName())}) );
            hmapResponse.put("status", false);
        }
        try{
            futurUser.setTel3(tel3);
            field.put("tel3", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("tel3", HashHelper.newHashMap(new String[]{"status", "message"}, new Object[]{false, StringHelper.coalesce(e.getMessage(), e.getClass().getName())}) );
            hmapResponse.put("status", false);
        }
        try{
            futurUser.setDesc(desc);
            field.put("desc", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("desc", HashHelper.newHashMap(new String[]{"status", "message"}, new Object[]{false, StringHelper.coalesce(e.getMessage(), e.getClass().getName())}) );
            hmapResponse.put("status", false);
        }
        try{
            futurUser.setInputPwd(inputPwd);
            field.put("pwd", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("pwd", HashHelper.newHashMap(new String[]{"status", "message"}, new Object[]{false, StringHelper.coalesce(e.getMessage(), e.getClass().getName())}) );
            hmapResponse.put("status", false);
        }
        try{
            futurUser.setPwdConfirmation(pwdConfirmation);
            field.put("pwd_repeat", HashHelper.newHashMap(new String[]{"status"}, new Object[]{true}) );
        } catch (Exception e) {
            field.put("pwd_repeat", HashHelper.newHashMap(new String[]{"status", "message"}, new Object[]{false, StringHelper.coalesce(e.getMessage(), e.getClass().getName())}) );
            hmapResponse.put("status", false);
        }
        if(hmapResponse.containsKey("status") && (!(Boolean)hmapResponse.get("status")) ){//efa faulse zay
            hmapScope.put("form_error", "Erreur de saisie!");
            return hmapResponse;
        }

        Connection connectionCentrale = null;
        try {
            try {
                connectionCentrale = Helper.getCentConn();
            } catch (SQLException e) {
                hmapScope.put("form_error", "problème de connection.");
                throw e;
            } catch(Exception e){
                hmapScope.put("form_error", "un problème est survenu <br> pendant la connection.<br>("+e.getMessage()+")");
                throw e;
            }
            String sqlR = ""+
                "SELECT\n" +
                "    state, user_id, profil_id, profil_autority,  profil_nom,  profil_date_suppr, login_date_suppr\n" +
                "FROM\n" +
                "    sing_up("+CRUD.stringify(futurUser.getGenre())+", "+CRUD.stringify(futurUser.getNom())+", "+CRUD.stringify(futurUser.getEmail())+", "+CRUD.stringify(futurUser.getTel1())+", "+CRUD.stringify(futurUser.getTel2())+", "+CRUD.stringify(futurUser.getTel3())+", "+CRUD.stringify(futurUser.getDesc())+" , "+CRUD.stringify(futurUser.getPwd())+")\n"
            ;    
            Key[] keys = new Key[]{new Key<>("state", String.class), new Key<>("user_id", Long.class), new Key<>("profil_id", Long.class), new Key<>("profil_autority", Integer.class), new Key<>("profil_nom", String.class), new Key<>("profil_date_suppr", Timestamp.class), new Key<>("login_date_suppr", Timestamp.class)};
            
            connectionCentrale.setAutoCommit(false);
            CRUD.scalar(sqlR, connectionCentrale, keys);
            String context = ((Key<String>)keys[0]).getValue();
            hmapResponse.put("context", context);
            if(PRE_AUTORISED_LOGIN_CONTEXT.equals(context)){
                hmapResponse.put("user", new User( ((Key<Long>)keys[1]).getValue().toString(), futurUser.getEmail(), futurUser.getPwd(), ((Key<Long>)keys[2]).getValue().toString(), ((Key<Integer>)keys[3]).getValue(), ((Key<String>)keys[4]).getValue(), ((Key<Timestamp>)keys[5]).getValue(), ((Key<Timestamp>)keys[6]).getValue()) );
            }else if(LOGIN_WAITING_VALIDATION_BY_E_MAIL_CONTEXT.equals(context)){
                hmapScope.put("notification_message", Person.displayGenreNom(futurUser.getGenre(), futurUser.getNom())+",<br> vous êtes actuellement en liste d'attente.<br> Un e-mail de confirmation vous sera envoyé bientôt!<br> Merci de consulter votre boîte e-mail!");
            }connectionCentrale.commit();
            
            hmapResponse.put("status", true);
        } catch (Exception e) {
            try {
                connectionCentrale.rollback();
            } catch (SQLException ex) {
                hmapScope.put("form_error", "un problème est survenu <br> pendant l'annulation de la transaction.<br>("+StringHelper.coalesce(e.getMessage(), e.getClass().getName())+")");
            }
            
            if( (e instanceof SQLException) && (NOT_OTORISED_EMAIL_SING_UP_ERROR_STATE.equals(((SQLException)e).getSQLState())) )
                hmapScope.put("form_error", "E-mail non autorisé!");
            
            if( (e instanceof SQLException) && (USER_EMAIL_SING_UP_ERROR_STATE.equals(((SQLException)e).getSQLState())) )
                hmapScope.put("form_error", "L'E-mail appartient déjà à un autre utilisateur!");
            
            if( (e instanceof SQLException) && (STAND_BY_USER_EMAIL_ERROR_STATE.equals(((SQLException)e).getSQLState())) )
                hmapScope.put("form_error", "L'E-mail existe déjà dans la liste d'attente!");
            
            
            if(!hmapScope.containsKey("form_error"))
                hmapScope.put("form_error", "un problème est survenu.<br>("+StringHelper.coalesce(e.getMessage(), e.getClass().getName())+")");
            hmapResponse.put("status", false);
        }finally{
            if(connectionCentrale!=null){
                try {
                    connectionCentrale.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Function.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }return hmapResponse;
    }

    public static HashMap<String, Object> logIn(String stationCibleType, String email, String inputPwd){
        HashMap<String, Object> hmapResponse = new HashMap<>(3);
        HashMap<String, Object> hmapScope = new HashMap<>(5);
        hmapResponse.put("scope", hmapScope);
        hmapScope.put("email", email);
        hmapScope.put("pwd", inputPwd);
        try {
            EmailHelper.checkEmail(email);
        } catch (NullArgumentException|AddressException e) {
            hmapScope.put("email_error", e.getMessage());
            hmapResponse.put("status", false);
        }
        try {
            PassWordHelper.checkInputPwd(inputPwd);
        } catch (PasswordException e) {
            hmapScope.put("pwd_error", e.getMessage());
            hmapResponse.put("status", false);
        }
        if(hmapResponse.containsKey("status"))//efa faulse zay
            return hmapResponse;
        

        
        Connection connection = null;
        try {
            if(Helper.centraleStationType.equals(stationCibleType)){
                try {
                    connection = Helper.getCentConn();
                } catch (SQLException e) {
                    hmapScope.put("form_error", "problème de connection.");
                    throw e;
                }catch(Exception e){
                    hmapScope.put("form_error", "un problème est survenu <br> pendant la connection.<br>("+e.getMessage()+")");
                    throw e;
                }
            }else if(Helper.peripheriqueStationType.equals(stationCibleType)){
                try {
                    connection = Helper.getPeriConn();
                } catch (SQLException e) {
                    hmapScope.put("form_error", "problème de connection.");
                    throw e;
                }catch(Exception e){
                    hmapScope.put("form_error", "un problème est survenu <br> pendant la connection.<br>("+e.getMessage()+")");
                    throw e;
                }
            }else{
                IndefinedStaionTypeException indefinedStaionTypeException = new IndefinedStaionTypeException();
                hmapScope.put("form_error", indefinedStaionTypeException.getMessage());
                throw indefinedStaionTypeException;
            }
        
            User user = null;
            try {
                user = User.findByEmail(stationCibleType, email, connection);
            } catch (EmailNotFoundException e) {
                hmapScope.put("email_error", "E-mail inconnu!");
                throw e;
            }catch(Exception e){
                hmapScope.put("form_error", "Un problème est survenu.("+e.getMessage()+")");
                throw e;
            }
            
            try {
                user.comparePwd(inputPwd);
            } catch (PasswordException e) {
                hmapScope.put("pwd_error", e.getMessage());
                throw e;
            }catch(Exception e){
                hmapScope.put("form_error", "Un problème est survenu.<br>("+e.getMessage()+")");
                throw e;
            }
            
            hmapResponse.put("user", user);
            hmapResponse.put("status", true);
        } catch (Exception e) {
            hmapResponse.put("status", false);
        }finally{
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Function.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return hmapResponse;
    }
    
}
