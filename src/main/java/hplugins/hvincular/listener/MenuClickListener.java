package hplugins.hvincular.listener;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.gui.TagSelectionMenu;
import hplugins.hvincular.integration.youtube.YouTubeVerifier;
import hplugins.hvincular.model.Tag;
import hplugins.hvincular.util.ColorUtils;
import hplugins.hvincular.util.SoundUtil;
import hplugins.hvincular.util.XInventoryUtils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Listener para interações do jogador com menus e entrada de links.
 */
public class MenuClickListener implements Listener {

    private final HVincular plugin;
    private final YouTubeVerifier youTubeVerifier;

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public MenuClickListener(HVincular plugin) {
        this.plugin = plugin;
        this.youTubeVerifier = new YouTubeVerifier(plugin);
    }

    /**
     * Manipula cliques no menu de seleção de tags.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        String menuTitle = ColorUtils.translate(plugin.getMenusConfig().getMenuTitle());
        
        String inventoryTitle = XInventoryUtils.getInventoryTitle(player, inventory);
        
        if (inventory != null && (XInventoryUtils.areInventoryTitlesEqual(inventoryTitle, menuTitle) ||
                XInventoryUtils.inventoryTitleContains(inventoryTitle, plugin.getMenusConfig().getMenuTitle()))) {
            event.setCancelled(true);
            
            int slot = event.getSlot();
            TagSelectionMenu menu = new TagSelectionMenu(plugin);
            String tagId = menu.getTagIdAtSlot(slot);
            
            if (tagId != null) {
                SoundUtil.playSound(player, SoundUtil.SoundType.CLICK);
                
                if (plugin.getDatabaseManager().isEnabled()) {
                    plugin.getDatabaseManager().hasActiveVinculation(player, tagId)
                        .thenAccept(hasVinculation -> {
                            if (hasVinculation) {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    SoundUtil.playSound(player, SoundUtil.SoundType.ERROR);
                                    player.sendMessage(plugin.getMessagesConfig().getTagAlreadyOwned());
                                    player.closeInventory();
                                });
                                return;
                            }
                            
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                player.closeInventory();
                                
                                SoundUtil.playSound(player, SoundUtil.SoundType.MENU_CLOSE);
                                
                                if (plugin.getConfigManager().isVerificationCodeEnabled()) {
                                    String verificationCode = plugin.getConfigManager().generateVerificationCode(player.getName());
                                    player.sendMessage(plugin.getMessagesConfig().getVerificationCodeInfo(verificationCode));
                                }
                                
                                player.sendMessage(plugin.getMessagesConfig().getEnterYouTubeLink());
                                
                                plugin.getMenuManager().setPendingVerification(player, tagId);
                            });
                        })
                        .exceptionally(ex -> {
                            plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §cErro ao verificar se jogador possui a tag: " + ex.getMessage());
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                processTagSelection(player, tagId);
                            });
                            return null;
                        });
                    return;
                } else {
                    String permission = plugin.getMenusConfig().getTag(tagId).getPermission();
                    boolean hasPermission = plugin.getPermissionManager().hasPermission(player, permission);
                    
                    if (plugin.getConfigManager().isDebugEnabled()) {
                        plugin.getServer().getConsoleSender().sendMessage("§8[§6hVincular§8] §b[DEBUG] §fVerificação de permissão (sem DB): " + player.getName() + 
                            " para permissão " + permission + ": " + hasPermission);
                    }
                    
                    processTagSelection(player, tagId);
                }
            }
        }
    }

    /**
     * Processa a seleção de tag e solicita link do YouTube ao jogador.
     * Este método centraliza a lógica comum do fluxo normal de seleção de tag.
     * 
     * @param player Jogador que está selecionando a tag
     * @param tagId ID da tag selecionada
     */
    private void processTagSelection(Player player, String tagId) {
        player.closeInventory();
        
        SoundUtil.playSound(player, SoundUtil.SoundType.MENU_CLOSE);
        
        if (plugin.getConfigManager().isVerificationCodeEnabled()) {
            String verificationCode = plugin.getConfigManager().generateVerificationCode(player.getName());
            player.sendMessage(plugin.getMessagesConfig().getVerificationCodeInfo(verificationCode));
        }
        
        final Tag tag = plugin.getMenusConfig().getTag(tagId);
        
        plugin.getChatInputManager().startChatInput(player, tag, (p, t, url) -> {
            plugin.getMenuManager().setPendingVerification(p, t.getId());
            plugin.getMenuManager().setPendingVideoUrl(p, url);
            
            youTubeVerifier.verifyVideo(p, url, t.getId());
        });
    }
}
