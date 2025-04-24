package hplugins.hvincular.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de cache para reduzir chamadas repetidas a APIs externas.
 * Implementa um sistema de cache em memória com expiração por tempo.
 */
public class CacheManager<K, V> {

    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    
    private final long defaultTtlSeconds;
    
    /**
     * Construtor com tempo de expiração padrão.
     * 
     * @param defaultTtlSeconds Tempo de vida padrão dos itens em cache (em segundos)
     */
    public CacheManager(long defaultTtlSeconds) {
        this.defaultTtlSeconds = defaultTtlSeconds;
    }
    
    /**
     * Construtor com tempo de expiração padrão de 5 minutos.
     */
    public CacheManager() {
        this(300); // 5 minutos como padrão
    }
    
    /**
     * Adiciona um item ao cache com o tempo de expiração padrão.
     * 
     * @param key Chave do item
     * @param value Valor do item
     */
    public void put(K key, V value) {
        put(key, value, defaultTtlSeconds);
    }
    
    /**
     * Adiciona um item ao cache com um tempo de expiração específico.
     * 
     * @param key Chave do item
     * @param value Valor do item
     * @param ttlSeconds Tempo de vida do item em segundos
     */
    public void put(K key, V value, long ttlSeconds) {
        if (key == null) return;
        
        long expirationTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        cache.put(key, new CacheEntry<>(value, expirationTime));
    }
    
    /**
     * Obtém um item do cache, se existir e não estiver expirado.
     * 
     * @param key Chave do item
     * @return O valor associado à chave, ou null se não existir ou estiver expirado
     */
    public V get(K key) {
        if (key == null) return null;
        
        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        
        return entry.getValue();
    }
    
    /**
     * Verifica se uma chave existe no cache e não está expirada.
     * 
     * @param key Chave a ser verificada
     * @return true se a chave existir e não estiver expirada, false caso contrário
     */
    public boolean contains(K key) {
        return get(key) != null;
    }
    
    /**
     * Remove um item do cache.
     * 
     * @param key Chave do item a ser removido
     */
    public void remove(K key) {
        cache.remove(key);
    }
    
    /**
     * Limpa o cache, removendo todos os itens.
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Limpa o cache removendo apenas os itens expirados.
     */
    public void cleanExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Classe interna para representar um item no cache com tempo de expiração.
     */
    private static class CacheEntry<V> {
        private final V value;
        private final long expirationTime;
        
        /**
         * Cria uma nova entrada de cache.
         * 
         * @param value Valor a ser armazenado
         * @param expirationTime Tempo de expiração em milissegundos (timestamp)
         */
        public CacheEntry(V value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
        
        /**
         * Obtém o valor armazenado.
         * 
         * @return Valor armazenado
         */
        public V getValue() {
            return value;
        }
        
        /**
         * Verifica se o item expirou.
         * 
         * @return true se o item expirou, false caso contrário
         */
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}