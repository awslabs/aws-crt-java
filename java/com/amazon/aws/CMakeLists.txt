
find_package(Java 1.7 REQUIRED)

set(AWS_JNI_HEADER_DIR "${PROJECT_SOURCE_DIR}/include/aws/jni")
file(GLOB AWS_CRT_JAVA_SRC "*.java")

# as of JDK 10, javah was moved to javac -h
# if the javah tool exists, then use it (two pass), otherwise attempt java -h (single pass)
if (Java_JAVAH_EXECUTABLE)
    message(STATUS "Compiling Java interfaces...")
    execute_process(
        COMMAND "${Java_JAVAC_EXECUTABLE}" "-d" "${CMAKE_BINARY_DIR}/java" ${AWS_CRT_JAVA_SRC}
    )

    # convert file paths to .class files into class names (inputs to javah)
    file(GLOB_RECURSE AWS_CRT_JAVA_CLASS_FILES "${CMAKE_BINARY_DIR}/java/*.class")
    foreach(JAVA_CLASS ${AWS_CRT_JAVA_CLASS_FILES})
        file(RELATIVE_PATH JAVA_CLASS "${CMAKE_BINARY_DIR}/java" ${JAVA_CLASS})
        string(REPLACE ".class" "" JAVA_CLASS ${JAVA_CLASS})
        string(REPLACE "/" "." JAVA_CLASS ${JAVA_CLASS})
        list(APPEND AWS_CRT_JAVA_CLASSES ${JAVA_CLASS})
    endforeach()

    message(STATUS "Generating JNI headers...")
    execute_process(
        COMMAND "${Java_JAVAH_EXECUTABLE}" "-classpath" "${CMAKE_BINARY_DIR}/java" "-d" "${PROJECT_SOURCE_DIR}/include/aws/jni" ${AWS_CRT_JAVA_CLASSES}
    )
else() # just use javac
    message(STATUS "Compiling Java interfaces and generating headers...")
    execute_process(
        COMMAND "${Java_JAVAC_EXECUTABLE}" "-d" "${CMAKE_BINARY_DIR}/java" "-h" "${AWS_JNI_HEADER_DIR}" ${AWS_CRT_JAVA_SRC}
    )
endif()
message(STATUS "Done.")
