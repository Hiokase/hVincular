package hplugins.hvincular.database;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.model.PlayerData;
import hplugins.hvincular.model.Tag;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Gerenciador responsável pela interação com o banco de dados.
 */
public class DatabaseManager {

    private final HVincular plugin;
    private Database database;
    private boolean enabled;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public DatabaseManager(HVincular plugin) {
        this.plugin = plugin;
        
        this.enabled = plugin.getConfigManager().isDatabaseEnabled();
        
        setupDatabase();
    }

    /**
     * Configura a conexão com o banco de dados e cria as tabelas se necessário.
     * Sempre usa SQLite por padrão para garantir persistência de dados.
     */
    private void setupDatabase() {
        String configDatabaseType = plugin.getConfigManager().getDatabaseType();
        String databaseType = enabled ? configDatabaseType : DatabaseFactory.TYPE_SQLITE;
        
        if (databaseType == null || databaseType.isEmpty() || 
            (!databaseType.equalsIgnoreCase(DatabaseFactory.TYPE_MYSQL) && 
             !databaseType.equalsIgnoreCase(DatabaseFactory.TYPE_SQLITE))) {
            databaseType = DatabaseFactory.TYPE_SQLITE;
        }
        
        this.database = DatabaseFactory.createDatabase(plugin, databaseType);
        
        try {
            database.connect();
            createTables();
            
            String dbTypeMsg = database.getType().equalsIgnoreCase(DatabaseFactory.TYPE_MYSQL) ? 
                "MySQL (conexão externa)" : "SQLite (arquivo local)";
            
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §a§lBanco de dados em uso: " + dbTypeMsg);
            
            if (!enabled) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §e§lBanco de dados não habilitado na configuração, " +
                        "usando SQLite para garantir persistência de dados.");
            }
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("Conectado ao banco de dados " + database.getType() + " com sucesso.");
            }
        } catch (SQLException e) {
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cFalha ao conectar ao banco de dados " + 
                    database.getType() + ": " + e.getMessage());
                    
            if (database.getType().equalsIgnoreCase(DatabaseFactory.TYPE_MYSQL)) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §eTentando conexão alternativa com SQLite...");
                
                this.database = DatabaseFactory.createDatabase(plugin, DatabaseFactory.TYPE_SQLITE);
                
                try {
                    database.connect();
                    createTables();
                    plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §a§lConexão com SQLite estabelecida com sucesso!");
                } catch (SQLException ex) {
                    plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §c§lFalha também na conexão com SQLite: " + ex.getMessage());
                    enabled = false;
                }
            } else {
                enabled = false;
            }
        }
    }

    /**
     * Cria as tabelas necessárias no banco de dados.
     * @throws SQLException se ocorrer um erro na execução da consulta SQL
     */
    private void createTables() throws SQLException {
        String tablePrefix = database.getTablePrefix();
        Connection connection = database.getConnection();
        String dbType = database.getType();
        
        String engineClause = dbType.equals("mysql") ? "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4" : "";
        
        String timestampClause = dbType.equals("mysql") 
                ? "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" 
                : "TIMESTAMP DEFAULT CURRENT_TIMESTAMP";
        
        String autoIncrementSyntax = dbType.equals("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT";
        
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "players ("
                + "uuid VARCHAR(36) PRIMARY KEY, "
                + "name VARCHAR(16) NOT NULL, "
                + "last_updated " + timestampClause
                + ") " + engineClause + ";";
        
        StringBuilder vinculationsBuilder = new StringBuilder();
        vinculationsBuilder.append("CREATE TABLE IF NOT EXISTS ").append(tablePrefix).append("vinculations (")
                .append("id INTEGER PRIMARY KEY ").append(autoIncrementSyntax).append(", ")
                .append("player_uuid VARCHAR(36) NOT NULL, ")
                .append("tag_id VARCHAR(64) NOT NULL, ")
                .append("video_id VARCHAR(32) NOT NULL, ")
                .append("channel_id VARCHAR(64) NOT NULL, ")
                .append("subscriber_count INT NOT NULL, ")
                .append("created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, ")
                .append("is_active BOOLEAN DEFAULT TRUE");
        
        if (dbType.equals("mysql")) {
            vinculationsBuilder.append(", ")
                .append("FOREIGN KEY (player_uuid) REFERENCES ").append(tablePrefix).append("players(uuid) ON DELETE CASCADE, ")
                .append("INDEX (player_uuid), ")
                .append("INDEX (tag_id)");
        }
        
        vinculationsBuilder.append(") ").append(engineClause).append(";");
        String createVinculationsTable = vinculationsBuilder.toString();
        
        StringBuilder cooldownsBuilder = new StringBuilder();
        cooldownsBuilder.append("CREATE TABLE IF NOT EXISTS ").append(tablePrefix).append("cooldowns (")
                .append("id INTEGER PRIMARY KEY ").append(autoIncrementSyntax).append(", ")
                .append("player_uuid VARCHAR(36) NOT NULL, ")
                .append("cooldown_key VARCHAR(64) NOT NULL, ")
                .append("expiration_time BIGINT NOT NULL, ")
                .append("created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        
        if (dbType.equals("mysql")) {
            cooldownsBuilder.append(", ")
                .append("FOREIGN KEY (player_uuid) REFERENCES ").append(tablePrefix).append("players(uuid) ON DELETE CASCADE, ")
                .append("INDEX (player_uuid), ")
                .append("INDEX (cooldown_key), ")
                .append("UNIQUE INDEX (player_uuid, cooldown_key)");
        } else {
            cooldownsBuilder.append(", ")
                .append("UNIQUE (player_uuid, cooldown_key)");
        }
        
        cooldownsBuilder.append(") ").append(engineClause).append(";");
        String createCooldownsTable = cooldownsBuilder.toString();
        
        List<String> additionalIndexes = new ArrayList<>();
        if (!dbType.equals("mysql")) {
            additionalIndexes.add("CREATE INDEX IF NOT EXISTS idx_vinculations_player_uuid ON " + 
                    tablePrefix + "vinculations(player_uuid);");
            additionalIndexes.add("CREATE INDEX IF NOT EXISTS idx_vinculations_tag_id ON " + 
                    tablePrefix + "vinculations(tag_id);");
            additionalIndexes.add("CREATE INDEX IF NOT EXISTS idx_cooldowns_player_uuid ON " + 
                    tablePrefix + "cooldowns(player_uuid);");
            additionalIndexes.add("CREATE INDEX IF NOT EXISTS idx_cooldowns_cooldown_key ON " + 
                    tablePrefix + "cooldowns(cooldown_key);");
        }
        
        List<String> foreignKeyTriggers = new ArrayList<>();
        if (!dbType.equals("mysql")) {
            foreignKeyTriggers.add(
                "CREATE TRIGGER IF NOT EXISTS fk_vinculations_player_uuid_insert " +
                "BEFORE INSERT ON " + tablePrefix + "vinculations " +
                "FOR EACH ROW BEGIN " +
                "  SELECT CASE WHEN ((SELECT uuid FROM " + tablePrefix + "players WHERE uuid = NEW.player_uuid) IS NULL) " +
                "    THEN RAISE(ABORT, 'Foreign key constraint failed: player_uuid not found in players table') " +
                "  END; " +
                "END;"
            );
            
            foreignKeyTriggers.add(
                "CREATE TRIGGER IF NOT EXISTS fk_cooldowns_player_uuid_insert " +
                "BEFORE INSERT ON " + tablePrefix + "cooldowns " +
                "FOR EACH ROW BEGIN " +
                "  SELECT CASE WHEN ((SELECT uuid FROM " + tablePrefix + "players WHERE uuid = NEW.player_uuid) IS NULL) " +
                "    THEN RAISE(ABORT, 'Foreign key constraint failed: player_uuid not found in players table') " +
                "  END; " +
                "END;"
            );
        }
        
        try {
            try (PreparedStatement playersStmt = connection.prepareStatement(createPlayersTable);
                 PreparedStatement vinculationsStmt = connection.prepareStatement(createVinculationsTable);
                 PreparedStatement cooldownsStmt = connection.prepareStatement(createCooldownsTable)) {
                
                playersStmt.executeUpdate();
                vinculationsStmt.executeUpdate();
                cooldownsStmt.executeUpdate();
            }
            
            for (String indexSql : additionalIndexes) {
                try (PreparedStatement stmt = connection.prepareStatement(indexSql)) {
                    stmt.executeUpdate();
                }
            }
            
            for (String triggerSql : foreignKeyTriggers) {
                try (PreparedStatement stmt = connection.prepareStatement(triggerSql)) {
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao criar tabelas do banco de dados: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Salva todos os dados pendentes no banco de dados.
     * Este método é útil para garantir que todos os dados estejam salvos
     * antes de recarregar o plugin.
     * 
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean saveAll() {
        if (database == null) {
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §eAtenção: Não há banco de dados disponível para salvar dados.");
            return false;
        }
        
        try {
            
            String dbType = database.getType().equalsIgnoreCase(DatabaseFactory.TYPE_MYSQL) ? 
                "MySQL" : "SQLite";
                
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §aDados salvos com sucesso no banco de dados " + dbType + ".");
            
            return true;
        } catch (Exception e) {
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao salvar dados: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Desconecta do banco de dados.
     * Sempre executará a desconexão se houver um banco de dados, independente se foi habilitado na config
     */
    public void disconnect() {
        if (database != null) {
            saveAll();
            
            String dbType = database.getType().equalsIgnoreCase(DatabaseFactory.TYPE_MYSQL) ? 
                "MySQL" : "SQLite";
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §7Fechando conexão com o banco de dados " + dbType + "...");
            
            database.disconnect();
            
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §7Conexão com o banco de dados fechada com sucesso.");
        }
    }

    /**
     * Registra ou atualiza um jogador no banco de dados.
     * @param player Jogador a ser registrado
     * @return CompletableFuture<Boolean> indicando se a operação foi bem-sucedida
     */
    public CompletableFuture<Boolean> registerPlayer(Player player) {
        if (!enabled) return CompletableFuture.completedFuture(false);
        
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = database.getTablePrefix();
            String dbType = database.getType();
            String query;
            
            if (dbType.equals("mysql")) {
                query = "INSERT INTO " + tablePrefix + "players (uuid, name) VALUES (?, ?) "
                      + "ON DUPLICATE KEY UPDATE name = ?";
            } else {
                query = "INSERT OR REPLACE INTO " + tablePrefix + "players (uuid, name) VALUES (?, ?)";
            }
                    
            try (Connection conn = database.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(query);
                
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, player.getName());
                
                if (dbType.equals("mysql")) {
                    stmt.setString(3, player.getName());
                }
                
                boolean result = stmt.executeUpdate() > 0;
                stmt.close();
                
                return result;
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao registrar jogador no banco de dados: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Registra uma nova vinculação de tag para um jogador.
     * @param player Jogador
     * @param tag Tag vinculada
     * @param videoId ID do vídeo do YouTube
     * @param channelId ID do canal do YouTube
     * @param subscriberCount Número de inscritos no canal
     * @return CompletableFuture<Boolean> indicando se a operação foi bem-sucedida
     */
    public CompletableFuture<Boolean> registerVinculation(Player player, Tag tag, String videoId, 
                                                        String channelId, int subscriberCount) {
        if (!enabled) return CompletableFuture.completedFuture(false);
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fRegistrando vinculação: " + player.getName() + " / tag: " + tag.getId());
        }
        
        return hasActiveVinculation(player, tag.getId())
            .thenCompose(hasVinculation -> {
                if (hasVinculation) {
                    return CompletableFuture.completedFuture(true); // Já possui, consideramos sucesso
                }
                
                return registerPlayer(player).thenCompose(registered -> {
                    if (!registered) {
                        plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §eFalha ao registrar jogador " + player.getName() + " antes da vinculação");
                        return CompletableFuture.completedFuture(false);
                    }
                    
                    return CompletableFuture.supplyAsync(() -> {
                        String tablePrefix = database.getTablePrefix();
                        Connection conn = null;
                        
                        try {
                            conn = database.getConnection();
                            
                            if (!deactivatePreviousVinculations(conn, player.getUniqueId(), tag.getId())) {
                                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §eFalha ao desativar vinculações anteriores para a tag: " + tag.getId());
                            }
                            
                            String query = "INSERT INTO " + tablePrefix + "vinculations "
                                    + "(player_uuid, tag_id, video_id, channel_id, subscriber_count) "
                                    + "VALUES (?, ?, ?, ?, ?)";
                            
                            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                                stmt.setString(1, player.getUniqueId().toString());
                                stmt.setString(2, tag.getId());
                                stmt.setString(3, videoId);
                                stmt.setString(4, channelId);
                                stmt.setInt(5, subscriberCount);
                                
                                int result = stmt.executeUpdate();
                                return result > 0;
                            }
                        } catch (SQLException e) {
                            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao registrar vinculação no banco de dados para " + 
                                           player.getName() + ", tag " + tag.getId() + ": " + e.getMessage());
                            return false;
                        } finally {
                            if (conn != null) {
                                try {
                                    conn.close();
                                } catch (SQLException e) {
                                    plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §eErro ao fechar conexão: " + e.getMessage());
                                }
                            }
                        }
                    });
                });
            });
    }

    /**
     * Desativa vinculações anteriores para a mesma tag.
     * @param conn Conexão com o banco de dados
     * @param playerUuid UUID do jogador
     * @param tagId ID da tag
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    private boolean deactivatePreviousVinculations(Connection conn, UUID playerUuid, String tagId) {
        String tablePrefix = database.getTablePrefix();
        String query = "UPDATE " + tablePrefix + "vinculations SET is_active = FALSE "
                + "WHERE player_uuid = ? AND tag_id = ? AND is_active = TRUE";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, tagId);
            
            int rowsUpdated = stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §eErro ao desativar vinculações anteriores: " + e.getMessage());
            return false;
        } // O statement será fechado automaticamente com o try-with-resources
    }
    
    /**
     * Desativa vinculações anteriores para a mesma tag (método para uso externo).
     * @param playerUuid UUID do jogador
     * @param tagId ID da tag
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean deactivatePreviousVinculations(UUID playerUuid, String tagId) {
        try (Connection conn = database.getConnection()) {
            return deactivatePreviousVinculations(conn, playerUuid, tagId);
        } catch (SQLException e) {
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §eErro ao obter conexão para desativar vinculações: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Desativa todas as vinculações do jogador.
     * Útil para o comando de desvinculação em massa.
     * @param playerUuid UUID do jogador
     * @return Número de vinculações desativadas
     */
    public int deactivateAllVinculations(UUID playerUuid) {
        String tablePrefix = database.getTablePrefix();
        String query = "UPDATE " + tablePrefix + "vinculations SET is_active = FALSE " +
                       "WHERE player_uuid = ? AND is_active = TRUE";
        
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, playerUuid.toString());
            int rowsUpdated = stmt.executeUpdate();
            
            if (rowsUpdated > 0 && plugin.getConfigManager().isDebugEnabled()) {
                plugin.getServer().getConsoleSender().sendMessage(
                    "§8[§6hVincular§8] §b[DEBUG] §f" + rowsUpdated + " vinculações desativadas para o jogador: " + playerUuid
                );
            }
            
            return rowsUpdated;
        } catch (SQLException e) {
            plugin.getServer().getConsoleSender().sendMessage(
                "§8[§6hVincular§8] §eErro ao desativar todas as vinculações: " + e.getMessage()
            );
            return 0;
        }
    }
    
    /**
     * Obtém uma lista de IDs de tags vinculadas a um jogador.
     * Útil para o comando de desvinculação.
     * @param playerUuid UUID do jogador
     * @return Lista de IDs de tags vinculadas
     */
    public List<String> getActiveTagIds(UUID playerUuid) {
        String tablePrefix = database.getTablePrefix();
        String query = "SELECT tag_id FROM " + tablePrefix + "vinculations " +
                       "WHERE player_uuid = ? AND is_active = TRUE";
        
        List<String> tagIds = new ArrayList<>();
        
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, playerUuid.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tagIds.add(rs.getString("tag_id"));
                }
            }
            
            return tagIds;
        } catch (SQLException e) {
            plugin.getServer().getConsoleSender().sendMessage(
                "§8[§6hVincular§8] §eErro ao obter tags vinculadas: " + e.getMessage()
            );
            return tagIds; // Retorna uma lista vazia em caso de erro
        }
    }

    /**
     * Verifica se um jogador já tem uma vinculação ativa para uma tag.
     * @param player Jogador
     * @param tagId ID da tag
     * @return CompletableFuture<Boolean> indicando se o jogador já possui a vinculação
     */
    public CompletableFuture<Boolean> hasActiveVinculation(Player player, String tagId) {
        if (!enabled) return CompletableFuture.completedFuture(false);
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fVerificando vinculação: " + player.getName() + " / tag: " + tagId);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = database.getTablePrefix();
            String query = "SELECT COUNT(*) FROM " + tablePrefix + "vinculations "
                    + "WHERE player_uuid = ? AND tag_id = ? AND is_active = TRUE";
            
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, tagId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        boolean hasVinculation = count > 0;
                        
                        if (hasVinculation && plugin.getConfigManager().isDebugEnabled()) {
                            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fJogador " + player.getName() + " já possui a tag " + tagId);
                        }
                        
                        return hasVinculation;
                    }
                }
                
                return false;
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao verificar vinculação no banco de dados: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Obtém os dados de um jogador a partir do UUID.
     * @param playerUuid UUID do jogador
     * @return CompletableFuture<PlayerData> com os dados do jogador, ou null se não encontrado
     */
    public CompletableFuture<PlayerData> getPlayerData(UUID playerUuid) {
        if (!enabled) return CompletableFuture.completedFuture(null);
        
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = database.getTablePrefix();
            String query = "SELECT name, last_updated FROM " + tablePrefix + "players WHERE uuid = ?";
                    
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerUuid.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        long lastUpdated = rs.getTimestamp("last_updated").getTime();
                        
                        return new PlayerData(playerUuid, name, lastUpdated);
                    }
                }
                
                return null;
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao obter dados do jogador do banco de dados: " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Obtém todas as vinculações ativas de um jogador.
     * @param playerUuid UUID do jogador
     * @return CompletableFuture<List<String>> com os IDs das tags vinculadas
     */
    public CompletableFuture<List<String>> getActiveVinculations(UUID playerUuid) {
        if (!enabled) return CompletableFuture.completedFuture(new ArrayList<>());
        
        return CompletableFuture.supplyAsync(() -> {
            List<String> tagIds = new ArrayList<>();
            String tablePrefix = database.getTablePrefix();
            String query = "SELECT tag_id FROM " + tablePrefix + "vinculations "
                    + "WHERE player_uuid = ? AND is_active = TRUE";
                    
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerUuid.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        tagIds.add(rs.getString("tag_id"));
                    }
                }
                
                return tagIds;
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao obter vinculações do jogador: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    /**
     * Verifica se o banco de dados está habilitado.
     * @return true se o banco de dados estiver habilitado, false caso contrário
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Define um cooldown para um jogador.
     * @param playerUuid UUID do jogador
     * @param cooldownKey Chave do cooldown (ex: "vincular", "tag:youtuber")
     * @param seconds Duração do cooldown em segundos
     * @return CompletableFuture<Boolean> indicando se a operação foi bem-sucedida
     */
    public CompletableFuture<Boolean> setCooldown(UUID playerUuid, String cooldownKey, int seconds) {
        if (!enabled) return CompletableFuture.completedFuture(false);
        
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = database.getTablePrefix();
            String query = "INSERT INTO " + tablePrefix + "cooldowns (player_uuid, cooldown_key, expiration_time) "
                    + "VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE expiration_time = ?";
            
            long expirationTime = System.currentTimeMillis() + (seconds * 1000L);
            
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerUuid.toString());
                stmt.setString(2, cooldownKey);
                stmt.setLong(3, expirationTime);
                stmt.setLong(4, expirationTime);
                
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao definir cooldown no banco de dados: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Verifica se um jogador está em cooldown.
     * @param playerUuid UUID do jogador
     * @param cooldownKey Chave do cooldown
     * @return CompletableFuture<Boolean> indicando se o jogador está em cooldown
     */
    public CompletableFuture<Boolean> isInCooldown(UUID playerUuid, String cooldownKey) {
        if (!enabled) return CompletableFuture.completedFuture(false);
        
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = database.getTablePrefix();
            String query = "SELECT expiration_time FROM " + tablePrefix + "cooldowns "
                    + "WHERE player_uuid = ? AND cooldown_key = ?";
                    
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerUuid.toString());
                stmt.setString(2, cooldownKey);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        long expirationTime = rs.getLong("expiration_time");
                        long currentTime = System.currentTimeMillis();
                        
                        if (currentTime < expirationTime) {
                            return true;
                        } else {
                            removeCooldown(playerUuid, cooldownKey);
                            return false;
                        }
                    }
                }
                
                return false;
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao verificar cooldown no banco de dados: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Obtém o tempo restante de um cooldown em segundos.
     * @param playerUuid UUID do jogador
     * @param cooldownKey Chave do cooldown
     * @return CompletableFuture<Integer> com o tempo restante em segundos, ou 0 se não estiver em cooldown
     */
    public CompletableFuture<Integer> getCooldownRemainingTime(UUID playerUuid, String cooldownKey) {
        if (!enabled) return CompletableFuture.completedFuture(0);
        
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = database.getTablePrefix();
            String query = "SELECT expiration_time FROM " + tablePrefix + "cooldowns "
                    + "WHERE player_uuid = ? AND cooldown_key = ?";
                    
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerUuid.toString());
                stmt.setString(2, cooldownKey);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        long expirationTime = rs.getLong("expiration_time");
                        long currentTime = System.currentTimeMillis();
                        
                        if (currentTime < expirationTime) {
                            return (int) Math.ceil((expirationTime - currentTime) / 1000.0);
                        } else {
                            removeCooldown(playerUuid, cooldownKey);
                        }
                    }
                }
                
                return 0;
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao obter tempo de cooldown do banco de dados: " + e.getMessage());
                return 0;
            }
        });
    }
    
    /**
     * Remove um cooldown específico de um jogador.
     * @param playerUuid UUID do jogador
     * @param cooldownKey Chave do cooldown
     * @return CompletableFuture<Boolean> indicando se a operação foi bem-sucedida
     */
    public CompletableFuture<Boolean> removeCooldown(UUID playerUuid, String cooldownKey) {
        if (!enabled) return CompletableFuture.completedFuture(false);
        
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = database.getTablePrefix();
            String query = "DELETE FROM " + tablePrefix + "cooldowns "
                    + "WHERE player_uuid = ? AND cooldown_key = ?";
                    
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerUuid.toString());
                stmt.setString(2, cooldownKey);
                
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao remover cooldown do banco de dados: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Verifica se um vídeo do YouTube já foi utilizado para obter uma tag.
     * Isto impede que jogadores usem o mesmo vídeo mais de uma vez.
     * 
     * @param videoId ID do vídeo do YouTube
     * @param playerUuid UUID do jogador atual (para permitir que o jogador use seu próprio vídeo novamente)
     * @return CompletableFuture<Boolean> que retorna true se o vídeo já foi usado por outro jogador
     */
    public CompletableFuture<Boolean> isVideoAlreadyUsed(String videoId, UUID playerUuid) {
        if (!enabled || videoId == null || videoId.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = database.getTablePrefix();
            String query = "SELECT player_uuid FROM " + tablePrefix + "vinculations " +
                    "WHERE video_id = ? AND player_uuid != ? LIMIT 1";
            
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, videoId);
                stmt.setString(2, playerUuid.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean isUsed = rs.next();
                    
                    if (isUsed && plugin.getConfigManager().isDebugEnabled()) {
                        String otherPlayerUuid = rs.getString("player_uuid");
                        plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fVídeo " + 
                                videoId + " já foi usado pelo jogador: " + otherPlayerUuid);
                    }
                    
                    return isUsed;
                }
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao verificar uso do vídeo: " + e.getMessage());
                return false; // Em caso de erro, permitimos que continue (para não prejudicar o jogador)
            }
        });
    }
    
    /**
     * Remove todos os dados de um jogador do banco de dados.
     * Isso inclui todas as vinculações e cooldowns.
     * @param playerUuid UUID do jogador
     * @return CompletableFuture<Boolean> indicando se a operação foi bem-sucedida
     */
    public CompletableFuture<Boolean> clearPlayerData(UUID playerUuid) {
        if (!enabled) return CompletableFuture.completedFuture(false);
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fIniciando limpeza de dados do jogador: " + playerUuid);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = database.getTablePrefix();
            
            String query = "DELETE FROM " + tablePrefix + "players WHERE uuid = ?";
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fQuery para limpeza de dados: " + query);
            }
            
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, playerUuid.toString());
                int result = stmt.executeUpdate();
                
                if (plugin.getConfigManager().isDebugEnabled()) {
                    plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fLimpeza de dados concluída para " + playerUuid + ", registros afetados: " + result);
                }
                return result > 0;
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao limpar dados do jogador: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Remove todos os cooldowns expirados do banco de dados.
     * @return CompletableFuture<Integer> com o número de cooldowns removidos
     */
    public CompletableFuture<Integer> cleanupExpiredCooldowns() {
        if (!enabled) return CompletableFuture.completedFuture(0);
        
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = database.getTablePrefix();
            String query = "DELETE FROM " + tablePrefix + "cooldowns "
                    + "WHERE expiration_time < ?";
                    
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setLong(1, System.currentTimeMillis());
                
                return stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao limpar cooldowns expirados: " + e.getMessage());
                return 0;
            }
        });
    }
}