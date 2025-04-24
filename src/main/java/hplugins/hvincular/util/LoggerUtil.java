package hplugins.hvincular.util;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilitário para log centralizado, com suporte a diferentes níveis
 * e formatação consistente. Compatível com todas as versões do Bukkit.
 */
public class LoggerUtil {
    
    private static final String PREFIX = "§8[§6hVincular§8] ";
    private static final String DEBUG_PREFIX = "§b[DEBUG] §f";
    private static final String INFO_COLOR = "§f";  // branco
    private static final String SUCCESS_COLOR = "§a";  // verde
    private static final String WARNING_COLOR = "§e";  // amarelo
    private static final String ERROR_COLOR = "§c";  // vermelho
    
    private static JavaPlugin plugin;
    private static boolean debugEnabled = false;
    
    /**
     * Inicializa o utilitário de log com a instância do plugin.
     * 
     * @param pluginInstance A instância do plugin
     * @param enableDebug Se os logs de debug devem ser exibidos
     */
    public static void initialize(JavaPlugin pluginInstance, boolean enableDebug) {
        plugin = pluginInstance;
        debugEnabled = enableDebug;
    }
    
    /**
     * Define se o modo de debug está ativo.
     * 
     * @param enabled true para ativar logs de debug, false para desativar
     */
    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }
    
    /**
     * Envia uma mensagem de informação para o console.
     * 
     * @param message A mensagem a ser logada
     */
    public static void info(String message) {
        log(INFO_COLOR + message);
    }
    
    /**
     * Envia uma mensagem de sucesso para o console.
     * 
     * @param message A mensagem a ser logada
     */
    public static void success(String message) {
        log(SUCCESS_COLOR + message);
    }
    
    /**
     * Envia uma mensagem de aviso para o console.
     * 
     * @param message A mensagem a ser logada
     */
    public static void warning(String message) {
        log(WARNING_COLOR + message);
        if (plugin != null) {
            plugin.getLogger().warning(ColorUtils.stripColor(message));
        }
    }
    
    /**
     * Envia uma mensagem de erro para o console.
     * 
     * @param message A mensagem a ser logada
     */
    public static void error(String message) {
        log(ERROR_COLOR + message);
        if (plugin != null) {
            plugin.getLogger().severe(ColorUtils.stripColor(message));
        }
    }
    
    /**
     * Envia uma mensagem de erro para o console com detalhes da exceção.
     * 
     * @param message A mensagem a ser logada
     * @param exception A exceção que causou o erro
     */
    public static void error(String message, Throwable exception) {
        error(message + ": " + exception.getMessage());
        
        if (plugin != null) {
            plugin.getLogger().log(Level.SEVERE, message, exception);
        }
    }
    
    /**
     * Envia uma mensagem de debug para o console, apenas se o debug estiver ativado.
     * 
     * @param message A mensagem a ser logada
     */
    public static void debug(String message) {
        if (debugEnabled) {
            log(DEBUG_PREFIX + message);
        }
    }
    
    /**
     * Método interno para enviar mensagens formatadas para o console.
     * 
     * @param message A mensagem a ser enviada
     */
    private static void log(String message) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        if (console != null) {
            console.sendMessage(PREFIX + message);
        } else {
            Logger logger = plugin != null ? plugin.getLogger() : Bukkit.getLogger();
            logger.info(ColorUtils.stripColor(message));
        }
    }
}