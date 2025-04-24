package hplugins.hvincular.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TabCompleter para o comando /hv.
 * Fornece sugestões de subcomandos quando o usuário escreve o comando.
 * Compatível com todas as versões do Minecraft (1.7+)
 */
public class HVTabCompleter implements TabCompleter {

    private final List<String> subCommands = Arrays.asList("help", "reload");
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("hvincular.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String current = args[0].toLowerCase();
            
            for (String subCmd : subCommands) {
                if (subCmd.toLowerCase().startsWith(current)) {
                    completions.add(subCmd);
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}