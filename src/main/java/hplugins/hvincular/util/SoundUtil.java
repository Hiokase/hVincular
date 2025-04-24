package hplugins.hvincular.util;

import com.cryptomorin.xseries.XSound;
import hplugins.hvincular.HVincular;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilitário para reprodução de sons com compatibilidade entre versões.
 * Utiliza a biblioteca XSeries para garantir compatibilidade com todas as versões do Minecraft.
 */
public class SoundUtil {

    private static HVincular plugin;
    private static final Map<SoundType, XSound> configuredSounds = new HashMap<>();
    private static boolean initialized = false;

    /**
     * Inicializa a utilidade de sons com a instância do plugin.
     * @param pluginInstance Instância do plugin
     */
    public static void initialize(HVincular pluginInstance) {
        plugin = pluginInstance;
        loadSoundsFromConfig();
        initialized = true;
    }

    /**
     * Carrega os sons configurados do arquivo de configuração
     */
    public static void loadSoundsFromConfig() {
        configuredSounds.clear();
        
        configuredSounds.put(SoundType.CLICK, XSound.UI_BUTTON_CLICK);
        configuredSounds.put(SoundType.SUCCESS, XSound.ENTITY_PLAYER_LEVELUP);
        configuredSounds.put(SoundType.ERROR, XSound.ENTITY_VILLAGER_NO);
        configuredSounds.put(SoundType.MENU_OPEN, XSound.BLOCK_CHEST_OPEN);
        configuredSounds.put(SoundType.MENU_CLOSE, XSound.BLOCK_CHEST_CLOSE);
        configuredSounds.put(SoundType.NOTIFICATION, XSound.ENTITY_EXPERIENCE_ORB_PICKUP);
        
        if (plugin != null) {
            ConfigurationSection soundsSection = plugin.getMenusConfig().getMenusConfig().getConfigurationSection("menu.sounds");
            if (soundsSection != null) {
                for (SoundType soundType : SoundType.values()) {
                    String soundName = soundsSection.getString(soundType.name().toLowerCase());
                    if (soundName != null && !soundName.isEmpty()) {
                        try {
                            XSound xSound = XSound.matchXSound(soundName).orElse(null);
                            if (xSound != null) {
                                configuredSounds.put(soundType, xSound);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("§c[hVincular] §fErro ao carregar som " + soundType.name() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Reproduz um som para um jogador específico.
     * 
     * @param player O jogador que ouvirá o som
     * @param sound O tipo de som a ser reproduzido
     * @param volume O volume do som (padrão: 1.0)
     * @param pitch O pitch do som (padrão: 1.0)
     */
    public static void playSound(Player player, SoundType sound, float volume, float pitch) {
        if (player == null || !player.isOnline()) return;
        
        if (!initialized && plugin != null) {
            initialize(plugin);
        }
        
        XSound xSound = configuredSounds.getOrDefault(sound, getDefaultSound(sound));
        if (xSound != null) {
            xSound.play(player, volume, pitch);
        }
    }
    
    /**
     * Reproduz um som para um jogador específico com volume e pitch padrão.
     * 
     * @param player O jogador que ouvirá o som
     * @param sound O tipo de som a ser reproduzido
     */
    public static void playSound(Player player, SoundType sound) {
        playSound(player, sound, 1.0f, 1.0f);
    }
    
    /**
     * Reproduz um som em uma localização específica.
     * 
     * @param location A localização onde o som será reproduzido
     * @param sound O tipo de som a ser reproduzido
     * @param volume O volume do som (padrão: 1.0)
     * @param pitch O pitch do som (padrão: 1.0)
     */
    public static void playSoundAt(Location location, SoundType sound, float volume, float pitch) {
        if (location == null || location.getWorld() == null) return;
        
        if (!initialized && plugin != null) {
            initialize(plugin);
        }
        
        XSound xSound = configuredSounds.getOrDefault(sound, getDefaultSound(sound));
        if (xSound != null) {
            xSound.play(location, volume, pitch);
        }
    }
    
    /**
     * Reproduz um som em uma localização específica com volume e pitch padrão.
     * 
     * @param location A localização onde o som será reproduzido
     * @param sound O tipo de som a ser reproduzido
     */
    public static void playSoundAt(Location location, SoundType sound) {
        playSoundAt(location, sound, 1.0f, 1.0f);
    }
    
    /**
     * Obtém o som padrão para um tipo específico.
     * 
     * @param soundType O tipo de som
     * @return O XSound padrão correspondente
     */
    private static XSound getDefaultSound(SoundType soundType) {
        switch (soundType) {
            case CLICK:
                return XSound.UI_BUTTON_CLICK;
            case SUCCESS:
                return XSound.ENTITY_PLAYER_LEVELUP;
            case ERROR:
                return XSound.ENTITY_VILLAGER_NO;
            case MENU_OPEN:
                return XSound.BLOCK_CHEST_OPEN;
            case MENU_CLOSE:
                return XSound.BLOCK_CHEST_CLOSE;
            case NOTIFICATION:
                return XSound.ENTITY_EXPERIENCE_ORB_PICKUP;
            default:
                return null;
        }
    }
    
    /**
     * Tipos de sons utilizados no plugin.
     */
    public enum SoundType {
        CLICK,
        SUCCESS,
        ERROR,
        MENU_OPEN,
        MENU_CLOSE,
        NOTIFICATION
    }
}