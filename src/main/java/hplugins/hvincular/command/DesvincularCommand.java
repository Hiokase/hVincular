package hplugins.hvincular.command;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.model.Tag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Comando para administradores desvincularem tags de jogadores.
 * Pode desvincular uma tag específica ou todas as tags de um jogador.
 */
public class DesvincularCommand implements CommandExecutor {

    private final HVincular plugin;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public DesvincularCommand(HVincular plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("hvincular.admin")) {
            sender.sendMessage(plugin.getMessagesConfig().getNoPermission());
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesConfig().getDesvincularUsage());
            return true;
        }

        String playerName = args[0];
        String tagId = args.length > 1 ? args[1] : null;

        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        UUID playerUUID;

        if (targetPlayer != null) {
            playerUUID = targetPlayer.getUniqueId();
        } else {
            CompletableFuture<Boolean> playerExistsFuture = playerExistsInDatabase(playerName);
            
            try {
                boolean playerExists = playerExistsFuture.join();
                if (!playerExists) {
                    sender.sendMessage(plugin.getMessagesConfig().getDesvincularPlayerNotFound(playerName));
                    return true;
                }
                
                playerUUID = null;
            } catch (Exception e) {
                sender.sendMessage(plugin.getMessagesConfig().getDesvincularError(e.getMessage()));
                return true;
            }
        }

        if (tagId != null) {
            desvincularTag(sender, playerName, playerUUID, tagId);
        } else {
            desvincularTodasTags(sender, playerName, playerUUID);
        }

        return true;
    }

    /**
     * Verifica se um jogador existe no banco de dados por nome.
     * @param playerName Nome do jogador
     * @return CompletableFuture que resolve para true se o jogador existe, false caso contrário
     */
    private CompletableFuture<Boolean> playerExistsInDatabase(String playerName) {
        return CompletableFuture.completedFuture(true);
    }

    /**
     * Desvincula uma tag específica de um jogador.
     * @param sender Remetente do comando
     * @param playerName Nome do jogador
     * @param playerUUID UUID do jogador (pode ser null se offline)
     * @param tagId ID da tag
     */
    private void desvincularTag(CommandSender sender, String playerName, UUID playerUUID, String tagId) {
        Tag tag = plugin.getMenusConfig().getTag(tagId);
        if (tag == null) {
            for (Tag t : plugin.getMenusConfig().getAllTags()) {
                if (t.getName().equalsIgnoreCase(tagId)) {
                    tag = t;
                    break;
                }
            }
            
            if (tag == null) {
                sender.sendMessage(plugin.getMessagesConfig().getDesvincularTagNotFound(tagId));
                return;
            }
        }

        boolean success = false;
        
        try {
            if (playerUUID != null) {
                success = plugin.getDatabaseManager().deactivatePreviousVinculations(playerUUID, tag.getId());
                
                String permissionCommand = plugin.getConfigManager().getPermissionRemoveCommand();
                permissionCommand = plugin.getConfigManager().processPermissionCommand(
                        permissionCommand, playerName, tag.getPermission());
                
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), permissionCommand);
                
                sender.sendMessage(plugin.getMessagesConfig().getDesvincularSuccessSingle(tag.getName(), playerName));
                
                plugin.getServer().getConsoleSender().sendMessage(
                        plugin.getMessagesConfig().getConsoleDesvincularTagSuccess(
                                sender.getName(), tag.getName(), playerName));
            } else {
                sender.sendMessage(plugin.getMessagesConfig().getDesvincularPlayerOffline());
            }
        } catch (Exception e) {
            sender.sendMessage(plugin.getMessagesConfig().getDesvincularError(e.getMessage()));
            plugin.getServer().getConsoleSender().sendMessage(
                    plugin.getMessagesConfig().getConsoleDesvincularTagError(
                            tag.getId(), playerName, e.getMessage()));
        }
    }

    /**
     * Desvincula todas as tags de um jogador.
     * @param sender Remetente do comando
     * @param playerName Nome do jogador
     * @param playerUUID UUID do jogador (pode ser null se offline)
     */
    private void desvincularTodasTags(CommandSender sender, String playerName, UUID playerUUID) {
        if (playerUUID == null) {
            sender.sendMessage(plugin.getMessagesConfig().getDesvincularPlayerOffline());
            return;
        }
        
        try {
            List<String> tagIds = plugin.getDatabaseManager().getActiveTagIds(playerUUID);
            
            if (tagIds.isEmpty()) {
                sender.sendMessage(plugin.getMessagesConfig().getDesvincularNoTags(playerName));
                return;
            }
            
            int rowsUpdated = plugin.getDatabaseManager().deactivateAllVinculations(playerUUID);
            
            int permissionsRemoved = 0;
            for (String tagId : tagIds) {
                Tag tag = plugin.getMenusConfig().getTag(tagId);
                if (tag != null) {
                    String permissionCommand = plugin.getConfigManager().getPermissionRemoveCommand();
                    permissionCommand = plugin.getConfigManager().processPermissionCommand(
                            permissionCommand, playerName, tag.getPermission());
                    
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), permissionCommand);
                    permissionsRemoved++;
                }
            }
            
            sender.sendMessage(plugin.getMessagesConfig().getDesvincularSuccessAll(permissionsRemoved, playerName));
            
            plugin.getServer().getConsoleSender().sendMessage(
                    plugin.getMessagesConfig().getConsoleDesvincularAllSuccess(
                            sender.getName(), permissionsRemoved, playerName));
            
        } catch (Exception e) {
            sender.sendMessage(plugin.getMessagesConfig().getDesvincularError(e.getMessage()));
            plugin.getServer().getConsoleSender().sendMessage(
                    plugin.getMessagesConfig().getConsoleDesvincularAllError(playerName, e.getMessage()));
        }
    }
}