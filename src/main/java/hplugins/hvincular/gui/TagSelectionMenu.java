package hplugins.hvincular.gui;

import hplugins.hvincular.HVincular;
import hplugins.hvincular.model.Tag;
import hplugins.hvincular.util.ColorUtils;
import hplugins.hvincular.util.CooldownManager;
import hplugins.hvincular.util.ItemBuilder;
import hplugins.hvincular.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menu de seleção de tags para vinculação.
 */
public class TagSelectionMenu {

    private final HVincular plugin;
    private final Inventory inventory;
    private final Map<Integer, String> tagSlots = new HashMap<>();

    /**
     * Construtor da classe.
     * @param plugin Instância do plugin principal
     */
    public TagSelectionMenu(HVincular plugin) {
        this.plugin = plugin;
        String title = plugin.getMenusConfig().getMenuTitle();
        int size = plugin.getMenusConfig().getMenuSize();
        this.inventory = Bukkit.createInventory(null, size, ColorUtils.translate(title));
        
        setupMenu();
    }

    /**
     * Configura os itens no menu.
     */
    private void setupMenu() {
        List<Tag> tags = plugin.getMenusConfig().getAllTags();
        
        for (Tag tag : tags) {
            ItemStack item = createTagItem(tag);
            int slot = tag.getMenuSlot();
            
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, item);
                tagSlots.put(slot, tag.getId());
            }
        }
    }

    /**
     * Cria o item representando uma tag no menu.
     * @param tag Tag
     * @return ItemStack configurado
     */
    private ItemStack createTagItem(Tag tag) {
        ItemBuilder builder = new ItemBuilder(tag.getIcon().clone());
        
        builder.setName(tag.getDisplayName());
        
        List<String> lore = tag.getLore();
        if (lore != null && !lore.isEmpty()) {
            lore = replacePlaceholders(lore, tag);
            builder.setLore(lore);
        }
        
        return builder.build();
    }
    
    /**
     * Substitui placeholders na lore do item.
     * @param lore Lista de strings da lore
     * @param tag Tag associada
     * @return Lore com placeholders substituídos
     */
    private List<String> replacePlaceholders(List<String> lore, Tag tag) {
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            line = line.replace("%min_subscribers%", String.valueOf(tag.getMinSubscribers()));
            line = line.replace("%min_views%", String.valueOf(tag.getMinViews()));
            line = line.replace("%cooldown_time%", CooldownManager.formatTime(tag.getCooldownSeconds()));
            lore.set(i, ColorUtils.translate(line));
        }
        return lore;
    }

    /**
     * Abre o menu para um jogador.
     * @param player Jogador
     */
    public void open(Player player) {
        player.openInventory(inventory);
        SoundUtil.playSound(player, SoundUtil.SoundType.MENU_OPEN);
    }
    
    /**
     * Verifica se um slot contém uma tag e retorna o ID da tag.
     * @param slot Slot do inventário
     * @return ID da tag ou null se não houver tag no slot
     */
    public String getTagIdAtSlot(int slot) {
        return tagSlots.get(slot);
    }
}
