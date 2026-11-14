package dev.che1s.trailtalesdelight.util;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Resolves the configured global language against the server JVM locale.
 * Configuration accepts either a bundled locale code or its YAML file name.
 */
public final class LanguageResolver {
    public static final String AUTO = "auto";
    public static final String FALLBACK = "en_us";
    public static final List<String> SUPPORTED = List.of(
            "en_us",
            "zh_cn",
            "ja_jp",
            "ko_kr",
            "zh_tw",
            "de_de",
            "fr_fr",
            "es_es",
            "ru_ru"
    );
    private static final Set<String> SUPPORTED_SET = Set.copyOf(SUPPORTED);

    private LanguageResolver() {
    }

    public static Selection select(String configured, Locale systemLocale) {
        String raw = configured == null || configured.isBlank() ? AUTO : configured.trim();
        if (AUTO.equalsIgnoreCase(raw)) {
            String detected = detect(systemLocale);
            String fallback = detected == null ? FALLBACK : detected;
            return new Selection(fallback, true, detected == null, systemLocale);
        }

        String normalized = normalizeConfigured(raw);
        String resolved = detect(parseLocale(normalized));
        if (resolved == null) {
            return new Selection(FALLBACK, false, true, systemLocale);
        }
        return new Selection(resolved, false, false, systemLocale);
    }

    public static String normalizeConfigured(String configured) {
        String value = configured.trim().toLowerCase(Locale.ROOT);
        if (value.endsWith(".yml")) {
            value = value.substring(0, value.length() - 4);
        }
        return value.replace('-', '_');
    }

    static String detect(Locale locale) {
        if (locale == null || locale.getLanguage() == null || locale.getLanguage().isBlank()) {
            return null;
        }
        String language = locale.getLanguage().toLowerCase(Locale.ROOT);
        String country = locale.getCountry().toLowerCase(Locale.ROOT);
        String script = locale.getScript().toLowerCase(Locale.ROOT);
        return switch (language) {
            case "en" -> "en_us";
            case "zh" -> country.equals("tw") || country.equals("hk") || country.equals("mo")
                    || script.equals("hant") ? "zh_tw" : "zh_cn";
            case "ja" -> "ja_jp";
            case "ko" -> "ko_kr";
            case "de" -> "de_de";
            case "fr" -> "fr_fr";
            case "es" -> "es_es";
            case "ru" -> "ru_ru";
            default -> null;
        };
    }

    public static boolean isSupported(String languageCode) {
        return SUPPORTED_SET.contains(languageCode);
    }

    private static Locale parseLocale(String configured) {
        String tag = configured.replace('_', '-');
        Locale locale = Locale.forLanguageTag(tag);
        return locale.getLanguage().isBlank() ? null : locale;
    }

    public record Selection(
            String language,
            boolean automatic,
            boolean fallback,
            Locale systemLocale
    ) {
    }
}
