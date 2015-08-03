package util;

/**
 * Boolean wrapper that allows setting the value of the boolean.
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class MyBoolean {
    private boolean value;
    
    public MyBoolean(boolean value) {
        this.value = value;
    }
    
    public boolean booleanValue() {
        return this.value;
    }
    
    public void setValue(boolean value) {
        this.value = value;
    }
}
