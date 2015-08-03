package securityconfigurator.Utils;

import securityconfigurator.SCException;


public class Generator {
    public static void AddClassBe(String jarpath, String jarfile, Class be) throws SCException, BTC_Exception {
        BusinessEntity_Context BE = new BusinessEntity_Context(be);
        GenerateBE GBE = new GenerateBE(BE, jarpath, jarfile);

    }
}
