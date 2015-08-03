package LocalTools;

/**
 * Class that handles operations of packagenames
 */
public class PackageNameUtils {

    public static String getPackage(Class itf) {
        return itf.getName().substring(0, itf.getName().lastIndexOf("."));
    }

    public static String getPackageName(String Packagestr) {
        String[] tmpstr = Packagestr.split("\\.");
        if (tmpstr.length > 1) {
            return tmpstr[1];
        } else {
            return tmpstr[0];
        }
    }

    /**
     * Method that gets the package as dir
     *
     * @param Packagestr the package
     * @return package as dir
     * @throws BTC_Exception
     */
    public static String getPackageNameDir(String Packagestr) throws BTC_Exception {
        String path = "";
        String[] tmpstr = Packagestr.split("\\.");
        if (tmpstr.length == 0)
            throw new BTC_Exception("Error Entity must be in a package.");

        if (tmpstr.length > 0) {
            for (int i = 0; i < tmpstr.length; i++) {
                path += tmpstr[i] + "/";
            }
            return path;
        } else
            return tmpstr[0] + "/";

    }
}
