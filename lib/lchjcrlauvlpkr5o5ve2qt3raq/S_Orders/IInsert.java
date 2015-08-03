package S_Orders;
import java.math.*;
import java.sql.*;

public interface IInsert {
 void beginInsert() throws SQLException;
 void endInsert(boolean moveToPreviousRow) throws SQLException;
 void cancelInsert() throws SQLException;

void iCustomerID(String CustomerID) throws SQLException;

void iEmployeeID(int EmployeeID) throws SQLException;

void iOrderDate(Date OrderDate) throws SQLException;

void iRequiredDate(Date RequiredDate) throws SQLException;

void iShippedDate(Date ShippedDate) throws SQLException;

void iShipVia(int ShipVia) throws SQLException;

void iFreight(double Freight) throws SQLException;

void iShipName(String ShipName) throws SQLException;

void iShipAddress(String ShipAddress) throws SQLException;

void iShipCity(String ShipCity) throws SQLException;

void iShipRegion(String ShipRegion) throws SQLException;

void iShipPostalCode(String ShipPostalCode) throws SQLException;

void iShipCountry(String ShipCountry) throws SQLException;

void iOrderID(int OrderID) throws SQLException;
}