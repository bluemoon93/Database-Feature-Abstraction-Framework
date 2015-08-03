package Security;

import LocalTools.BTC_Exception;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class ReflectionUtils {

    public static void setFieldValue(Object obj, Object value, String fieldName) {
        try {
            Class objc = obj.getClass();
            Field field = objc.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            throw new BTC_Exception(ex);
        }
    }
    
    public static Boolean getFieldBoolean(Object obj, String fieldName) {
        return getFieldBoolean(obj, 0, fieldName);
    }

    public static Boolean getFieldBoolean(Object obj, int superLevel, String fieldName) {
        try {
            Class objc = obj.getClass();
            for (int i = 0; i < superLevel; i++) {
                objc = objc.getSuperclass();
            }

            Field field = objc.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getBoolean(obj);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public static Integer getFieldInt(Object obj, String fieldName) {
        return getFieldInt(obj, 0, fieldName);
    }

    public static Integer getFieldInt(Object obj, int superLevel, String fieldName) {
        try {
            Class objc = obj.getClass();
            for (int i = 0; i < superLevel; i++) {
                objc = objc.getSuperclass();
            }

            Field field = objc.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(obj);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj, 0, fieldName);
    }

    public static Object getFieldValue(Object obj, int superLevel, String fieldName) {
        try {
            Class objc = obj.getClass();
            for (int i = 0; i < superLevel; i++) {
                objc = objc.getSuperclass();
            }

            Field field = objc.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public static Map<Object, Field> searchLike(Object obj, String fieldClassTypeLike, int recMaxLevel) throws IllegalArgumentException, IllegalAccessException {
        HashMap<Object, Field> ret = new HashMap<>();
        searchLikeRec(obj, fieldClassTypeLike, ret, 0, recMaxLevel);
        return ret;
    }

    private static void searchLikeRec(Object obj, String fieldClassTypeLike, Map<Object, Field> map, int recLevel, int recMaxLevel) throws IllegalArgumentException, IllegalAccessException {
        Class cl = obj.getClass();

        do {
            for (Field f : cl.getDeclaredFields()) {
                f.setAccessible(true);
                Object fieldObj = f.get(obj);
                
                if (f.getType().getName().contains(fieldClassTypeLike)) {
                    map.put(obj, f);
                }

                if (recLevel < recMaxLevel && fieldObj != null) {
                    searchLikeRec(fieldObj, fieldClassTypeLike, map, recLevel + 1, recMaxLevel);
                }
            }

            cl = cl.getSuperclass();
        } while (cl != null);
    }

    public static void printInfo(Object obj) throws IllegalArgumentException, IllegalAccessException {
        printInfo(obj, 0);
    }

    public static void printInfo(Object obj, int superLevel) throws IllegalArgumentException, IllegalAccessException {
        Class objc = obj.getClass();
        for (int i = 0; i < superLevel; i++) {
            objc = objc.getSuperclass();
        }

        System.out.println("\n" + objc.getName() + " FIELDS");
        for (Field f : objc.getDeclaredFields()) {
            f.setAccessible(true);
            System.out.println(String.format("%s %s : %s", f.getType().getName(), f.getName(), f.get(obj)));
        }
        
        System.out.println("\n" + objc.getName() + " CONSTRUCTORS");
        for (Constructor c : objc.getDeclaredConstructors()) {
            System.out.print(c.getName() + "(");
            boolean addSep = false;
            for(Parameter p : c.getParameters()) {
                if(addSep) System.out.print(", ");
                System.out.print(p.getType().getName() + " " + p.getName());
                addSep = true;
            }
            System.out.println(")");
        }

        System.out.println("\n" + objc.getName() + " METHODS");
        for (Method m : objc.getDeclaredMethods()) {
            System.out.print(String.format("%s %s", m.getReturnType().getName(), m.getName()) + "(");
            boolean addSep = false;
            for(Parameter p : m.getParameters()) {
                if(addSep) System.out.print(", ");
                System.out.print(p.getType().getName() + " " + p.getName());
                addSep = true;
            }
            System.out.println(")");
        }
    }
}
