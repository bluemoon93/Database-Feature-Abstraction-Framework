/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package LocalTools;

/**
 *
 * @author bluemoon
 */
public class MessageTypes {
    public static final int NEXT=-1, ABSOLUTE=12, RELATIVE=-13, PREVIOUS=-14, 
            BEFORE_FIRST=-15, AFTER_LAST=-16, FIRST=-18, LAST=-19,
            GET=-2, SET=-3, EXEC=-17, DELETE_ROW=-20, 
            BEGIN_UPDATE=-4, CANCEL_UPDATE=-5, UPDATE_ROW=-6, UPDATE_VAL=-7, 
            BEGIN_INSERT=-8, CANCEL_INSERT=-9, INSERT_ROW=-10, INSERT_VAL=-11;
    
    public static final int COMMIT=-21, RB=-23, AUTOCOMMIT=-26,
            RELSP=-22, RBSP=-24, SP=-25, SPNAME=-27;
}
