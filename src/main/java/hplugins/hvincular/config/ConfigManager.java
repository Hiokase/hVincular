package hplugins.hvincular.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import hplugins.hvincular.HVincular;
import hplugins.hvincular.util.ConfigUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador responsável pela configuração principal do plugin.
 */
public class ConfigManager {

    private final HVincular plugin;
    private File configFile;
    private FileConfiguration config;
    
    private String youTubeApiKey;
    private List<String> serverIps;
    private boolean testApiOnStartup;
    private boolean preventVideoReuse;
    private boolean verificationCodeEnabled;
    private String verificationCodePrefix;
    private boolean debugEnabled;
    
    private String permissionAddCommand;
    private String permissionRemoveCommand;
    
    private boolean rewardsEnabled;
    private List<String> rewardCommands;
    
    
    private boolean databaseEnabled;
    private String databaseType;
    private String databaseHost;
    private int databasePort;
    private String databaseName;
    private String databaseUsername;
    private String databasePassword;
    private String databaseTablePrefix;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public ConfigManager(HVincular plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        createConfig();
    }

    /**
     * Cria o arquivo de configuração caso não exista.
     */
    private void createConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    /**
     * Carrega ou recarrega as configurações do plugin.
     * Usa UTF-8 para garantir que caracteres especiais sejam lidos corretamente.
     */
    public void loadConfig() {
        config = ConfigUtil.loadConfiguration(plugin, configFile, "config.yml");
        loadConfigValues();
    }

    /**
     * Carrega os valores de configuração do arquivo.
     */
    private void loadConfigValues() {
        youTubeApiKey = config.getString("youtube.api-key", "");
        
        serverIps = config.getStringList("server.ips");
        
        if (serverIps.isEmpty()) {
            serverIps = new ArrayList<>();
            serverIps.add("play.servidor.com.br");
        }
        
        testApiOnStartup = config.getBoolean("server.test-api-on-startup", true);
        
        preventVideoReuse = config.getBoolean("server.prevent-video-reuse", true);
        
        verificationCodeEnabled = config.getBoolean("server.verification-code.enabled", true);
        verificationCodePrefix = config.getString("server.verification-code.prefix", "CODIGO-VERIFICACAO:");
        
        debugEnabled = config.getBoolean("server.debug", false);
        
        permissionAddCommand = config.getString("server.permissions.add-command", "lp user {player} permission set {permission} true");
        permissionRemoveCommand = config.getString("server.permissions.remove-command", "lp user {player} permission unset {permission}");
        
        rewardsEnabled = config.getBoolean("server.rewards.enabled", true);
        rewardCommands = config.getStringList("server.rewards.commands");
        
        databaseEnabled = config.getBoolean("database.enabled", false);
        databaseType = config.getString("database.type", "sqlite").toLowerCase();
        databaseHost = config.getString("database.host", "localhost");
        databasePort = config.getInt("database.port", 3306);
        databaseName = config.getString("database.database", "hvincular");
        databaseUsername = config.getString("database.username", "root");
        databasePassword = config.getString("database.password", "");
        databaseTablePrefix = config.getString("database.table-prefix", "hv_");
    }

    /**
     * Salva as configurações no arquivo.
     */
    public void saveConfig() {
        ConfigUtil.saveConfiguration(plugin, config, configFile);
    }

    /**
     * Retorna a chave da API do YouTube.
     * @return Chave da API
     */
    public String getYouTubeApiKey() {
        return youTubeApiKey;
    }

    /**
     * Retorna a lista de IPs do servidor configurados.
     * @return Lista de IPs
     */
    public List<String> getServerIps() {
        return serverIps;
    }


    
    /**
     * Verifica se o banco de dados está habilitado.
     * @return true se o banco de dados estiver habilitado, false caso contrário
     */
    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }
    
    /**
     * Retorna o tipo de banco de dados (mysql ou sqlite).
     * @return Tipo do banco de dados
     */
    public String getDatabaseType() {
        return databaseType;
    }
    
    /**
     * Retorna o host do banco de dados.
     * @return Host do banco de dados
     */
    public String getDatabaseHost() {
        return databaseHost;
    }
    
    /**
     * Retorna a porta do banco de dados.
     * @return Porta do banco de dados
     */
    public int getDatabasePort() {
        return databasePort;
    }
    
    /**
     * Retorna o nome do banco de dados.
     * @return Nome do banco de dados
     */
    public String getDatabaseName() {
        return databaseName;
    }
    
    /**
     * Retorna o nome de usuário do banco de dados.
     * @return Nome de usuário do banco de dados
     */
    public String getDatabaseUsername() {
        return databaseUsername;
    }
    
    /**
     * Retorna a senha do banco de dados.
     * @return Senha do banco de dados
     */
    public String getDatabasePassword() {
        return databasePassword;
    }
    
    /**
     * Retorna o prefixo das tabelas do banco de dados.
     * @return Prefixo das tabelas
     */
    public String getDatabaseTablePrefix() {
        return databaseTablePrefix;
    }
    
    /**
     * Verifica se o teste da API do YouTube deve ser executado na inicialização.
     * @return true se o teste deve ser realizado, false caso contrário
     */
    public boolean isTestApiOnStartup() {
        return testApiOnStartup;
    }
    
    /**
     * Verifica se a prevenção de reutilização de vídeos está ativada.
     * Quando ativada, impede que jogadores usem vídeos que já foram utilizados por outras pessoas.
     * @return true se a prevenção estiver ativada, false caso contrário
     */
    public boolean isPreventVideoReuse() {
        return preventVideoReuse;
    }
    
    /**
     * Verifica se o modo de debug está ativado.
     * @return true se o debug estiver ativado, false caso contrário
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
    
    /**
     * Verifica se a verificação de código personalizado está ativada.
     * @return true se a verificação estiver ativada, false caso contrário
     */
    public boolean isVerificationCodeEnabled() {
        return verificationCodeEnabled;
    }
    
    /**
     * Retorna o prefixo usado para o código de verificação.
     * @return Prefixo do código de verificação
     */
    public String getVerificationCodePrefix() {
        return verificationCodePrefix;
    }
    
    /**
     * Gera o código de verificação completo para um jogador.
     * @param playerName Nome do jogador
     * @return Código de verificação completo
     */
    public String generateVerificationCode(String playerName) {
        return verificationCodePrefix + playerName;
    }
    
    /**
     * Verifica se as recompensas estão habilitadas.
     * @return true se as recompensas estiverem habilitadas, false caso contrário
     */
    public boolean isRewardsEnabled() {
        return rewardsEnabled;
    }
    
    /**
     * Retorna a lista de comandos de recompensa.
     * @return Lista de comandos de recompensa
     */
    public List<String> getRewardCommands() {
        return rewardCommands;
    }
    
    /**
     * Retorna o comando para adicionar permissão.
     * @return Comando para adicionar permissão
     */
    public String getPermissionAddCommand() {
        return permissionAddCommand;
    }
    
    /**
     * Retorna o comando para remover permissão.
     * @return Comando para remover permissão
     */
    public String getPermissionRemoveCommand() {
        return permissionRemoveCommand;
    }
    
    /**
     * Processa um comando de permissão substituindo as variáveis.
     * @param command Comando a ser processado
     * @param player Nome do jogador
     * @param permission Permissão a ser adicionada/removida
     * @return Comando processado
     */
    public String processPermissionCommand(String command, String player, String permission) {
        return command
            .replace("{player}", player)
            .replace("{permission}", permission);
    }
    
    /**
     * Processa um comando de recompensa substituindo as variáveis.
     * @param command Comando a ser processado
     * @param player Nome do jogador
     * @param tag Nome da tag
     * @param channelId ID do canal
     * @return Comando processado
     */
    public String processRewardCommand(String command, String player, String tag, String channelId) {
        return command
            .replace("{player}", player)
            .replace("{tag}", tag)
            .replace("{channel}", channelId);
    }
    
}
