package hplugins.hvincular.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import hplugins.hvincular.HVincular;
import hplugins.hvincular.util.ConfigUtil;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

/**
 * Configuração dos webhooks do Discord.
 */
public class WebhookConfig {

    private final HVincular plugin;
    private final File configFile;
    private FileConfiguration config;
    
    private boolean enabled;
    private String webhookUrl;
    private String webhookUsername;
    private String webhookAvatarUrl;
    private String messageContent;
    
    private boolean mentionsEnabled;
    private String roleId;
    
    private boolean embedEnabled;
    private Color embedColor;
    private String embedTitle;
    private String embedDescription;
    private String footerText;
    private String footerIconUrl;
    private boolean useTimestamp;
    
    private boolean thumbnailEnabled;
    private String thumbnailUrl;
    
    private boolean suspiciousDetectionEnabled;
    private int suspiciousMinSubscribers;
    private boolean hasSuspiciousRoleId;
    private String suspiciousRoleId;
    private String suspiciousFlagEmoji;
    private String suspiciousEmbedTitle;
    private boolean highlightSuspiciousWithColor;
    private Color suspiciousEmbedColor;
    private boolean hasSuspiciousEmbedDescription;
    private String suspiciousEmbedDescription;
    private boolean hasSuspiciousThumbnailUrl;
    private String suspiciousThumbnailUrl;
    
    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public WebhookConfig(HVincular plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "webhook.yml");
        createConfigFile();
    }
    
    /**
     * Cria o arquivo de configuração caso não exista.
     */
    private void createConfigFile() {
        if (!configFile.exists()) {
            plugin.saveResource("webhook.yml", false);
        }
    }
    
    /**
     * Carrega ou recarrega as configurações do webhook.
     * Usa um carregador especializado que preserva emojis e caracteres Unicode
     * enquanto garante compatibilidade com todas as versões do Bukkit.
     */
    public void loadConfig() {
        try {
            hplugins.hvincular.util.EmojiSafeYamlLoader loader = new hplugins.hvincular.util.EmojiSafeYamlLoader(plugin);
            
            config = loader.load(configFile, "webhook.yml");
            
            loadConfigValues();
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar configuracao de webhook: " + e.getMessage());
            
            tryLoadDefaultConfig();
        }
    }
    
    /**
     * Tenta carregar uma configuração padrão em caso de falha na leitura.
     */
    private void tryLoadDefaultConfig() {
        try {
            File backupFile = new File(plugin.getDataFolder(), "webhook.yml.broken");
            if (backupFile.exists()) {
                backupFile.delete();
            }
            
            if (configFile.exists()) {
                java.nio.file.Files.copy(configFile.toPath(), backupFile.toPath());
                configFile.delete();
            }
            
            plugin.saveResource("webhook.yml", true);
            
            hplugins.hvincular.util.EmojiSafeYamlLoader loader = new hplugins.hvincular.util.EmojiSafeYamlLoader(plugin);
            config = loader.load(configFile, null);
            loadConfigValues();
        } catch (Exception e) {
            config = new YamlConfiguration();
            
            enabled = false;
        }
    }
    
    /**
     * Carrega os valores de configuração do arquivo.
     */
    private void loadConfigValues() {
        enabled = config.getBoolean("enabled", false);
        
        webhookUrl = config.getString("webhook-url", "https://discord.com/api/webhooks/seu-webhook-aqui");
        
        webhookUsername = config.getString("webhook-username", "Sistema de Verificação YouTuber");
        webhookAvatarUrl = config.getString("webhook-avatar-url", "");
        
        ConfigurationSection mentionsSection = config.getConfigurationSection("mentions");
        if (mentionsSection != null) {
            mentionsEnabled = mentionsSection.getBoolean("enabled", false);
            roleId = mentionsSection.getString("role-id", "");
        } else {
            mentionsEnabled = false;
            roleId = "";
        }
        
        messageContent = config.getString("message-content", 
            "{role_mention} Nova vinculação de **{tag}** detectada! A equipe precisa verificar.");
        
        ConfigurationSection embedSection = config.getConfigurationSection("embed");
        if (embedSection != null) {
            embedEnabled = embedSection.getBoolean("enabled", true);
            
            String colorHex = embedSection.getString("color", "#6441A4");
            if (colorHex.startsWith("#")) {
                colorHex = colorHex.substring(1);
            }
            try {
                embedColor = Color.decode("#" + colorHex);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Cor do embed inválida em webhook.yml: " + colorHex);
                embedColor = new Color(100, 65, 164);
            }
            
            embedTitle = embedSection.getString("title", "🎮 Nova Tag Vinculada: {tag}");
            embedDescription = embedSection.getString("description", 
                "### Informações do Jogador\n" +
                "👤 **Nickname:** `{player}`\n" +
                "🏆 **Tag Solicitada:** `{tag}`\n" +
                "📊 **Inscritos no Canal:** `{subscribers}`\n\n" +
                "### Links para Verificação\n" +
                "📹 [Vídeo Utilizado para Verificação]({video_url})\n" +
                "📺 [Canal do YouTube]({channel_url})\n\n" +
                "⚠️ **Importante:** Verifique se o vídeo pertence ao jogador e se o código de verificação está presente na descrição.");
            
            ConfigurationSection thumbnailSection = embedSection.getConfigurationSection("thumbnail");
            if (thumbnailSection != null) {
                thumbnailEnabled = thumbnailSection.getBoolean("enabled", true);
                thumbnailUrl = thumbnailSection.getString("url", "https://i.imgur.com/oBolWUn.png");
            } else {
                thumbnailEnabled = false;
                thumbnailUrl = "";
            }
            
            ConfigurationSection footerSection = embedSection.getConfigurationSection("footer");
            if (footerSection != null) {
                footerText = footerSection.getString("text", "hVincular • Verificação em {datetime}");
                footerIconUrl = footerSection.getString("icon-url", "");
            } else {
                footerText = "hVincular • Verificação em {datetime}";
                footerIconUrl = "";
            }
            
            useTimestamp = embedSection.getBoolean("timestamp", true);
        } else {
            embedEnabled = true;
            embedColor = new Color(100, 65, 164); // Roxo do Twitch
            embedTitle = "🎮 Nova Tag Vinculada: {tag}";
            embedDescription = "### Informações do Jogador\n" +
                "👤 **Nickname:** `{player}`\n" +
                "🏆 **Tag Solicitada:** `{tag}`\n" +
                "📊 **Inscritos no Canal:** `{subscribers}`";
            thumbnailEnabled = false;
            thumbnailUrl = "";
            footerText = "hVincular • Verificação em {datetime}";
            footerIconUrl = "";
            useTimestamp = true;
        }
        
        ConfigurationSection suspiciousSection = config.getConfigurationSection("suspicious-detection");
        if (suspiciousSection != null) {
            suspiciousDetectionEnabled = suspiciousSection.getBoolean("enabled", false);
            suspiciousMinSubscribers = suspiciousSection.getInt("min-subscribers", 1000);
            suspiciousFlagEmoji = suspiciousSection.getString("flag-emoji", "⚠️");
            
            ConfigurationSection suspiciousMentionsSection = suspiciousSection.getConfigurationSection("mentions");
            if (suspiciousMentionsSection != null) {
                hasSuspiciousRoleId = suspiciousMentionsSection.getBoolean("enabled", false);
                suspiciousRoleId = suspiciousMentionsSection.getString("role-id", "");
            } else {
                hasSuspiciousRoleId = false;
                suspiciousRoleId = "";
            }
            
            ConfigurationSection suspiciousEmbedSection = suspiciousSection.getConfigurationSection("embed");
            if (suspiciousEmbedSection != null) {
                suspiciousEmbedTitle = suspiciousEmbedSection.getString("title", "⚠️ Verificação Suspeita: {tag}");
                
                highlightSuspiciousWithColor = suspiciousEmbedSection.getBoolean("highlight-with-color", true);
                String suspiciousColorHex = suspiciousEmbedSection.getString("color", "#FF0000");
                if (suspiciousColorHex.startsWith("#")) {
                    suspiciousColorHex = suspiciousColorHex.substring(1);
                }
                try {
                    suspiciousEmbedColor = Color.decode("#" + suspiciousColorHex);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Cor do embed para casos suspeitos inválida em webhook.yml: " + suspiciousColorHex);
                    suspiciousEmbedColor = Color.RED; // Cor padrão para casos suspeitos
                }
                
                hasSuspiciousEmbedDescription = suspiciousEmbedSection.getBoolean("has-additional-description", false);
                suspiciousEmbedDescription = suspiciousEmbedSection.getString("additional-description", 
                    "\n\n⚠️ **ATENÇÃO: ESTA VINCULAÇÃO É SUSPEITA!**\n" +
                    "O canal possui poucos inscritos e pode não atender aos requisitos mínimos.\n" +
                    "Por favor, verifique com atenção antes de aprovar.");
                
                ConfigurationSection suspiciousThumbnailSection = suspiciousEmbedSection.getConfigurationSection("thumbnail");
                if (suspiciousThumbnailSection != null) {
                    hasSuspiciousThumbnailUrl = suspiciousThumbnailSection.getBoolean("enabled", false);
                    suspiciousThumbnailUrl = suspiciousThumbnailSection.getString("url", "https://i.imgur.com/qWAQXak.png");
                } else {
                    hasSuspiciousThumbnailUrl = false;
                    suspiciousThumbnailUrl = "";
                }
            } else {
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
    }
    
    /**
     * Salva as configurações no arquivo.
     */
    public void saveConfig() {
        ConfigUtil.saveConfiguration(plugin, config, configFile);
    }
    
    /**
     * Verifica se os webhooks estão habilitados.
     * @return true se os webhooks estiverem habilitados, false caso contrário
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Retorna a URL do webhook do Discord.
     * @return URL do webhook
     */
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    /**
     * Retorna o nome de usuário personalizado para o webhook.
     * @return Nome de usuário para o webhook
     */
    public String getWebhookUsername() {
        return webhookUsername;
    }
    
    /**
     * Retorna a URL do avatar personalizado para o webhook.
     * @return URL do avatar para o webhook
     */
    public String getWebhookAvatarUrl() {
        return webhookAvatarUrl;
    }
    
    /**
     * Verifica se a menção de cargos está habilitada.
     * @return true se a menção estiver habilitada, false caso contrário
     */
    public boolean isMentionsEnabled() {
        return mentionsEnabled;
    }
    
    /**
     * Retorna o ID do cargo a ser mencionado.
     * @return ID do cargo
     */
    public String getRoleId() {
        return roleId;
    }
    
    /**
     * Retorna a menção formatada conforme a configuração.
     * @return String formatada para menção de cargo para uso no Discord
     */
    public String getRoleMention() {
        if (!mentionsEnabled || roleId == null || roleId.isEmpty()) {
            return "";
        }
        
        if (roleId.equalsIgnoreCase("everyone")) {
            return "@everyone";
        } else if (roleId.equalsIgnoreCase("here")) {
            return "@here";
        } else {
            return "<@&" + roleId + ">";
        }
    }
    
    /**
     * Retorna o conteúdo da mensagem.
     * @return Conteúdo da mensagem
     */
    public String getMessageContent() {
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
     * Retorna o texto do rodapé do embed.
     * @return Texto do rodapé
     */
    public String getFooterText() {
        if (footerText != null && hplugins.hvincular.util.TextUtil.containsEmojiTokens(footerText)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(footerText);
        }
        return footerText;
    }
    
    /**
     * Retorna a URL do ícone do rodapé do embed.
     * @return URL do ícone do rodapé
     */
    public String getFooterIconUrl() {
        return footerIconUrl;
    }
    
    /**
     * Verifica se o timestamp deve ser incluído no embed.
     * @return true se o timestamp deve ser incluído, false caso contrário
     */
    public boolean useTimestamp() {
        return useTimestamp;
    }
    
    /**
     * Verifica se a detecção de casos suspeitos está habilitada.
     * @return true se a detecção de casos suspeitos estiver habilitada, false caso contrário
     */
    public boolean isSuspiciousDetectionEnabled() {
        return suspiciousDetectionEnabled;
    }
    
    /**
     * Retorna o número mínimo de inscritos para considerar um canal como suspeito.
     * @return número mínimo de inscritos
     */
    public int getSuspiciousMinSubscribers() {
        return suspiciousMinSubscribers;
    }
    
    /**
     * Verifica se existe um ID de cargo configurado para casos suspeitos.
     * @return true se existe um ID de cargo para casos suspeitos, false caso contrário
     */
    public boolean hasSuspiciousRoleId() {
        return hasSuspiciousRoleId;
    }
    
    /**
     * Retorna a menção formatada para casos suspeitos.
     * @return String formatada para menção de cargo para casos suspeitos
     */
    public String getSuspiciousRoleMention() {
        if (!hasSuspiciousRoleId || suspiciousRoleId == null || suspiciousRoleId.isEmpty()) {
            return "";
        }
        
        if (suspiciousRoleId.equalsIgnoreCase("everyone")) {
            return "@everyone";
        } else if (suspiciousRoleId.equalsIgnoreCase("here")) {
            return "@here";
        } else {
            return "<@&" + suspiciousRoleId + ">";
        }
    }
    
    /**
     * Retorna o emoji de flag para casos suspeitos.
     * @return emoji de flag para casos suspeitos
     */
    public String getSuspiciousFlagEmoji() {
        if (hplugins.hvincular.util.TextUtil.containsEmojiTokens(suspiciousFlagEmoji)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(suspiciousFlagEmoji);
        }
        return suspiciousFlagEmoji;
    }
    
    /**
     * Retorna o título do embed para casos suspeitos.
     * @return título do embed para casos suspeitos
     */
    public String getSuspiciousEmbedTitle() {
        if (hplugins.hvincular.util.TextUtil.containsEmojiTokens(suspiciousEmbedTitle)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(suspiciousEmbedTitle);
        }
        return suspiciousEmbedTitle;
    }
    
    /**
     * Verifica se casos suspeitos devem ser destacados com cor diferente.
     * @return true se casos suspeitos devem ser destacados com cor diferente, false caso contrário
     */
    public boolean isHighlightSuspiciousWithColor() {
        return highlightSuspiciousWithColor;
    }
    
    /**
     * Retorna a cor do embed para casos suspeitos.
     * @return cor do embed para casos suspeitos
     */
    public Color getSuspiciousEmbedColor() {
        return suspiciousEmbedColor;
    }
    
    /**
     * Verifica se existe uma descrição adicional para casos suspeitos.
     * @return true se existe uma descrição adicional para casos suspeitos, false caso contrário
     */
    public boolean hasSuspiciousEmbedDescription() {
        return hasSuspiciousEmbedDescription;
    }
    
    /**
     * Retorna a descrição adicional para casos suspeitos.
     * @return descrição adicional para casos suspeitos
     */
    public String getSuspiciousEmbedDescription() {
        if (hplugins.hvincular.util.TextUtil.containsEmojiTokens(suspiciousEmbedDescription)) {
            return hplugins.hvincular.util.TextUtil.processEmojiTokens(suspiciousEmbedDescription);
        }
        return suspiciousEmbedDescription;
    }
    
    /**
     * Verifica se existe uma URL de thumbnail específica para casos suspeitos.
     * @return true se existe uma URL de thumbnail para casos suspeitos, false caso contrário
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
     * Verifica se existe um texto de rodapé configurado.
     * @return true se existe um texto de rodapé, false caso contrário
     */
    public boolean hasFooterText() {
        return footerText != null && !footerText.isEmpty();
    }
    
    /**
     * Verifica se existe uma URL de ícone para o rodapé configurada.
     * @return true se existe uma URL de ícone para o rodapé, false caso contrário
     */
    public boolean hasFooterIconUrl() {
        return footerIconUrl != null && !footerIconUrl.isEmpty();
    }
}