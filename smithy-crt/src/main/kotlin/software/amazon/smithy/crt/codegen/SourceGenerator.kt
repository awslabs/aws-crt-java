package software.amazon.smithy.crt.codegen

import java.nio.file.Path
import java.util.function.BiConsumer
import java.util.function.Consumer

interface SourceGenerator : BiConsumer<Path, Consumer<Path>> {}
