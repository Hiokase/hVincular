package hplugins.hvincular.command;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.util.SoundUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Comando principal do plugin que abre o menu de vinculação de tags.
 */
public class VincularCommand implements CommandExecutor {

    private final HVincular plugin;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public VincularCommand(HVincular plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesConfig().getPlayerOnlyCommand());
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("hvincular.use")) {
            player.sendMessage(plugin.getMessagesConfig().getNoPermission());
            return true;
        }
        
        if (args.length > 0) {
            String url = args[0];
            
            if (plugin.getChatInputManager().isWaitingForInput(player)) {
                plugin.getChatInputManager().processMessage(player, url);
                return true;
            } 
            else if (plugin.getMenuManager().hasPendingVerification(player)) {
                String tagId = plugin.getMenuManager().getPendingTagId(player);
                if (url.contains("youtube.com/watch?v=") || url.contains("youtu.be/")) {
                    plugin.getMenuManager().setPendingVideoUrl(player, url);
                    plugin.getYouTubeVerifier().verifyVideo(player, url, tagId);
                    return true;
                } else {
                    player.sendMessage(plugin.getMessagesConfig().getInvalidLink());
                    return true;
                }
            } 
            else {
                if (url.contains("youtube.com/watch?v=") || url.contains("youtu.be/")) {
                    player.sendMessage(plugin.getMessagesConfig().getSelectTagFirst());
                    
                    SoundUtil.playSound(player, SoundUtil.SoundType.MENU_OPEN);
                    plugin.getMenuManager().openTagSelectionMenu(player);
                    return true;
                }
            }
        }

        SoundUtil.playSound(player, SoundUtil.SoundType.MENU_OPEN);
        plugin.getMenuManager().openTagSelectionMenu(player);
        return true;
    }
}
