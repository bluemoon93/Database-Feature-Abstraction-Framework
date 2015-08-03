
package sc;

public interface ICrud {
    /*
    String S_Custumers_all = "SELECT * FROM Northwind.dbo.Customers WHERE Country = ''USA'';";
    String S_Orders_byShipCountry = "SELECT * FROM Northwind.dbo.Orders WHERE CustomerId = @CustomerId and ShipCountry = @ShipCountry;";
    String I_Orders_withCostumerID = "EXEC PolicyServer2._remote.RemoteCall @SessionID = ?, @QuerySRID = ?, @Params = '@CustomerID = ?, @EmployeeID = ?, @OrderDate = ?, @RequiredDate = ?, @ShippedDate = ?, @ShipVia = ?, @Freight = ?, @ShipName = ?, @ShipAddress = ?, @ShipCity = ?, @ShipRegion = ?, @ShipPostalCode = ?, @ShipCountry = ?';";
    String U_Orders_shipDestination = "UPDATE Northwind.dbo.Orders SET ShipAddress = @ShippedAddress, ShipCity = @ShippedCity, ShipCountry = @ShippedCountry WHERE OrderID = @OrderID;";

    */
    // to do
    String S_Custumers_all = "EXEC PolicyServer2._remote.RemoteCall @SessionID = ?, @QuerySRID = ?;";
    String S_Orders_byShipCountry = "EXEC PolicyServer2._remote.RemoteCall @SessionID = ?, @QuerySRID = ?, @Params = '@CustomerID = ?, @ShipCountry = ?';";
    String I_Orders_withCostumerID = "EXEC PolicyServer2._remote.RemoteCall @SessionID = ?, @QuerySRID = ?, @Params = '@CustomerID = ?, @EmployeeID = ?, @OrderDate = ?, @RequiredDate = ?, @ShippedDate = ?, @ShipVia = ?, @Freight = ?, @ShipName = ?, @ShipAddress = ?, @ShipCity = ?, @ShipRegion = ?, @ShipPostalCode = ?, @ShipCountry = ?';";
    String U_Orders_shipDestination = "EXEC PolicyServer2._remote.RemoteCall @SessionID = ?, @QuerySRID = ?, @Params = '@OrderID = ?, @ShippedAddress = ?, @ShippedCity = ?, @ShippedCountry = ?';";
    String S_Customers_byName = "EXEC PolicyServer2._remote.RemoteCall @SessionID = ?, @QuerySRID = ?, @Params = '@CustomerID = ?;";
}
