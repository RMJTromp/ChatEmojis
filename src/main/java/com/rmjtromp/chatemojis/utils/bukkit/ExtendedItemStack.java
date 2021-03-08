package com.rmjtromp.chatemojis.utils.bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.rmjtromp.chatemojis.utils.NumberUtils;
import org.jetbrains.annotations.NotNull;

public class ExtendedItemStack extends ItemStack {

	public ExtendedItemStack(@NotNull ItemStack itemStack) {
		super(itemStack);
	}
	
	public ExtendedItemStack(@NotNull Material material) {
		super(material);
	}
	
	public ExtendedItemStack(@NotNull Material material, int amount) {
		super(material);
		setAmount(NumberUtils.constraintToRange(amount, 1, getMaxStackSize()));
	}
	
	public ExtendedItemStack(@NotNull String material) {
		this(material, 1);
	}
	
	public ExtendedItemStack(@NotNull String material, int amount) {
		super(Material.getMaterial(material));
		setAmount(NumberUtils.constraintToRange(amount, 1, getMaxStackSize()));
	}
	
	/**
	 * Sets the displayname of the item
	 * @param name The new display name
	 */
	public ExtendedItemStack setDisplayName(@NotNull String name) {
		ItemMeta meta = this.getItemMeta();
		meta.setDisplayName(BukkitUtils.colorEncode(name));
		this.setItemMeta(meta);
		return this;
	}
	
	/**
	 * Returns the display name of the item
	 * @return The item's display name
	 */
	public String getDisplayName() {
		return this.getItemMeta().getDisplayName();
	}
	
	/**
	 * Sets the lore of the item, replaces existing one
	 * @param lores The lores which should be set
	 */
	public ExtendedItemStack setLore(@NotNull Iterable<String> lores) {
		ItemMeta meta = this.getItemMeta();
		meta.setLore(BukkitUtils.colorEncode(lores));
		this.setItemMeta(meta);
		return this;
	}

	/**
	 * Prepend a lore to the bottom of the existing lore
	 * @param lore The lore which should be prepended
	 */
	public ExtendedItemStack prependLore(@NotNull String lore) {
		List<String> lores = new ArrayList<>();
		lores.add(BukkitUtils.colorEncode(lore));
		lores.addAll(getLores());
		setLore(lores);
		return this;
	}

	/**
	 * Append a lore to the bottom of the existing lore
	 * @param lore The lore which should be appended
	 */
	public ExtendedItemStack appendLore(@NotNull String lore) {
		List<String> lores = new ArrayList<>(getLores());
		lores.add(BukkitUtils.colorEncode(lore));
		setLore(lores);
		return this;
	}

	/**
	 * Append a lore to the bottom of the existing lore
	 * @param lore The lore which should be added
	 * @see ExtendedItemStack#appendLore(String)
	 */
	public ExtendedItemStack addLore(@NotNull String ...lore) {
		for(String l : lore) appendLore(l);
		return this;
	}

	/**
	 * Prepend multiple lores to the bottom of the existing lore
	 * @param lores The lores which should be prepended
	 */
	public ExtendedItemStack prependLores(@NotNull Iterable<String> lores) {
		List<String> newLores = new ArrayList<>();
		newLores.addAll(BukkitUtils.colorEncode(lores));
		newLores.addAll(getLores());
		setLore(newLores);
		return this;
	}
	
	/**
	 * Append multiple lores to the bottom of the existing lore
	 * @param lores The lores which should be prepended
	 */
	public ExtendedItemStack appendLores(@NotNull Iterable<String> lores) {
		List<String> newLores = new ArrayList<>();
		newLores.addAll(getLores());
		newLores.addAll(BukkitUtils.colorEncode(lores));
		setLore(newLores);
		return this;
	}
	
	/**
	 * Returns the lores or the item
	 * @return The item's lores
	 */
	public List<String> getLores() {
		List<String> lores = new ArrayList<>();
		ItemMeta meta = this.getItemMeta();
		return hasLore() ? meta.getLore() : lores;
	}
	
	/**
	 * Removes the current item lores
	 */
	public ExtendedItemStack removeLores() {
		setLore(new ArrayList<>());
		return this;
	}
	
	/**
	 * Checks and returns if the item has a lore
	 * @return Whether or not the item has a lore
	 */
	public boolean hasLore() {
		ItemMeta meta = this.getItemMeta();
		return meta.hasLore();
	}
	
	/**
	 * Adds ItemFlags to the item
	 * @param arg0 The ItemFlags which should be added
	 */
	public ExtendedItemStack addItemFlags(@NotNull ItemFlag ...arg0) {
		ItemMeta meta = this.getItemMeta();
		meta.addItemFlags(arg0);
		this.setItemMeta(meta);
		return this;
	}
	
	/**
	 * Removes ItemFlags from the item
	 * @param arg0 The ItemFlags which should be removed
	 */
	public ExtendedItemStack removeItemFlags(@NotNull ItemFlag ...arg0) {
		ItemMeta meta = this.getItemMeta();
		meta.removeItemFlags(arg0);
		this.setItemMeta(meta);
		return this;
	}
	
	/**
	 * Returns all ItemFlags applied to item
	 * @return ItemFlags which are present on the item
	 */
	public Set<ItemFlag> getItemFlags() {
		ItemMeta meta = this.getItemMeta();
		return meta.getItemFlags();
	}
	
	/**
	 * Returns whether or not a specific ItemFlag is present
	 * @param flag Which ItemFlag should be sought for
	 * @return boolean whether or not a ItemFlag is present
	 */
	public boolean hasItemFlag(@NotNull ItemFlag flag) {
		ItemMeta meta = this.getItemMeta();
		return meta.hasItemFlag(flag);
	}
	
	/**
	 * Adds/removes glowing effect to the item, though it hides all enchantents
	 * @param glow Toggle whether or not the glow effect should be on or off
	 */
	public ExtendedItemStack setGlow(boolean glow) {
		ItemMeta meta = this.getItemMeta();
		if(glow) {
			if(!meta.hasEnchant(Enchantment.LURE)) meta.addEnchant(Enchantment.LURE, 1, false);
			if(!meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		} else {
			if(meta.hasEnchant(Enchantment.LURE)) meta.removeEnchant(Enchantment.LURE);
			if(meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		this.setItemMeta(meta);
		return this;
	}

	/**
	 * Set the color of leather armors
	 * @param arg0 The color which the leather armor should be
	 */
	public ExtendedItemStack setColor(@NotNull Color arg0) {
		if(getItemMeta() instanceof LeatherArmorMeta) {
			LeatherArmorMeta meta = (LeatherArmorMeta) getItemMeta();
			meta.setColor(arg0);
			setItemMeta(meta);
		}
		return this;
	}
	
	/**
	 * Returns whether or not the item has glowing effect to it
	 * @return whether or not the item has glowing effect
	 */
	public boolean hasGlow() {
		ItemMeta meta = this.getItemMeta();
		return meta.hasEnchant(Enchantment.LURE) && meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
	}
	
	/**
	 * Clones the item and give you access to all of it's NBT data
	 * If changes are made to the NBTComponenent, you must copy the metadata
	 * to the item you want to apply it to.
	 * <br><br>
	 * <code>ItemStack#setItemMeta(NBTComponent#getItemMeta())</code>
	 * 
	 * @return NBTCompound of the item
	 */
	public NBTCompound getNBTCompound() {
		return new NBTCompound(this);
	}
	
	/**
	 * Makes the item unbreakable
	 * @param unbreakable Whether or not the item should be unbreakable
	 * @throws Exception Thrown if there are any reflection exceptions
	 */
	public ExtendedItemStack setUnbreakable(boolean unbreakable) throws Exception {
		getNBTCompound().setBoolean("Unbreakable", unbreakable);
		return this;
	}
	
	@Override
	public ExtendedItemStack clone() {
		return new ExtendedItemStack(super.clone());
	}
	
}
