
package software.amazon.smithy.crt.java;

import software.amazon.smithy.build.PluginContext;
import software.amazon.smithy.build.SmithyBuildPlugin;
import java.util.logging.Level

import java.util.logging.Logger;

class JavaCodegenPlugin : SmithyBuildPlugin {
    private val LOG = Logger.getLogger(JavaCodegenPlugin::class.simpleName);

    override fun getName(): String {
        return "crt"
    }

    override fun execute(pluginContext: PluginContext) {
        val root = Logger.getLogger("")
        root.level = Level.ALL
        root.handlers.forEach {
            it.level = Level.ALL
        }
        LOG.warning("Running JavaCodegenPlugin...");

        val generators = listOf<JavaFileGenerator>(
            AnnotationsGenerator(pluginContext),
            ServiceGenerator(pluginContext)
        )

        generators.forEach {
            it.accept { javaFile ->
                val javaFilePath = javaFile.writeToPath(pluginContext.fileManifest.baseDir);
                pluginContext.fileManifest.addFile(javaFilePath)
                LOG.info("Generated ${javaFilePath.toAbsolutePath()}")
            }
        }
    }
}
