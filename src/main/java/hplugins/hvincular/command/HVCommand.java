package hplugins.hvincular.command;

import hplugins.hvincular.HVincular;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Comando administrativo do plugin hVincular.
 * Permite recarregar configurações e ver a ajuda.
 * Compatível com todas as versões do Minecraft (1.7+)
 */
public class HVCommand implements CommandExecutor {

    private final HVincular plugin;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public HVCommand(HVincular plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("hvincular.admin")) {
            sender.sendMessage(plugin.getMessagesConfig().getNoPermission());
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCmd = args[0].toLowerCase();
        if ("reload".equals(subCmd)) {
            reloadPlugin(sender);
        } else if ("help".equals(subCmd)) {
            showHelp(sender);
        } else {
            sender.sendMessage(plugin.getMessagesConfig().getCommandUnknown());
        }

        return true;
    }

    /**
     * Recarrega as configurações do plugin.
     * @param sender Remetente do comando
     */
    private void reloadPlugin(CommandSender sender) {
        try {
            if (plugin.getDatabaseManager() != null) {
                plugin.getDatabaseManager().saveAll();
            }
            
            plugin.loadConfigurations();
            
            sender.sendMessage(plugin.getMessagesConfig().getReloadSuccess());
            
            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.getServer().getConsoleSender().sendMessage(
                        plugin.getMessagesConfig().getReloadConsoleByPlayer(player.getName()));
            } else {
                plugin.getServer().getConsoleSender().sendMessage(
                        plugin.getMessagesConfig().getReloadConsoleByConsole());
            }
            
            plugin.getServer().getConsoleSender().sendMessage(
                    plugin.getMessagesConfig().getReloadReloadingSounds());
        } catch (Exception e) {
            sender.sendMessage(plugin.getMessagesConfig().getReloadError());
            plugin.getServer().getConsoleSender().sendMessage(
                    plugin.getMessagesConfig().getReloadErrorConsole(e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * Mostra a ajuda do plugin.
     * @param sender Remetente do comando
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessagesConfig().getHelpHeader());
        sender.sendMessage(plugin.getMessagesConfig().getHelpVincular());
        sender.sendMessage(plugin.getMessagesConfig().getHelpVincularUrl());
        sender.sendMessage(plugin.getMessagesConfig().getHelpHvHelp());
        sender.sendMessage(plugin.getMessagesConfig().getHelpHvReload());
        
        sender.sendMessage(plugin.getMessagesConfig().getHelpFooter(plugin.getDescription().getVersion()));
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "            " + ChatColor.GOLD + "* " + 
                          ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "hVincular " + ChatColor.GOLD + "* " +
                          ChatColor.GRAY + "- " + 
                          ChatColor.GOLD + "YouTube Verification System");
        sender.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "        * " + ChatColor.GOLD + "Desenvolvido com " + 
                          ChatColor.RED + "" + ChatColor.BOLD + "amor " + ChatColor.GOLD + "por " +
                          ChatColor.AQUA + "" + ChatColor.BOLD + "Hokase" + ChatColor.GOLD + "" + ChatColor.BOLD + " *");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_GRAY + "     +----------------------------------------+");
        sender.sendMessage(ChatColor.DARK_GRAY + "     |" + ChatColor.YELLOW + "  GitHub: " + ChatColor.WHITE + "https://github.com/Hiokase" + ChatColor.DARK_GRAY + "    |");
        sender.sendMessage(ChatColor.DARK_GRAY + "     |" + ChatColor.RED + "  YouTube: " + ChatColor.WHITE + "youtube.com/@SoOHokase" + ChatColor.DARK_GRAY + "   |");
        sender.sendMessage(ChatColor.DARK_GRAY + "     |" + ChatColor.BLUE + "  Discord: " + ChatColor.WHITE + "hokase" + ChatColor.DARK_GRAY + "                  |");
        sender.sendMessage(ChatColor.DARK_GRAY + "     +----------------------------------------+");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "          Obrigado por usar o hVincular!");
        sender.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        sender.sendMessage("");
    }
}