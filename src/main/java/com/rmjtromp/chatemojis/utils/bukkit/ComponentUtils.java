package com.rmjtromp.chatemojis.utils.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public final class ComponentUtils {

    private ComponentUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a text component from string
     * Supports chatcolors
     * @param arg0 The string which should be converted to a text component
     * @return TextComponent
     */
    public static TextComponent createComponent(String arg0) {
        return new TextComponent(createBaseComponent(arg0));
    }

    /**
     * Creates multiple components from strings
     * Supports chatcolor
     * @param arg0 The strings which should be converted into text components
     * @return TextComponents
     */
    public static TextComponent[] createComponents(String ...arg0) {
        TextComponent[] components = new TextComponent[arg0.length];
        for(int i = 0; i < arg0.length; i++) components[i] = createComponent(arg0[i]);
        return components;
    }

    /**
     * Creates multiple components from strings
     * Supports chatcolor
     * @param arg0 The strings which should be converted into text components
     * @return TextComponents
     */
    public static TextComponent[] createComponents(Iterable<String> arg0) {
        List<String> c = new ArrayList<>();
        arg0.forEach(c::add);
        return createComponents(c.toArray(new String[0]));
    }

    /**
     * Appends components to eachother
     * @param arg0 The TextComponents which should be merged
     * @return merged TextComponent
     */
    public static TextComponent mergeComponents(TextComponent ...arg0) {
        TextComponent component = new TextComponent();
        for(TextComponent arg : arg0) component.addExtra(arg);
        return component;
    }

    /**
     * Appends components to each other
     * @param arg0 The TextComponents which should be merged
     * @return merged TextComponent
     */
    public static TextComponent mergeComponents(Iterable<TextComponent> arg0) {
        List<TextComponent> c = new ArrayList<>();
        arg0.forEach(c::add);
        return mergeComponents(c.toArray(new TextComponent[0]));
    }

    /**
     * Creates basecomponents from string
     * supports chatcolor
     * @param arg0 Converts string to BaseComponenets
     * @return BaseComponents
     */
    public static BaseComponent[] createBaseComponent(String arg0) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', arg0));
    }

    /**
     * Glues textcomponents together
     * @param arg0 The component separator
     * @param arg1 The TextComponent which should be joined
     * @return Joined TextComponents
     */
    public static TextComponent joinComponents(String arg0, TextComponent ...arg1) {
        TextComponent component = new TextComponent();
        for(int i = 0; i < arg1.length; i++) {
        	TextComponent arg = arg1[i];
        	if(i < 1) component.addExtra(arg);
        	else component.addExtra(mergeComponents(createComponent(arg0), arg));
        }
        return component;
    }

    /**
     * Glues textcomponents together
     * @param arg0 The component separator
     * @param arg1 The TextComponent which should be joined
     * @return Joined TextComponents
     */
    public static TextComponent joinComponents(String arg0, Iterable<TextComponent> arg1) {
        List<TextComponent> c = new ArrayList<>();
        arg1.forEach(c::add);
        return joinComponents(arg0, c.toArray(new TextComponent[0]));
    }

}