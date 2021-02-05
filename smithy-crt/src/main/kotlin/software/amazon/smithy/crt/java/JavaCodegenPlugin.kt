
package software.amazon.smithy.crt.java;

import software.amazon.smithy.build.PluginContext;
import software.amazon.smithy.build.SmithyBuildPlugin;
import software.amazon.smithy.crt.codegen.SourceGenerator
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

        try {

            val generators = listOf(
                AnnotationsGenerator(pluginContext),
                ServiceGenerator(pluginContext)
            )

            generators.forEach {
                it.accept(pluginContext.fileManifest.baseDir) { javaFile ->
                    pluginContext.fileManifest.addFile(javaFile)
                    LOG.info("Generated ${javaFile.toAbsolutePath()}")
                }
            }
        } catch (ex: Throwable) {
            System.err.println(ex.message)
        }
    }
}
