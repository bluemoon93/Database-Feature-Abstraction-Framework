package S_Orders;

import BusinessManager.ISession;
import BusinessInterfaces.*;

public abstract interface IS_Orders extends ISet, IExecute, IScrollable, IRead, IUpdate, IInsert, IDelete {

    public U_Orders.IU_Orders nextBE_S1(int crud, ISession session) throws LocalTools.BTC_Exception;
}
