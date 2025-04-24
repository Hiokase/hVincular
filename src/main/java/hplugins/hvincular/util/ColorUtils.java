package hplugins.hvincular.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitário para manipulação de cores em strings.
 */
public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    private static final Pattern COLOR_PATTERN = Pattern.compile("§[0-9a-fk-orA-FK-OR]");
    
    /**
     * Traduz códigos de cor para suas representações no Minecraft.
     * @param text Texto com códigos de cor
     * @return Texto formatado com cores
     */
    public static String translate(String text) {
        if (text == null) return "";
        
        try {
            Matcher matcher = HEX_PATTERN.matcher(text);
            StringBuffer buffer = new StringBuffer();
            
            while (matcher.find()) {
                String hexColor = matcher.group(1);
                try {
                    Class.forName("net.md_5.bungee.api.ChatColor")
                            .getMethod("of", String.class)
                            .invoke(null, "#" + hexColor);
                            
                    matcher.appendReplacement(buffer, "§x§" + String.join("§", hexColor.split("")));
                } catch (Exception e) {
                    matcher.appendReplacement(buffer, "");
                }
            }
            
            matcher.appendTail(buffer);
            text = buffer.toString();
        } catch (Exception e) {
        }
        
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    /**
     * Traduz códigos de cor em uma lista de strings.
     * @param list Lista de strings com códigos de cor
     * @return Lista formatada com cores
     */
    public static List<String> translate(List<String> list) {
        List<String> translated = new ArrayList<>();
        
        for (String line : list) {
            translated.add(translate(line));
        }
        
        return translated;
    }
    
    /**
     * Remove todos os códigos de cor de uma string.
     * Útil para mensagens de console que não suportam cores.
     * @param text Texto com códigos de cor
     * @return Texto sem códigos de cor
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        
        text = HEX_PATTERN.matcher(text).replaceAll("");
        
        text = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text));
        
        text = COLOR_PATTERN.matcher(text).replaceAll("");
        
        return text;
    }
    
    /**
     * Formata uma mensagem de log para o console do servidor.
     * Adiciona prefixo e converte para cores, ou remove cores se necessário.
     * @param message Mensagem para exibir
     * @param usePrefix Se deve adicionar o prefixo do plugin
     * @param supportsColor Se o console suporta cores
     * @return Mensagem formatada para o console
     */
    public static String formatLogMessage(String message, boolean usePrefix, boolean supportsColor) {
        if (message == null) return "";
        
        String prefix = usePrefix ? "§8[§6hVincular§8] " : "";
        String formattedMessage = prefix + message;
        
        if (supportsColor) {
            return translate(formattedMessage);
        } else {
            return stripColor(formattedMessage);
        }
    }
    
    /**
     * Formata uma mensagem para exibição no console, usando prefixo.
     * @param message Mensagem para exibir
     * @return Mensagem formatada para o console
     */
    public static String formatConsole(String message) {
        return formatLogMessage(message, true, false);
    }
}
