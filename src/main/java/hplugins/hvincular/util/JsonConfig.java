package hplugins.hvincular.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Utilitário para manipulação de arquivos de configuração JSON.
 * Suporta nativamente caracteres Unicode, emojis e outros caracteres especiais.
 */
public class JsonConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping() // Importante para preservar emojis e caracteres Unicode
            .create();
            
    /**
     * Verifica se uma string é um JSON válido.
     * 
     * @param jsonString String que contém o suposto JSON
     * @return true se for um JSON válido, false caso contrário
     */
    public static boolean isValidJson(String jsonString) {
        try {
            GSON.fromJson(jsonString, JsonElement.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Carrega um arquivo JSON de configuração.
     *
     * @param plugin Instância do plugin
     * @param fileName Nome do arquivo (sem o caminho)
     * @return JsonObject contendo a configuração
     */
    public static JsonObject load(JavaPlugin plugin, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        
        if (!configFile.exists()) {
            if (plugin.getResource(fileName) != null) {
                plugin.saveResource(fileName, false);
            } else {
                try {
                    if (!configFile.getParentFile().exists()) {
                        configFile.getParentFile().mkdirs();
                    }
                    configFile.createNewFile();
                    try (Writer writer = FileUtils.createUtf8Writer(configFile)) {
                        writer.write("{}");
                    }
                } catch (IOException e) {
                    plugin.getLogger().severe("Erro ao criar arquivo JSON " + fileName + ": " + e.getMessage());
                    e.printStackTrace();
                    return new JsonObject();
                }
            }
        }
        
        try (Reader reader = FileUtils.createUtf8Reader(configFile)) {
            return GSON.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar arquivo JSON " + fileName + ": " + e.getMessage());
            
            if (plugin.getResource(fileName) != null) {
                try (Reader reader = new InputStreamReader(
                        plugin.getResource(fileName), StandardCharsets.UTF_8)) {
                    return GSON.fromJson(reader, JsonObject.class);
                } catch (Exception ex) {
                    plugin.getLogger().severe("Falha crítica ao tentar carregar JSON padrão: " + ex.getMessage());
                }
            }
            
            return new JsonObject();
        }
    }
    
    /**
     * Salva o objeto JSON no arquivo.
     *
     * @param plugin Instância do plugin
     * @param fileName Nome do arquivo (sem o caminho)
     * @param json Objeto JSON a ser salvo
     * @return true se o salvamento foi bem-sucedido, false caso contrário
     */
    public static boolean save(JavaPlugin plugin, String fileName, JsonElement json) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        
        try {
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            
            try (Writer writer = FileUtils.createUtf8Writer(configFile)) {
                GSON.toJson(json, writer);
                return true;
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar arquivo JSON " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtém uma string de um objeto JSON, com suporte a caminho de acesso usando pontos.
     * 
     * @param json Objeto JSON a ser consultado
     * @param path Caminho no formato "chave.subchave.propriedade"
     * @param defaultValue Valor padrão caso o caminho não exista
     * @return Valor encontrado ou o valor padrão
     */
    public static String getString(JsonObject json, String path, String defaultValue) {
        if (json == null || path == null) {
            return defaultValue;
        }
        
        String[] parts = path.split("\\.");
        JsonObject current = json;
        
        for (int i = 0; i < parts.length - 1; i++) {
            if (current.has(parts[i]) && current.get(parts[i]).isJsonObject()) {
                current = current.getAsJsonObject(parts[i]);
            } else {
                return defaultValue;
            }
        }
        
        String lastKey = parts[parts.length - 1];
        if (current.has(lastKey) && current.get(lastKey).isJsonPrimitive() && 
                current.get(lastKey).getAsJsonPrimitive().isString()) {
            return current.get(lastKey).getAsString();
        }
        
        return defaultValue;
    }
    
    /**
     * Obtém um valor booleano de um objeto JSON.
     * 
     * @param json Objeto JSON a ser consultado
     * @param path Caminho no formato "chave.subchave.propriedade"
     * @param defaultValue Valor padrão caso o caminho não exista
     * @return Valor encontrado ou o valor padrão
     */
    public static boolean getBoolean(JsonObject json, String path, boolean defaultValue) {
        if (json == null || path == null) {
            return defaultValue;
        }
        
        String[] parts = path.split("\\.");
        JsonObject current = json;
        
        for (int i = 0; i < parts.length - 1; i++) {
            if (current.has(parts[i]) && current.get(parts[i]).isJsonObject()) {
                current = current.getAsJsonObject(parts[i]);
            } else {
                return defaultValue;
            }
        }
        
        String lastKey = parts[parts.length - 1];
        if (current.has(lastKey) && current.get(lastKey).isJsonPrimitive() && 
                current.get(lastKey).getAsJsonPrimitive().isBoolean()) {
            return current.get(lastKey).getAsBoolean();
        }
        
        return defaultValue;
    }
    
    /**
     * Obtém um valor inteiro de um objeto JSON.
     * 
     * @param json Objeto JSON a ser consultado
     * @param path Caminho no formato "chave.subchave.propriedade"
     * @param defaultValue Valor padrão caso o caminho não exista
     * @return Valor encontrado ou o valor padrão
     */
    public static int getInt(JsonObject json, String path, int defaultValue) {
        if (json == null || path == null) {
            return defaultValue;
        }
        
        String[] parts = path.split("\\.");
        JsonObject current = json;
        
        for (int i = 0; i < parts.length - 1; i++) {
            if (current.has(parts[i]) && current.get(parts[i]).isJsonObject()) {
                current = current.getAsJsonObject(parts[i]);
            } else {
                return defaultValue;
            }
        }
        
        String lastKey = parts[parts.length - 1];
        if (current.has(lastKey) && current.get(lastKey).isJsonPrimitive() && 
                current.get(lastKey).getAsJsonPrimitive().isNumber()) {
            return current.get(lastKey).getAsInt();
        }
        
        return defaultValue;
    }
    
    /**
     * Verifica se um objeto JSON tem uma determinada propriedade.
     * 
     * @param json Objeto JSON a ser consultado
     * @param path Caminho no formato "chave.subchave.propriedade"
     * @return true se o caminho existir, false caso contrário
     */
    public static boolean hasPath(JsonObject json, String path) {
        if (json == null || path == null) {
            return false;
        }
        
        String[] parts = path.split("\\.");
        JsonObject current = json;
        
        for (int i = 0; i < parts.length - 1; i++) {
            if (current.has(parts[i]) && current.get(parts[i]).isJsonObject()) {
                current = current.getAsJsonObject(parts[i]);
            } else {
                return false;
            }
        }
        
        String lastKey = parts[parts.length - 1];
        return current.has(lastKey);
    }
    
    /**
     * Obtém um objeto JSON de um caminho aninhado.
     * 
     * @param json Objeto JSON a ser consultado
     * @param path Caminho no formato "chave.subchave.propriedade"
     * @return JsonObject encontrado ou null caso não exista
     */
    public static JsonObject getObject(JsonObject json, String path) {
        if (json == null || path == null) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        JsonObject current = json;
        
        for (int i = 0; i < parts.length - 1; i++) {
            if (current.has(parts[i]) && current.get(parts[i]).isJsonObject()) {
                current = current.getAsJsonObject(parts[i]);
            } else {
                return null;
            }
        }
        
        String lastKey = parts[parts.length - 1];
        if (current.has(lastKey) && current.get(lastKey).isJsonObject()) {
            return current.getAsJsonObject(lastKey);
        }
        
        return null;
    }
    
    /**
     * Converte uma configuração YAML do webhook para JSON.
     * Útil para migrar configurações existentes.
     * 
     * @param plugin Instância do plugin
     * @return true se a conversão foi bem-sucedida, false caso contrário
     */
    public static boolean convertYamlWebhookToJson(JavaPlugin plugin) {
        File yamlFile = new File(plugin.getDataFolder(), "webhook.yml");
        if (!yamlFile.exists()) {
            return false;
        }
        
        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(yamlFile);
        
        JsonObject json = new JsonObject();
        JsonObject mensagens = new JsonObject();
        
        JsonObject mensagensPadrao = new JsonObject();
        
        String tituloPadrao = TextUtil.processEmojiTokens(
            yamlConfig.getString("embed.title", ":jogo: Nova Tag Vinculada: {tag}"));
        mensagensPadrao.addProperty("titulo", tituloPadrao);
        
        String descricaoPadrao = TextUtil.processEmojiTokens(
            yamlConfig.getString("embed.description", 
            "### Informações do Jogador\n" +
            ":usuario: **Nickname:** `{player}`\n" +
            ":tag: **Tag Solicitada:** `{tag}`\n" +
            ":stats: **Inscritos no Canal:** `{subscribers}`\n\n" +
            "### Links para Verificação\n" +
            ":video: [Vídeo Utilizado para Verificação]({video_url})\n" +
            ":canal: [Canal do YouTube]({channel_url})\n\n" +
            ":atencao: **Importante:** Verifique se o vídeo pertence ao jogador e se o código de verificação está presente na descrição."));
        mensagensPadrao.addProperty("descricao", descricaoPadrao);
        
        String rodapePadrao = TextUtil.processEmojiTokens(
            yamlConfig.getString("embed.footer.text", "hVincular :info: Verificação em {datetime}"));
        mensagensPadrao.addProperty("rodape", rodapePadrao);
        
        String notificacaoPadrao = TextUtil.processEmojiTokens(
            yamlConfig.getString("message-content", 
            "{role_mention} Nova vinculação de **{tag}** detectada! A equipe precisa verificar."));
        mensagensPadrao.addProperty("notificacao", notificacaoPadrao);
        
        mensagens.add("padrao", mensagensPadrao);
        
        JsonObject mensagensSuspeito = new JsonObject();
        
        String tituloSuspeito = TextUtil.processEmojiTokens(
            yamlConfig.getString("suspicious-detection.embed.title", 
            ":atencao: POSSÍVEL FRAUDE - Tag Vinculada: {tag}"));
        mensagensSuspeito.addProperty("titulo", tituloSuspeito);
        
        String descricaoSuspeito = TextUtil.processEmojiTokens(
            yamlConfig.getString("suspicious-detection.embed.additional-description", 
            "\n\n:atencao: **ATENÇÃO: ESTA VINCULAÇÃO É SUSPEITA!**\n" +
            "O canal possui poucos inscritos e pode não atender aos requisitos mínimos.\n" +
            "Por favor, verifique com atenção antes de aprovar."));
        mensagensSuspeito.addProperty("descricao_adicional", descricaoSuspeito);
        
        String emojiAlerta = TextUtil.processEmojiTokens(
            yamlConfig.getString("suspicious-detection.flag-emoji", ":atencao:"));
        mensagensSuspeito.addProperty("emoji_alerta", emojiAlerta);
        
        String notificacaoSuspeito = TextUtil.processEmojiTokens(
            "{role_mention} {suspicious_role_mention} " + emojiAlerta + " Possível FRAUDE na vinculação de **{tag}**! Verificação urgente necessária.");
        mensagensSuspeito.addProperty("notificacao", notificacaoSuspeito);
        
        mensagens.add("suspeito", mensagensSuspeito);
        
        json.add("mensagens", mensagens);
        
        return save(plugin, "webhook.json", json);
    }
}