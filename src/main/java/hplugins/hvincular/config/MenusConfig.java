package hplugins.hvincular.config;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.model.Tag;
import hplugins.hvincular.util.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gerenciador responsável pelos menus e tags configuráveis do plugin.
 */
public class MenusConfig {

    private final HVincular plugin;
    private final File menusFile;
    private FileConfiguration menusConfig;
    
    private final Map<String, Tag> tags = new HashMap<>();
    private String menuTitle;
    private int menuSize;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public MenusConfig(HVincular plugin) {
        this.plugin = plugin;
        this.menusFile = new File(plugin.getDataFolder(), "menus.yml");
        createMenusFile();
    }

    /**
     * Cria o arquivo de menus caso não exista.
     */
    private void createMenusFile() {
        if (!menusFile.exists()) {
            plugin.saveResource("menus.yml", false);
        }
    }

    /**
     * Carrega ou recarrega os menus e tags do plugin.
     * Usa UTF-8 para garantir que caracteres especiais sejam lidos corretamente.
     */
    public void loadMenus() {
        menusConfig = ConfigUtil.loadConfiguration(plugin, menusFile, "menus.yml");
        tags.clear();
        
        menuTitle = menusConfig.getString("menu.title", "&8Vinculação de Tags");
        
        int menuRows = menusConfig.getInt("menu.size", 3);
        menuRows = Math.max(1, Math.min(6, menuRows));
        menuSize = menuRows * 9;

        ConfigurationSection tagsSection = menusConfig.getConfigurationSection("tags");
        if (tagsSection == null) {
            plugin.getLogger().warning("Nenhuma tag encontrada no arquivo de configuracao.");
            return;
        }
        
        for (String tagId : tagsSection.getKeys(false)) {
            ConfigurationSection tagSection = tagsSection.getConfigurationSection(tagId);
            if (tagSection == null) continue;
            
            String name = tagSection.getString("name", tagId);
            String permission = tagSection.getString("permission", "hvincular.tag." + tagId.toLowerCase());
            int minSubscribers = tagSection.getInt("min-subscribers", 100);
            int minViews = tagSection.getInt("min-views", 0);
            int cooldownSeconds = tagSection.getInt("cooldown-seconds", 86400);
            int menuSlot = tagSection.getInt("menu-slot", 0);
            
            String materialName = tagSection.getString("item.material", "GOLD_INGOT");
            int amount = tagSection.getInt("item.amount", 1);
            short durability = (short) tagSection.getInt("item.durability", 0);
            String displayName = tagSection.getString("item.display-name", "§6" + name);
            List<String> lore = tagSection.getStringList("item.lore");
            
            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                try {
                    material = Material.getMaterial(materialName);
                } catch (Exception e2) {
                    plugin.getLogger().warning("Material inválido para a tag " + tagId + ": " + materialName);
                    material = Material.GOLD_INGOT;
                }
            }
            
            ItemStack icon = new ItemStack(material, amount, durability);
            
            ConfigurationSection permissionsSection = tagSection.getConfigurationSection("permissions");
            String permissionAddCommand = null;
            String permissionRemoveCommand = null;
            
            if (permissionsSection != null) {
                permissionAddCommand = permissionsSection.getString("add-command");
                permissionRemoveCommand = permissionsSection.getString("remove-command");
            }
            
            ConfigurationSection rewardsSection = tagSection.getConfigurationSection("rewards");
            boolean rewardsEnabled = false;
            List<String> rewardCommands = new ArrayList<>();
            
            if (rewardsSection != null) {
                rewardsEnabled = rewardsSection.getBoolean("enabled", true);
                rewardCommands = rewardsSection.getStringList("commands");
            }
            
            Tag tag = new Tag(tagId, name, permission, minSubscribers, minViews, cooldownSeconds, 
                            menuSlot, icon, displayName, lore, permissionAddCommand, 
                            permissionRemoveCommand, rewardsEnabled, rewardCommands);
            
            tags.put(tagId, tag);
            
            plugin.getServer().getConsoleSender().sendMessage("§a[hVincular] §fTag carregada: " + tagId + " (Inscritos: " + minSubscribers + ", Views: " + minViews + ")");
        }
    }

    /**
     * Salva as configurações de menus no arquivo.
     */
    public void saveMenus() {
        ConfigUtil.saveConfiguration(plugin, menusConfig, menusFile);
    }

    /**
     * Retorna todas as tags configuradas.
     * @return Lista de tags
     */
    public List<Tag> getAllTags() {
        return new ArrayList<>(tags.values());
    }

    /**
     * Busca uma tag pelo ID.
     * @param tagId ID da tag
     * @return Tag encontrada ou null se não existir
     */
    public Tag getTag(String tagId) {
        return tags.get(tagId);
    }
    
    /**
     * Retorna o título do menu.
     * @return Título do menu
     */
    public String getMenuTitle() {
        return menuTitle;
    }
    
    /**
     * Retorna o tamanho do menu.
     * @return Tamanho do menu
     */
    public int getMenuSize() {
        return menuSize;
    }
    
    /**
     * Retorna a configuração de menus.
     * @return Configuração de menus
     */
    public FileConfiguration getMenusConfig() {
        return menusConfig;
    }
}