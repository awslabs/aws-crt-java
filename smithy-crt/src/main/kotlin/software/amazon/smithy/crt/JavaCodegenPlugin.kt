
package software.amazon.smithy.crt.java;

import software.amazon.smithy.build.PluginContext;
import software.amazon.smithy.build.SmithyBuildPlugin;
import software.amazon.smithy.codegen.core.CodegenException;
import software.amazon.smithy.model.Model;

import java.util.logging.Logger;

public class JavaCodegenPlugin : SmithyBuildPlugin {
    val LOG = Logger.getLogger(JavaCodegenPlugin::class.simpleName);

    override fun getName(): String {
        return "crt"
    }

    override fun execute(pluginContext: PluginContext) {
        LOG.info("Running JavaCodegenPlugin...");
    }
}
