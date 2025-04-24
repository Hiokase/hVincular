package hplugins.hvincular.listener;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.util.ChatInputManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener para capturar mensagens de chat e processar entradas de URL.
 */
public class ChatInputListener implements Listener {

    private final HVincular plugin;
    private final ChatInputManager chatInputManager;
    private final boolean isLegacyVersion;
    private final Pattern youtubePattern;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     * @param chatInputManager Gerenciador de entrada de chat
     */
    public ChatInputListener(HVincular plugin, ChatInputManager chatInputManager) {
        this.plugin = plugin;
        this.chatInputManager = chatInputManager;
        
        String version = plugin.getServer().getBukkitVersion();
        this.isLegacyVersion = version.contains("1.7") || version.contains("1.8") || 
                               version.contains("1.9") || version.contains("1.10") || 
                               version.contains("1.11") || version.contains("1.12");
        
        this.youtubePattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([A-Za-z0-9_-]{11}).*$");
    }

    /**
     * Processa mensagens de chat dos jogadores nas versões modernas.
     * Este evento é executado de forma assíncrona.
     * 
     * @param event Evento de chat assíncrono (1.13+)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (isLegacyVersion) {
            return; // Ignora este evento em versões antigas, usa o outro handler
        }
        
        processChat(event.getPlayer(), event.getMessage(), event);
    }
    
    /**
     * Processa mensagens de chat dos jogadores nas versões antigas.
     * Este evento é executado de forma síncrona nas versões antigas.
     * 
     * @param event Evento de chat síncrono (1.7-1.12)
     */
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChatLegacy(PlayerChatEvent event) {
        if (!isLegacyVersion) {
            return; // Ignora este evento em versões novas, usa o outro handler
        }
        
        processChat(event.getPlayer(), event.getMessage(), event);
    }
    
    /**
     * Método unificado para processar mensagens de chat, independente da versão do servidor.
     * @param player Jogador que enviou a mensagem
     * @param message Mensagem enviada
     * @param event Evento (pode ser PlayerChatEvent ou AsyncPlayerChatEvent)
     */
    private void processChat(Player player, String message, Object event) {
        Matcher matcher = youtubePattern.matcher(message);
        boolean isYouTubeLink = matcher.find();
        
        if (chatInputManager.isWaitingForInput(player)) {
            if (event instanceof AsyncPlayerChatEvent) {
                ((AsyncPlayerChatEvent) event).setCancelled(true);
            } else if (event instanceof PlayerChatEvent) {
                ((PlayerChatEvent) event).setCancelled(true);
            }
            
            if (event instanceof AsyncPlayerChatEvent) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    chatInputManager.processMessage(player, message);
                });
            } else {
                chatInputManager.processMessage(player, message);
            }
            
            return;
        }
        
        if (isYouTubeLink && plugin.getMenuManager().hasPendingVerification(player)) {
            String videoId = matcher.group(1);
            if (videoId != null && !videoId.isEmpty()) {
                String fullUrl = "https://www.youtube.com/watch?v=" + videoId;
                String tagId = plugin.getMenuManager().getPendingTagId(player);
                
                if (event instanceof AsyncPlayerChatEvent) {
                    ((AsyncPlayerChatEvent) event).setCancelled(true);
                } else if (event instanceof PlayerChatEvent) {
                    ((PlayerChatEvent) event).setCancelled(true);
                }
                
                final String finalFullUrl = fullUrl;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getMenuManager().setPendingVideoUrl(player, finalFullUrl);
                    plugin.getYouTubeVerifier().verifyVideo(player, finalFullUrl, tagId);
                });
            }
        } 
        else if (isYouTubeLink) {
            String videoId = matcher.group(1);
            if (videoId != null && !videoId.isEmpty()) {
                String fullUrl = "https://www.youtube.com/watch?v=" + videoId;
                
                String cooldownKey = "youtube_link_notify";
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (player.hasPermission("hvincular.use")) {
                        if (!plugin.getCooldownManager().isInCooldown(player, cooldownKey)) {
                            player.sendMessage(plugin.getMessagesConfig().getCommandRequired());
                            
                            plugin.getCooldownManager().setCooldown(player, cooldownKey, 5);
                        }
                    }
                });
            }
        }
    }
    
    /**
     * Cancela o processo de espera por input quando o jogador sai do servidor.
     * 
     * @param event Evento de saída do jogador
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (chatInputManager.isWaitingForInput(player)) {
            chatInputManager.cancelChatInput(player);
        }
    }
}