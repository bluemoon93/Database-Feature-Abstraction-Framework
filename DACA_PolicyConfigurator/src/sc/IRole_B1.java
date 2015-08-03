package sc;

import I_Orders.II_Orders;
import S_Customers.IS_Customers;
import S_Customers_byName.IS_Customers2;
import S_Orders.IS_Orders;
import U_Orders.IU_Orders;


public interface IRole_B1 {

    public static final Class<IS_Customers> S_Customers = IS_Customers.class;
    public static String[] crud_S_Custumers_all = new String[]{ICrud.S_Custumers_all};
    public static final Class<IS_Orders> S_Orders = IS_Orders.class;
    public static String[] crud_S_Orders_byShipCountry = new String[]{ICrud.S_Orders_byShipCountry};
    public static final Class<II_Orders> I_Orders = II_Orders.class;
    public static String[] crud_I_Orders_withCostumerID = new String[]{ICrud.I_Orders_withCostumerID};
    public static final Class<IU_Orders> U_Orders = IU_Orders.class;
    public static String[] crud_U_Orders_shipDestination = new String[]{ICrud.U_Orders_shipDestination};
    
    //to do
    public static final Class<IS_Customers2> S_Customers_byName = IS_Customers2.class;
    public static String[] crud_S_Customers_byName = new String[]{ICrud.S_Customers_byName};
}
