/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.mail.internet.AddressException;
import usefull.user.PassWordHelper;
import usefull.user.exception.PasswordException;
import synchronisable.exception.NullArgumentException;
import usefull.StringHelper;
import usefull.dao.CRUD;
import usefull.dao.Helper;
import usefull.dao.Key;
import usefull.dao.exception.PKNotFoundException;
import usefull.dao.exception.UnsupportedKeyTypeException;
import usefull.user.EmailHelper;
import usefull.user.exception.EmailNotFoundException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class User{
    private String id = "DEFAULT";
    private String email = null;
    private String pwd = null;
    private String profilId = null;
    private int profilAutority = 0;
    private String profilNom = null;
    private Timestamp profilDateSuppr = null;
    private  Timestamp loginDateSuppr = null;
   

    
    public User(String id, String email, String pwd,String profilId, int profilAutority,
            String profilNom, Timestamp profilDateSuppr, Timestamp loginDateSuppr
    )throws NullArgumentException, AddressException,PasswordException{
        setId(id);setEmail(email);setPwd(pwd);setProfilId(profilId);
        setProfilAutority(profilAutority);setProfilNom(profilNom);
        setProfilDateSuppr(profilDateSuppr); setLoginDateSuppr(loginDateSuppr);
    }

    public User(){
    }

    
    public void setProfilId(String profilId) throws NullArgumentException,NumberFormatException{
        if(StringHelper.isEmpty(profilId))
            throw new NullArgumentException();
        new Long(profilId);
        this.profilId = profilId;
    }
    
    
    public void setInputPwd(String inputPwd) throws PasswordException, NoSuchAlgorithmException, UnsupportedEncodingException{
        PassWordHelper.checkInputPwd(inputPwd);
        this.pwd = PassWordHelper.getHash(PassWordHelper.SALT.replaceFirst("@input_pwd", inputPwd));
    }
    
    public void comparePwd(String checkedInputPassword) throws PasswordException,NoSuchAlgorithmException, UnsupportedEncodingException{//efa nandalo checkInputPwd
        if(!PassWordHelper.getHash(PassWordHelper.SALT.replaceFirst("@input_pwd", checkedInputPassword)).equals(getPwd())){
            throw new PasswordException("Mot de passe incorrect.");
        }
    }
            
    public void setPwd(String password) throws PasswordException{
        this.pwd = password;
    }
    
    public void setEmail(String email) throws NullArgumentException, AddressException {
        EmailHelper.checkEmail(email);
        this.email = email;
    }
    
    public void setId(String id) throws NullArgumentException,NumberFormatException{
        if(StringHelper.isEmpty(id))
            throw new NullArgumentException();
        new Long(id);
        this.id = id;
    }

    
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPwd() {
        return pwd;
    }

    public String getProfilId() {
        return profilId;
    }

    public int getProfilAutority() {
        return profilAutority;
    }

    public void setProfilAutority(int profilAutority) {
        this.profilAutority = profilAutority;
    }

    public String getProfilNom() {
        return profilNom;
    }

    public void setProfilNom(String profilNom) {
        this.profilNom = profilNom;
    }

    public Timestamp getProfilDateSuppr() {
        return profilDateSuppr;
    }

    public void setProfilDateSuppr(Timestamp profilDateSuppr) {
        this.profilDateSuppr = profilDateSuppr;
    }

    public Timestamp getLoginDateSuppr() {
        return loginDateSuppr;
    }

    public void setLoginDateSuppr(Timestamp loginDateSuppr) {
        this.loginDateSuppr = loginDateSuppr;
    }

    public void setPK(String pk) {
        setId(id);
    }

    public String getPK() {
        return getId();
    }

    
    public static User findByEmail(String stationCibleType, String email, Connection periConnection) throws NullArgumentException, EmailNotFoundException, UnsupportedKeyTypeException, SQLException, Exception {
        String sqlR = (Helper.centraleStationType.equals(stationCibleType))
            ?
            "select\n" +
            "    user_id, email, user_pwd, profil_id,\n" +
            "    profil_autority, profil_nom,\n" +
            "    profil_date_suppr, login_date_suppr\n" +
            "from v_user_data\n" +
            "where lower(email) = lower(@t_login_email)"
            :
            "select\n"
                + " t_user_id, t_login_email, t_user_pwd, t_profil_id, "
                + " t_profil_autority, t_profil_nom, "
                + " t_profil_date_suppr, t_login_date_suppr\n" +
            "from\n" +
            "    t_user_data\n" +
            "where lower(t_login_email) = lower(@t_login_email)"
        ;
        sqlR = sqlR.replaceFirst("@t_login_email", CRUD.stringify(email));
        Key[] orderdKeys = new Key[]{
            new Key<>(String.class), new Key<>(String.class),new Key<>(String.class),new Key<>(String.class),
            new Key<>(Integer.class), new Key<>(String.class),
            new Key<>(Timestamp.class), new Key<>(Timestamp.class)
        };
        CRUD.scalar(sqlR, periConnection, orderdKeys);
        if(orderdKeys[0].getValue()==null) throw new EmailNotFoundException();
        return new User(
            (String)orderdKeys[0].getValue(), (String)orderdKeys[1].getValue(), (String)orderdKeys[2].getValue(),(String)orderdKeys[3].getValue(), 
            (Integer)orderdKeys[4].getValue(), (String)orderdKeys[5].getValue(), 
            (Timestamp)orderdKeys[6].getValue(), (Timestamp)orderdKeys[7].getValue()
        );        
    }

    public static User findByPk(String pk, Connection periConnection) throws NullArgumentException, NumberFormatException, UnsupportedKeyTypeException, PKNotFoundException, SQLException, Exception {
        if(StringHelper.isEmpty(pk))throw new NullArgumentException();
        new Long(pk);
        String sqlR = ""
            + "select\n" +
            "      t_user_id, t_login_email, t_user_pwd, t_profil_id, "
                + "t_profil_autority, t_profil_nom, "
                + "t_profil_date_suppr, t_login_date_suppr\n" +
            "from\n" +
            "    t_user_data\n" +
            "where t_user_id = @t_user_id"
        ;
        sqlR = sqlR.replaceFirst("@t_user_id", pk);
        Key[] orderdKeys = new Key[]{
            new Key<>(String.class), new Key<>(String.class),new Key<>(String.class),new Key<>(String.class),
            new Key<>(Integer.class), new Key<>(String.class),
            new Key<>(Timestamp.class), new Key<>(Timestamp.class)
        };
        CRUD.scalar(sqlR, periConnection, orderdKeys);
        if(orderdKeys[0].getValue()==null) throw new PKNotFoundException();
        return new User(
            (String)orderdKeys[0].getValue(), (String)orderdKeys[1].getValue(), (String)orderdKeys[2].getValue(),(String)orderdKeys[3].getValue(), 
            (Integer)orderdKeys[4].getValue(), (String)orderdKeys[5].getValue(), 
            (Timestamp)orderdKeys[6].getValue(), (Timestamp)orderdKeys[7].getValue()
        );        
    }
   
    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException, PasswordException {
        
        PassWordHelper.checkInputPwd("Dev0.");
        
        String hash = PassWordHelper.getHash(PassWordHelper.SALT.replaceFirst("@input_pwd", "Dev0."));
        String hash1 = PassWordHelper.getHash(PassWordHelper.SALT.replaceFirst("@input_pwd", "Usr1."));
        String hash2 = PassWordHelper.getHash(PassWordHelper.SALT.replaceFirst("@input_pwd", "Usr2."));
        
        hash1.length();
        hash2.length();
        hash1.equals(hash2);
        System.out.println("login.User.main()");
    }
}
