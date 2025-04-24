package hplugins.hvincular;

import hplugins.hvincular.command.DesvincularCommand;
import hplugins.hvincular.command.DesvincularTabCompleter;
import hplugins.hvincular.command.HVCommand;
import hplugins.hvincular.command.HVTabCompleter;
import hplugins.hvincular.command.VincularCommand;
import hplugins.hvincular.config.ConfigManager;
import hplugins.hvincular.config.JsonWebhookConfig;
import hplugins.hvincular.config.MenusConfig;
import hplugins.hvincular.config.MessagesConfig;
import hplugins.hvincular.config.WebhookConfig;
import hplugins.hvincular.database.DatabaseManager;
import hplugins.hvincular.gui.MenuManager;
import hplugins.hvincular.integration.PermissionManager;
import hplugins.hvincular.integration.discord.DiscordWebhook;
import hplugins.hvincular.integration.youtube.YouTubeAPI;
import hplugins.hvincular.integration.youtube.YouTubeVerifier;
import hplugins.hvincular.listener.ChatInputListener;
import hplugins.hvincular.listener.MenuClickListener;
import hplugins.hvincular.util.ChatInputManager;
import hplugins.hvincular.util.ConfigUtil;
import hplugins.hvincular.util.CooldownManager;
import hplugins.hvincular.util.InventoryUtils;
import hplugins.hvincular.util.LoggerUtil;
import hplugins.hvincular.util.SoundUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Classe principal do plugin hVincular.
 * Responsável pela inicialização e gerenciamento dos componentes principais.
 */
public class HVincular extends JavaPlugin {

    private static HVincular instance;
    private ConfigManager configManager;
    private MessagesConfig messagesConfig;
    private MenusConfig menusConfig;
    private WebhookConfig webhookConfig;
    private JsonWebhookConfig jsonWebhookConfig;
    private MenuManager menuManager;
    private PermissionManager permissionManager;
    private YouTubeAPI youTubeAPI;
    private YouTubeVerifier youTubeVerifier;
    private DatabaseManager databaseManager;
    private CooldownManager cooldownManager;
    private DiscordWebhook discordWebhook;
    private ChatInputManager chatInputManager;
    
    private boolean usingJsonWebhook = true;
    
    private BukkitTask cacheClearTask;

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);
        this.messagesConfig = new MessagesConfig(this);
        this.menusConfig = new MenusConfig(this);
        this.webhookConfig = new WebhookConfig(this);
        this.jsonWebhookConfig = new JsonWebhookConfig(this);
        loadConfigurations();
        SoundUtil.initialize(this);
        InventoryUtils.setLogger(getLogger());
        this.youTubeAPI = new YouTubeAPI(configManager.getYouTubeApiKey());
        if (configManager.isTestApiOnStartup()) {
            testYouTubeApiConnection();
        }
        
        this.databaseManager = new DatabaseManager(this);
        this.permissionManager = new PermissionManager(this);
        this.cooldownManager = new CooldownManager(this);
        this.discordWebhook = new DiscordWebhook(this);
        this.menuManager = new MenuManager(this);
        this.youTubeVerifier = new YouTubeVerifier(this);
        this.chatInputManager = new ChatInputManager(this);
        
        Objects.requireNonNull(getCommand("vincular")).setExecutor(new VincularCommand(this));
        Objects.requireNonNull(getCommand("hv")).setExecutor(new HVCommand(this));
        Objects.requireNonNull(getCommand("hv")).setTabCompleter(new HVTabCompleter());
        Objects.requireNonNull(getCommand("hdesvincular")).setExecutor(new DesvincularCommand(this));
        Objects.requireNonNull(getCommand("hdesvincular")).setTabCompleter(new DesvincularTabCompleter(this));
        
        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this, chatInputManager), this);
        
        setupCacheCleanTask();
        getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §ahVincular v" + getDescription().getVersion() + " ativado com sucesso!");
    }
    
    /**
     * Testa a conexão com a API do YouTube e exibe mensagens no console.
     */
    private void testYouTubeApiConnection() {
        youTubeAPI.testConnection().thenAccept(success -> {
            if (success) {
                getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §aConexao com a API do YouTube estabelecida com sucesso!");
            } else {
                getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §c§lFalha ao conectar com a API do YouTube!");
                getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §c§lVerifique se sua chave de API está correta em config.yml.");
            }
        }).exceptionally(ex -> {
            getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao testar conexao com a API do YouTube: " + ex.getMessage());
            return null;
        });
    }

    /**
     * Configura a tarefa periódica para limpar o cache expirado.
     * Isso ajuda a reduzir o uso de memória e evita vazamentos.
     * A limpeza é executada silenciosamente para não sobrecarregar o console.
     */
    private void setupCacheCleanTask() {
        if (cacheClearTask != null && !cacheClearTask.isCancelled()) {
            cacheClearTask.cancel();
        }
        
        cacheClearTask = getServer().getScheduler().runTaskTimer(this, () -> {
            if (youTubeAPI != null) {
                youTubeAPI.cleanExpiredCaches();
            }
            
            LoggerUtil.debug("Limpeza silenciosa de caches concluída");
        }, 6000L, 36000L);
    }

    @Override
    public void onDisable() {
        if (cacheClearTask != null && !cacheClearTask.isCancelled()) {
            cacheClearTask.cancel();
            getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §7Tarefa de limpeza de cache cancelada.");
        }
        
        if (youTubeAPI != null) {
            youTubeAPI.clearAllCaches();
            getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §7Caches do YouTube limpos.");
        }
        
        if (databaseManager != null) {
            databaseManager.disconnect();
            getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §7Conexão com o banco de dados fechada.");
        }
        
        getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §c§lPlugin hVincular desativado.");
    }
    
    /**
     * Recarrega todas as configurações do plugin.
     */
    public void loadConfigurations() {
        configManager.loadConfig();
        messagesConfig.loadMessages();
        menusConfig.loadMenus();
        
        checkAndMigrateWebhookConfig();
        
        
        webhookConfig.loadConfig();
        
        try {
            jsonWebhookConfig.loadConfig();
            usingJsonWebhook = true;
        } catch (Exception e) {
            getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §eErro ao carregar webhook.json: " + e.getMessage());
            usingJsonWebhook = false;
        }
        
        SoundUtil.loadSoundsFromConfig();
    }
    
    /**
     * Verifica a existência das configurações de webhook (YAML e JSON) e garante que ambas existam.
     * No novo modelo, o YAML contém configurações funcionais e o JSON contém apenas mensagens com emojis.
     */
    private void checkAndMigrateWebhookConfig() {
        File yamlFile = new File(getDataFolder(), "webhook.yml");
        File jsonFile = new File(getDataFolder(), "webhook.json");
        
        if (!yamlFile.exists()) saveResource("webhook.yml", false);
        if (!jsonFile.exists()) saveResource("webhook.json", false);
        
        if (yamlFile.exists() && !jsonFile.exists()) {
            if (!hplugins.hvincular.util.JsonConfig.convertYamlWebhookToJson(this)) {
                saveResource("webhook.json", false);
            }
        }
        
        usingJsonWebhook = jsonFile.exists();
    }
    
    /**
     * Retorna a instância do plugin.
     * @return Instância do plugin
     */
    public static HVincular getInstance() {
        return instance;
    }
    
    /**
     * Retorna o gerenciador de configurações.
     * @return ConfigManager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Retorna o gerenciador de mensagens.
     * @return MessagesConfig
     */
    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }
    
    /**
     * Retorna o gerenciador de menus e tags.
     * @return MenusConfig
     */
    public MenusConfig getMenusConfig() {
        return menusConfig;
    }
    
    /**
     * Retorna o gerenciador de menus.
     * @return MenuManager
     */
    public MenuManager getMenuManager() {
        return menuManager;
    }
    
    /**
     * Retorna o gerenciador de permissões.
     * @return PermissionManager
     */
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
    
    /**
     * Retorna a API do YouTube.
     * @return YouTubeAPI
     */
    public YouTubeAPI getYouTubeAPI() {
        return youTubeAPI;
    }
    
    /**
     * Retorna o gerenciador de banco de dados.
     * @return DatabaseManager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    /**
     * Retorna o gerenciador de cooldowns.
     * @return CooldownManager
     */
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    /**
     * Retorna a configuração de webhooks do Discord.
     * @return WebhookConfig
     * @deprecated Use getJSONWebhookConfig() para a versão JSON com suporte nativo a emojis
     */
    @Deprecated
    public WebhookConfig getWebhookConfig() {
        return webhookConfig;
    }
    
    /**
     * Retorna a configuração de webhooks do Discord no formato JSON.
     * @return JsonWebhookConfig
     */
    public JsonWebhookConfig getJsonWebhookConfig() {
        return jsonWebhookConfig;
    }
    
    /**
     * Verifica se o sistema está usando a configuração JSON para webhooks.
     * @return true se estiver usando JSON, false se estiver usando YAML
     */
    public boolean isUsingJsonWebhook() {
        return usingJsonWebhook;
    }
    
    /**
     * Retorna o serviço de webhook do Discord para envio de notificações.
     * @return DiscordWebhook
     */
    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }
    
    /**
     * Retorna o gerenciador de entrada via chat.
     * @return ChatInputManager
     */
    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }
    
    /**
     * Retorna o verificador de vídeos do YouTube.
     * @return YouTubeVerifier
     */
    public YouTubeVerifier getYouTubeVerifier() {
        return youTubeVerifier;
    }
    
}
