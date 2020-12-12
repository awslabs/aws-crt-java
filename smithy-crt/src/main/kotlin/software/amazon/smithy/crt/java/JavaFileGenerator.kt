
package software.amazon.smithy.crt.java;

import com.squareup.javapoet.JavaFile;

import java.util.function.Consumer;

interface JavaFileGenerator : Consumer<Consumer<JavaFile>> {

}
