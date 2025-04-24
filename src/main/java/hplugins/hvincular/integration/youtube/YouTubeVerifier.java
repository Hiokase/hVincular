package hplugins.hvincular.integration.youtube;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hplugins.hvincular.HVincular;
import hplugins.hvincular.database.DatabaseManager;
import hplugins.hvincular.integration.discord.DiscordWebhook;
import hplugins.hvincular.model.Tag;
import hplugins.hvincular.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Realiza a verificação de requisitos do YouTube para vinculação de tags.
 */
public class YouTubeVerifier {

    private final HVincular plugin;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public YouTubeVerifier(HVincular plugin) {
        this.plugin = plugin;
    }

    /**
     * Inicia o processo de verificação do vídeo do YouTube.
     * @param player Jogador que solicitou a verificação
     * @param videoUrl URL do vídeo
     * @param tagId ID da tag selecionada
     */
    public void verifyVideo(Player player, String videoUrl, String tagId) {
        Tag tag = plugin.getMenusConfig().getTag(tagId);
        if (tag == null) {
            player.sendMessage(plugin.getMessagesConfig().getVerificationFailed());
            return;
        }
        
        /* IMPORTANTE: Removemos a verificação de permissão aqui, pois estava causando
           problemas com detecção incorreta. Agora confiamos apenas no banco de dados
           para verificar se o jogador já possui a tag. */
        
        String videoId = plugin.getYouTubeAPI().extractVideoId(videoUrl);
        if (videoId == null) {
            player.sendMessage(plugin.getMessagesConfig().getInvalidLink());
            return;
        }
        
        
        
        continueVideoVerification(player, videoUrl, videoId, tagId);
    }
    
    /**
     * Realiza a verificação do vídeo do YouTube. Este método inicia o processo
     * principal de validação, verificando o conteúdo do vídeo, informações do canal,
     * requisitos de inscritos e visualizações.
     * 
     * @param player Jogador que solicitou a verificação
     * @param videoUrl URL do vídeo
     * @param videoId ID do vídeo extraído
     * @param tagId ID da tag selecionada
     */
    private void continueVideoVerification(Player player, String videoUrl, String videoId, String tagId) {
        player.sendMessage(plugin.getMessagesConfig().getVerificationStarted());
        
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    CompletableFuture<JsonObject> videoInfoFuture = plugin.getYouTubeAPI().getVideoInfo(videoId);
                    JsonObject videoInfo = videoInfoFuture.join();
                    
                    Tag tag = plugin.getMenusConfig().getTag(tagId);
                    if (tag == null) {
                        player.sendMessage(plugin.getMessagesConfig().getVerificationFailed());
                        plugin.getMenuManager().removePendingVerification(player);
                        return;
                    }
                    
                    processVideoInfo(player, videoInfo, tag);
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §c§lErro durante a verificação do vídeo: " + e.getMessage());
                    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(plugin.getMessagesConfig().getVerificationFailed());
                            plugin.getMenuManager().removePendingVerification(player);
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    
    /**
     * Processa as informações do vídeo obtidas da API do YouTube.
     * @param player Jogador
     * @param videoInfo Informações do vídeo
     * @param tag Tag selecionada
     */
    private void processVideoInfo(Player player, JsonObject videoInfo, Tag tag) {
        JsonArray items = videoInfo.getAsJsonArray("items");
        if (items == null || items.size() == 0) {
            sendFailureMessage(player, plugin.getMessagesConfig().getVerificationFailed());
            return;
        }
        
        JsonObject videoItem = items.get(0).getAsJsonObject();
        JsonObject snippet = videoItem.getAsJsonObject("snippet");
        
        String channelId = snippet.get("channelId").getAsString();
        String description = snippet.get("description").getAsString();
        
        boolean hasServerIp = plugin.getYouTubeAPI().containsServerIp(
                description, 
                plugin.getConfigManager().getServerIps()
        );
        
        if (!hasServerIp) {
            Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §eIP do servidor não encontrado na descrição do vídeo!");
            sendFailureMessage(player, plugin.getMessagesConfig().getMissingServerIp());
            return;
        }
        
        if (plugin.getConfigManager().isVerificationCodeEnabled()) {
            String verificationCode = plugin.getConfigManager().generateVerificationCode(player.getName());
            boolean hasVerificationCode = plugin.getYouTubeAPI().containsVerificationCode(
                    description,
                    verificationCode
            );
            
            if (!hasVerificationCode) {
                Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §eCódigo de verificação não encontrado na descrição do vídeo!");
                sendFailureMessage(player, plugin.getMessagesConfig().getMissingVerificationCode(verificationCode));
                return;
            }
        }
        
        if (tag.getMinViews() > 0) {
            try {
                JsonObject statistics = videoItem.getAsJsonObject("statistics");
                int viewCount = statistics.get("viewCount").getAsInt();
                
                if (viewCount < tag.getMinViews()) {
                    sendFailureMessage(player, plugin.getMessagesConfig().getInsufficientViews(tag.getMinViews()));
                    return;
                }
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §eErro ao obter contagem de visualizações: " + e.getMessage());
            }
        }
        
        CompletableFuture<JsonObject> channelInfoFuture = plugin.getYouTubeAPI().getChannelInfo(channelId);
        JsonObject channelInfo = channelInfoFuture.join();
        
        processChannelInfo(player, channelInfo, tag);
    }
    
    /**
     * Processa as informações do canal obtidas da API do YouTube.
     * @param player Jogador
     * @param channelInfo Informações do canal
     * @param tag Tag selecionada
     */
    private void processChannelInfo(Player player, JsonObject channelInfo, Tag tag) {
        JsonArray items = channelInfo.getAsJsonArray("items");
        if (items == null || items.size() == 0) {
            sendFailureMessage(player, plugin.getMessagesConfig().getVerificationFailed());
            return;
        }
        
        try {
            JsonObject channelItem = items.get(0).getAsJsonObject();
            JsonObject statistics = channelItem.getAsJsonObject("statistics");
            JsonObject snippet = channelItem.getAsJsonObject("snippet");
            
            String channelId = channelItem.get("id").getAsString();
            int subscriberCount = statistics.get("subscriberCount").getAsInt();
            int viewCount = statistics.get("viewCount").getAsInt();
            
            if (subscriberCount < tag.getMinSubscribers()) {
                sendFailureMessage(player, plugin.getMessagesConfig().getInsufficientSubscribers(tag.getMinSubscribers()));
                return;
            }
            
            if (tag.getMinViews() > 0 && viewCount < tag.getMinViews()) {
                sendFailureMessage(player, plugin.getMessagesConfig().getInsufficientViews(tag.getMinViews()));
                return;
            }
            
            String videoId = "";
            if (plugin.getMenuManager().hasPendingVerification(player)) {
                videoId = plugin.getYouTubeAPI().extractVideoId(plugin.getMenuManager().getPendingVerification(player));
            }
            
            DatabaseManager dbManager = plugin.getDatabaseManager();
            final String finalVideoId = videoId;
            final int finalSubscriberCount = subscriberCount;
            final String finalChannelId = channelId;
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    boolean success = plugin.getPermissionManager().addPermission(player, tag);
                    
                    if (success) {
                        if (dbManager.isEnabled()) {
                            dbManager.registerVinculation(
                                player, 
                                tag, 
                                finalVideoId, 
                                finalChannelId, 
                                finalSubscriberCount
                            ).whenComplete((result, ex) -> {
                                if (ex != null) {
                                    Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §eErro ao salvar vinculação no banco de dados: " + ex.getMessage());
                                }
                            });
                        }
                        
                        
                        SoundUtil.playSound(player, SoundUtil.SoundType.SUCCESS);
                        
                        player.sendMessage(plugin.getMessagesConfig().getVerificationSuccess());
                        
                        sendDiscordWebhookNotification(player.getName(), tag, finalVideoId, finalChannelId);
                        
                        executeRewardCommands(player, tag, finalChannelId);
                    } else {
                        SoundUtil.playSound(player, SoundUtil.SoundType.ERROR);
                        player.sendMessage(plugin.getMessagesConfig().getVerificationFailed());
                    }
                    
                    plugin.getMenuManager().removePendingVerification(player);
                }
            }.runTask(plugin);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao processar informações do canal: " + e.getMessage());
            sendFailureMessage(player, plugin.getMessagesConfig().getVerificationFailed());
        }
    }
    
    /**
     * Envia uma mensagem de falha para o jogador de volta no thread principal.
     * @param player Jogador
     * @param message Mensagem
     */
    private void sendFailureMessage(Player player, String message) {
        new BukkitRunnable() {
            @Override
            public void run() {
                SoundUtil.playSound(player, SoundUtil.SoundType.ERROR);
                player.sendMessage(message);
                plugin.getMenuManager().removePendingVerification(player);
            }
        }.runTask(plugin);
    }
    
    /**
     * Envia uma notificação para o webhook do Discord quando um jogador vincular uma tag.
     * 
     * @param playerName Nome do jogador que obteve a tag
     * @param tag Tag obtida pelo jogador
     * @param videoId ID do vídeo no YouTube
     * @param channelId ID do canal do YouTube
     */
    private void sendDiscordWebhookNotification(String playerName, Tag tag, String videoId, String channelId) {
        DiscordWebhook webhook = plugin.getDiscordWebhook();
        if (webhook == null || !webhook.isEnabled()) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fWebhook do Discord desativado ou não configurado.");
            }
            return;
        }
        
        String videoUrl = "https://www.youtube.com/watch?v=" + videoId;
        
        webhook.sendTagVinculationNotification(playerName, tag, videoUrl, channelId)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    if (plugin.getConfigManager().isDebugEnabled()) {
                        Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §c§lErro ao enviar webhook para o Discord: " + ex.getMessage());
                    }
                } else if (plugin.getConfigManager().isDebugEnabled()) {
                    Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fWebhook enviado com sucesso para o Discord.");
                }
            });
    }
    
    /**
     * Executa os comandos de recompensa configurados.
     * @param player Jogador que concluiu a verificação
     * @param tag Tag obtida pelo jogador
     * @param channelId ID do canal do YouTube
     */
    private void executeRewardCommands(Player player, Tag tag, String channelId) {
        List<String> commands = new ArrayList<>();
        
        if (tag.isRewardsEnabled() && !tag.getRewardCommands().isEmpty()) {
            commands = tag.getRewardCommands();
            if (plugin.getConfigManager().isDebugEnabled()) {
                Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fUsando comandos de recompensa específicos da tag: " + tag.getName());
            }
        } else if (plugin.getConfigManager().isRewardsEnabled()) {
            commands = plugin.getConfigManager().getRewardCommands();
            if (plugin.getConfigManager().isDebugEnabled()) {
                Bukkit.getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fUsando comandos de recompensa globais para tag: " + tag.getName());
            }
        }
        
        if (commands.isEmpty()) {
            return; // Sem comandos para executar
        }
        
        final String playerName = player.getName();
        final String tagName = tag.getName();
        
        for (String command : commands) {
            String processedCommand = plugin.getConfigManager().processRewardCommand(
                command, playerName, tagName, channelId
            );
            
            plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                processedCommand
            );
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fExecutado comando de recompensa: " + processedCommand);
            }
        }
    }
}
