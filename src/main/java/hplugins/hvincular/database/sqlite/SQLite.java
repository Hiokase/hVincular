package hplugins.hvincular.database.sqlite;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.database.Database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe responsável por gerenciar a conexão com o banco de dados SQLite.
 */
public class SQLite implements Database {

    private final HVincular plugin;
    private final String databaseFile;
    private final String tablePrefix;
    private Connection connection;

    /**
     * Construtor da classe SQLite.
     *
     * @param plugin Instância principal do plugin
     * @param databaseFile Nome do arquivo do banco de dados (sem a extensão)
     * @param tablePrefix Prefixo para as tabelas
     */
    public SQLite(HVincular plugin, String databaseFile, String tablePrefix) {
        this.plugin = plugin;
        this.databaseFile = databaseFile;
        this.tablePrefix = tablePrefix;
    }

    /**
     * Estabelece a conexão com o banco de dados.
     *
     * @throws SQLException Se ocorrer um erro na conexão
     */
    @Override
    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().severe("Driver JDBC do SQLite nao encontrado!");
                throw new SQLException("Driver JDBC do SQLite nao encontrado: " + e.getMessage());
            }

            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File dbFile = new File(dataFolder, databaseFile + ".db");
            String connectionURL = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            
            connection = DriverManager.getConnection(connectionURL);
            
            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
    }

    /**
     * Desconecta do banco de dados.
     */
    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Erro ao fechar conexao com o banco de dados SQLite: " + e.getMessage());
        }
    }

    /**
     * Obtém a conexão com o banco de dados, reconectando se necessário.
     *
     * @return Conexão com o banco de dados
     * @throws SQLException Se ocorrer um erro na conexão
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    /**
     * Obtém o prefixo das tabelas.
     *
     * @return Prefixo das tabelas
     */
    @Override
    public String getTablePrefix() {
        return tablePrefix;
    }
    
    /**
     * Obtém o tipo de banco de dados.
     * 
     * @return "sqlite" como tipo de banco de dados
     */
    @Override
    public String getType() {
        return "sqlite";
    }
}