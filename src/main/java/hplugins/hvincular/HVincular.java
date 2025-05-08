package hplugins.hvincular;

import hplugins.hvincular.command.*;
import hplugins.hvincular.config.*;
import hplugins.hvincular.database.DatabaseManager;
import hplugins.hvincular.gui.MenuManager;
import hplugins.hvincular.integration.PermissionManager;
import hplugins.hvincular.integration.discord.DiscordWebhook;
import hplugins.hvincular.integration.youtube.YouTubeAPI;
import hplugins.hvincular.integration.youtube.YouTubeVerifier;
import hplugins.hvincular.listener.ChatInputListener;
import hplugins.hvincular.listener.MenuClickListener;
import hplugins.hvincular.util.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Objects;

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
        if (configManager.isTestApiOnStartup()) testYouTubeApiConnection();

        this.databaseManager = new DatabaseManager(this);
        this.permissionManager = new PermissionManager(this);
        this.cooldownManager = new CooldownManager(this);
        this.discordWebhook = new DiscordWebhook(this);
        this.menuManager = new MenuManager(this);
        this.youTubeVerifier = new YouTubeVerifier(this);
        this.chatInputManager = new ChatInputManager(this);

        registerCommands();
        registerListeners();
        setupCacheCleanTask();

        getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §ahVincular v" + getDescription().getVersion() + " ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (youTubeAPI != null) youTubeAPI.clearAllCaches();
        if (databaseManager != null) databaseManager.disconnect();

        getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §c§lPlugin hVincular desativado!.");
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("vincular")).setExecutor(new VincularCommand(this));
        Objects.requireNonNull(getCommand("hv")).setExecutor(new HVCommand(this));
        Objects.requireNonNull(getCommand("hv")).setTabCompleter(new HVTabCompleter());
        Objects.requireNonNull(getCommand("hdesvincular")).setExecutor(new DesvincularCommand(this));
        Objects.requireNonNull(getCommand("hdesvincular")).setTabCompleter(new DesvincularTabCompleter(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this, chatInputManager), this);
    }

    private void setupCacheCleanTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (youTubeAPI != null) {
                    youTubeAPI.cleanExpiredCaches();
                    LoggerUtil.debug("Limpeza silenciosa de caches concluída");
                }
            }
        }.runTaskTimer(this, 6000L, 36000L);
    }

    private void testYouTubeApiConnection() {
        youTubeAPI.testConnection().thenAccept(success -> {
            if (success) {
                getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §aConexão com a API do YouTube estabelecida.");
            } else {
                getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cFalha ao conectar com a API do YouTube. Verifique a chave no config.yml.");
            }
        }).exceptionally(ex -> {
            getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao testar conexão com a API do YouTube: §f" + ex.getMessage());
            return null;
        });
    }

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
            getLogger().warning("Erro ao carregar webhook.json: " + e.getMessage());
            usingJsonWebhook = false;
        }

        SoundUtil.loadSoundsFromConfig();
    }

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

    public static HVincular getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public MenusConfig getMenusConfig() {
        return menusConfig;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public YouTubeAPI getYouTubeAPI() {
        return youTubeAPI;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    @Deprecated
    public WebhookConfig getWebhookConfig() {
        return webhookConfig;
    }

    public JsonWebhookConfig getJsonWebhookConfig() {
        return jsonWebhookConfig;
    }

    public boolean isUsingJsonWebhook() {
        return usingJsonWebhook;
    }

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }

    public YouTubeVerifier getYouTubeVerifier() {
        return youTubeVerifier;
    }
}
