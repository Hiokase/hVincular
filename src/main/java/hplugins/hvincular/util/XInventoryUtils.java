package hplugins.hvincular.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utilitário para operações com inventários com compatibilidade entre versões.
 * Esta classe lida com as diferenças de implementação entre as versões do Minecraft 1.7 até 1.21.
 */
public class XInventoryUtils {

    /**
     * Obtém o título do inventário de forma compatível com todas as versões do Minecraft.
     * 
     * @param player Jogador que tem o inventário aberto
     * @param inventory Inventário que está sendo visualizado
     * @return O título do inventário, ou null se não for possível obter
     */
    public static String getInventoryTitle(Player player, Inventory inventory) {
        if (player == null || inventory == null) {
            return null;
        }
        
        try {
            Method getTitleMethod = inventory.getClass().getMethod("getTitle");
            return (String) getTitleMethod.invoke(inventory);
        } catch (Exception ignored) {
        }
        
        try {
            String bukkitVersion = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            int minorVersion = Integer.parseInt(bukkitVersion.split("_")[1]);
            
            if (minorVersion >= 9) {
                InventoryView view = player.getOpenInventory();
                return view.getTitle();
            }
        } catch (Exception ignored) {
        }
        
        try {
            Object inventoryView = player.getOpenInventory();
            Method getTitleMethod = inventoryView.getClass().getMethod("getTitle");
            return (String) getTitleMethod.invoke(inventoryView);
        } catch (Exception ignored) {
        }
        
        try {
            Object topInventory = player.getOpenInventory().getTopInventory();
            
            try {
                Method getTitleMethod = topInventory.getClass().getMethod("getTitle");
                return (String) getTitleMethod.invoke(topInventory);
            } catch (Exception ignored) {
                Field titleField = getFieldRecursively(topInventory.getClass());
                if (titleField != null) {
                    titleField.setAccessible(true);
                    return (String) titleField.get(topInventory);
                }
            }
        } catch (Exception ignored) {
            return inventory.getType().name();
        }
        
        return inventory.getType().name();
    }
    
    /**
     * Busca um campo em uma classe, incluindo em suas superclasses.
     *
     * @param clazz Classe onde o campo deve ser buscado
     * @return O Field encontrado ou null se não existe
     */
    private static Field getFieldRecursively(Class<?> clazz) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField("title");
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }
    
    /**
     * Verifica se dois títulos de inventário são equivalentes, considerando
     * possíveis diferenças de formatação de cores (§ vs &).
     * 
     * @param title1 Primeiro título
     * @param title2 Segundo título
     * @return true se os títulos forem equivalentes
     */
    public static boolean areInventoryTitlesEqual(String title1, String title2) {
        if (title1 == null || title2 == null) {
            return false;
        }
        
        if (title1.equals(title2)) {
            return true;
        }
        
        String normalizedTitle1 = ColorUtils.translate(title1);
        String normalizedTitle2 = ColorUtils.translate(title2);
        
        return normalizedTitle1.equals(normalizedTitle2);
    }
    
    /**
     * Verifica se um título de inventário contém outro, considerando
     * possíveis diferenças de formatação de cores.
     * 
     * @param title O título completo do inventário
     * @param substring A substring a ser procurada
     * @return true se o título contém a substring
     */
    public static boolean inventoryTitleContains(String title, String substring) {
        if (title == null || substring == null) {
            return false;
        }
        
        if (title.contains(substring)) {
            return true;
        }
        
        String normalizedTitle = ColorUtils.translate(title);
        String normalizedSubstring = ColorUtils.translate(substring);
        
        return normalizedTitle.contains(normalizedSubstring);
    }
}