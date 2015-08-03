/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package securityconfigurator.Utils;

public class BTC_Exception extends Exception {

    public BTC_Exception(String exception) {
        super(exception);
    }

    public BTC_Exception() {
    }

    public BTC_Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public BTC_Exception(Throwable cause) {
        super(cause);
    }

    public BTC_Exception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    
}
