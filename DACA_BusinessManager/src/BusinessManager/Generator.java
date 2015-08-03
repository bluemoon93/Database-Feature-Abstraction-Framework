package BusinessManager;

import BusinessManager.Utils.GenerateBE;
import BusinessManager.Utils.HiveGenerateBE;
import BusinessManager.Utils.ClientSideGenerator;
import LocalTools.BTC_Exception;
import LocalTools.BusinessEntity_Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Generator {

    public static List<GenerateBE> BEGenerators = new ArrayList<>();

    // adds a new BE to the list to be generated and compiled
    public static void AddClassBe(String jarpath, String jarfile, Class be, Map<Integer, Map<Integer, String>> orqinfo) throws BTC_Exception {
        BusinessEntity_Context BE = new BusinessEntity_Context(be);
        BEGenerators.add(new ClientSideGenerator(BE, jarpath, jarfile, orqinfo)); // TO DO trocar para hive
        //BEGenerators.add(new HiveGenerateBE(BE, jarpath, jarfile, orqinfo));
    }

    /**
     * Compiles the classes added using AddClassBe method.
     */
    public static void compileGeneratedBEs() {
        BEGenerators.stream().forEach((begen) -> {
            begen.generate();
        });

        BEGenerators.stream().forEach((begen) -> {
            System.out.println("----------- a compiler");
            begen.compile();
        });

        BEGenerators.clear();
    }
}
