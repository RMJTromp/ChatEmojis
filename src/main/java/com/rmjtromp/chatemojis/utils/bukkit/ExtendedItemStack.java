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

public class ExtendedItemStack extends ItemStack {

	public ExtendedItemStack(ItemStack itemStack) {
		super(itemStack == null ? new ItemStack(Material.AIR, 1) : itemStack);
	}
	
	public ExtendedItemStack(Material material) {
		super(material);
	}
	
	public ExtendedItemStack(Material material, int amount) {
		super(material);
		setAmount(NumberUtils.constraintToRange(amount, 1, getMaxStackSize()));
	}
	
	public ExtendedItemStack(String material) {
		this(material, 1);
	}
	
	public ExtendedItemStack(String material, int amount) {
		super(Material.getMaterial(material));
		setAmount(NumberUtils.constraintToRange(amount, 1, getMaxStackSize()));
	}
	
	/**
	 * Sets the displayname of the item
	 * @param name
	 * @return
	 */
	public ExtendedItemStack setDisplayName(String name) {
		ItemMeta meta = this.getItemMeta();
		meta.setDisplayName(BukkitUtils.colorEncode(name));
		this.setItemMeta(meta);
		return this;
	}
	
	/**
	 * Returns the display name of the item
	 * @return
	 */
	public String getDisplayName() {
		return this.getItemMeta().getDisplayName();
	}
	
	/**
	 * Sets the lore of the item, replaces existing one
	 * @param lores
	 * @return
	 */
	public ExtendedItemStack setLore(Iterable<String> lores) {
		ItemMeta meta = this.getItemMeta();
		meta.setLore(BukkitUtils.colorEncode(lores));
		this.setItemMeta(meta);
		return this;
	}

	/**
	 * Prepend a lore to the bottom of the existing lore
	 * @param lore
	 * @return
	 */
	public ExtendedItemStack prependLore(String lore) {
		List<String> lores = new ArrayList<>();
		lores.add(BukkitUtils.colorEncode(lore));
		lores.addAll(getLores());
		setLore(lores);
		return this;
	}

	/**
	 * Append a lore to the bottom of the existing lore
	 * @param lore
	 * @return
	 */
	public ExtendedItemStack appendLore(String lore) {
		List<String> lores = new ArrayList<>(getLores());
		lores.add(BukkitUtils.colorEncode(lore));
		setLore(lores);
		return this;
	}

	/**
	 * Append a lore to the bottom of the existing lore
	 * @param lore
	 * @return
	 */
	public ExtendedItemStack addLore(String ...lore) {
		for(String l : lore) appendLore(l);
		return this;
	}

	/**
	 * Prepend multiple lores to the bottom of the existing lore
	 * @param lores
	 * @return
	 */
	public ExtendedItemStack prependLores(Iterable<String> lores) {
		List<String> newLores = new ArrayList<>();
		newLores.addAll(BukkitUtils.colorEncode(lores));
		newLores.addAll(getLores());
		setLore(newLores);
		return this;
	}
	
	/**
	 * Append multiple lores to the bottom of the existing lore
	 * @param lores
	 * @return
	 */
	public ExtendedItemStack appendLores(Iterable<String> lores) {
		List<String> newLores = new ArrayList<>();
		newLores.addAll(getLores());
		newLores.addAll(BukkitUtils.colorEncode(lores));
		setLore(newLores);
		return this;
	}
	
	/**
	 * Returns the lores or the item
	 * @return
	 */
	public List<String> getLores() {
		List<String> lores = new ArrayList<>();
		ItemMeta meta = this.getItemMeta();
		return hasLore() ? meta.getLore() : lores;
	}
	
	/**
	 * Removes the current item lores
	 * @return
	 */
	public ExtendedItemStack removeLores() {
		setLore(new ArrayList<>());
		return this;
	}
	
	/**
	 * Returns whether or not the item has a lore
	 * @return
	 */
	public boolean hasLore() {
		ItemMeta meta = this.getItemMeta();
		return meta.hasLore();
	}
	
	/**
	 * Adds ItemFlags to the item
	 * @param arg0
	 * @return
	 */
	public ExtendedItemStack addItemFlags(ItemFlag ...arg0) {
		ItemMeta meta = this.getItemMeta();
		meta.addItemFlags(arg0);
		this.setItemMeta(meta);
		return this;
	}
	
	/**
	 * Removes ItemFlags from the item
	 * @param arg0
	 * @return
	 */
	public ExtendedItemStack removeItemFlags(ItemFlag ...arg0) {
		ItemMeta meta = this.getItemMeta();
		meta.removeItemFlags(arg0);
		this.setItemMeta(meta);
		return this;
	}
	
	/**
	 * Returns all ItemFlags applied to item
	 * @return
	 */
	public Set<ItemFlag> getItemFlags() {
		ItemMeta meta = this.getItemMeta();
		return meta.getItemFlags();
	}
	
	/**
	 * Returns whether or not a specific ItemFlag is present
	 * @param flag
	 * @return
	 */
	public boolean hasItemFlag(ItemFlag flag) {
		ItemMeta meta = this.getItemMeta();
		return meta.hasItemFlag(flag);
	}
	
	/**
	 * Adds/removes glowing effect to the item, though it hides all enchantents
	 * @param glow
	 * @return
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
	 * @param arg0
	 */
	public ExtendedItemStack setColor(Color arg0) {
		if(getItemMeta() instanceof LeatherArmorMeta) {
			LeatherArmorMeta meta = (LeatherArmorMeta) getItemMeta();
			meta.setColor(arg0);
			setItemMeta(meta);
		}
		return this;
	}
	
	/**
	 * Returns whether or not the item has glowing effect to it
	 * @return
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
	 * @return
	 */
	public NBTCompound getNBTCompound() {
		return new NBTCompound(this);
	}
	
	/**
	 * Makes the item unbreakable
	 * @param unbreakable
	 * @return
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 */
	public ExtendedItemStack setUnbreakable(boolean unbreakable) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		getNBTCompound().setBoolean("Unbreakable", unbreakable);
		return this;
	}
	
	@Override
	public ExtendedItemStack clone() {
		return new ExtendedItemStack(super.clone());
	}
	
}
