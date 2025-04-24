package hplugins.hvincular.integration.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hplugins.hvincular.HVincular;
import hplugins.hvincular.config.JsonWebhookConfig;
import hplugins.hvincular.config.WebhookConfig;
import hplugins.hvincular.model.Tag;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço para envio de notificações para webhooks do Discord.
 */
public class DiscordWebhook {

    private final HVincular plugin;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public DiscordWebhook(HVincular plugin) {
        this.plugin = plugin;
    }

    /**
     * Verifica se o webhook está habilitado.
     * @return true se o webhook estiver habilitado, false caso contrário
     */
    public boolean isEnabled() {
        return plugin.getWebhookConfig().isEnabled();
    }

    /**
     * Envia uma notificação para o webhook do Discord sobre uma nova vinculação de tag.
     * 
     * @param playerName Nome do jogador
     * @param tag Tag que foi vinculada
     * @param videoUrl URL do vídeo usado para vinculação
     * @param channelId ID do canal do YouTube
     * @return CompletableFuture que completa quando a notificação é enviada
     */
    public CompletableFuture<Void> sendTagVinculationNotification(
            String playerName,
            Tag tag,
            String videoUrl,
            String channelId) {
        
        return sendVinculationNotification(playerName, tag, videoUrl, channelId, 0);
    }

    /**
     * Envia uma notificação para o webhook do Discord sobre uma nova vinculação.
     * Este método é executado de forma assíncrona para não bloquear o thread principal.
     * 
     * @param playerName Nome do jogador que vinculou a tag
     * @param tag Tag que foi vinculada
     * @param videoUrl URL do vídeo usado para vinculação
     * @param channelId ID do canal do YouTube
     * @param subscribers Número de inscritos no canal
     * @return CompletableFuture que completa quando a notificação é enviada
     */
    public CompletableFuture<Void> sendVinculationNotification(
            String playerName,
            Tag tag,
            String videoUrl,
            String channelId,
            int subscribers) {
        
        WebhookConfig webhookConfig = plugin.getWebhookConfig();
        boolean isEnabled = webhookConfig.isEnabled();
        String webhookUrl = webhookConfig.getWebhookUrl();
        
        if (!isEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("https://discord.com/api/webhooks/seu-webhook-aqui")) {
            plugin.getLogger().warning("Webhooks do Discord estão habilitados, mas a URL do webhook não está configurada corretamente!");
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                String channelUrl = "https://www.youtube.com/channel/" + channelId;
                
                String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                String subscribersStr = subscribers > 0 ? formatNumber(subscribers) : "Desconhecido";
                
                WebhookConfig yamlConfig = plugin.getWebhookConfig();
                
                String roleMention = yamlConfig.getRoleMention();
                String username = yamlConfig.getWebhookUsername();
                String avatarUrl = yamlConfig.getWebhookAvatarUrl();
                
                boolean isSuspicious = false;
                if (yamlConfig.isSuspiciousDetectionEnabled()) {
                    int minSubscribers = yamlConfig.getSuspiciousMinSubscribers();
                    isSuspicious = subscribers > 0 && subscribers < minSubscribers;
                }
                
                String suspiciousRoleMention = "";
                if (isSuspicious && yamlConfig.hasSuspiciousRoleId()) {
                    suspiciousRoleMention = yamlConfig.getSuspiciousRoleMention();
                }
                
                String content;
                String suspiciousFlag;
                
                if (plugin.isUsingJsonWebhook()) {
                    JsonWebhookConfig jsonConfig = plugin.getJsonWebhookConfig();
                    
                    content = jsonConfig.getMessageContent();
                    
                    suspiciousFlag = isSuspicious ? jsonConfig.getSuspiciousFlagEmoji() : "";
                    
                } else {
                    content = yamlConfig.getMessageContent();
                    suspiciousFlag = isSuspicious ? yamlConfig.getSuspiciousFlagEmoji() : "";
                }
                
                String channelDisplayName = "Canal do " + playerName;
                
                JsonObject jsonObject = new JsonObject();
                
                if (username != null && !username.isEmpty()) {
                    jsonObject.addProperty("username", username);
                }
                
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    jsonObject.addProperty("avatar_url", avatarUrl);
                }
                
                if (content != null && !content.isEmpty()) {
                    content = replaceMessageVariables(content, 
                            playerName, 
                            tag.getName(), 
                            videoUrl, 
                            channelUrl, 
                            channelDisplayName, 
                            subscribersStr, 
                            roleMention, 
                            suspiciousRoleMention, 
                            suspiciousFlag, 
                            datetime);
                    
                    jsonObject.addProperty("content", content);
                }
                
                boolean embedEnabled;
                String embedTitle;
                String suspiciousEmbedTitle;
                Color embedColor;
                Color suspiciousEmbedColor;
                boolean highlightSuspiciousWithColor;
                String embedDescription;
                boolean hasSuspiciousEmbedDescription;
                String suspiciousEmbedDescription;
                boolean thumbnailEnabled;
                String thumbnailUrl;
                boolean hasSuspiciousThumbnailUrl;
                String suspiciousThumbnailUrl;
                String footerText;
                String footerIconUrl;
                boolean useTimestamp;
                
                
                embedEnabled = yamlConfig.isEmbedEnabled();
                embedColor = yamlConfig.getEmbedColor();
                suspiciousEmbedColor = yamlConfig.getSuspiciousEmbedColor();
                highlightSuspiciousWithColor = yamlConfig.isHighlightSuspiciousWithColor();
                thumbnailEnabled = yamlConfig.isThumbnailEnabled();
                useTimestamp = yamlConfig.useTimestamp();
                
                if (plugin.isUsingJsonWebhook()) {
                    JsonWebhookConfig jsonConfig = plugin.getJsonWebhookConfig();
                    
                    embedTitle = jsonConfig.getEmbedTitle();
                    suspiciousEmbedTitle = jsonConfig.getSuspiciousEmbedTitle();
                    
                    embedDescription = jsonConfig.getEmbedDescription();
                    hasSuspiciousEmbedDescription = jsonConfig.hasSuspiciousEmbedDescription();
                    suspiciousEmbedDescription = jsonConfig.getSuspiciousEmbedDescription();
                    
                    thumbnailUrl = jsonConfig.getThumbnailUrl();
                    hasSuspiciousThumbnailUrl = jsonConfig.hasSuspiciousThumbnailUrl();
                    suspiciousThumbnailUrl = jsonConfig.getSuspiciousThumbnailUrl();
                    
                    footerText = jsonConfig.getFooterText();
                    footerIconUrl = jsonConfig.getFooterIconUrl();
                    
                } else {
                    embedTitle = yamlConfig.getEmbedTitle();
                    suspiciousEmbedTitle = yamlConfig.getSuspiciousEmbedTitle();
                    embedDescription = yamlConfig.getEmbedDescription();
                    hasSuspiciousEmbedDescription = yamlConfig.hasSuspiciousEmbedDescription();
                    suspiciousEmbedDescription = yamlConfig.getSuspiciousEmbedDescription();
                    thumbnailUrl = yamlConfig.getThumbnailUrl();
                    hasSuspiciousThumbnailUrl = yamlConfig.hasSuspiciousThumbnailUrl();
                    suspiciousThumbnailUrl = yamlConfig.getSuspiciousThumbnailUrl();
                    footerText = yamlConfig.getFooterText();
                    footerIconUrl = yamlConfig.getFooterIconUrl();
                }
                
                if (embedEnabled) {
                    JsonObject embed = new JsonObject();
                    
                    String titleConfig = isSuspicious ? suspiciousEmbedTitle : embedTitle;
                    
                    String formattedEmbedTitle = replaceMessageVariables(titleConfig,
                            playerName, 
                            tag.getName(), 
                            videoUrl, 
                            channelUrl, 
                            channelDisplayName, 
                            subscribersStr, 
                            roleMention, 
                            suspiciousRoleMention, 
                            suspiciousFlag, 
                            datetime);
                    embed.addProperty("title", formattedEmbedTitle);
                    
                    int embedColorValue;
                    if (isSuspicious && highlightSuspiciousWithColor) {
                        embedColorValue = suspiciousEmbedColor.getRGB() & 0xFFFFFF;
                    } else {
                        embedColorValue = embedColor.getRGB() & 0xFFFFFF;
                    }
                    embed.addProperty("color", embedColorValue);
                    
                    String descriptionConfig = embedDescription;
                    
                    if (isSuspicious && hasSuspiciousEmbedDescription) {
                        descriptionConfig += suspiciousEmbedDescription;
                    }
                    
                    String formattedEmbedDescription = replaceMessageVariables(descriptionConfig,
                            playerName, 
                            tag.getName(), 
                            videoUrl, 
                            channelUrl, 
                            channelDisplayName, 
                            subscribersStr, 
                            roleMention, 
                            suspiciousRoleMention, 
                            suspiciousFlag, 
                            datetime);
                    embed.addProperty("description", formattedEmbedDescription);
                    
                    if (thumbnailEnabled) {
                        JsonObject thumbnail = new JsonObject();
                        
                        String thumbnailUrlToUse;
                        if (isSuspicious && hasSuspiciousThumbnailUrl) {
                            thumbnailUrlToUse = suspiciousThumbnailUrl;
                        } else {
                            thumbnailUrlToUse = thumbnailUrl;
                        }
                        
                        if (thumbnailUrlToUse != null && !thumbnailUrlToUse.isEmpty()) {
                            thumbnail.addProperty("url", thumbnailUrlToUse);
                            embed.add("thumbnail", thumbnail);
                        }
                    }
                    
                    if (footerText != null && !footerText.isEmpty()) {
                        JsonObject footer = new JsonObject();
                        
                        String formattedFooterText = replaceMessageVariables(footerText,
                                playerName, 
                                tag.getName(), 
                                videoUrl, 
                                channelUrl, 
                                channelDisplayName, 
                                subscribersStr, 
                                roleMention, 
                                suspiciousRoleMention, 
                                suspiciousFlag, 
                                datetime);
                        footer.addProperty("text", formattedFooterText);
                        
                        if (footerIconUrl != null && !footerIconUrl.isEmpty()) {
                            footer.addProperty("icon_url", footerIconUrl);
                        }
                        
                        embed.add("footer", footer);
                    }
                    
                    if (useTimestamp) {
                        embed.addProperty("timestamp", java.time.OffsetDateTime.now().toString());
                    }
                    
                    JsonArray embedsArray = new JsonArray();
                    embedsArray.add(embed);
                    jsonObject.add("embeds", embedsArray);
                }
                
                sendHttpRequest(webhookUrl, jsonObject.toString());
            } catch (Exception e) {
                plugin.getLogger().severe("Erro ao enviar notificação para o Discord: " + e.getMessage());
            }
        });
    }
    
    /**
     * Envia uma requisição HTTP POST para o webhook do Discord.
     * 
     * @param webhookUrl URL do webhook do Discord
     * @param jsonPayload Payload JSON a ser enviado
     * @throws IOException Se ocorrer um erro durante o envio da requisição
     */
    private void sendHttpRequest(String webhookUrl, String jsonPayload) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(webhookUrl, jsonPayload);

        int responseCode = connection.getResponseCode();
        if (responseCode != 204) {
            plugin.getLogger().warning("Recebido código de resposta inesperado ao enviar webhook: " + responseCode);
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    plugin.getLogger().warning("Resposta de erro do Discord: " + response.toString());
                } catch (Exception e) {
                    plugin.getLogger().warning("Não foi possível ler a resposta de erro: " + e.getMessage());
                }
            }
        }
        
        connection.disconnect();
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(String webhookUrl, String jsonPayload) throws IOException {
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("User-Agent", "hVincular Plugin");
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }
        return connection;
    }

    /**
     * Formata um número para exibição mais amigável.
     * Adiciona separadores de milhar e sufixos para números grandes (K, M, B).
     * 
     * @param number Número a ser formatado
     * @return String formatada do número
     */
    private String formatNumber(int number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            double result = number / 1000.0;
            return String.format("%.1fK", result);
        } else if (number < 1000000000) {
            double result = number / 1000000.0;
            return String.format("%.1fM", result);
        } else {
            double result = number / 1000000000.0;
            return String.format("%.1fB", result);
        }
    }
    
    /**
     * Substitui todas as variáveis em uma string de mensagem pelos seus valores correspondentes.
     * Este método suporta todas as variáveis definidas no sistema de webhook.
     * 
     * @param message Mensagem original com as variáveis a serem substituídas
     * @param playerName Nome do jogador
     * @param tagName Nome da tag
     * @param videoUrl URL do vídeo
     * @param channelUrl URL do canal
     * @param channelDisplayName Nome de exibição do canal
     * @param subscribers Número de inscritos (já formatado)
     * @param roleMention Menção ao cargo normal
     * @param suspiciousRoleMention Menção ao cargo para casos suspeitos
     * @param suspiciousFlag Emoji ou flag para casos suspeitos
     * @param datetime Data e hora formatada
     * @return Mensagem com todas as variáveis substituídas
     */
    private String replaceMessageVariables(String message, 
                                          String playerName, 
                                          String tagName, 
                                          String videoUrl, 
                                          String channelUrl,
                                          String channelDisplayName,
                                          String subscribers, 
                                          String roleMention, 
                                          String suspiciousRoleMention, 
                                          String suspiciousFlag, 
                                          String datetime) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
        String result = message
            .replace("{player}", playerName)
            .replace("{tag}", tagName)
            .replace("{video_url}", videoUrl)
            .replace("{channel_url}", channelUrl)
            .replace("{channel_name}", channelDisplayName)
            .replace("{subscribers}", subscribers)
            .replace("{role_mention}", roleMention)
            .replace("{suspicious_role_mention}", suspiciousRoleMention)
            .replace("{suspicious_flag}", suspiciousFlag)
            .replace("{datetime}", datetime);
        
        return hplugins.hvincular.util.EmojiSafeYamlLoader.convertTokensToEmojis(result);
    }
}