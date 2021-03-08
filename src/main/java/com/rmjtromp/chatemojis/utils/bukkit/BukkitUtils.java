package com.rmjtromp.chatemojis.utils.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Iterables;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public final class BukkitUtils {

	private BukkitUtils() {
		throw new IllegalStateException("Utility class");
	}
	
	/**
	 * Returns the server version for reflection use
	 * @return Server Version
	 */
	public static String getServerVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}
	
	/**
	 * Color encode a string
	 * @param string The string which should be color-encoded
	 * @return Color encoded string
	 */
	public static String colorEncode(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}
	
	/**
	 * Color encode a collection of string
	 * @param collection The string collection which should be color-encoded
	 * @return Color encoded string list
	 */
	public static List<String> colorEncode(Collection<String> collection) {
		return collection.stream().map(BukkitUtils::colorEncode).collect(Collectors.toList());
	}
	
	/**
	 * Color encode a collection of string
	 * @param iterable The iterable which should be color-encoded
	 * @return Color encoded string list
	 */
	public static List<String> colorEncode(Iterable<String> iterable) {
		return colorEncode(Arrays.asList(Iterables.toArray(iterable, String.class)));
	}
	
	/**
	 * Color encode an array of string
	 * @param strings The strings which should be color-encoded
	 * @return Color encoded string list
	 */
	public static List<String> colorEncode(String...strings) {
		return colorEncode(Arrays.asList(strings));
	}
	
	/**
	 * Send a {@link BaseComponent} message to a player
	 * @param player Receiver
	 * @param message BaseComponent message
	 * @throws Exception Thrown if there any reflection exceptions
	 */
	public static void sendComponent(Player player, BaseComponent message) throws Exception {
		Class<?> chatSerializerClass = BukkitUtils.getClass("net.minecraft.server.%s.ChatSerializer");
		Method toStringMethod = chatSerializerClass.getMethod("a", String.class);
		Object component = toStringMethod.invoke(chatSerializerClass, ComponentSerializer.toString(message));
		
		Class<?> craftPlayerClass = BukkitUtils.getClass("org.bukkit.craftbukkit.%s.entity.CraftPlayer");
		Object craftPlayer = craftPlayerClass.cast(player);
		Method getHandleMethod = craftPlayerClass.getMethod("getHandle");
		Object handle = getHandleMethod.invoke(craftPlayer);
		Field playerConnectionField = handle.getClass().getField("playerConnection");
		Object playerConnection = playerConnectionField.get(handle);
		Class<?> packetClass = BukkitUtils.getClass("net.minecraft.server.%s.Packet");
		Method sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", packetClass);
		
		Class<?> packetPlayOutChatClass = BukkitUtils.getClass("net.minecraft.server.%s.PacketPlayOutChat");
		Class<?> iChatBaseComponentClass = BukkitUtils.getClass("net.minecraft.server.%s.IChatBaseComponent");
		Constructor<?> packetPlayOutChatConstructor = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass);
		Object packet = packetPlayOutChatConstructor.newInstance(component);
		
		sendPacketMethod.invoke(playerConnection, packet);
	}
	
	/**
	 * Returns the version-specific class
	 * This will replace '%s' with the server's version
	 * @param string The class which should be sought
	 * @return The class
	 * @throws ClassNotFoundException Thrown if class could not be found
	 */
	public static Class<?> getClass(@NotNull String string) throws ClassNotFoundException {
		return Class.forName(String.format(string, BukkitUtils.getServerVersion()));
	}
	
}
