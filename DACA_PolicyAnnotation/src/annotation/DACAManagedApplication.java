package annotation;

import Security.AuthenticationMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classe que declara a anotação DACAManagedApplication
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value = {ElementType.TYPE})
public @interface DACAManagedApplication {
    /**
     * Username to use to access the Policy database.
     */
    String username();
    /**
     * Password de acesso ao servidor de políticas.
     */
    String password();
    /**
     * Localização do policy manager.
     */
    String url() default "localhost";
    /**
     * Porta do policy manager.
     */
    int port();
    /**
     * O nome da aplicação cujas políticas devem ser obtidas.
     */
    String app();
    /**
     * O método de autenticação a utilizar.
     */
    AuthenticationMethod authenticationMethod() default AuthenticationMethod.PSKSSL;
    /**
     * O caminho para a keystore com o certificado de chave pública do servidor. Obrigatório apenas se o método de autenticação for SSL.
     */
    String keyStorePath() default "";
    /**
     * A password da keystore com o certificado de chave pública do servidor. Obrigatório apenas se o método de autenticação for SSL.
     */
    String keyStorePassword() default "";
    /**
     * Não colocar este atributo a 'true' com o jar obtido aberto (pelo IDE ou afins).
     * Irá impedir o processo de compilação de terminar.
     */
    boolean updatePolicies() default false;
    /**
     * Não colocar este atributo a 'true' (o valor por omissão) com o jar obtido no classpath.
     * Poderá causar colisão de nomes de classes e impedirá a atualização automática.
     */
    boolean includePolicies() default true;
}
