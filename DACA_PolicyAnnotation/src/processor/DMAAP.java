package processor;

import LocalTools.BTC_Exception;
import annotation.DACAManagedApplication;
import interfacegen.InterfaceGenerator;
import policyextractor.PolicyExtractor;
import util.MyBoolean;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import util.BuildPath;

/**
 * DMAAP - DACA Managed Application Annotation Processor
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
@SupportedAnnotationTypes("annotation.DACAManagedApplication")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DMAAP extends AbstractProcessor {

    /**
     * public for ServiceLoader
     */
    public DMAAP() {
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        boolean processed = false;

        //For each element annotated with the Handleable annotation
        for (Element e : roundEnv.getElementsAnnotatedWith(DACAManagedApplication.class)) {

            //Check if the type of the annotated element is not a field. If yes, return a warning.
            if (e.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Not a class", e);
                continue;
            }

            if (processed) {
                return true;
            }

            //Generate a source file with a specified class name.
            try {
                MyBoolean seqActive = new MyBoolean(true);
                Map<Integer, Map<Integer, String>> orqinfo = new HashMap<>();

                DACAManagedApplication ann = e.getAnnotation(DACAManagedApplication.class);

                List<Class> interfaces = getInterfaces(ann, orqinfo, seqActive);

                for (Class i : interfaces) {
                    JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                            i.getPackage().getName() + "." + i.getSimpleName());

                    if (ann.includePolicies()) {
                        //Add the content to the newly generated file.
                        try (PrintWriter pw = new PrintWriter(jfo.openWriter())) {
                            pw.println(InterfaceGenerator.genInterfaceSourceCode(
                                    i, interfaces, orqinfo, seqActive.booleanValue()));
                            pw.flush();
                        }
                    }
                }
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, x.toString());
                return false;
            }

            processed = true;
        }
        return true;
    }

    /**
     * Obtém a lista das classes que formam o política de acesso da aplicação.
     *
     * @param ann A anotação usada pela aplicação.
     * @return A lista das classes que formam o política de acesso da aplicação.
     */
    private List<Class> getInterfaces(DACAManagedApplication ann, Map<Integer, Map<Integer, String>> orqinfo, MyBoolean orqActive) {
        PolicyExtractor pe = new PolicyExtractor();
        List<Class> classes = new ArrayList<>();
        String jarname = "policies.jar";

        try {
            // Get the jar with the policies from the database
            File fjar = new File(BuildPath.DIR, jarname);
            boolean updateJar = !fjar.exists() || ann.updatePolicies();

            if (!pe.ObtainInfo(ann.username(), ann.password(), ann.url(),
                    ann.port(), ann.app(), jarname, ann.authenticationMethod(),
                    ann.keyStorePath(), ann.keyStorePassword(), updateJar,
                    orqinfo, orqActive)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Policies could not be obtained.");
            }

            // Load the classes within the jar
            JarFile jar = new JarFile(new File(BuildPath.DIR, jarname));
            Enumeration<JarEntry> entries = jar.entries();

//            URL[] urls = {new URL("jar:file:" + jarname + "!/")};
            URL[] urls = {fjar.toURI().toURL()};
            try (URLClassLoader cl = URLClassLoader.newInstance(urls)) {
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                        continue;
                    }

                    // -6 because of .class
                    String className = entry.getName().substring(0, entry.getName().length() - 6);
                    className = className.replace('/', '.');
                    classes.add(cl.loadClass(className));
                }
            }
        } catch (BTC_Exception | IOException | ClassNotFoundException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.toString());
            classes.clear();
        }

        return classes;
    }

    /**
     * Apaga um ficheiro ou uma pasta recursivamente.
     *
     * @param f O ficheiro a ser apagado.
     * @return True se tudo correr bem, false caso contrário.
     */
    private boolean recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                recursiveDelete(c);
            }
        }
        return f.delete();
    }
}
