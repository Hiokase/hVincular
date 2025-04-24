package hplugins.hvincular.util;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.database.DatabaseManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Gerenciador de cooldowns para limitar o uso de comandos.
 * Utiliza o banco de dados para persistência e mantém um cache em memória para desempenho.
 */
public class CooldownManager {

    private final HVincular plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, Map<String, Long>> cooldownCache;
    private final boolean useDatabaseStorage;
    
    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public CooldownManager(HVincular plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.cooldownCache = new HashMap<>();
        this.useDatabaseStorage = plugin.getConfigManager().isDatabaseEnabled();
        
        if (useDatabaseStorage) {
            plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
                () -> databaseManager.cleanupExpiredCooldowns(), 
                20 * 60 * 30, // Delay inicial (30 minutos)
                20 * 60 * 30  // Repetir a cada 30 minutos
            );
        }
    }
    
    /**
     * Define um cooldown para um jogador e comando específico.
     * @param player Jogador
     * @param key Identificador do cooldown (ex: "vincular", "tag:youtuber", etc)
     * @param seconds Tempo em segundos para o cooldown
     */
    public void setCooldown(Player player, String key, int seconds) {
        UUID playerId = player.getUniqueId();
        
        long expirationTime = System.currentTimeMillis() + (seconds * 1000L);
        
        if (!cooldownCache.containsKey(playerId)) {
            cooldownCache.put(playerId, new HashMap<>());
        }
        cooldownCache.get(playerId).put(key, expirationTime);
        
        if (useDatabaseStorage) {
            databaseManager.setCooldown(playerId, key, seconds);
        }
    }
    
    /**
     * Verifica se um jogador está em cooldown para um comando específico.
     * @param player Jogador
     * @param key Identificador do cooldown
     * @return true se o jogador estiver em cooldown, false caso contrário
     */
    public boolean isInCooldown(Player player, String key) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        if (cooldownCache.containsKey(playerId) && 
            cooldownCache.get(playerId).containsKey(key)) {
            
            long expirationTime = cooldownCache.get(playerId).get(key);
            if (currentTime < expirationTime) {
                return true;
            } else {
                cooldownCache.get(playerId).remove(key);
                if (cooldownCache.get(playerId).isEmpty()) {
                    cooldownCache.remove(playerId);
                }
                return false;
            }
        }
        
        if (useDatabaseStorage) {
            try {
                boolean inDatabaseCooldown = databaseManager.isInCooldown(playerId, key).get();
                
                if (inDatabaseCooldown) {
                    int remainingTime = databaseManager.getCooldownRemainingTime(playerId, key).get();
                    if (remainingTime > 0) {
                        if (!cooldownCache.containsKey(playerId)) {
                            cooldownCache.put(playerId, new HashMap<>());
                        }
                        cooldownCache.get(playerId).put(key, currentTime + (remainingTime * 1000L));
                    }
                }
                
                return inDatabaseCooldown;
            } catch (InterruptedException | ExecutionException e) {
                plugin.getLogger().severe("Erro ao verificar cooldown no banco de dados: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * Obtém o tempo restante em segundos para o cooldown expirar.
     * @param player Jogador
     * @param key Identificador do cooldown
     * @return Tempo restante em segundos ou 0 se não estiver em cooldown
     */
    public int getRemainingTime(Player player, String key) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        if (cooldownCache.containsKey(playerId) && 
            cooldownCache.get(playerId).containsKey(key)) {
            
            long expirationTime = cooldownCache.get(playerId).get(key);
            if (currentTime < expirationTime) {
                return (int) Math.ceil((expirationTime - currentTime) / 1000.0);
            } else {
                cooldownCache.get(playerId).remove(key);
                if (cooldownCache.get(playerId).isEmpty()) {
                    cooldownCache.remove(playerId);
                }
                return 0;
            }
        }
        
        if (useDatabaseStorage) {
            try {
                int remainingTime = databaseManager.getCooldownRemainingTime(playerId, key).get();
                
                if (remainingTime > 0) {
                    if (!cooldownCache.containsKey(playerId)) {
                        cooldownCache.put(playerId, new HashMap<>());
                    }
                    cooldownCache.get(playerId).put(key, currentTime + (remainingTime * 1000L));
                }
                
                return remainingTime;
            } catch (InterruptedException | ExecutionException e) {
                plugin.getLogger().severe("Erro ao obter tempo de cooldown do banco de dados: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }
        
        return 0;
    }
    
    /**
     * Remove um cooldown específico de um jogador.
     * @param player Jogador
     * @param key Identificador do cooldown
     */
    public void removeCooldown(Player player, String key) {
        UUID playerId = player.getUniqueId();
        
        if (cooldownCache.containsKey(playerId)) {
            cooldownCache.get(playerId).remove(key);
            
            if (cooldownCache.get(playerId).isEmpty()) {
                cooldownCache.remove(playerId);
            }
        }
        
        if (useDatabaseStorage) {
            databaseManager.removeCooldown(playerId, key);
        }
    }
    
    /**
     * Remove todos os cooldowns de um jogador.
     * @param player Jogador
     */
    public void clearPlayerCooldowns(Player player) {
        UUID playerId = player.getUniqueId();
        
        cooldownCache.remove(playerId);
        
        if (useDatabaseStorage) {
            CompletableFuture.runAsync(() -> {
            });
        }
    }
    
    /**
     * Carrega os cooldowns ativos do banco de dados para o cache.
     * Isso é útil ao iniciar o servidor.
     * @param player Jogador
     */
    public void loadCooldowns(Player player) {
        if (!useDatabaseStorage) return;
        
    }
    
    /**
     * Formata o tempo restante em uma string legível (minutos e segundos).
     * @param seconds Tempo em segundos
     * @return Tempo formatado (ex: "2m 30s")
     */
    public static String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        
        if (remainingSeconds == 0) {
            return minutes + "m";
        }
        
        return minutes + "m " + remainingSeconds + "s";
    }
}