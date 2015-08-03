package LocalTools;

/**
 * Class that handles the filename of files
 */
public class Filename {
    private String fullPath;
    private char pathSeparator, extensionSeparator;

    /**
     * Method that sets the filename
     *
     * @param str str correspoding to the file path
     * @param sep the char that separates of the path
     * @param ext the char that separates the extension
     */
    public Filename(String str, char sep, char ext) {
        fullPath = str;
        pathSeparator = sep;
        extensionSeparator = ext;
    }

    /**
     * Method that gets the extension
     *
     * @return the extension
     */
    public String extension() {
        int dot = fullPath.lastIndexOf(extensionSeparator);
        return fullPath.substring(dot + 1);
    }

    /**
     * Method that gets the filename
     *
     * @return the filename
     */
    public String filename() { // gets filename without extension
        int dot = fullPath.lastIndexOf(extensionSeparator);
        int sep = fullPath.lastIndexOf(pathSeparator);
        return fullPath.substring(sep + 1, dot);
    }

    /**
     * Method that gets the path of the file
     *
     * @return the path of the file
     */
    public String path() {
        int sep = fullPath.lastIndexOf(pathSeparator);
        return fullPath.substring(0, sep);
    }
}
