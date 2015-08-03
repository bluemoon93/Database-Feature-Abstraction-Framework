package S_Orders;
import java.math.*;
import java.sql.*;

import S_Customers.IRCustomerID;
public interface IUpdate {
 void beginUpdate() throws SQLException;
 void updateRow() throws SQLException;
 void cancelUpdate() throws SQLException;

void uShipName(String ShipName) throws SQLException;

void uEmployeeID(int EmployeeID) throws SQLException;

void uOrderDate(Date OrderDate) throws SQLException;

void uRequiredDate(Date RequiredDate) throws SQLException;

void uShippedDate(Date ShippedDate) throws SQLException;

void uShipVia(int ShipVia) throws SQLException;

void uOrderID(int OrderID) throws SQLException;

void uCustomerID(IRCustomerID CustomerID) throws SQLException;

void uShipRegion(String ShipRegion) throws SQLException;

void uShipPostalCode(String ShipPostalCode) throws SQLException;

void uShipAddress(String ShipAddress) throws SQLException;

void uShipCity(String ShipCity) throws SQLException;

void uShipCountry(String ShipCountry) throws SQLException;

void uFreight(double Freight) throws SQLException;
}