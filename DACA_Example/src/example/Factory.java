package example;

import BusinessManager.IBusinessManager;
import BusinessManager.ISession;
import Classes.Role_IRole_B1;
import S_Customers.IS_Customers;

/**
 * Created by Regateiro on 24-02-2014.
 */
public class Factory {

    private final IBusinessManager businessManager;

    public Factory(IBusinessManager manager) {
        assert manager != null : "Manager passed as null.";
        this.businessManager = manager;
    }

    public IS_Customers get_S_Customers_all(ISession session) throws LocalTools.BTC_Exception {
        return businessManager.instantiateBS(Role_IRole_B1.s_customers, Role_IRole_B1.s_customers_S_Custumers_all, session);
    }
}
