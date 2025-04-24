package hplugins.hvincular.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface que define os métodos comuns para implementações de banco de dados.
 * Implementada tanto por MySQL quanto por SQLite.
 */
public interface Database {
    
    /**
     * Estabelece a conexão com o banco de dados.
     *
     * @throws SQLException Se ocorrer um erro na conexão
     */
    void connect() throws SQLException;
    
    /**
     * Desconecta do banco de dados.
     */
    void disconnect();
    
    /**
     * Obtém a conexão com o banco de dados.
     *
     * @return Conexão com o banco de dados
     * @throws SQLException Se ocorrer um erro na conexão
     */
    Connection getConnection() throws SQLException;
    
    /**
     * Obtém o prefixo das tabelas.
     *
     * @return Prefixo das tabelas
     */
    String getTablePrefix();
    
    /**
     * Obtém o tipo de banco de dados.
     * 
     * @return Tipo de banco de dados ("mysql" ou "sqlite")
     */
    String getType();
}