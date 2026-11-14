package dev.che1s.trailtalesdelight.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AddonLang {
    private static final Map<String, String> MESSAGES = new ConcurrentHashMap<>();
    private static final Map<String, String> FALLBACK_MESSAGES = new ConcurrentHashMap<>();
    private static JavaPlugin plugin;
    private static String loadedLanguage = LanguageResolver.FALLBACK;

    private AddonLang() {
    }

    public static void init(JavaPlugin owner) {
        plugin = owner;
        reload();
    }

    public static void reload() {
        MESSAGES.clear();
        FALLBACK_MESSAGES.clear();
        List<Message> diagnostics = new ArrayList<>();
        String configured = plugin.getConfig().getString("language", LanguageResolver.AUTO);
        LanguageResolver.Selection selection =
                LanguageResolver.select(configured, Locale.getDefault());
        String locale = selection.language();
        if (selection.fallback()) {
            diagnostics.add(new Message("language.invalid_code",
                    "Could not resolve language '{code}' from server locale {server}; using en_us.",
                    "code", configured == null ? "auto" : configured,
                    "server", String.valueOf(selection.systemLocale())));
        }

        for (String languageCode : LanguageResolver.SUPPORTED) {
            ensureExternalLanguage(languageCode, diagnostics);
        }
        loadLanguage(LanguageResolver.FALLBACK, FALLBACK_MESSAGES);
        if (!loadLanguage(locale)) {
            diagnostics.add(new Message("language.load_failed",
                    "Could not load language '{code}', using en_us.",
                    "code", locale));
            locale = LanguageResolver.FALLBACK;
        }
        loadedLanguage = locale;
        for (Message diagnostic : diagnostics) {
            plugin.getLogger().warning(get(diagnostic.key(), diagnostic.fallback(), diagnostic.values()));
        }
    }

    public static String language() {
        return loadedLanguage;
    }

    private static boolean loadLanguage(String languageCode) {
        return loadLanguage(languageCode, MESSAGES);
    }

    private static boolean loadLanguage(String languageCode, Map<String, String> messages) {
        Path external = plugin.getDataFolder().toPath()
                .resolve("lang")
                .resolve(languageCode + ".yml");
        try {
            Reader reader;
            if (Files.isRegularFile(external)) {
                reader = Files.newBufferedReader(external, StandardCharsets.UTF_8);
            } else {
                var stream = plugin.getClass().getClassLoader()
                        .getResourceAsStream("lang/" + languageCode + ".yml");
                if (stream == null) {
                    return false;
                }
                reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            }
            try (reader) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(reader);
                for (String key : yaml.getKeys(true)) {
                    if (yaml.isString(key)) {
                        messages.put(key, yaml.getString(key));
                    }
                }
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static void ensureExternalLanguage(String languageCode, List<Message> diagnostics) {
        Path target = plugin.getDataFolder().toPath()
                .resolve("lang")
                .resolve(languageCode + ".yml");
        YamlConfiguration packaged = loadPackaged(languageCode);
        if (packaged == null) {
            diagnostics.add(new Message("language.missing_bundled",
                    "Bundled language file is missing: {code}",
                    "code", languageCode));
            return;
        }

        try {
            Files.createDirectories(target.getParent());
            YamlConfiguration external = Files.isRegularFile(target)
                    ? YamlConfiguration.loadConfiguration(target.toFile())
                    : new YamlConfiguration();
            boolean changed = !Files.isRegularFile(target);
            for (String key : packaged.getKeys(true)) {
                if (packaged.isString(key) && !external.isSet(key)) {
                    external.set(key, packaged.getString(key));
                    changed = true;
                }
            }
            if (changed) {
                external.save(target.toFile());
            }
        } catch (Exception exception) {
            diagnostics.add(new Message("language.save_failed",
                    "Could not prepare language file '{file}': {error}",
                    "file", target, "error", exception.getMessage()));
        }
    }

    private static YamlConfiguration loadPackaged(String languageCode) {
        String path = "lang/" + languageCode + ".yml";
        try (var stream = plugin.getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return YamlConfiguration.loadConfiguration(reader);
            }
        } catch (Exception exception) {
            return null;
        }
    }

    public static String get(String key, String fallback, Object... values) {
        String message = MESSAGES.getOrDefault(
                key,
                FALLBACK_MESSAGES.getOrDefault(key, fallback)
        );
        if (values != null) {
            for (int i = 0; i + 1 < values.length; i += 2) {
                message = message.replace("{" + values[i] + "}", String.valueOf(values[i + 1]));
            }
        }
        return message;
    }

    public static String get(String key) {
        return get(key, key);
    }

    private record Message(String key, String fallback, Object... values) {
    }
}
