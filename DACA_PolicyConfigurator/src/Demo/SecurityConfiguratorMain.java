package Demo;

import sc.IApplication;
import sc.ICrud;
import securityconfigurator.SCException;
import securityconfigurator.SecurityConfigurator;
import securityconfigurator.Utils.BTC_Exception;


public class SecurityConfiguratorMain {


    public static void main(String[] args) throws SCException, BTC_Exception {
        // Change this url, accordingly to your database
        String url = "sqlserver://192.168.57.101:1433;database=PolicyServer2;";
        SecurityConfigurator sc = new SecurityConfigurator();
        
        // Change username and password accordingly to your database
        sc.ConfigureServer("Regateiro", "123456", url);
        
        //Set the Application class where roles are defined, and the crud class where the cruds are defined
        sc.Configure(IApplication.class, ICrud.class);
        sc.ConfigureApplication(IApplication.class, ICrud.class);
    }
}
