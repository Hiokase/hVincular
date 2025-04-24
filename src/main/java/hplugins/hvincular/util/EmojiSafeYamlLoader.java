package hplugins.hvincular.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitário para carregar arquivos de configuração YAML com suporte a tokens de emoji.
 * Este sistema usa tokens simples no formato :emoji_nome: que são compatíveis com todas as
 * versões do Minecraft e do Bukkit.
 * 
 * Os emojis reais são aplicados apenas no momento do envio para o Discord.
 */
public class EmojiSafeYamlLoader {
    
    private final JavaPlugin plugin;
    
    /**
     * Construtor do carregador de YAML.
     * 
     * @param plugin Instância do plugin que está carregando a configuração
     */
    public EmojiSafeYamlLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Carrega um arquivo YAML de forma segura.
     * 
     * @param configFile Arquivo de configuração a ser carregado
     * @param defaultResPath Caminho do recurso padrão (pode ser nulo)
     * @return FileConfiguration carregada
     */
    public FileConfiguration load(File configFile, String defaultResPath) {
        try {
            if (!configFile.exists()) {
                if (defaultResPath != null) {
                    plugin.saveResource(defaultResPath, false);
                } else {
                    configFile.getParentFile().mkdirs();
                    configFile.createNewFile();
                }
            }
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            
            return config;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar arquivo " + configFile.getName() + ": " + e.getMessage());
            
            return new YamlConfiguration();
        }
    }
    

    
    /**
     * Mapa de conversão de tokens de emoji para caracteres reais.
     * Este mapa é usado apenas quando os textos são enviados para o Discord.
     * 
     * @return Mapa com as conversões de tokens para emojis
     */
    public static Map<String, String> getEmojiTokenMap() {
        Map<String, String> emojiMap = new HashMap<>();
        
        emojiMap.put(":jogo:", "🎮");
        emojiMap.put(":video:", "📹");
        emojiMap.put(":usuario:", "👤");
        emojiMap.put(":tag:", "🏆");
        emojiMap.put(":canal:", "📺");
        emojiMap.put(":stats:", "📊");
        emojiMap.put(":atencao:", "⚠️");
        emojiMap.put(":verificado:", "✅");
        emojiMap.put(":negado:", "❌");
        emojiMap.put(":mensagem:", "💬");
        emojiMap.put(":data:", "📅");
        emojiMap.put(":link:", "🔗");
        emojiMap.put(":hora:", "⏰");
        emojiMap.put(":youtube:", "▶️");
        emojiMap.put(":discord:", "🔷");
        emojiMap.put(":info:", "ℹ️");
        emojiMap.put(":estrela:", "⭐");
        emojiMap.put(":alerta:", "🚨");
        emojiMap.put(":ok:", "👍");
        emojiMap.put(":nok:", "👎");
        
        
        return emojiMap;
    }
    
    /**
     * Converte tokens de emoji em um texto para os emojis reais.
     * Use este método apenas quando estiver pronto para enviar o texto para o Discord.
     * 
     * @param text Texto com tokens de emoji (:nome_emoji:)
     * @return Texto com emojis reais
     */
    public static String convertTokensToEmojis(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String result = text;
        Map<String, String> emojiMap = getEmojiTokenMap();
        
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        
        return result;
    }
}