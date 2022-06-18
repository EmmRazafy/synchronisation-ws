/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronisation.u_i_conflict.solve.exception;

/**
 *
 * @author P12A-92-Emmanuel
 */
public class ConflictUpdateException extends Exception{

    public ConflictUpdateException() {
        super("l'élement en ligne(centrale) en conflit avec l'élement en locale à changer");
    }

    public ConflictUpdateException(String message) {
        super(message);
    }
    
}
