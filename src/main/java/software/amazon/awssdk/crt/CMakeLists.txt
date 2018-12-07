
find_package(Java 1.8 REQUIRED)

set(AWS_JNI_HEADER_DIR "${PROJECT_SOURCE_DIR}/include/aws/jni")
set(AWS_JNI_BINARY_DIR "${CMAKE_BINARY_DIR}/java")
file(GLOB AWS_CRT_JAVA_SRC "*.java" "mqtt/*.java")

# clean up previous java compiled classes
file(REMOVE_RECURSE "${AWS_JNI_BINARY_DIR}")
file(MAKE_DIRECTORY "${AWS_JNI_BINARY_DIR}")
file(REMOVE_RECURSE "${AWS_JNI_HEADER_DIR}")
file(MAKE_DIRECTORY "${AWS_JNI_HEADER_DIR}")

# as of JDK 10, javah was moved to javac -h
# if the javah tool exists, then use it (two pass), otherwise attempt java -h (single pass)
if (Java_JAVAH_EXECUTABLE)
    message(STATUS "Compiling Java interfaces...")
    execute_process(
        COMMAND "${Java_JAVAC_EXECUTABLE}" "-d" "${AWS_JNI_BINARY_DIR}" ${AWS_CRT_JAVA_SRC}
    )

    # convert file paths to .class files into class names (inputs to javah)
    file(GLOB_RECURSE AWS_CRT_JAVA_CLASS_FILES "${AWS_JNI_BINARY_DIR}/*.class")
    foreach(JAVA_CLASS ${AWS_CRT_JAVA_CLASS_FILES})
        file(RELATIVE_PATH JAVA_CLASS "${AWS_JNI_BINARY_DIR}" ${JAVA_CLASS})
        string(REPLACE ".class" "" JAVA_CLASS ${JAVA_CLASS})
        string(REPLACE "/" "." JAVA_CLASS ${JAVA_CLASS})
        list(APPEND AWS_CRT_JAVA_CLASSES ${JAVA_CLASS})
    endforeach()

    message(STATUS "Generating JNI headers...")
    execute_process(
        COMMAND "${Java_JAVAH_EXECUTABLE}" "-classpath" "${AWS_JNI_BINARY_DIR}" "-d" "${PROJECT_SOURCE_DIR}/include/aws/jni" ${AWS_CRT_JAVA_CLASSES}
    )
else() # just use javac
    message(STATUS "Compiling Java interfaces and generating headers...")
    execute_process(
        COMMAND "${Java_JAVAC_EXECUTABLE}" "-d" "${AWS_JNI_BINARY_DIR}" "-h" "${AWS_JNI_HEADER_DIR}" ${AWS_CRT_JAVA_SRC}
    )
endif()
message(STATUS "Done.")
