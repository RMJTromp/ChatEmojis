package com.rmjtromp.chatemojis.windows;

import com.rmjtromp.chatemojis.ChatEmojis;
import com.rmjtromp.chatemojis.utils.Version;
import com.rmjtromp.chatemojis.utils.Window;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsWindow extends Window {

    private final ChatEmojis PLUGIN;

    private final ItemStack BOOK, SIGN, CLOSE;

    public SettingsWindow(@NotNull ChatEmojis plugin) {
        super(null, InventoryType.HOPPER, "ChatEmojis Settings");
        PLUGIN = plugin;

        // never destroy the window
        this.TTL = -1;

        if(Version.getServerVersion().isNewerThan(Version.V1_7)) CLOSE = new ItemStack(Material.BARRIER);
        else CLOSE = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 14);
        ItemMeta barrierMeta = CLOSE.getItemMeta();
        assert barrierMeta != null;
        barrierMeta.setDisplayName(ChatColor.RED+"Close");
        CLOSE.setItemMeta(barrierMeta);

        BOOK = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = BOOK.getItemMeta();
        assert bookMeta != null;
        bookMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lBooks"));
        List<String> bookLore = new ArrayList<>();
        Arrays.asList("&7Click this item to toggle whether or not", "&7emojis can be used in books.").forEach(lore -> bookLore.add(ChatColor.translateAlternateColorCodes('&', lore)));
        bookMeta.setLore(bookLore);
        if(Version.getServerVersion().isNewerThan(Version.V1_7)) bookMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        BOOK.setItemMeta(bookMeta);

        SIGN = new ItemStack(Version.getServerVersion().isNewerThan(Version.V1_13) ? Material.valueOf("OAK_SIGN") : Material.valueOf("SIGN"));
        ItemMeta signMeta = SIGN.getItemMeta();
        assert signMeta != null;
        signMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSigns"));
        List<String> signLore = new ArrayList<>();
        Arrays.asList("&7Click this item to toggle whether or not", "&7emojis can be used on signs.").forEach(lore -> signLore.add(ChatColor.translateAlternateColorCodes('&', lore)));
        signMeta.setLore(signLore);
        if(Version.getServerVersion().isNewerThan(Version.V1_7)) signMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        SIGN.setItemMeta(signMeta);
    }

    @Override
    public void build() {
        clear();

        final boolean
            useInBooks = Boolean.TRUE.equals(PLUGIN.useInBooks.getValue()),
            useOnSigns = Boolean.TRUE.equals(PLUGIN.useOnSigns.getValue());

        final ItemStack
            close = CLOSE.clone(),
            book = BOOK.clone(),
            sign = SIGN.clone();

        setItemEvents(4, close).onClick(e -> e.getWhoClicked().closeInventory());

        if(useInBooks) {
            ItemMeta bookMeta = book.getItemMeta();
            assert bookMeta != null;
            bookMeta.addEnchant(Enchantment.LURE, 1, true);
            book.setItemMeta(bookMeta);
        }
        setItemEvents(0, book).onClick(e -> {
            PLUGIN.useInBooks.setValue(!useInBooks);
            build();
        });

        if(useOnSigns) {
            ItemMeta signMeta = sign.getItemMeta();
            assert signMeta != null;
            signMeta.addEnchant(Enchantment.LURE, 1, true);
            book.setItemMeta(signMeta);
        }
        setItemEvents(1, sign).onClick(e -> {
            PLUGIN.useOnSigns.setValue(!useOnSigns);
            build();
        });
    }

}
