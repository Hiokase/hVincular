package hplugins.hvincular.util;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.model.Tag;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Gerenciador de entrada de URL via chat.
 * Controla o tempo limite para inserção da URL e validação do formato.
 */
public class ChatInputManager {

    private final HVincular plugin;
    
    private final Map<UUID, PlayerInput> playerInputs = new HashMap<>();
    
    private Pattern urlPattern;
    
    private int timeout;
    private String timeoutMessage;
    private String invalidUrlMessage;
    private String promptMessage;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public ChatInputManager(HVincular plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Carrega as configurações do arquivo config.yml.
     */
    public void loadConfig() {
        timeout = plugin.getConfig().getInt("server.chat-input.timeout", 60);
        timeoutMessage = ColorUtils.translate(plugin.getConfig().getString("server.chat-input.timeout-message", "&cTempo esgotado! A ação foi cancelada."));
        invalidUrlMessage = ColorUtils.translate(plugin.getConfig().getString("server.chat-input.invalid-url-message", "&cURL inválida! A ação foi cancelada."));
        String pattern = plugin.getConfig().getString("server.chat-input.url-pattern", "^(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([A-Za-z0-9_-]{11}).*$");
        promptMessage = plugin.getConfig().getString("server.chat-input.prompt-message", "&aDigite a URL do seu vídeo do YouTube no chat. Você tem {timeout} segundos.");
        
        try {
            urlPattern = Pattern.compile(pattern);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao compilar padrão de URL: " + e.getMessage());
            urlPattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([A-Za-z0-9_-]{11}).*$");
        }
    }
    
    /**
     * Inicia o processo de espera por input do jogador via chat.
     * 
     * @param player Jogador que está realizando a verificação
     * @param tag Tag escolhida pelo jogador
     * @param callback Callback chamado quando o jogador inserir uma URL válida
     * @return true se o processo foi iniciado com sucesso, false caso já exista outro processo em andamento para o jogador
     */
    public boolean startChatInput(Player player, Tag tag, UrlInputCallback callback) {
        UUID playerId = player.getUniqueId();
        
        if (playerInputs.containsKey(playerId)) {
            return false;
        }
        
        String message = ColorUtils.translate(promptMessage.replace("{timeout}", String.valueOf(timeout)));
        player.sendMessage(message);
        
        BukkitTask timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                playerInputs.remove(playerId);
                player.sendMessage(timeoutMessage);
            }
        }.runTaskLater(plugin, timeout * 20L);
        
        playerInputs.put(playerId, new PlayerInput(tag, callback, timeoutTask));
        return true;
    }
    
    /**
     * Cancela o processo de espera por input do jogador.
     * 
     * @param player Jogador que está realizando a verificação
     */
    public void cancelChatInput(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerInput input = playerInputs.remove(playerId);
        
        if (input != null && input.timeoutTask != null) {
            input.timeoutTask.cancel();
        }
    }
    
    /**
     * Processa a mensagem de chat do jogador.
     * Este método deve ser chamado a partir de um listener de PlayerChatEvent.
     * 
     * @param player Jogador que enviou a mensagem
     * @param message Mensagem enviada
     * @return true se a mensagem foi processada como uma URL (evento deve ser cancelado), false caso contrário
     */
    public boolean processMessage(Player player, String message) {
        UUID playerId = player.getUniqueId();
        PlayerInput input = playerInputs.get(playerId);
        
        if (input == null) {
            return false;
        }
        
        input.timeoutTask.cancel();
        
        playerInputs.remove(playerId);
        
        Matcher matcher = urlPattern.matcher(message);
        if (!matcher.matches()) {
            player.sendMessage(invalidUrlMessage);
            return true;
        }
        
        input.callback.onUrlInput(player, input.tag, message);
        return true;
    }
    
    /**
     * Verifica se o jogador está com um processo de input em andamento.
     * 
     * @param player Jogador a ser verificado
     * @return true se o jogador está com um processo em andamento, false caso contrário
     */
    public boolean isWaitingForInput(Player player) {
        return playerInputs.containsKey(player.getUniqueId());
    }
    
    /**
     * Interface para o callback de entrada de URL.
     */
    public interface UrlInputCallback {
        /**
         * Método chamado quando o jogador inserir uma URL válida.
         * 
         * @param player Jogador que realizou o input
         * @param tag Tag escolhida pelo jogador
         * @param url URL inserida pelo jogador
         */
        void onUrlInput(Player player, Tag tag, String url);
    }
    
    /**
     * Classe interna para armazenar informações de entrada do jogador.
     */
    private static class PlayerInput {
        private final Tag tag;
        private final UrlInputCallback callback;
        private final BukkitTask timeoutTask;
        
        public PlayerInput(Tag tag, UrlInputCallback callback, BukkitTask timeoutTask) {
            this.tag = tag;
            this.callback = callback;
            this.timeoutTask = timeoutTask;
        }
    }
}