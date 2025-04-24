package hplugins.hvincular.integration;

import hplugins.hvincular.HVincular;
import org.bukkit.entity.Player;

/**
 * Gerenciador responsável pela execução de comandos de permissão.
 */
public class PermissionManager {

    private final HVincular plugin;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public PermissionManager(HVincular plugin) {
        this.plugin = plugin;
    }

    /**
     * Adiciona uma permissão a um jogador usando o comando configurado.
     * @param player Jogador
     * @param permission Permissão a ser adicionada
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean addPermission(Player player, String permission) {
        try {
            String command = plugin.getConfigManager().getPermissionAddCommand();
            
            String processedCommand = plugin.getConfigManager().processPermissionCommand(
                command, 
                player.getName(), 
                permission
            );
            
            plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                processedCommand
            );
            
            plugin.getLogger().info("§a[hVincular] §fPermissao adicionada para " + player.getName() + ": " + permission);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("§c[hVincular] §fErro ao adicionar permissao: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Adiciona uma permissão a um jogador usando o comando configurado para a tag específica.
     * @param player Jogador
     * @param tag Tag contendo configurações específicas
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean addPermission(Player player, hplugins.hvincular.model.Tag tag) {
        try {
            String command;
            if (tag.hasCustomPermissionAddCommand()) {
                command = tag.getPermissionAddCommand();
            } else {
                command = plugin.getConfigManager().getPermissionAddCommand();
            }
            
            String processedCommand = plugin.getConfigManager().processPermissionCommand(
                command, 
                player.getName(), 
                tag.getPermission()
            );
            
            plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                processedCommand
            );
            
            plugin.getLogger().info("§a[hVincular] §fPermissao adicionada para " + player.getName() + ": " + tag.getPermission() + " (Tag: " + tag.getName() + ")");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("§c[hVincular] §fErro ao adicionar permissao para tag " + tag.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove uma permissão de um jogador usando o comando configurado.
     * @param player Jogador
     * @param permission Permissão a ser removida
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean removePermission(Player player, String permission) {
        try {
            String command = plugin.getConfigManager().getPermissionRemoveCommand();
            
            String processedCommand = plugin.getConfigManager().processPermissionCommand(
                command, 
                player.getName(), 
                permission
            );
            
            plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                processedCommand
            );
            
            plugin.getLogger().info("§e[hVincular] §fPermissao removida de " + player.getName() + ": " + permission);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("§c[hVincular] §fErro ao remover permissao: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove uma permissão de um jogador usando o comando configurado para a tag específica.
     * @param player Jogador
     * @param tag Tag contendo configurações específicas
     * @return true se a operação foi bem-sucedida, false caso contrário
     */
    public boolean removePermission(Player player, hplugins.hvincular.model.Tag tag) {
        try {
            String command;
            if (tag.hasCustomPermissionRemoveCommand()) {
                command = tag.getPermissionRemoveCommand();
            } else {
                command = plugin.getConfigManager().getPermissionRemoveCommand();
            }
            
            String processedCommand = plugin.getConfigManager().processPermissionCommand(
                command, 
                player.getName(), 
                tag.getPermission()
            );
            
            plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                processedCommand
            );
            
            plugin.getLogger().info("§e[hVincular] §fPermissao removida de " + player.getName() + ": " + tag.getPermission() + " (Tag: " + tag.getName() + ")");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("§c[hVincular] §fErro ao remover permissao para tag " + tag.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se um jogador possui uma permissão.
     * @param player Jogador
     * @param permission Permissão a ser verificada
     * @return true se o jogador possui a permissão, false caso contrário
     */
    public boolean hasPermission(Player player, String permission) {
        plugin.getLogger().info("Verificando permissao: " + permission + " para jogador " + player.getName());
        
        boolean has = player.hasPermission(permission);
        plugin.getLogger().info("Resultado da verificacao de permissao para " + player.getName() + ": " + has);

        try {
            String command = "lp user " + player.getName() + " permission info " + permission;
            plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                command
            );
        } catch (Exception e) {
            plugin.getLogger().info("Nao foi possível verificar permissso via LuckPerms command: " + e.getMessage());
        }

        return has;
    }
}
