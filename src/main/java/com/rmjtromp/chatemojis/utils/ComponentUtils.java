package com.rmjtromp.chatemojis.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

public final class ComponentUtils {

    private ComponentUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a text component from string
     * Supports chatcolors
     * @param arg0
     * @return
     */
    public static TextComponent createComponent(String arg0) {
        return new TextComponent(createBaseComponent(arg0));
    }

    /**
     * Creates multiple components from strings
     * Supports chatcolor
     * @param arg0
     * @return
     */
    public static TextComponent[] createComponents(String ...arg0) {
        TextComponent[] components = new TextComponent[arg0.length];
        for(int i = 0; i < arg0.length; i++) components[i] = createComponent(arg0[i]);
        return components;
    }

    /**
     * Creates multiple components from strings
     * Supports chatcolor
     * @param arg0
     * @return
     */
    @SuppressWarnings("unused")
    public static TextComponent[] createComponents(Iterable<String> arg0) {
        int size = 0;
        for(String arg : arg0) size++;
        String[] components = new String[size];

        int i = 0;
        for(String arg : arg0) {
            components[i] = arg;
            i++;
        }

        return createComponents(components);
    }

    /**
     * Appends components to eachother
     * @param arg0
     * @return
     */
    public static TextComponent mergeComponents(TextComponent ...arg0) {
        TextComponent component = new TextComponent();
        for(TextComponent arg : arg0) component.addExtra(arg);
        return component;
    }

    /**
     * Appends components to eachother
     * @param arg0
     * @return
     */
    public static TextComponent mergeComponents(Iterable<TextComponent> arg0) {
        TextComponent component = new TextComponent();
        for(TextComponent arg : arg0) component.addExtra(arg);
        return component;
    }

    /**
     * Creates basecomponents from string
     * supports chatcolor
     * @param arg0
     * @return
     */
    public static BaseComponent[] createBaseComponent(String arg0) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', arg0));
    }

    /**
     * Glues textcomponents together
     * @param arg0
     * @param arg1
     * @return
     */
    public static TextComponent joinComponents(String arg0, TextComponent ...arg1) {
        TextComponent component = new TextComponent();
        for(TextComponent arg : arg1) component.addExtra(mergeComponents(createComponent(arg0), arg));
        return component;
    }

    /**
     * Glues textcomponents together
     * @param arg0
     * @param arg1
     * @return
     */
    public static TextComponent joinComponents(String arg0, Iterable<TextComponent> arg1) {
        TextComponent component = new TextComponent();
        for(TextComponent arg : arg1) component.addExtra(mergeComponents(createComponent(arg0), arg));
        return component;
    }

}