package BusinessInterfaces;

import java.sql.SQLException;

/**
 * Interface com o método de movimentação de cursor em tabela para resultset do tipo ForwardOnly
 */
public interface IForwardOnly {

    /**
     * Método para movimentação do cursor para a próxima entrada
     *
     * @return Boolean - True se comando executado com exito
     *
     */
    boolean moveNext();
}
