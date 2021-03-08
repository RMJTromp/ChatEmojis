package com.rmjtromp.chatemojis.utils;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Language translation manager class.
 * @author Melvin
 * @since 2.2.1
 */
public final class Lang {
	
	private Lang() {
		throw new IllegalStateException("Utility class");
	}
	
	private static YamlConfiguration language = null;
	private static YamlConfiguration layeredLanguage = null;
	
	/**
	 * Loads a language file to Lang instance
	 * @param file The file which it should load language from
	 */
	public static void load(File file) {
		language = YamlConfiguration.loadConfiguration(file);
		layeredLanguage = null;
	}

	/**
	 * Loads a language from a string reader instance
	 * @param reader The reader which it should load language from
	 */
	public static void load(Reader reader) {
		language = YamlConfiguration.loadConfiguration(reader);
		layeredLanguage = null;
	}

	/**
	 * Layers a language file on top of loaded language instance
	 * @param file The language file which should be layered
	 */
	public static void layer(File file) {
		layeredLanguage = YamlConfiguration.loadConfiguration(file);
	}

	/**
	 * Layers a StringReader on top of loaded language instance
	 * @param reader The StringReader which should be layered
	 */
	public static void layer(StringReader reader) {
		layeredLanguage = YamlConfiguration.loadConfiguration(reader);
	}
	
	/**
	 * Returns the value for the layered language first
	 * if no value is found, it returns the default
	 * if no value is found, it throw a {@link NullPointerException}
	 * @param key The key which should be sought for
	 * @param replacements The placeholder replacements
	 * @return Translated version of the expected value
	 */
	public static String translate(@NotNull String key, @NotNull Replacements replacements) {
		if(key.isEmpty()) throw new NullPointerException("Translation key can not be empty");
		if(replacements.map.isEmpty()) return translate(key);
		if(layeredLanguage == null && language == null) throw new NullPointerException("No language file provided");
		
		if(layeredLanguage != null && (layeredLanguage.isString(key) || layeredLanguage.isList(key))) {
			String string = layeredLanguage.isString(key) ? layeredLanguage.getString(key) : String.join("\n", layeredLanguage.getStringList(key));

			if(string != null) {
				// parse placeholders
				for(Entry<String, String> entry : replacements.entrySet()) {
					Pattern pattern = Pattern.compile(Pattern.quote("${"+entry.getKey()+"}"), Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(string);
					if(matcher.find()) string = matcher.replaceAll(entry.getValue());
				}
				return string;
			}
		} else if(language != null && (language.isString(key) || language.isList(key))) {
			String string = language.isString(key) ? language.getString(key) : String.join("\n", language.getStringList(key));

			if(string != null) {
				// parse placeholders
				for(Entry<String, String> entry : replacements.entrySet()) {
					Pattern pattern = Pattern.compile(Pattern.quote("${"+entry.getKey()+"}"), Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(string);
					if(matcher.find()) string = matcher.replaceAll(entry.getValue());
				}
				return string;
			}
		}
		throw new NullPointerException(String.format("Could not find key `%s` in language files.", key));
	}

	/**
	 * Returns the value for the layered language first
	 * if no value is found, it returns the default
	 * if no value is found, it throw a {@link NullPointerException}
	 * @param key The key which should be sought for
	 * @return Translated version of the expected value
	 */
	public static String translate(@NotNull String key) {
		if(key.isEmpty()) throw new NullPointerException("Translation key can not be empty");
		if(layeredLanguage == null && language == null) throw new NullPointerException("No language file provided");
		
		if(layeredLanguage != null && (layeredLanguage.isString(key) || layeredLanguage.isList(key))) {
			return layeredLanguage.isString(key) ? layeredLanguage.getString(key) : String.join("\n", layeredLanguage.getStringList(key));
		} else if(language != null && (language.isString(key) || language.isList(key))) {
			return language.isString(key) ? language.getString(key) : String.join("\n", language.getStringList(key));
		} else throw new NullPointerException(String.format("Could not find key `%s` in language files.", key));
	}
	
	public static final class Replacements {
		
		private final HashMap<String, String> map = new HashMap<>();
		
		public static Replacements singleton(String key, String value) {
			Replacements r = new Replacements();
			r.add(key, value);
			return r;
		}
		
		public Replacements add(String key, String value) {
			map.put(key, value);
			return this;
		}
		
		public Set<Entry<String, String>> entrySet() {
			return map.entrySet();
		}
		
	}

}
