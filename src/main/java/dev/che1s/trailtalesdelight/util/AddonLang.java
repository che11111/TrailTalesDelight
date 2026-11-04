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
import java.util.regex.Pattern;

public final class AddonLang {
    private static final String DEFAULT_LANGUAGE = "zh_cn";
    private static final Pattern LANGUAGE_CODE = Pattern.compile("[a-z0-9_.-]+");
    private static final Map<String, String> MESSAGES = new ConcurrentHashMap<>();
    private static JavaPlugin plugin;
    private static String loadedLanguage = DEFAULT_LANGUAGE;

    private AddonLang() {
    }

    public static void init(JavaPlugin owner) {
        plugin = owner;
        reload();
    }

    public static void reload() {
        MESSAGES.clear();
        List<Message> diagnostics = new ArrayList<>();
        String configured = plugin.getConfig().getString("language", DEFAULT_LANGUAGE);
        String locale = configured == null || configured.isBlank()
                ? DEFAULT_LANGUAGE
                : configured.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        if ("auto".equals(locale)) {
            locale = Locale.getDefault().toString().toLowerCase(Locale.ROOT).replace('-', '_');
        }
        if (!LANGUAGE_CODE.matcher(locale).matches()) {
            diagnostics.add(new Message("language.invalid_code",
                    "Invalid language code '{code}', using zh_cn.",
                    "code", locale));
            locale = DEFAULT_LANGUAGE;
        }

        ensureExternalLanguage(DEFAULT_LANGUAGE, false, diagnostics);
        ensureExternalLanguage("en_us", false, diagnostics);
        ensureExternalLanguage(locale, true, diagnostics);
        if (!loadLanguage(locale)) {
            diagnostics.add(new Message("language.load_failed",
                    "Could not load language '{code}', using zh_cn.",
                    "code", locale));
            loadLanguage(DEFAULT_LANGUAGE);
            locale = DEFAULT_LANGUAGE;
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
                        MESSAGES.put(key, yaml.getString(key));
                    }
                }
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static void ensureExternalLanguage(String languageCode, boolean seedFromDefault,
                                               List<Message> diagnostics) {
        Path target = plugin.getDataFolder().toPath()
                .resolve("lang")
                .resolve(languageCode + ".yml");
        YamlConfiguration packaged = loadPackaged(languageCode);
        if (packaged == null && seedFromDefault) {
            packaged = loadPackaged(DEFAULT_LANGUAGE);
        }
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
        String message = MESSAGES.getOrDefault(key, fallback);
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
