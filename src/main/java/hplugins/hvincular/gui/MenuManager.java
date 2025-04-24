package hplugins.hvincular.gui;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.database.DatabaseManager;
import hplugins.hvincular.model.Tag;
import hplugins.hvincular.util.SoundUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Gerenciador responsável pelos menus do plugin.
 */
public class MenuManager {

    private final HVincular plugin;
    
    private final Map<UUID, String> pendingTagIds = new HashMap<>();
    
    private final Map<UUID, String> pendingVideoUrls = new HashMap<>();

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public MenuManager(HVincular plugin) {
        this.plugin = plugin;
    }

    /**
     * Abre o menu de seleção de tags para um jogador.
     * @param player Jogador para abrir o menu
     */
    public void openTagSelectionMenu(Player player) {
        if (plugin.getDatabaseManager().isEnabled()) {
            CompletableFuture<Boolean> tagCheckFuture = checkExistingTags(player);
            tagCheckFuture.thenAccept(hasActiveTags -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    SoundUtil.playSound(player, SoundUtil.SoundType.MENU_OPEN);
                    
                    TagSelectionMenu menu = new TagSelectionMenu(plugin);
                    menu.open(player);
                });
            });
        } else {
            SoundUtil.playSound(player, SoundUtil.SoundType.MENU_OPEN);
            
            TagSelectionMenu menu = new TagSelectionMenu(plugin);
            menu.open(player);
        }
    }
    
    /**
     * Verifica vinculações existentes no banco de dados para o jogador.
     * @param player Jogador a ser verificado
     * @return CompletableFuture<Boolean> indicando se o jogador tem tags ativas
     */
    private CompletableFuture<Boolean> checkExistingTags(Player player) {
        DatabaseManager dbManager = plugin.getDatabaseManager();
        
        return dbManager.registerPlayer(player).thenCompose(registered -> {
            return dbManager.getActiveVinculations(player.getUniqueId());
        }).thenApply(tagIds -> {
            if (!tagIds.isEmpty()) {
                for (String tagId : tagIds) {
                    Tag tag = plugin.getMenusConfig().getTag(tagId);
                    if (tag != null) {
                        plugin.getPermissionManager().addPermission(player, tag.getPermission());
                    }
                }
                return true;
            }
            return false;
        }).exceptionally(ex -> {
            plugin.getLogger().warning("Erro ao verificar vinculacoes do jogador: " + ex.getMessage());
            return false;
        });
    }
    
    /**
     * Registra que um jogador está aguardando a entrada de um link do YouTube.
     * @param player Jogador
     * @param tagId ID da tag selecionada
     */
    public void setPendingVerification(Player player, String tagId) {
        pendingTagIds.put(player.getUniqueId(), tagId);
    }
    
    /**
     * Salva a URL do vídeo enviada pelo jogador.
     * @param player Jogador
     * @param videoUrl URL do vídeo
     */
    public void setPendingVideoUrl(Player player, String videoUrl) {
        pendingVideoUrls.put(player.getUniqueId(), videoUrl);
    }
    
    /**
     * Verifica se um jogador está aguardando a entrada de um link do YouTube.
     * @param player Jogador
     * @return true se estiver aguardando, false caso contrário
     */
    public boolean hasPendingVerification(Player player) {
        return pendingTagIds.containsKey(player.getUniqueId());
    }
    
    /**
     * Obtém o ID da tag que o jogador está tentando vincular.
     * @param player Jogador
     * @return ID da tag ou null se não estiver aguardando
     */
    public String getPendingTagId(Player player) {
        return pendingTagIds.get(player.getUniqueId());
    }
    
    /**
     * Obtém a URL do vídeo que o jogador enviou.
     * @param player Jogador
     * @return URL do vídeo ou null se não houver
     */
    public String getPendingVerification(Player player) {
        return pendingVideoUrls.get(player.getUniqueId());
    }
    
    /**
     * Remove o jogador da lista de pendentes após o processamento.
     * @param player Jogador
     */
    public void removePendingVerification(Player player) {
        pendingTagIds.remove(player.getUniqueId());
        pendingVideoUrls.remove(player.getUniqueId());
    }
}
