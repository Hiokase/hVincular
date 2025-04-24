package hplugins.hvincular.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utilitário para manipulação de arquivos de configuração com suporte a UTF-8.
 * Simplificado para uso com o sistema de tokens de emoji.
 */
public class ConfigUtil {

    /**
     * Carrega um arquivo de configuração YAML de forma simples.
     *
     * @param plugin Instância do plugin
     * @param fileName Nome do arquivo a ser carregado
     * @return Configuração carregada
     */
    public static FileConfiguration loadConfig(JavaPlugin plugin, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        
        return YamlConfiguration.loadConfiguration(configFile);
    }
    
    /**
     * Carrega uma configuração a partir de um arquivo.
     * 
     * @param plugin Instância do plugin
     * @param file Arquivo a ser carregado
     * @param defaultFileName Nome do arquivo padrão (caso o arquivo não exista)
     * @return Configuração carregada
     */
    public static FileConfiguration loadConfiguration(JavaPlugin plugin, File file, String defaultFileName) {
        if (!file.exists() && defaultFileName != null) {
            plugin.saveResource(defaultFileName, false);
        }
        
        return YamlConfiguration.loadConfiguration(file);
    }
    
    /**
     * Salva uma configuração em um arquivo com suporte a UTF-8.
     * 
     * @param plugin Plugin que está salvando a configuração
     * @param config Configuração a ser salva
     * @param file Arquivo de destino
     */
    public static void saveConfiguration(JavaPlugin plugin, FileConfiguration config, File file) {
        try {
            config.save(file);
            plugin.getLogger().info("Arquivo " + file.getName() + " salvo com sucesso.");
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar arquivo " + file.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Processa uma string, aplicando codes de cores e garantindo que caracteres especiais
     * sejam exibidos corretamente.
     *
     * @param text Texto a ser processado
     * @return Texto processado com cores e codificação correta
     */
    public static String formatString(String text) {
        if (text == null) {
            return "";
        }
        
        String utf8Text = TextUtil.toUTF8(text);
        
        return ChatColor.translateAlternateColorCodes('&', utf8Text);
    }
    
    /**
     * Obtém uma mensagem formatada do arquivo de configuração.
     *
     * @param config Configuração
     * @param path Caminho da mensagem na configuração
     * @param defaultValue Valor padrão caso a mensagem não exista
     * @return Mensagem formatada
     */
    public static String getMessage(FileConfiguration config, String path, String defaultValue) {
        String message = config.getString(path, defaultValue);
        return formatString(message);
    }
    
    /**
     * Obtém uma string do arquivo de configuração e a converte para UTF-8.
     * 
     * @param config Configuração
     * @param path Caminho da string na configuração
     * @param defaultValue Valor padrão caso não exista
     * @return String em UTF-8
     */
    public static String getUTF8String(FileConfiguration config, String path, String defaultValue) {
        String value = config.getString(path, defaultValue);
        return TextUtil.toUTF8(value);
    }
}