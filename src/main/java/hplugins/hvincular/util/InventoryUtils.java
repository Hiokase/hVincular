package hplugins.hvincular.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Utilitário para compatibilidade de inventários entre diferentes versões do Minecraft.
 * Esta classe oferece métodos para obter informações de inventário de forma consistente
 * nas versões 1.7 até 1.21 do Minecraft.
 */
public class InventoryUtils {

    private static Logger logger;

    /**
     * Define o logger para mensagens de depuração.
     * @param logger Logger a ser usado
     */
    public static void setLogger(Logger logger) {
        InventoryUtils.logger = logger;
    }

    /**
     * Obtém o título de um inventário de forma compatível com todas as versões.
     * @param player Jogador que tem o inventário aberto
     * @param inventory Inventário cujo título se deseja obter
     * @return Título do inventário ou null se não for possível obter
     */
    public static String getInventoryTitle(Player player, Inventory inventory) {
        if (player == null || inventory == null) {
            return null;
        }

        String title = null;

        title = getInventoryTitleFor18(player, inventory);
        
        if (title == null) {
            title = getInventoryTitleFor17(player, inventory);
        }
        
        if (title == null) {
            title = getInventoryTitleFor19Plus(player, inventory);
        }
        
        if (title == null) {
            title = inventory.getType().name();
            logDebug("Usando tipo de inventário como fallback: " + title);
        }
        
        return title;
    }
    
    /**
     * Obtém o título do inventário para Minecraft 1.8.
     * @param player Jogador que tem o inventário aberto
     * @param inventory Inventário cujo título se deseja obter
     * @return Título do inventário ou null se não for possível obter
     */
    private static String getInventoryTitleFor18(Player player, Inventory inventory) {
        try {
            
            if (inventory != null && hasMethod(inventory.getClass(), "getTitle")) {
                return (String) inventory.getClass().getMethod("getTitle").invoke(inventory);
            }
            
            try {
                Class<?> craftInventoryClass = inventory.getClass();
                Method getTitleMethod = craftInventoryClass.getMethod("getTitle");
                return (String) getTitleMethod.invoke(inventory);
            } catch (NoSuchMethodException e) {
            }
            
            return inventory.getType().name();
        } catch (Exception e) {
            logDebug("Erro ao obter título para 1.8: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verifica se uma classe possui um determinado método.
     * @param clazz Classe a verificar
     * @param methodName Nome do método
     * @param parameterTypes Tipos de parâmetros (opcional)
     * @return true se o método existir, false caso contrário
     */
    private static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            clazz.getMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    
    /**
     * Obtém o título do inventário para Minecraft 1.7.
     * @param player Jogador que tem o inventário aberto
     * @param inventory Inventário cujo título se deseja obter
     * @return Título do inventário ou null se não for possível obter
     */
    private static String getInventoryTitleFor17(Player player, Inventory inventory) {
        try {
            if (inventory != null) {
                Field titleField = getFieldRecursively(inventory.getClass(), "title");
                if (titleField != null) {
                    titleField.setAccessible(true);
                    return (String) titleField.get(inventory);
                }
                
                
                try {
                    Method getInventoryMethod = player.getClass().getMethod("getInventory");
                    Object playerInventory = getInventoryMethod.invoke(player);
                    Field playerTitleField = getFieldRecursively(playerInventory.getClass(), "title");
                    if (playerTitleField != null) {
                        playerTitleField.setAccessible(true);
                        return (String) playerTitleField.get(playerInventory);
                    }
                } catch (Exception ex) {
                }
                
                return inventory.getType().name();
            }
            return null;
        } catch (Exception e) {
            logDebug("Erro ao obter título para 1.7: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtém o título do inventário para Minecraft 1.9 e superior.
     * @param player Jogador que tem o inventário aberto
     * @param inventory Inventário cujo título se deseja obter
     * @return Título do inventário ou null se não for possível obter
     */
    private static String getInventoryTitleFor19Plus(Player player, Inventory inventory) {
        try {
            
            try {
                Method getOpenInvMethod = Player.class.getMethod("getOpenInventory");
                Object inventoryView = getOpenInvMethod.invoke(player);
                
                Method getTitleMethod = inventoryView.getClass().getMethod("getTitle");
                return (String) getTitleMethod.invoke(inventoryView);
            } catch (Exception e) {
            }
            
            if (inventory != null) {
                if (hasMethod(inventory.getClass(), "getTitle")) {
                    Method getTitleMethod = inventory.getClass().getMethod("getTitle");
                    return (String) getTitleMethod.invoke(inventory);
                }
                
                try {
                    Field titleField = getFieldRecursively(inventory.getClass(), "title");
                    if (titleField != null) {
                        titleField.setAccessible(true);
                        return (String) titleField.get(inventory);
                    }
                } catch (Exception e) {
                }
            }
            
            if (inventory != null) {
                return inventory.getType().name();
            }
            
            return null;
        } catch (Exception e) {
            logDebug("Erro ao obter título para 1.9+: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Busca um campo em uma classe, incluindo em suas superclasses.
     * @param clazz Classe onde o campo deve ser buscado
     * @param fieldName Nome do campo
     * @return O Field encontrado ou null se não existe
     */
    private static Field getFieldRecursively(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }
    
    /**
     * Registra uma mensagem de depuração se o logger estiver disponível.
     * @param message Mensagem a ser registrada
     */
    private static void logDebug(String message) {
        if (logger != null) {
            logger.fine("§8[§6hVincular§8] §7[InventoryUtils] §f" + message);
        }
    }
}