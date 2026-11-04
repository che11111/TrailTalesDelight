package dev.che1s.trailtalesdelight.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 以版本感知方式安装本附属的 CraftEngine 资源包。
 *
 * 启用自动补全时，每次启动都会恢复缺失文件。仅在插件版本变化时覆盖内置文件，
 * 因此当前版本下的本地修改能在普通重启后保留。版本更新会替换所属资源包内容，
 * 并删除新版本不再分发的文件。
 */
public final class AddonResourceInstaller {
    private static final String MARKER = ".trail-resources-version";

    private AddonResourceInstaller() {
    }

    public static InstallResult install(JavaPlugin plugin, String namespace, String version,
                                        boolean autoCompletion, boolean updateOnVersionChange) {
        try {
            Path pluginsDirectory = plugin.getDataFolder().toPath().getParent();
            if (pluginsDirectory == null) {
                return InstallResult.NONE;
            }
            URI source = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            Path jar = Path.of(source);
            if (!Files.isRegularFile(jar)) {
                return InstallResult.NONE;
            }

            Path resourcesRoot = pluginsDirectory.resolve("CraftEngine").resolve("resources").normalize();
            Path targetRoot = resourcesRoot.resolve(namespace).normalize();
            if (!targetRoot.startsWith(resourcesRoot)) {
                return InstallResult.NONE;
            }

            Path marker = targetRoot.resolve(MARKER);
            boolean firstInstall = !Files.isDirectory(targetRoot);
            boolean versionChanged = !Files.isRegularFile(marker)
                    || !version.equals(Files.readString(marker, StandardCharsets.UTF_8).trim());
            boolean replaceExisting = firstInstall || (updateOnVersionChange && versionChanged);
            if (!replaceExisting && !autoCompletion) {
                return InstallResult.NONE;
            }

            InstallResult result = copyPack(
                    jar, "craftengine/" + namespace + "/", targetRoot, replaceExisting);
            if (replaceExisting || result.copied() > 0) {
                Files.createDirectories(targetRoot);
                Files.writeString(marker, version, StandardCharsets.UTF_8);
            }
            return result;
        } catch (Exception exception) {
            plugin.getLogger().warning(AddonLang.get(
                    "plugin.resources_install_failed",
                    "Could not install CraftEngine resources: {error}",
                    "error", exception.getMessage()
            ));
            return InstallResult.NONE;
        }
    }

    static InstallResult copyPack(Path jarPath, String prefix, Path targetRoot, boolean replaceExisting)
            throws Exception {
        int copied = 0;
        Set<Path> expectedFiles = new HashSet<>();
        try (ZipFile zip = new ZipFile(jarPath.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith(prefix)) {
                    continue;
                }
                Path target = targetRoot.resolve(entry.getName().substring(prefix.length())).normalize();
                if (!target.startsWith(targetRoot) || (!replaceExisting && Files.exists(target))) {
                    continue;
                }
                expectedFiles.add(target);
                Files.createDirectories(target.getParent());
                try (InputStream input = zip.getInputStream(entry)) {
                    Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                }
            }
        }
        int removed = replaceExisting
                ? deleteStaleFiles(targetRoot, expectedFiles)
                : 0;
        return new InstallResult(copied, removed);
    }

    private static int deleteStaleFiles(Path targetRoot, Set<Path> expectedFiles) throws Exception {
        if (!Files.isDirectory(targetRoot)) {
            return 0;
        }
        int removed = 0;
        try (Stream<Path> paths = Files.walk(targetRoot)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                Path normalized = path.normalize();
                if (!normalized.equals(targetRoot.resolve(MARKER))
                        && !expectedFiles.contains(normalized)) {
                    Files.deleteIfExists(normalized);
                    removed++;
                }
            }
        }
        try (Stream<Path> paths = Files.walk(targetRoot)) {
            for (Path directory : paths
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(targetRoot))
                    .sorted(Comparator.comparingInt(Path::getNameCount).reversed())
                    .toList()) {
                try {
                    Files.deleteIfExists(directory);
                } catch (DirectoryNotEmptyException ignored) {
                    // 保留仍包含当前资源包文件的目录。
                }
            }
        }
        return removed;
    }

    public record InstallResult(int copied, int removed) {
        private static final InstallResult NONE = new InstallResult(0, 0);

        public boolean changed() {
            return copied > 0 || removed > 0;
        }
    }
}
