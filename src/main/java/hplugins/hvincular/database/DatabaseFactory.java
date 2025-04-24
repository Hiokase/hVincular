package hplugins.hvincular.database;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.database.mysql.MySQL;
import hplugins.hvincular.database.sqlite.SQLite;

/**
 * Fábrica para criar instâncias de banco de dados de acordo com a configuração.
 */
public class DatabaseFactory {
    
    /**
     * Tipo de banco de dados MySQL
     */
    public static final String TYPE_MYSQL = "mysql";
    
    /**
     * Tipo de banco de dados SQLite
     */
    public static final String TYPE_SQLITE = "sqlite";
    
    /**
     * Cria uma instância de banco de dados de acordo com o tipo especificado.
     * 
     * @param plugin Instância do plugin
     * @param type Tipo de banco de dados ("mysql" ou "sqlite")
     * @return Uma instância de Database configurada
     */
    public static Database createDatabase(HVincular plugin, String type) {
        if (type.equalsIgnoreCase(TYPE_MYSQL)) {
            String host = plugin.getConfigManager().getDatabaseHost();
            int port = plugin.getConfigManager().getDatabasePort();
            String database = plugin.getConfigManager().getDatabaseName();
            String username = plugin.getConfigManager().getDatabaseUsername();
            String password = plugin.getConfigManager().getDatabasePassword();
            String tablePrefix = plugin.getConfigManager().getDatabaseTablePrefix();
            
            return new MySQL(plugin, host, port, database, username, password, tablePrefix);
        } else if (type.equalsIgnoreCase(TYPE_SQLITE)) {
            String databaseFile = plugin.getConfigManager().getDatabaseName();
            String tablePrefix = plugin.getConfigManager().getDatabaseTablePrefix();
            
            return new SQLite(plugin, databaseFile, tablePrefix);
        } else {
            plugin.getLogger().warning("Tipo de banco de dados desconhecido: " + type + ". Usando SQLite como padrão.");
            
            String databaseFile = "hvincular";
            String tablePrefix = plugin.getConfigManager().getDatabaseTablePrefix();
            
            return new SQLite(plugin, databaseFile, tablePrefix);
        }
    }
}