package S_Customers;
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



//This is a modded generator!

public class S_Customers implements IS_Customers{
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

	public S_Customers(IBusinessManager man, ObjectOutputStream oos, ObjectInputStream ois, String crud, Integer sessionID, Integer querySRID, IBusinessSchemaController controller, Integer activeSequence) throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	public void execute() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	public S_Orders.IS_Orders nextBE_S1(int crud, ISession session) throws BusinessManager.BTC_Exception {
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		return man.instantiateBS(S_Orders.IS_Orders.class, crud, session, activeSequence);
	}
	public I_Orders.II_Orders nextBE_S2(int crud, ISession session) throws BusinessManager.BTC_Exception {
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		return man.instantiateBS(I_Orders.II_Orders.class, crud, session, activeSequence);
	}
	@Override
	public boolean moveNext() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	public String Country() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("Country");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String Address() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("Address");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String City() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("City");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String Region() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("Region");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String Phone() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("Phone");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String Fax() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("Fax");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String CompanyName() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("CompanyName");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String ContactName() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("ContactName");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String ContactTitle() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("ContactTitle");
			oos.flush();
			oos.writeUTF("String");
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			return (String) ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
		return null;
	}
	@Override
	public String PostalCode() throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(GET);
			oos.flush();
			oos.writeUTF("PostalCode");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
public void beginUpdate() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
public void uPostalCode(String PostalCode) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("PostalCode");
			oos.flush();
			oos.writeObject(PostalCode);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uCountry(String Country) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("Country");
			oos.flush();
			oos.writeObject(Country);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uPhone(String Phone) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("Phone");
			oos.flush();
			oos.writeObject(Phone);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uFax(String Fax) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("Fax");
			oos.flush();
			oos.writeObject(Fax);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uCompanyName(String CompanyName) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("CompanyName");
			oos.flush();
			oos.writeObject(CompanyName);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uContactName(String ContactName) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("ContactName");
			oos.flush();
			oos.writeObject(ContactName);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uContactTitle(String ContactTitle) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("ContactTitle");
			oos.flush();
			oos.writeObject(ContactTitle);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uAddress(String Address) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("Address");
			oos.flush();
			oos.writeObject(Address);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uCity(String City) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("City");
			oos.flush();
			oos.writeObject(City);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void uRegion(String Region) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=updating)
		throw new SQLException("Cant Update, While Inserting, or not BeginUpdating!");
	else{
		try{
			oos.writeInt(UPDATE_VAL);
			oos.flush();
			oos.writeUTF("Region");
			oos.flush();
			oos.writeObject(Region);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void beginInsert() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
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
public void iCompanyName(String CompanyName) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("CompanyName");
			oos.flush();
			oos.writeObject(CompanyName);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iContactName(String ContactName) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("ContactName");
			oos.flush();
			oos.writeObject(ContactName);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iContactTitle(String ContactTitle) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("ContactTitle");
			oos.flush();
			oos.writeObject(ContactTitle);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iAddress(String Address) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("Address");
			oos.flush();
			oos.writeObject(Address);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iCity(String City) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("City");
			oos.flush();
			oos.writeObject(City);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iRegion(String Region) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("Region");
			oos.flush();
			oos.writeObject(Region);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iPostalCode(String PostalCode) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("PostalCode");
			oos.flush();
			oos.writeObject(PostalCode);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iCountry(String Country) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("Country");
			oos.flush();
			oos.writeObject(Country);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iPhone(String Phone) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("Phone");
			oos.flush();
			oos.writeObject(Phone);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void iFax(String Fax) throws SQLException{
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
	if(_state!=inserting)
		throw new SQLException("Cant insert, While updating or not BeginInsert!");
	else{
		try{
			oos.writeInt(INSERT_VAL);
			oos.flush();
			oos.writeUTF("Fax");
			oos.flush();
			oos.writeObject(Fax);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
	}
}
@Override
public void deleteRow() throws SQLException {
	if(!controller.validateExecution(activeSequence, "S_Customers.IS_Customers")) {
		throw new SequenceException("IS_Customers was used out of order!");
	}
		try{
			oos.writeInt(DELETE_ROW);
			oos.flush();
			oos.writeInt(queryIdentifier);oos.flush();
			ois.readObject();
		}catch(Exception ex){ ex.printStackTrace(); }
}
}