package hplugins.hvincular.model;

import java.util.UUID;

/**
 * Classe que representa os dados de um jogador armazenados no banco de dados.
 */
public class PlayerData {

    private final UUID uuid;
    private final String name;
    private final long lastUpdated;

    /**
     * Construtor para a classe PlayerData.
     * 
     * @param uuid UUID do jogador
     * @param name Nome do jogador
     * @param lastUpdated Timestamp da última atualização
     */
    public PlayerData(UUID uuid, String name, long lastUpdated) {
        this.uuid = uuid;
        this.name = name;
        this.lastUpdated = lastUpdated;
    }

    /**
     * Obtém o UUID do jogador.
     * 
     * @return UUID do jogador
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Obtém o nome do jogador.
     * 
     * @return Nome do jogador
     */
    public String getName() {
        return name;
    }

    /**
     * Obtém o timestamp da última atualização.
     * 
     * @return Timestamp da última atualização
     */
    public long getLastUpdated() {
        return lastUpdated;
    }
}