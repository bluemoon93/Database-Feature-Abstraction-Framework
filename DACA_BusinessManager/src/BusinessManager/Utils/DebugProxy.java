package BusinessManager.Utils;

import LocalTools.BTC_Exception;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DebugProxy implements java.lang.reflect.InvocationHandler {

    private final Object obj;

    public static Object newInstance(Object obj) {
        return java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new DebugProxy(obj));
    }

    private DebugProxy(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args)
            throws Throwable {
        if (validateSchema()) {
            //System.out.println("VALID "+validateSchema());
            Object result;
            try {
                result = m.invoke(obj, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } catch (Exception e) {
                throw new RuntimeException("unexpected invocation exception: "
                        + e.getMessage());
            }
            return result;
        } else {
            throw new BTC_Exception("ERRO Class não permitida!");
        }
    }

    public boolean validateSchema() {
        String myloc = this.obj.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        try {
            JarEntry entry;
            try (JarFile jar = new JarFile(myloc.substring(5))) {
                entry = jar.getJarEntry(this.obj.getClass().getName().replace('.', '/') + ".class");
            }
            return entry != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
