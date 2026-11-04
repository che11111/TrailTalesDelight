import java.util.Properties

rootProject.name = "trailtalesdelight"

// 本地 FarmersDelight 路径通过 Gradle 属性、环境变量或忽略提交的 local.properties 指定。
// 仅当目录存在时启用 Included Build，避免把个人路径写入版本库。
// 否则 Gradle 会从公共仓库解析固定版本的源码工件。
val farmersDelightRepo = "https://github.com/IOVEYOUMC0/Farmersdelight-Plugin.git"
val localProperties = Properties()
val localPropertiesFile = file("local.properties")
if (localPropertiesFile.isFile) {
    localPropertiesFile.reader(Charsets.UTF_8).use { localProperties.load(it) }
}
val configuredCheckout = providers.gradleProperty("farmersDelightCheckout").orNull
    ?: providers.environmentVariable("FARMERSDELIGHT_CHECKOUT").orNull
    ?: localProperties.getProperty("farmersDelightCheckout")
val farmersDelightCheckout = configuredCheckout
    ?.takeIf { it.isNotBlank() }
    ?.let(::file)
    ?.takeIf { it.isDirectory }

sourceControl {
    gitRepository(uri(farmersDelightRepo)) {
        producesModule("com.huidu.farmersdelight:farmersdelight-plugin")
    }
}

if (farmersDelightCheckout != null) {
    includeBuild(farmersDelightCheckout) {
        name = "farmersdelight-plugin"
    }
}
