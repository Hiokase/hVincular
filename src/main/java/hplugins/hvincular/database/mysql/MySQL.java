package hplugins.hvincular.database.mysql;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.database.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe responsável por gerenciar a conexão com o banco de dados MySQL.
 */
public class MySQL implements Database {

    private final HVincular plugin;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;
    private Connection connection;

    /**
     * Construtor da classe MySQL.
     *
     * @param plugin Instância principal do plugin
     * @param host Host do servidor MySQL
     * @param port Porta do servidor MySQL
     * @param database Nome do banco de dados
     * @param username Nome de usuário
     * @param password Senha
     * @param tablePrefix Prefixo para as tabelas
     */
    public MySQL(HVincular plugin, String host, int port, String database, String username, String password, String tablePrefix) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
    }

    /**
     * Estabelece a conexão com o banco de dados.
     *
     * @throws SQLException Se ocorrer um erro na conexão
     */
    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().severe("Driver JDBC do MySQL não encontrado!");
                throw new SQLException("Driver JDBC do MySQL não encontrado: " + e.getMessage());
            }

            String connectionURL = "jdbc:mysql://" + host + ":" + port + "/" + database;
            connectionURL += "?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=utf8";
            
            connection = DriverManager.getConnection(connectionURL, username, password);
        }
    }

    /**
     * Desconecta do banco de dados.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Erro ao fechar conexao com o banco de dados: " + e.getMessage());
        }
    }

    /**
     * Obtém a conexão com o banco de dados, reconectando se necessário.
     *
     * @return Conexão com o banco de dados
     * @throws SQLException Se ocorrer um erro na conexão
     */
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
    public String getTablePrefix() {
        return tablePrefix;
    }
    
    /**
     * Obtém o tipo de banco de dados.
     * 
     * @return "mysql" como tipo de banco de dados
     */
    @Override
    public String getType() {
        return "mysql";
    }
}