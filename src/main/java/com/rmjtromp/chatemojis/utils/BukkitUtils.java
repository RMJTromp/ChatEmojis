package com.rmjtromp.chatemojis.utils;

import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class BukkitUtils {

    private static Plugin plugin = null;

    private BukkitUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void init(@NotNull Plugin plugin) {
        BukkitUtils.plugin = plugin;
    }

    public static Plugin getPlugin() {
        if(plugin == null) throw new NullPointerException("Working plugin is null");
        return plugin;
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

