package hplugins.hvincular.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Construtor de itens para facilitar a criação de ItemStacks com compatibilidade entre versões.
 * Utiliza XSeries para garantir compatibilidade com todas as versões do Minecraft.
 */
public class ItemBuilder {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private static int minorVersion = -1;

    static {
        try {
            String bukkitVersion = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            minorVersion = Integer.parseInt(bukkitVersion.split("_")[1]);
        } catch (Exception e) {
            minorVersion = 16;
        }
    }

    /**
     * Construtor a partir de um material usando XMaterial para compatibilidade.
     * @param material Material do XMaterial
     */
    public ItemBuilder(XMaterial material) {
        this(material.parseMaterial(), 1);
    }

    /**
     * Construtor a partir de um material original do Bukkit.
     * @param material Material do Bukkit
     */
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    /**
     * Construtor a partir de um material e quantidade.
     * @param material Material do XMaterial
     * @param amount Quantidade
     */
    public ItemBuilder(XMaterial material, int amount) {
        Material mat = material.parseMaterial();
        if (mat != null) {
            this.itemStack = new ItemStack(mat, amount);
        } else {
            this.itemStack = new ItemStack(Material.valueOf("STONE"), amount);
        }
        this.itemMeta = this.itemStack.getItemMeta();
    }

    /**
     * Construtor a partir de um material e quantidade.
     * @param material Material do Bukkit
     * @param amount Quantidade
     */
    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    /**
     * Construtor a partir de um material, quantidade e durabilidade (para versões antigas).
     * @param material Material do XMaterial
     * @param amount Quantidade
     * @param durability Durabilidade (ignorada em versões modernas)
     */
    public ItemBuilder(XMaterial material, int amount, short durability) {
        Material mat = material.parseMaterial();
        if (mat != null) {
            this.itemStack = new ItemStack(mat, amount);
            
            if (minorVersion < 13) {
                try {
                    itemStack.setDurability(durability);
                } catch (Exception ignored) {
                }
            }
        } else {
            this.itemStack = new ItemStack(Material.valueOf("STONE"), amount);
        }
        this.itemMeta = this.itemStack.getItemMeta();
    }

    /**
     * Construtor a partir de um ItemStack existente.
     * @param itemStack ItemStack base
     */
    public ItemBuilder(ItemStack itemStack) {
        if (itemStack == null) {
            this.itemStack = new ItemStack(Material.valueOf("STONE"));
        } else {
            this.itemStack = itemStack.clone();
        }
        this.itemMeta = this.itemStack.getItemMeta();
    }

    /**
     * Define o nome de exibição do item.
     * @param name Nome de exibição
     * @return Esta instância para encadeamento
     */
    public ItemBuilder setName(String name) {
        if (name != null && itemMeta != null) {
            itemMeta.setDisplayName(ColorUtils.translate(name));
        }
        return this;
    }

    /**
     * Define a lore (descrição) do item.
     * @param lore Linhas da lore
     * @return Esta instância para encadeamento
     */
    public ItemBuilder setLore(String... lore) {
        if (lore != null) {
            setLore(Arrays.asList(lore));
        }
        return this;
    }

    /**
     * Define a lore (descrição) do item.
     * @param lore Lista de linhas da lore
     * @return Esta instância para encadeamento
     */
    public ItemBuilder setLore(List<String> lore) {
        if (lore != null && itemMeta != null) {
            List<String> coloredLore = ColorUtils.translate(lore);
            itemMeta.setLore(coloredLore);
        }
        return this;
    }

    /**
     * Adiciona linhas à lore existente.
     * @param lines Linhas a serem adicionadas
     * @return Esta instância para encadeamento
     */
    public ItemBuilder addLoreLines(String... lines) {
        if (lines == null || itemMeta == null) return this;
        
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        
        for (String line : lines) {
            lore.add(ColorUtils.translate(line));
        }
        
        itemMeta.setLore(lore);
        return this;
    }

    /**
     * Adiciona um encantamento ao item.
     * @param enchantment Encantamento
     * @param level Nível do encantamento
     * @param ignoreRestrictions Se deve ignorar restrições de encantamento
     * @return Esta instância para encadeamento
     */
    public ItemBuilder addEnchant(Enchantment enchantment, int level, boolean ignoreRestrictions) {
        if (itemMeta != null && enchantment != null) {
            itemMeta.addEnchant(enchantment, level, ignoreRestrictions);
        }
        return this;
    }

    /**
     * Adiciona uma flag de item com compatibilidade para versões antigas.
     * @param flag Flag de item
     * @return Esta instância para encadeamento
     */
    public ItemBuilder addItemFlag(ItemFlag flag) {
        if (itemMeta != null && flag != null && minorVersion >= 8) {
            try {
                itemMeta.addItemFlags(flag);
            } catch (Exception ignored) {
            }
        }
        return this;
    }

    /**
     * Define se o item é indestrutível, com compatibilidade para todas as versões.
     * @param unbreakable Se o item é indestrutível
     * @return Esta instância para encadeamento
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        if (itemMeta == null) return this;
        
        if (minorVersion >= 13) {
            itemMeta.setUnbreakable(unbreakable);
        } else if (minorVersion >= 8) {
            try {
                Method spigotMethod = itemMeta.getClass().getMethod("spigot");
                Object spigot = spigotMethod.invoke(itemMeta);
                Method setUnbreakableMethod = spigot.getClass().getMethod("setUnbreakable", boolean.class);
                setUnbreakableMethod.invoke(spigot, unbreakable);
            } catch (Exception ignored) {
            }
        }
        return this;
    }

    /**
     * Constrói o ItemStack final.
     * @return ItemStack configurado
     */
    public ItemStack build() {
        if (itemStack != null && itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
}
