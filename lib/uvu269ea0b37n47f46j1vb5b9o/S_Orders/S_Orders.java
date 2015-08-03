package S_Orders;
import BusinessInterfaces.*;
import BusinessManager.*;
import java.math.*;
import java.sql.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.io.File;
import java.util.Random;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.lang.reflect.Constructor;
import java.util.jar.*;
import java.lang.reflect.Method;

import S_Customers.IRCustomerID;
import S_Customers.IRCustomerID;


//This is a modded generator!

public class S_Orders implements IS_Orders{
public static final int NEXT=-1, ABSOLUTE=12, RELATIVE=-13, PREVIOUS=-14, 
            BEFORE_FIRST=-15, AFTER_LAST=-16, FIRST=-18, LAST=-19,
            GET=-2, SET=-3, EXEC=-17, DELETE_ROW=-20,
            BEGIN_UPDATE=-4, CANCEL_UPDATE=-5, UPDATE_ROW=-6, UPDATE_VAL=-7, 
            BEGIN_INSERT=-8, CANCEL_INSERT=-9, INSERT_ROW=-10, INSERT_VAL=-11;

public static final int COMMIT=-21, RB=-23, AUTOCOMMIT=-26,
            RELSP=-22, RBSP=-24, SP=-25, SPNAME=-27;	public final int idle=0;
	public final int updating=1;
	public final int inserting=2;
	public int _state=idle;
	private String crud;
	private Integer sessionID;
	private Integer querySRID;
	private String params;
	private IBusinessSchemaController controller;
	private Integer activeSequence;
	private IBusinessManager man;
	private int queryIdentifier;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	public S_Orders(IBusinessManager man, ObjectOutputStream oos, ObjectInputStream ois, String crud, Integer sessionID, Integer querySRID, IBusinessSchemaController controller, Integer activeSequence) throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		this.sessionID=sessionID;
		this.querySRID=querySRID;
		this.crud=crud;
		this.man=man;
		this.controller=controller;
		this.activeSequence=activeSequence;
		this.oos = oos;
		this.ois = ois;
		this.queryIdentifier = new Random().nextInt();		int idx;
		if((idx = crud.indexOf("@Params")) != -1) {
			int pi = crud.indexOf('\'', idx) + 1;
			int pf = crud.indexOf('\'', pi);
			this.params = crud.substring(pi, pf);
			this.crud = crud.substring(0, pi-1) + "?";
		} else this.params = null;
		_state=idle;
	}
	@Override
	public void execute(IRCustomerID args0, String args1) throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	String execParams = this.params;
			execParams = execParams.replaceFirst("\\?","\""+args0.CustomerID()+"\"");
			execParams = execParams.replaceFirst("\\?","\""+args1+"\"");
		try{
			oos.writeInt(3);
			oos.flush();
			oos.writeBoolean(true);
			oos.flush();
			oos.writeObject(this.sessionID);
			oos.flush();
			oos.writeObject(this.querySRID);
			oos.flush();
			oos.writeObject(execParams);
			oos.flush();			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
	@Override
	public void execute() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(2);
			oos.flush();
			oos.writeBoolean(true);
			oos.flush();
			oos.writeObject(this.sessionID);
			oos.flush();
			oos.writeObject(this.querySRID);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
	public boolean validateSchema(Class interfaceSchema) {
		String myloc=this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		File jarIn = new File(myloc.substring(5));
		URL url = null;
		Class BTE = null;
		Constructor c = null;
		URLClassLoader cls=null;
		try {
			url = jarIn.toURL();
			URL[] urls = new URL[]{url};
		cls = new URLClassLoader(urls);
		ClassLoader cl = cls;
		String interfacestr=interfaceSchema.getInterfaces()[0].getName();
		String st1=interfacestr.substring(0,interfacestr.lastIndexOf('.')+1);
		String st2=interfacestr.substring(interfacestr.lastIndexOf('.')+2);
		String mainesq=st1+st2;
		BTE = cl.loadClass(mainesq);
		String otherloc=BTE.getProtectionDomain().getCodeSource().getLocation().toString();
		cls.close();
		if(myloc.compareTo(otherloc)==0)
			return true;
		else
			return false;
		} catch (Exception ex) {
			try {
				cls.close();
				return false;
			} catch (Exception ex2) {
			return false;
		}		}
	}
	public U_Orders.IU_Orders nextBE_S1(int crud, ISession session) throws BusinessManager.BTC_Exception {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		return man.instantiateBS(U_Orders.IU_Orders.class, crud, session, activeSequence);
	}
	@Override
	public boolean moveNext() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(NEXT);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return ois.readBoolean();
		}catch(Exception ex){ ex.printStackTrace(); }
		return false;
	}

	@Override
	public boolean moveAbsolute(int pos) throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(ABSOLUTE);
			oos.flush();
			oos.writeInt(pos);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return ois.readBoolean();
		}catch(Exception ex){ ex.printStackTrace(); }
		return false;
	}

	@Override
	public boolean moveRelative(int offset) throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(RELATIVE);
			oos.flush();
			oos.writeInt(offset);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return ois.readBoolean();
		}catch(Exception ex){ ex.printStackTrace(); }
		return false;
	}

	@Override
	public void moveBeforeFirst() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(BEFORE_FIRST);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}

	@Override
	public boolean moveFirst() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(FIRST);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return ois.readBoolean();
		}catch(Exception ex){ ex.printStackTrace(); }
		return false;
	}

	@Override
	public void moveAfterLast() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(AFTER_LAST);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}

	@Override
	public boolean moveLast() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(LAST);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return ois.readBoolean();
		}catch(Exception ex){ ex.printStackTrace(); }
		return false;
	}

	@Override
	public void set(IRCustomerID args0, String args1) throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	String execParams = this.params;
			execParams = execParams.replaceFirst("\\?","\""+args0.CustomerID()+"\"");
			execParams = execParams.replaceFirst("\\?","\""+args1+"\"");
		try{
			oos.writeInt(SET);
			oos.flush();
			oos.writeObject(execParams);
			oos.flush();			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
}
	@Override
	public void set() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
}
	@Override
	public String ShipName() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("ShipName");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String CustomerID() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("CustomerID");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public int EmployeeID() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("EmployeeID");
			oos.flush();
			oos.writeUTF("int");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (int) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return 0;
	}
	@Override
	public Date OrderDate() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("OrderDate");
			oos.flush();
			oos.writeUTF("Date");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (Date) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public Date RequiredDate() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("RequiredDate");
			oos.flush();
			oos.writeUTF("Date");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (Date) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public Date ShippedDate() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("ShippedDate");
			oos.flush();
			oos.writeUTF("Date");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (Date) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String ShipCountry() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("ShipCountry");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String ShipRegion() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("ShipRegion");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public double Freight() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("Freight");
			oos.flush();
			oos.writeUTF("double");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (double) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return 0;
	}
	@Override
	public String ShipAddress() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("ShipAddress");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String ShipCity() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("ShipCity");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public int ShipVia() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("ShipVia");
			oos.flush();
			oos.writeUTF("int");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (int) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return 0;
	}
	@Override
	public String ShipPostalCode() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("ShipPostalCode");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public int OrderID() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("OrderID");
			oos.flush();
			oos.writeUTF("int");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (int) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return 0;
	}
@Override
public void beginUpdate() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state==inserting){
		throw new SQLException("Cant Update, While Inserting!");
	} else {
		_state=updating;
		try{
			oos.writeInt(BEGIN_UPDATE);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}

@Override
public void updateRow() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating){
		throw new SQLException("Cant Update, While not updating!");
	}else{
		_state=idle;
		try{
			oos.writeInt(UPDATE_ROW);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void cancelUpdate() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(CANCEL_UPDATE);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	_state=idle;
}
@Override
public void uShipName(String ShipName) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("ShipName");
			oos.flush();
			oos.writeObject(ShipName);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uEmployeeID(int EmployeeID) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("EmployeeID");
			oos.flush();
			oos.writeObject(EmployeeID);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uOrderDate(Date OrderDate) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("OrderDate");
			oos.flush();
			oos.writeObject(OrderDate);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uRequiredDate(Date RequiredDate) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("RequiredDate");
			oos.flush();
			oos.writeObject(RequiredDate);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uShippedDate(Date ShippedDate) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("ShippedDate");
			oos.flush();
			oos.writeObject(ShippedDate);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uShipVia(int ShipVia) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("ShipVia");
			oos.flush();
			oos.writeObject(ShipVia);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uOrderID(int OrderID) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("OrderID");
			oos.flush();
			oos.writeObject(OrderID);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uCustomerID(IRCustomerID CustomerID) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("CustomerID");
			oos.flush();
			oos.writeObject(CustomerID.CustomerID());
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uShipRegion(String ShipRegion) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("ShipRegion");
			oos.flush();
			oos.writeObject(ShipRegion);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uShipPostalCode(String ShipPostalCode) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("ShipPostalCode");
			oos.flush();
			oos.writeObject(ShipPostalCode);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uShipAddress(String ShipAddress) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("ShipAddress");
			oos.flush();
			oos.writeObject(ShipAddress);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uShipCity(String ShipCity) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("ShipCity");
			oos.flush();
			oos.writeObject(ShipCity);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uShipCountry(String ShipCountry) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("ShipCountry");
			oos.flush();
			oos.writeObject(ShipCountry);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uFreight(double Freight) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("Freight");
			oos.flush();
			oos.writeObject(Freight);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void beginInsert() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state==updating)
		throw new SQLException("Cant Insert, While Updating!");
	else{
		_state=inserting;}
		try{
			oos.writeInt(BEGIN_INSERT);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
}
public void endInsert(boolean moveToPreviousRow) throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant Insert, While not inserting!");
	else{
		_state=idle;
		try{
			oos.writeInt(INSERT_ROW);
			oos.flush();
			oos.writeBoolean(moveToPreviousRow);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}}
@Override
public void cancelInsert() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(CANCEL_INSERT);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	_state=idle;
}
@Override
public void iCustomerID(String CustomerID) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("CustomerID");
			oos.flush();
			oos.writeObject(CustomerID);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iEmployeeID(int EmployeeID) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("EmployeeID");
			oos.flush();
			oos.writeObject(EmployeeID);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iOrderDate(Date OrderDate) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("OrderDate");
			oos.flush();
			oos.writeObject(OrderDate);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iRequiredDate(Date RequiredDate) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("RequiredDate");
			oos.flush();
			oos.writeObject(RequiredDate);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iShippedDate(Date ShippedDate) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("ShippedDate");
			oos.flush();
			oos.writeObject(ShippedDate);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iShipVia(int ShipVia) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("ShipVia");
			oos.flush();
			oos.writeObject(ShipVia);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iFreight(double Freight) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("Freight");
			oos.flush();
			oos.writeObject(Freight);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iShipName(String ShipName) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("ShipName");
			oos.flush();
			oos.writeObject(ShipName);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iShipAddress(String ShipAddress) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("ShipAddress");
			oos.flush();
			oos.writeObject(ShipAddress);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iShipCity(String ShipCity) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("ShipCity");
			oos.flush();
			oos.writeObject(ShipCity);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iShipRegion(String ShipRegion) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("ShipRegion");
			oos.flush();
			oos.writeObject(ShipRegion);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iShipPostalCode(String ShipPostalCode) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("ShipPostalCode");
			oos.flush();
			oos.writeObject(ShipPostalCode);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iShipCountry(String ShipCountry) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("ShipCountry");
			oos.flush();
			oos.writeObject(ShipCountry);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iOrderID(int OrderID) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("OrderID");
			oos.flush();
			oos.writeObject(OrderID);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void deleteRow() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Orders.IS_Orders")) {
		throw new SequenceException("IS_Orders was used out of order!");
	}
		try{
			oos.writeInt(DELETE_ROW);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
}
}