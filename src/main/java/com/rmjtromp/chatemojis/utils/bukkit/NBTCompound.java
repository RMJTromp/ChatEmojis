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
	 * @param key The tag which should be sought
	 * @return Whether or not the compound has that tag
	 * @throws Exception Throw if there are any reflection exception
	 */
	public boolean hasTag(@NotNull String key) throws Exception {
		Object compound = getNBTCompound(getCraftItemStack());
		Method hasKeyMethod = compound.getClass().getMethod("hasKey", String.class);
		return (boolean) hasKeyMethod.invoke(compound, key);
	}
	
	/**
	 * Sets a boolean value to the NBTCompound
	 * @param key The tag which should be manipulated
	 * @param value The value of the tag
	 * @throws Exception Throw if there are any reflection exception
	 */
	public void setBoolean(@NotNull String key, boolean value) throws Exception {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setBooleanMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets a boolean value to the NBTCompound
	 * @param key The tag which should be manipulated
	 * @param value The value of the tag
	 * @throws Exception Throw if there are any reflection exception
	 */
	public void setDouble(@NotNull String key, double value) throws Exception {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setDoubleMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets a float value to the NBTCompound
	 * @param key The tag which should be manipulated
	 * @param value The value of the tag
	 * @throws Exception Throw if there are any reflection exception
	 */
	public void setFloat(@NotNull String key, float value) throws Exception {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setFloatMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets an integer value to the NBTCompound
	 * @param key The tag which should be manipulated
	 * @param value The value of the tag
	 * @throws Exception Throw if there are any reflection exception
	 */
	public void setInt(@NotNull String key, int value) throws Exception {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setIntMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets a short value to the NBTCompound
	 * @param key The tag which should be manipulated
	 * @param value The value of the tag
	 * @throws Exception Throw if there are any reflection exception
	 */
	public void setShort(@NotNull String key, short value) throws Exception {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setShortMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets a string value to the NBTCompound
	 * @param key The tag which should be manipulated
	 * @param value The value of the tag
	 * @throws Exception Throw if there are any reflection exception
	 */
	public void setString(@NotNull String key, @NotNull String value) throws Exception {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setStringMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}

	/**
	 * Sets a long value to the NBTCompound
	 * @param key The tag which should be manipulated
	 * @param value The value of the tag
	 * @throws Exception Throw if there are any reflection exception
	 */
	public void setLong(@NotNull String key, long value) throws Exception {
		Object craftItem = getCraftItemStack();
		Object compound = getNBTCompound(craftItem);
		setLongMethod.invoke(compound, key, value);
		setTagMethod.invoke(craftItem, compound);
		asBukkitCopyMethod.invoke(craftItemStackClass, craftItem);
		item.setItemMeta((ItemMeta) getItemMetaMethod.invoke(craftItemStackClass, craftItem));
	}
	
	/**
	 * Returns the boolean value for key
	 * @param key The tag which should be sought
	 * @return Boolean Value
	 * @throws Exception Throw if there are any reflection exception
	 */
	public boolean getBoolean(@NotNull String key) throws Exception {
		return (boolean) getBooleanMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}
	
	/**
	 * Returns the double value for key
	 * @param key The tag which should be sought
	 * @return Double Value
	 * @throws Exception Throw if there are any reflection exception
	 */
	public double getDouble(@NotNull String key) throws Exception {
		return (double) getDoubleMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}
	
	/**
	 * Returns the float value for key
	 * @param key The tag which should be sought
	 * @return Float Value
	 * @throws Exception Throw if there are any reflection exception
	 */
	public float getFloat(@NotNull String key) throws Exception {
		return (float) getFloatMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}
	
	/**
	 * Returns the int value for key
	 * @param key The tag which should be sought
	 * @return Integer Value
	 * @throws Exception Throw if there are any reflection exception
	 */
	public int getInt(@NotNull String key) throws Exception {
		return (int) getIntMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}

	/**
	 * Returns the short value for key
	 * @param key The tag which should be sought
	 * @return Short Value
	 * @throws Exception Throw if there are any reflection exception
	 */
	public short getShort(@NotNull String key) throws Exception {
		return (short) getShortMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}

	/**
	 * Returns the string value for key
	 * @param key The tag which should be sought
	 * @return String Value
	 * @throws Exception Throw if there are any reflection exception
	 */
	public String getString(@NotNull String key) throws Exception {
		return (String) getStringMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}

	/**
	 * Returns the long value for key
	 * @param key The tag which should be sought
	 * @return Long Value
	 * @throws Exception Throw if there are any reflection exception
	 */
	public long getLong(@NotNull String key) throws Exception {
		return (long) getLongMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}
	
	/**
	 * Removes a key from the NBTCompound
	 * @param key The tag which should be sought
	 * @throws Exception Throw if there are any reflection exception
	 */
	public void remove(@NotNull String key) throws Exception {
		removeMethod.invoke(getNBTCompound(getCraftItemStack()), key);
	}

	/**
	 * Converts the Item to CraftItemStack
	 * @return CraftItemStack object
	 * @throws Exception Throw if there are any reflection exception
	 */
	private Object getCraftItemStack() throws Exception {
		return asNMSCopyMethod.invoke(craftItemStackClass, item);
	}

	/**
	 * Gets the NBTCompound of the CraftItemStack
	 * @param craftItemStack The CraftItemStack whose NBTCompound is being utilized
	 * @return NBTCompound
	 * @throws Exception Throw if there are any reflection exception
	 */
	private Object getNBTCompound(@NotNull Object craftItemStack) throws Exception {
		return (boolean) hasTagMethod.invoke(craftItemStack) ? getTagMethod.invoke(craftItemStack) : NBTTagCompoundConstructor.newInstance();
	}
	
	private static Class<?> getClass(@NotNull String string) throws ClassNotFoundException {
		return Class.forName(String.format(string, BukkitUtils.getServerVersion()));
	}
	
}
