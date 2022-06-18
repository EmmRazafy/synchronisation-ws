/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import javax.mail.internet.AddressException;
import synchronisable.exception.NullArgumentException;
import usefull.user.PhoneNumberHelper;
import usefull.StringHelper;
import usefull.dao.type.DBEnumType;
import usefull.dao.type.DBTypeLength;
import usefull.user.EmailHelper;
import usefull.user.PassWordHelper;
import usefull.user.exception.PasswordException;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class FuturUser {
    private String id = "DEFAULT";
    private String genre = null;
    private String nom = null;
    private String email = null;
    private String tel1 = null;
    private String tel2 = null;
    private String tel3 = null;
    private String desc = null;
    
    private String pwd = null;
    private String inputPwd = null;
    private String pwdConfirmation = null;
    
    public void setId(String id) throws NullArgumentException,NumberFormatException{
        if(StringHelper.isEmpty(id))
            throw new NullArgumentException();
        new Long(id);
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
        
    
    public void setPK(String pk) {
        setId(id);
    }

    public String getPK() {
        return getId();
    }
    
    public void setEmail(String email) throws NullArgumentException, AddressException {
        EmailHelper.checkEmail(email);
        this.email = email;
    }
        
         
    public String getEmail() {
        return email;
    }

    
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        if(StringHelper.isEmpty(genre)) throw new NullArgumentException("Le Genre est obligatoire!");
        if(!DBEnumType.IS_GENRE_ID(genre))throw new NullArgumentException("Le Genre est invalide!");
        this.genre = genre;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = StringHelper.checkRealStrLength(nom, 3, DBTypeLength.NOM_LONG);
    }

    public String getTel1() {
        return tel1;
    }

    public void setTel1(String tel1) throws NullArgumentException, Exception {
        this.tel1 = StringHelper.isEmpty(tel1)? null: PhoneNumberHelper.checkPhoneNumber(tel1);
    }

    public String getTel2() {
        return tel2;
    }

    public void setTel2(String tel2) throws NullArgumentException, Exception {
        this.tel2 = StringHelper.isEmpty(tel2)? null: PhoneNumberHelper.checkPhoneNumber(tel2);
    }

    public String getTel3() {
        return tel3;
    }

    public void setTel3(String tel3) throws NullArgumentException, Exception {
        this.tel3 = StringHelper.isEmpty(tel3)? null: PhoneNumberHelper.checkPhoneNumber(tel3);
    }
    
    
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = StringHelper.isEmpty(desc)? null : StringHelper.checkStrLength(desc, 1, DBTypeLength.SHORT_DESCRIPTION);
    }
    
    public void setInputPwd(String inputPwd) throws PasswordException, NoSuchAlgorithmException, UnsupportedEncodingException{
        PassWordHelper.checkInputPwd(inputPwd);
        this.inputPwd = inputPwd;
        this.pwd = PassWordHelper.getHash(PassWordHelper.SALT.replaceFirst("@input_pwd", inputPwd));
    }
    
    public void setPwd(String password) throws PasswordException{
        this.pwd = password;
    }   

    public String getPwd() {
        return pwd;
    }
    
    public static String checkPwdConfirmation(String inputPwd, String inputPwdConfirmation){
        if(! ((StringHelper.isEmpty(inputPwd) && StringHelper.isEmpty(inputPwdConfirmation)) || ((inputPwd != null) && (inputPwd.equals(inputPwdConfirmation)))))
            throw new NullArgumentException("Confirmation incorrecte!");
        return inputPwdConfirmation;
    }

    public String getPwdConfirmation() {
        return pwdConfirmation;
    }

    public void setPwdConfirmation(String pwdConfirmation) {
        this.pwdConfirmation = checkPwdConfirmation(this.inputPwd, pwdConfirmation);
    }
    
    public void set(String name, String value) throws NullArgumentException, AddressException, Exception{
        FuturUser futurUser = new FuturUser();
        switch (name) {
            case "genre":
                futurUser.setGenre(value);
                break;
            case "nom":
                futurUser.setNom(value);
                break;
            case "email":
                futurUser.setEmail(value);
                break;
            case "tel1":
                futurUser.setTel1(value);
                break;
            case "tel2":
                futurUser.setTel2(value);
                break;
            case "tel3":
                futurUser.setTel3(value);
                break;
            case "desc":
                futurUser.setDesc(value);
                break;
            case "pwd":
                futurUser.setInputPwd(value);
                break;
        }
    }
}
