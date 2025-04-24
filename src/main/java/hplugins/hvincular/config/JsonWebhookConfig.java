package hplugins.hvincular.config;

import com.google.gson.JsonObject;
import hplugins.hvincular.HVincular;
import hplugins.hvincular.util.JsonConfig;

import java.awt.Color;
import java.io.File;

/**
 * Configuração dos webhooks do Discord usando JSON.
 * Suporta nativamente caracteres Unicode e emojis sem necessidade de processamento especial.
 */
public class JsonWebhookConfig {

    private final HVincular plugin;
    private final String configFileName = "webhook.json";
    private File configFile;
    private JsonObject config;
    
    private String messageContent;
    
    private boolean enabled = true;
    private String webhookUrl = "";
    private String webhookUsername = "";
    private String webhookAvatarUrl = "";
    private boolean mentionsEnabled = false;
    private String roleId = "";
    
    private boolean embedEnabled;
    private Color embedColor;
    private String embedTitle;
    private String embedDescription;
    private boolean thumbnailEnabled;
    private String thumbnailUrl;
    private String footerText;
    private String footerIconUrl;
    private boolean useTimestamp;
    
    private boolean suspiciousDetectionEnabled;
    private int suspiciousMinSubscribers;
    private String suspiciousFlagEmoji;
    private boolean hasSuspiciousRoleId;
    private String suspiciousRoleId;
    private String suspiciousEmbedTitle;
    private boolean highlightSuspiciousWithColor;
    private Color suspiciousEmbedColor;
    private boolean hasSuspiciousEmbedDescription;
    private String suspiciousEmbedDescription;
    private boolean hasSuspiciousThumbnailUrl;
    private String suspiciousThumbnailUrl;
    
    /**
     * Construtor da configuração.
     * 
     * @param plugin Instância do plugin
     */
    public JsonWebhookConfig(HVincular plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), configFileName);
    }
    
    /**
     * Carrega ou recarrega as configurações do webhook usando JSON.
     * O JSON suporta nativamente caracteres Unicode e emojis sem necessidade
     * de processamento especial.
     */
    public void loadConfig() {
        try {
            config = JsonConfig.load(plugin, configFileName);
            
            loadConfigValues();
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar configuracao de webhook JSON: " + e.getMessage());
            
        }
    }
    
    /**
     * Carrega os valores de configuração do arquivo JSON.
     */
    private void loadConfigValues() {
        embedEnabled = true;
        embedColor = new Color(100, 65, 164); // Roxo Twitch
        thumbnailEnabled = true;
        thumbnailUrl = "https://i.imgur.com/oBolWUn.png";
        footerText = "hVincular ℹ️ Verificação em {datetime}";
        footerIconUrl = "";
        useTimestamp = true;
        
        embedTitle = "🎮 Nova Tag Vinculada: {tag}";
        embedDescription = "### Informações do Jogador\n" +
            "👤 **Nickname:** `{player}`\n" +
            "🏆 **Tag Solicitada:** `{tag}`\n" +
            "📊 **Inscritos no Canal:** `{subscribers}`\n\n" +
            "### Links para Verificação\n" +
            "📹 [Vídeo Utilizado para Verificação]({video_url})\n" +
            "📺 [Canal do YouTube]({channel_url})\n\n" +
            "⚠️ **Importante:** Verifique se o vídeo pertence ao jogador e se o código de verificação está presente na descrição.";
        messageContent = "{role_mention} Nova vinculação de **{tag}** detectada! A equipe precisa verificar.";
        
        if (JsonConfig.hasPath(config, "mensagens")) {
            if (JsonConfig.hasPath(config, "mensagens.padrao")) {                
                if (JsonConfig.hasPath(config, "mensagens.padrao.titulo")) {
                    embedTitle = JsonConfig.getString(config, "mensagens.padrao.titulo", embedTitle);
                }
                
                if (JsonConfig.hasPath(config, "mensagens.padrao.descricao")) {
                    embedDescription = JsonConfig.getString(config, "mensagens.padrao.descricao", embedDescription);
                }
                
                if (JsonConfig.hasPath(config, "mensagens.padrao.rodape")) {
                    footerText = JsonConfig.getString(config, "mensagens.padrao.rodape", footerText);
                }
                
                if (JsonConfig.hasPath(config, "mensagens.padrao.notificacao")) {
                    messageContent = JsonConfig.getString(config, "mensagens.padrao.notificacao", messageContent);
                }
            }
            
            if (JsonConfig.hasPath(config, "mensagens.suspeito")) {
                suspiciousDetectionEnabled = true;
                
                if (JsonConfig.hasPath(config, "mensagens.suspeito.titulo")) {
                    suspiciousEmbedTitle = JsonConfig.getString(config, "mensagens.suspeito.titulo", 
                            "⚠️ Verificação Suspeita: {tag}");
                }
                
                if (JsonConfig.hasPath(config, "mensagens.suspeito.descricao_adicional")) {
                    suspiciousEmbedDescription = JsonConfig.getString(config, "mensagens.suspeito.descricao_adicional", 
                            "\n\n⚠️ **ATENÇÃO: ESTA VINCULAÇÃO É SUSPEITA!**\n" +
                            "O canal possui poucos inscritos e pode não atender aos requisitos mínimos.\n" +
                            "Por favor, verifique com atenção antes de aprovar.");
                    hasSuspiciousEmbedDescription = true;
                }
                
                if (JsonConfig.hasPath(config, "mensagens.suspeito.emoji_alerta")) {
                    suspiciousFlagEmoji = JsonConfig.getString(config, "mensagens.suspeito.emoji_alerta", "⚠️");
                }
            }
            
            highlightSuspiciousWithColor = true;
            suspiciousEmbedColor = Color.RED;
            hasSuspiciousThumbnailUrl = false;
            suspiciousMinSubscribers = 1000;
        } 
        else if (JsonConfig.hasPath(config, "enabled")) {
            
            messageContent = JsonConfig.getString(config, "message-content", messageContent);
            
            if (JsonConfig.hasPath(config, "embed")) {
                embedEnabled = JsonConfig.getBoolean(config, "embed.enabled", embedEnabled);
                
                String colorHex = JsonConfig.getString(config, "embed.color", "#6441A4");
                if (colorHex.startsWith("#")) {
                    colorHex = colorHex.substring(1);
                }
                try {
                    embedColor = Color.decode("#" + colorHex);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Cor do embed inválida em " + configFileName + ": " + colorHex);
                    embedColor = new Color(100, 65, 164);
                }
                
                embedTitle = JsonConfig.getString(config, "embed.title", embedTitle);
                embedDescription = JsonConfig.getString(config, "embed.description", embedDescription);
                
                if (JsonConfig.hasPath(config, "embed.thumbnail")) {
                    thumbnailEnabled = JsonConfig.getBoolean(config, "embed.thumbnail.enabled", thumbnailEnabled);
                    thumbnailUrl = JsonConfig.getString(config, "embed.thumbnail.url", thumbnailUrl);
                }
                
                if (JsonConfig.hasPath(config, "embed.footer")) {
                    footerText = JsonConfig.getString(config, "embed.footer.text", footerText);
                    footerIconUrl = JsonConfig.getString(config, "embed.footer.icon-url", footerIconUrl);
                }
                
                useTimestamp = JsonConfig.getBoolean(config, "embed.timestamp", useTimestamp);
            }
        } else {
            suspiciousDetectionEnabled = false;
            suspiciousMinSubscribers = 1000;
            hasSuspiciousRoleId = false;
            suspiciousRoleId = "";
            suspiciousFlagEmoji = "⚠️";
            suspiciousEmbedTitle = "⚠️ Verificação Suspeita: {tag}";
            highlightSuspiciousWithColor = true;
            suspiciousEmbedColor = Color.RED;
            hasSuspiciousEmbedDescription = true;
            suspiciousEmbedDescription = "\n\n⚠️ **ATENÇÃO: ESTA VINCULAÇÃO É SUSPEITA!**\n" +
                "O canal possui poucos inscritos e pode não atender aos requisitos mínimos.\n" +
                "Por favor, verifique com atenção antes de aprovar.";
            hasSuspiciousThumbnailUrl = false;
            suspiciousThumbnailUrl = "";
        }
        
        if (!JsonConfig.hasPath(config, "mensagens") && JsonConfig.hasPath(config, "suspicious-detection")) {
            
            suspiciousDetectionEnabled = JsonConfig.getBoolean(config, "suspicious-detection.enabled", suspiciousDetectionEnabled);
            suspiciousMinSubscribers = JsonConfig.getInt(config, "suspicious-detection.min-subscribers", suspiciousMinSubscribers);
            suspiciousFlagEmoji = JsonConfig.getString(config, "suspicious-detection.flag-emoji", suspiciousFlagEmoji);
            
            if (JsonConfig.hasPath(config, "suspicious-detection.mentions")) {
                hasSuspiciousRoleId = JsonConfig.getBoolean(config, "suspicious-detection.mentions.enabled", hasSuspiciousRoleId);
                suspiciousRoleId = JsonConfig.getString(config, "suspicious-detection.mentions.role-id", suspiciousRoleId);
            }
            
            if (JsonConfig.hasPath(config, "suspicious-detection.embed")) {
                suspiciousEmbedTitle = JsonConfig.getString(config, "suspicious-detection.embed.title", suspiciousEmbedTitle);
                
                highlightSuspiciousWithColor = JsonConfig.getBoolean(config, "suspicious-detection.embed.highlight-with-color", highlightSuspiciousWithColor);
                String suspiciousColorHex = JsonConfig.getString(config, "suspicious-detection.embed.color", "#FF0000");
                if (suspiciousColorHex.startsWith("#")) {
                    suspiciousColorHex = suspiciousColorHex.substring(1);
                }
                try {
                    suspiciousEmbedColor = Color.decode("#" + suspiciousColorHex);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Cor do embed para casos suspeitos inválida em " + configFileName + ": " + suspiciousColorHex);
                    suspiciousEmbedColor = Color.RED;
                }
                
                hasSuspiciousEmbedDescription = JsonConfig.getBoolean(config, "suspicious-detection.embed.has-additional-description", hasSuspiciousEmbedDescription);
                if (hasSuspiciousEmbedDescription) {
                    suspiciousEmbedDescription = JsonConfig.getString(config, "suspicious-detection.embed.additional-description", suspiciousEmbedDescription);
                }
                
                if (JsonConfig.hasPath(config, "suspicious-detection.embed.thumbnail")) {
                    hasSuspiciousThumbnailUrl = JsonConfig.getBoolean(config, "suspicious-detection.embed.thumbnail.enabled", hasSuspiciousThumbnailUrl);
                    suspiciousThumbnailUrl = JsonConfig.getString(config, "suspicious-detection.embed.thumbnail.url", suspiciousThumbnailUrl);
                }
            }
        }
    }
    
    
    /**
     * Verifica se os webhooks estão habilitados.
     * @deprecated Use WebhookConfig.isEnabled() em vez disso
     * @return true se os webhooks estiverem habilitados, false caso contrário
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Retorna a URL do webhook do Discord.
     * @deprecated Use WebhookConfig.getWebhookUrl() em vez disso
     * @return URL do webhook
     */
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    /**
     * Retorna o nome de usuário personalizado para o webhook.
     * @deprecated Use WebhookConfig.getWebhookUsername() em vez disso
     * @return Nome de usuário para o webhook
     */
    public String getWebhookUsername() {
        return webhookUsername;
    }
    
    /**
     * Retorna a URL do avatar personalizado para o webhook.
     * @deprecated Use WebhookConfig.getWebhookAvatarUrl() em vez disso
     * @return URL do avatar para o webhook
     */
    public String getWebhookAvatarUrl() {
        return webhookAvatarUrl;
    }
    
    /**
     * Verifica se a menção de cargos está habilitada.
     * @deprecated Use WebhookConfig.isMentionsEnabled() em vez disso
     * @return true se a menção estiver habilitada, false caso contrário
     */
    public boolean isMentionsEnabled() {
        return mentionsEnabled;
    }
    
    /**
     * Retorna o ID do cargo a ser mencionado.
     * @deprecated Use WebhookConfig.getRoleId() em vez disso
     * @return ID do cargo
     */
    public String getRoleId() {
        return roleId;
    }
    
    /**
     * Retorna a menção formatada conforme a configuração.
     * @deprecated Use WebhookConfig.getRoleMention() em vez disso
     * @return Menção formatada ou string vazia se desabilitada
     */
    public String getRoleMention() {
        if (mentionsEnabled && !roleId.isEmpty()) {
            return "<@&" + roleId + ">";
        }
        return "";
    }
    
    /**
     * Retorna o conteúdo da mensagem a ser enviada.
     * @return Conteúdo da mensagem
     */
    public String getMessageContent() {
        if (messageContent != null && hplugins.hvincular.util.TextUtil.containsEmojiTokens(messageContent)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(messageContent);
        }
        return messageContent;
    }
    
    /**
     * Verifica se o embed está habilitado.
     * @return true se o embed estiver habilitado, false caso contrário
     */
    public boolean isEmbedEnabled() {
        return embedEnabled;
    }
    
    /**
     * Retorna a cor do embed.
     * @return Cor do embed
     */
    public Color getEmbedColor() {
        return embedColor;
    }
    
    /**
     * Retorna o título do embed.
     * @return Título do embed
     */
    public String getEmbedTitle() {
        if (hplugins.hvincular.util.TextUtil.containsEmojiTokens(embedTitle)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(embedTitle);
        }
        return embedTitle;
    }
    
    /**
     * Retorna a descrição do embed.
     * @return Descrição do embed
     */
    public String getEmbedDescription() {
        if (hplugins.hvincular.util.TextUtil.containsEmojiTokens(embedDescription)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(embedDescription);
        }
        return embedDescription;
    }
    
    /**
     * Verifica se a thumbnail está habilitada.
     * @return true se a thumbnail estiver habilitada, false caso contrário
     */
    public boolean isThumbnailEnabled() {
        return thumbnailEnabled;
    }
    
    /**
     * Retorna a URL da thumbnail.
     * @return URL da thumbnail
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    /**
     * Retorna o texto do rodapé.
     * @return Texto do rodapé
     */
    public String getFooterText() {
        if (footerText != null && hplugins.hvincular.util.TextUtil.containsEmojiTokens(footerText)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(footerText);
        }
        return footerText;
    }
    
    /**
     * Retorna a URL do ícone do rodapé.
     * @return URL do ícone do rodapé
     */
    public String getFooterIconUrl() {
        return footerIconUrl;
    }
    
    /**
     * Verifica se o timestamp deve ser usado.
     * @return true se o timestamp deve ser usado, false caso contrário
     */
    public boolean useTimestamp() {
        return useTimestamp;
    }
    
    /**
     * Verifica se a detecção de casos suspeitos está habilitada.
     * @return true se a detecção estiver habilitada, false caso contrário
     */
    public boolean isSuspiciousDetectionEnabled() {
        return suspiciousDetectionEnabled;
    }
    
    /**
     * Retorna o número mínimo de inscritos para não ser considerado suspeito.
     * @return Número mínimo de inscritos
     */
    public int getSuspiciousMinSubscribers() {
        return suspiciousMinSubscribers;
    }
    
    /**
     * Retorna o emoji de alerta para casos suspeitos.
     * @return Emoji de alerta
     */
    public String getSuspiciousFlagEmoji() {
        if (hplugins.hvincular.util.TextUtil.containsEmojiTokens(suspiciousFlagEmoji)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(suspiciousFlagEmoji);
        }
        return suspiciousFlagEmoji;
    }
    
    /**
     * Verifica se há um cargo específico para ser mencionado em casos suspeitos.
     * @return true se houver um cargo específico, false caso contrário
     */
    public boolean hasSuspiciousRoleId() {
        return hasSuspiciousRoleId;
    }
    
    /**
     * Retorna o ID do cargo a ser mencionado em casos suspeitos.
     * @return ID do cargo para casos suspeitos
     */
    public String getSuspiciousRoleId() {
        return suspiciousRoleId;
    }
    
    /**
     * Retorna a menção formatada para casos suspeitos.
     * @return Menção formatada ou string vazia se desabilitada
     */
    public String getSuspiciousRoleMention() {
        if (hasSuspiciousRoleId && !suspiciousRoleId.isEmpty()) {
            return "<@&" + suspiciousRoleId + ">";
        }
        return "";
    }
    
    /**
     * Retorna o título do embed para casos suspeitos.
     * @return Título do embed para casos suspeitos
     */
    public String getSuspiciousEmbedTitle() {
        if (hplugins.hvincular.util.TextUtil.containsEmojiTokens(suspiciousEmbedTitle)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(suspiciousEmbedTitle);
        }
        return suspiciousEmbedTitle;
    }
    
    /**
     * Verifica se casos suspeitos devem ser destacados com uma cor diferente.
     * @return true se devem ser destacados, false caso contrário
     */
    public boolean highlightSuspiciousWithColor() {
        return highlightSuspiciousWithColor;
    }
    
    /**
     * Retorna a cor do embed para casos suspeitos.
     * @return Cor do embed para casos suspeitos
     */
    public Color getSuspiciousEmbedColor() {
        return suspiciousEmbedColor;
    }
    
    /**
     * Verifica se há uma descrição adicional para casos suspeitos.
     * @return true se houver uma descrição adicional, false caso contrário
     */
    public boolean hasSuspiciousEmbedDescription() {
        return hasSuspiciousEmbedDescription;
    }
    
    /**
     * Retorna a descrição adicional para casos suspeitos.
     * @return Descrição adicional para casos suspeitos
     */
    public String getSuspiciousEmbedDescription() {
        if (suspiciousEmbedDescription != null && hplugins.hvincular.util.TextUtil.containsEmojiTokens(suspiciousEmbedDescription)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(suspiciousEmbedDescription);
        }
        return suspiciousEmbedDescription;
    }
    
    /**
     * Verifica se há uma thumbnail específica para casos suspeitos.
     * @return true se houver uma thumbnail específica, false caso contrário
     */
    public boolean hasSuspiciousThumbnailUrl() {
        return hasSuspiciousThumbnailUrl;
    }
    
    /**
     * Retorna a URL da thumbnail para casos suspeitos.
     * @return URL da thumbnail para casos suspeitos
     */
    public String getSuspiciousThumbnailUrl() {
        return suspiciousThumbnailUrl;
    }
    
    /**
     * Verifica se há um texto de rodapé configurado.
     * @return true se houver um texto de rodapé, false caso contrário
     */
    public boolean hasFooterText() {
        return footerText != null && !footerText.isEmpty();
    }
    
    /**
     * Verifica se há uma URL de ícone para o rodapé configurada.
     * @return true se houver uma URL de ícone, false caso contrário
     */
    public boolean hasFooterIconUrl() {
        return footerIconUrl != null && !footerIconUrl.isEmpty();
    }
    
    /**
     * Verifica se esta configuração JSON contém as mensagens equivalentes às da configuração YAML.
     * Útil para verificar se o formato de mensagens foi atualizado.
     * 
     * @param yamlConfig Configuração YAML para comparação
     * @return true se as mensagens são equivalentes, false caso contrário
     */
    public boolean isEquivalentTo(WebhookConfig yamlConfig) {
        return embedEnabled == yamlConfig.isEmbedEnabled() &&
               embedTitle.equals(yamlConfig.getEmbedTitle()) &&
               embedDescription.equals(yamlConfig.getEmbedDescription());
    }
}