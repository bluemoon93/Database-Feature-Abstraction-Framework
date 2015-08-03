package S_Customers;

import BusinessManager.ISession;
import BusinessInterfaces.*;

public abstract interface IS_Customers extends IExecute, IScrollable, IRead, IUpdate, IInsert, IDelete {

    public S_Orders.IS_Orders nextBE_S1(int crud, ISession session) throws LocalTools.BTC_Exception;
    public I_Orders.II_Orders nextBE_S2(int crud, ISession session) throws LocalTools.BTC_Exception;
}
