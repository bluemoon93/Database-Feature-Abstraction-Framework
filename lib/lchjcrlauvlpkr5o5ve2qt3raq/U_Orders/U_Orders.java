package U_Orders;
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

import S_Orders.IROrderID;


//This is a modded generator!

public class U_Orders implements IU_Orders{
public static final int NEXT=-1, ABSOLUTE=12, RELATIVE=-13, PREVIOUS=-14, 
            BEFORE_FIRST=-15, AFTER_LAST=-16, FIRST=-18, LAST=-19,
            GET=-2, SET=-3, EXEC=-17, DELETE_ROW=-20,
            BEGIN_UPDATE=-4, CANCEL_UPDATE=-5, UPDATE_ROW=-6, UPDATE_VAL=-7, 
            BEGIN_INSERT=-8, CANCEL_INSERT=-9, INSERT_ROW=-10, INSERT_VAL=-11;

public static final int COMMIT=-21, RB=-23, AUTOCOMMIT=-26,
            RELSP=-22, RBSP=-24, SP=-25, SPNAME=-27;	private String crud;
	private Integer sessionID;
	private Integer querySRID;
	private String params;
	private IBusinessSchemaController controller;
	private Integer activeSequence;
	private IBusinessManager man;
	private int queryIdentifier;
	private int nrows;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	public U_Orders(IBusinessManager man, ObjectOutputStream oos, ObjectInputStream ois, String crud, Integer sessionID, Integer querySRID, IBusinessSchemaController controller, Integer activeSequence) throws SQLException {
	if(!controller.validateExecution(activeSequence, "U_Orders.IU_Orders")) {
		throw new SequenceException("IU_Orders was used out of order!");
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
	}
	@Override
	public void execute(IROrderID args0, String args1, String args2, String args3) throws SQLException {
	if(!controller.validateExecution(activeSequence, "U_Orders.IU_Orders")) {
		throw new SequenceException("IU_Orders was used out of order!");
	}
	String execParams = this.params;
			execParams = execParams.replaceFirst("\\?",""+args0.OrderID());
			execParams = execParams.replaceFirst("\\?","\""+args1+"\"");
			execParams = execParams.replaceFirst("\\?","\""+args2+"\"");
			execParams = execParams.replaceFirst("\\?","\""+args3+"\"");
		try{
			oos.writeInt(3);
			oos.flush();
			oos.writeBoolean(false);
			oos.flush();
			oos.writeObject(this.sessionID);
			oos.flush();
			oos.writeObject(this.querySRID);
			oos.flush();
			oos.writeObject(execParams);
			oos.flush();			oos.writeInt(queryIdentifier);oos.flush();
			nrows=ois.readInt();
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
	@Override
	public int getNAffectedRows(){
	if(!controller.validateExecution(activeSequence, "U_Orders.IU_Orders")) {
		throw new SequenceException("IU_Orders was used out of order!");
	}
		return nrows;
	}
}