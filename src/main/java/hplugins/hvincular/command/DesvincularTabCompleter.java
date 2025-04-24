package hplugins.hvincular.command;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.model.Tag;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * TabCompleter para o comando /hdesvincular.
 * Fornece sugestões de jogadores e tags.
 * Compatível com todas as versões do Minecraft (1.7+)
 */
public class DesvincularTabCompleter implements TabCompleter {

    private final HVincular plugin;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public DesvincularTabCompleter(HVincular plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("hvincular.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            String current = args[0].toLowerCase();
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(current)) {
                    players.add(player.getName());
                }
            }
            
            return players;
        }
        
        else if (args.length == 2) {
            List<String> tags = new ArrayList<>();
            String current = args[1].toLowerCase();
            
            for (Tag tag : plugin.getMenusConfig().getAllTags()) {
                if (tag.getId().toLowerCase().startsWith(current)) {
                    tags.add(tag.getId());
                } else if (tag.getName().toLowerCase().startsWith(current)) {
                    tags.add(tag.getName());
                }
            }
            
            return tags;
        }
        
        return new ArrayList<>();
    }
}