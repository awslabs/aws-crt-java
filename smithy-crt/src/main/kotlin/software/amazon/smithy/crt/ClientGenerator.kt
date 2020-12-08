
package software.amazon.smithy.crt.java;

import software.amazon.smithy.model.Model;

import com.squareup.javapoet.JavaFile;

import java.util.function.Consumer;
import java.util.logging.Logger;

class ClientGenerator : JavaFileGenerator {
    val LOG = Logger.getLogger(ClientGenerator::class.simpleName)

    val model: Model;

    constructor(model: Model) {
        this.model = model;
    }

    override fun accept(javaFileConsumer: Consumer<JavaFile>) {

    }

    override fun getOutputSubdirectory(): String {
        return "client";
    }
}
