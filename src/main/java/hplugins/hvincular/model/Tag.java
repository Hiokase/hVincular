package hplugins.hvincular.model;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de dados para representar uma tag configurável.
 */
public class Tag {

    private final String id;
    private final String name;
    private final String permission;
    private final int minSubscribers;
    private final int minViews;
    private final int cooldownSeconds;
    private final int menuSlot;
    private final ItemStack icon;
    private final String displayName;
    private final List<String> lore;
    
    private final String permissionAddCommand;
    private final String permissionRemoveCommand;
    private final boolean rewardsEnabled;
    private final List<String> rewardCommands;

    /**
     * Construtor da classe.
     * @param id ID único da tag
     * @param name Nome da tag
     * @param permission Permissão associada à tag
     * @param minSubscribers Número mínimo de inscritos necessários
     * @param minViews Número mínimo de visualizações necessárias
     * @param cooldownSeconds Tempo de cooldown em segundos para esta tag
     * @param menuSlot Posição no menu
     * @param icon Ícone usado no menu
     * @param displayName Nome de exibição no menu
     * @param lore Descrição no menu
     * @param permissionAddCommand Comando personalizado para adicionar permissão (opcional)
     * @param permissionRemoveCommand Comando personalizado para remover permissão (opcional)
     * @param rewardsEnabled Flag para indicar se as recompensas estão habilitadas
     * @param rewardCommands Lista de comandos de recompensa específicos para esta tag
     */
    public Tag(String id, String name, String permission, int minSubscribers, int minViews, 
              int cooldownSeconds, int menuSlot, ItemStack icon, String displayName, List<String> lore,
              String permissionAddCommand, String permissionRemoveCommand, boolean rewardsEnabled, List<String> rewardCommands) {
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.minSubscribers = minSubscribers;
        this.minViews = minViews;
        this.cooldownSeconds = cooldownSeconds;
        this.menuSlot = menuSlot;
        this.icon = icon;
        this.displayName = displayName;
        this.lore = lore;
        this.permissionAddCommand = permissionAddCommand;
        this.permissionRemoveCommand = permissionRemoveCommand;
        this.rewardsEnabled = rewardsEnabled;
        this.rewardCommands = rewardCommands != null ? rewardCommands : new ArrayList<>();
    }
    
    /**
     * Construtor simplificado para compatibilidade com código legado.
     */
    public Tag(String id, String name, String permission, int minSubscribers, int minViews, 
              int cooldownSeconds, int menuSlot, ItemStack icon, String displayName, List<String> lore) {
        this(id, name, permission, minSubscribers, minViews, cooldownSeconds, menuSlot, icon, displayName, lore, 
             null, null, false, new ArrayList<>());
    }

    /**
     * Retorna o ID da tag.
     * @return ID da tag
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna o nome da tag.
     * @return Nome da tag
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna a permissão associada à tag.
     * @return Permissão
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Retorna o número mínimo de inscritos necessários.
     * @return Número mínimo de inscritos
     */
    public int getMinSubscribers() {
        return minSubscribers;
    }
    
    /**
     * Retorna o número mínimo de visualizações necessárias.
     * @return Número mínimo de visualizações
     */
    public int getMinViews() {
        return minViews;
    }
    
    /**
     * Retorna o tempo de cooldown em segundos para esta tag.
     * @return Tempo de cooldown em segundos
     */
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    /**
     * Retorna a posição no menu.
     * @return Slot do menu
     */
    public int getMenuSlot() {
        return menuSlot;
    }

    /**
     * Retorna o ícone usado no menu.
     * @return ItemStack do ícone
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * Retorna o nome de exibição no menu.
     * @return Nome de exibição
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Retorna a descrição no menu.
     * @return Lista de linhas da descrição
     */
    public List<String> getLore() {
        return lore;
    }
    
    /**
     * Verifica se a tag possui um comando personalizado para adicionar permissão.
     * @return true se existir um comando personalizado, false caso contrário
     */
    public boolean hasCustomPermissionAddCommand() {
        return permissionAddCommand != null && !permissionAddCommand.isEmpty();
    }
    
    /**
     * Retorna o comando personalizado para adicionar permissão.
     * @return Comando personalizado ou null se não estiver definido
     */
    public String getPermissionAddCommand() {
        return permissionAddCommand;
    }
    
    /**
     * Verifica se a tag possui um comando personalizado para remover permissão.
     * @return true se existir um comando personalizado, false caso contrário
     */
    public boolean hasCustomPermissionRemoveCommand() {
        return permissionRemoveCommand != null && !permissionRemoveCommand.isEmpty();
    }
    
    /**
     * Retorna o comando personalizado para remover permissão.
     * @return Comando personalizado ou null se não estiver definido
     */
    public String getPermissionRemoveCommand() {
        return permissionRemoveCommand;
    }
    
    /**
     * Verifica se as recompensas estão habilitadas para esta tag.
     * @return true se as recompensas estiverem habilitadas, false caso contrário
     */
    public boolean isRewardsEnabled() {
        return rewardsEnabled;
    }
    
    /**
     * Retorna a lista de comandos de recompensa específicos para esta tag.
     * @return Lista de comandos de recompensa
     */
    public List<String> getRewardCommands() {
        return rewardCommands;
    }
}
