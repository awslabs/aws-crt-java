
package software.amazon.smithy.crt.java;

import software.amazon.smithy.build.PluginContext;
import software.amazon.smithy.build.SmithyBuildPlugin;
import software.amazon.smithy.codegen.core.CodegenException;
import software.amazon.smithy.model.Model;

import java.util.logging.Logger;

class JavaCodegenPlugin : SmithyBuildPlugin {
    private val LOG = Logger.getLogger(JavaCodegenPlugin::class.simpleName);

    override fun getName(): String {
        return "crt"
    }

    override fun execute(pluginContext: PluginContext) {
        LOG.warning("Running JavaCodegenPlugin...");

        val model = pluginContext.model;

        val generators = listOf<JavaFileGenerator>(DataModelGenerator(pluginContext))

        generators.forEach {
            it.accept { javaFile ->
                val path = pluginContext.fileManifest.baseDir.resolve(it.getOutputSubdirectory())
                val javaFilePath = javaFile.writeToPath(path);
                pluginContext.fileManifest.addFile(javaFilePath)
                LOG.info("Generated {}".format(javaFilePath.toAbsolutePath().toString()))
            }
        }
    }
}
