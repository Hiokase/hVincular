package hplugins.hvincular.integration.youtube;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hplugins.hvincular.HVincular;
import hplugins.hvincular.util.CacheManager;
import hplugins.hvincular.util.LoggerUtil;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API para interação com o YouTube.
 */
public class YouTubeAPI {

    private final String apiKey;
    private final Pattern VIDEO_ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com\\/(?:[^\\/]+\\/.+\\/|(?:v|e(?:mbed)?)\\/|.*[?&]v=)|youtu\\.be\\/)([^\"&?\\/ ]{11})");
    
    private static final Gson GSON = new GsonBuilder().create();
    
    private final CacheManager<String, JsonObject> videoCache = new CacheManager<>(600);
    private final CacheManager<String, JsonObject> channelCache = new CacheManager<>(600);

    /**
     * Construtor da classe.
     * @param apiKey Chave da API do YouTube
     */
    public YouTubeAPI(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * Testa a conexão com a API do YouTube.
     * @return CompletableFuture com true se a conexão for bem-sucedida, false caso contrário
     */
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = "https://www.googleapis.com/youtube/v3/videos?part=snippet&chart=mostPopular&maxResults=1&key=" + apiKey;
                JsonObject response = makeApiRequest(url);
                
                return response != null && response.has("items");
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Extrai o ID do vídeo de um link do YouTube.
     * @param youtubeUrl Link do YouTube
     * @return ID do vídeo ou null se inválido
     */
    public String extractVideoId(String youtubeUrl) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(youtubeUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Obtém informações sobre um vídeo do YouTube de forma assíncrona.
     * Utiliza cache para reduzir chamadas à API.
     * 
     * @param videoId ID do vídeo
     * @return CompletableFuture com JsonObject contendo as informações do vídeo
     */
    public CompletableFuture<JsonObject> getVideoInfo(String videoId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject cachedData = videoCache.get(videoId);
                if (cachedData != null) {
                    LoggerUtil.debug("Usando informações em cache para o vídeo: " + videoId);
                    return cachedData;
                }
                
                String url = "https://www.googleapis.com/youtube/v3/videos?id=" + videoId 
                        + "&part=snippet,statistics&key=" + apiKey;
                
                JsonObject data = makeApiRequest(url);
                
                if (data != null && data.has("items")) {
                    videoCache.put(videoId, data);
                    LoggerUtil.debug("Armazenando informações do vídeo em cache: " + videoId);
                }
                
                return data;
            } catch (Exception e) {
                LoggerUtil.error("Erro ao obter informações do vídeo: " + videoId, e);
                throw new RuntimeException("Erro ao obter informações do vídeo: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Obtém informações sobre um canal do YouTube de forma assíncrona.
     * Utiliza cache para reduzir chamadas à API.
     * 
     * @param channelId ID do canal
     * @return CompletableFuture com JsonObject contendo as informações do canal
     */
    public CompletableFuture<JsonObject> getChannelInfo(String channelId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject cachedData = channelCache.get(channelId);
                if (cachedData != null) {
                    LoggerUtil.debug("Usando informações em cache para o canal: " + channelId);
                    return cachedData;
                }
                
                String url = "https://www.googleapis.com/youtube/v3/channels?id=" + channelId 
                        + "&part=statistics&key=" + apiKey;
                
                JsonObject data = makeApiRequest(url);
                
                if (data != null && data.has("items")) {
                    channelCache.put(channelId, data);
                    LoggerUtil.debug("Armazenando informações do canal em cache: " + channelId);
                }
                
                return data;
            } catch (Exception e) {
                LoggerUtil.error("Erro ao obter informações do canal: " + channelId, e);
                throw new RuntimeException("Erro ao obter informações do canal: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Faz uma requisição para a API do YouTube.
     * @param urlString URL da requisição
     * @return JsonObject com a resposta
     * @throws IOException se ocorrer um erro na requisição
     */
    private JsonObject makeApiRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        
        if (connection.getResponseCode() != 200) {
            throw new IOException("Falha na requisição HTTP. Código: " + connection.getResponseCode());
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            try {
                return GSON.fromJson(response.toString(), JsonObject.class);
            } catch (NoSuchMethodError e) {
                JsonParser parser = new JsonParser();
                JsonElement jsonElement = parser.parse(response.toString());
                return jsonElement.getAsJsonObject();
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Verifica se uma descrição contém um dos IPs especificados.
     * @param description Descrição a ser verificada
     * @param serverIps Lista de IPs a serem procurados
     * @return true se a descrição contiver pelo menos um dos IPs, false caso contrário
     */
    public boolean containsServerIp(String description, Iterable<String> serverIps) {
        if (description == null || serverIps == null) {
            return false;
        }
        
        
        description = description.toLowerCase();
        
        for (String ip : serverIps) {
            String ipLower = ip.toLowerCase();
            String censoredIp = censorIp(ipLower);
            
            if (description.contains(ipLower)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Censura parcialmente um IP para exibição nos logs.
     * @param ip IP a ser censurado
     * @return IP parcialmente censurado
     */
    private String censorIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "";
        }
        
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                return parts[0] + ".***.***";
            }
        }
        
        if (ip.length() > 4) {
            return ip.substring(0, 3) + "***";
        }
        
        return "***";
    }
    
    /**
     * Verifica se o código de verificação está presente na descrição do vídeo.
     * 
     * @param description Descrição do vídeo
     * @param verificationCode Código de verificação para buscar na descrição
     * @return true se o código estiver presente, false caso contrário
     */
    public boolean containsVerificationCode(String description, String verificationCode) {
        if (description == null || description.isEmpty() || verificationCode == null || verificationCode.isEmpty()) {
            return false;
        }
        
        description = description.toLowerCase();
        verificationCode = verificationCode.toLowerCase();
        
        return description.contains(verificationCode);
    }
    
    /**
     * Limpa o cache de vídeos para um ID específico.
     * Útil quando você precisa obter dados atualizados para um vídeo específico.
     * 
     * @param videoId ID do vídeo a ser removido do cache
     */
    public void clearVideoCache(String videoId) {
        if (videoId != null && !videoId.isEmpty()) {
            videoCache.remove(videoId);
            LoggerUtil.debug("Cache removido para o vídeo: " + videoId);
        }
    }
    
    /**
     * Limpa o cache de canais para um ID específico.
     * Útil quando você precisa obter dados atualizados para um canal específico.
     * 
     * @param channelId ID do canal a ser removido do cache
     */
    public void clearChannelCache(String channelId) {
        if (channelId != null && !channelId.isEmpty()) {
            channelCache.remove(channelId);
            LoggerUtil.debug("Cache removido para o canal: " + channelId);
        }
    }
    
    /**
     * Limpa completamente todos os caches (vídeos e canais).
     * Útil ao reiniciar o plugin ou quando há alterações significativas na API.
     */
    public void clearAllCaches() {
        videoCache.clear();
        channelCache.clear();
        LoggerUtil.debug("Todos os caches da API do YouTube foram limpos");
    }
    
    /**
     * Remove entradas expiradas dos caches para liberar memória.
     * Recomendado chamar periodicamente para manter o uso de memória sob controle.
     * Execução silenciosa para evitar flood no console.
     */
    public void cleanExpiredCaches() {
        videoCache.cleanExpired();
        channelCache.cleanExpired();
    }
}
