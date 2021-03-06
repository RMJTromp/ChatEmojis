package com.rmjtromp.chatemojis.utils.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * 
 * @author Melvin
 * 
 */
public class NBTCompound {
	
	private static Constructor<?> NBTTagCompoundConstructor;
	private static Class<?> craftItemStackClass, NBTTagCompoundClass, itemStackClass, compoundClass;
	private static Method asNMSCopyMethod, hasTagMethod, getTagMethod, setTagMethod, asBukkitCopyMethod, getItemMetaMethod, setBooleanMethod, setDoubleMethod, setFloatMethod, setIntMethod, setStringMethod, setShortMethod, setLongMethod, getBooleanMethod, getDoubleMethod, getFloatMethod, getIntMethod, getStringMethod, getShortMethod, getLongMethod, removeMethod;
	private static boolean initialized = false;
	
	/**
	 * Initializes all of the classes, methods, and constructors
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	private static final void init() {
		initialized = true;
		
		try {
			craftItemStackClass = getClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
			itemStackClass = getClass("net.minecraft.server.%s.ItemStack");
			compoundClass = getClass("net.minecraft.server.%s.NBTTagCompound");
			NBTTagCompoundClass = getClass("net.minecraft.server.%s.NBTTagCompound");

			NBTTagCompoundConstructor = NBTTagCompoundClass.getConstructor();
			
			asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
			hasTagMethod = itemStackClass.getMethod("hasTag");
			getTagMethod = itemStackClass.getMethod("getTag");
			setTagMethod = itemStackClass.getMethod("setTag", compoundClass);
			asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", itemStackClass);
			getItemMetaMethod = craftItemStackClass.getMethod("getItemMeta", itemStackClass);
			removeMethod = compoundClass.getMethod("remove", String.class);

			setBooleanMethod = compoundClass.getMethod("setBoolean", String.class, boolean.class);
			setDoubleMethod = compoundClass.getMethod("setDouble", String.class, double.class);
			setFloatMethod = compoundClass.getMethod("setFloat", String.class, float.class);
			setIntMethod = compoundClass.getMethod("setInt", String.class, int.class);
			setLongMethod = compoundClass.getMethod("setLong", String.class, long.class);
			setShortMethod = compoundClass.getMethod("setShort", String.class, short.class);
			setStringMethod = compoundClass.getMethod("setString", String.class, String.class);

			getBooleanMethod = compoundClass.getMethod("getBoolean", String.class);
			getDoubleMethod = compoundClass.getMethod("getDouble", String.class);
			getFloatMethod = compoundClass.getMethod("getFloat", String.class);
			getIntMethod = compoundClass.getMethod("getInt", String.class);
			getLongMethod = compoundClass.getMethod("getLong", String.class);
			getShortMethod = compoundClass.getMethod("getShort", String.class);
			getStringMethod = compoundClass.getMethod("getString", String.class);
		} catch(NoSuchMethodException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private final ItemStack item;
	
	public NBTCompound(ItemStack item) {
		if(!initialized) init();
		this.item = item;
	}
	
	/**
	 * Returns whether or not the item has a specific key in its NBTCompound
	 * @param key
	 * @return Whether or not the compound has that tag
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public boolean hasTag(@NotNull String key) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Object compound = getNBTCompound(getCraftItemStack());
		Method hasKeyMethod = compound.getClass().getMethod("hasKey", String.class);
		return (boolean) hasKeyMethod.invoke(compound, key);
	}
	
	/**
	 * Sets a boolean value to the NBTCompound
	 * @param key
	 * @param value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public void setBoolean(@NotNull String key, @NotNull boolean value) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setBooleanMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets a boolean value to the NBTCompound
	 * @param key
	 * @param value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public void setDouble(@NotNull String key, @NotNull double value) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setDoubleMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets a float value to the NBTCompound
	 * @param key
	 * @param value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public void setFloat(@NotNull String key, @NotNull float value) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setFloatMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets an integer value to the NBTCompound
	 * @param key
	 * @param value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public void setInt(@NotNull String key, @NotNull int value) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setIntMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets a short value to the NBTCompound
	 * @param key
	 * @param value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public void setShort(@NotNull String key, @NotNull short value) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setShortMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets a string value to the NBTCompound
	 * @param key
	 * @param value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public void setString(@NotNull String key, @NotNull String value) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setStringMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets a long value to the NBTCompound
	 * @param key
	 * @param value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public void setLong(@NotNull String key, @NotNull long value) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setLongMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}
	
	/**
	 * Returns the boolean value for key
	 * @param key
	 * @return Boolean Value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public boolean getBoolean(@NotNull String key) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		return (boolean) getBooleanMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}
	
	/**
	 * Returns the double value for key
	 * @param key
	 * @return Double Value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public double getDouble(@NotNull String key) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		return (double) getDoubleMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}
	
	/**
	 * Returns the float value for key
	 * @param key
	 * @return Float Value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public float getFloat(@NotNull String key) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		return (float) getFloatMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}
	
	/**
	 * Returns the int value for key
	 * @param key
	 * @return Integer Value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public int getInt(@NotNull String key) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		return (int) getIntMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}

	/**
	 * Returns the short value for key
	 * @param key
	 * @return Short Value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public short getShort(@NotNull String key) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		return (short) getShortMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}

	/**
	 * Returns the string value for key
	 * @param key
	 * @return String Value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public String getString(@NotNull String key) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		return (String) getStringMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}

	/**
	 * Returns the long value for key
	 * @param key
	 * @return Long Value
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public long getLong(@NotNull String key) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		return (long) getLongMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}
	
	/**
	 * Removes a key from the NBTCompound
	 * @param key
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public void remove(@NotNull String key) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		removeMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}
	
	private Object getCraftItemStack() throws IllegalAccessException, InvocationTargetException {
		return asNMSCopyMethod.invoke(craftItemStackClass, item);
	}

	private Object getNBTCompound(@NotNull Object craftItemStack) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		return (boolean) hasTagMethod.invoke(craftItemStack) ? getTagMethod.invoke(craftItemStack) : NBTTagCompoundConstructor.newInstance();
	}
	
	private static Class<?> getClass(@NotNull String string) throws ClassNotFoundException {
		return Class.forName(String.format(string, BukkitUtils.getServerVersion()));
	}
	
}
