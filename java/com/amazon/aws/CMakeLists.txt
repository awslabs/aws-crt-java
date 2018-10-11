
find_package(Java 1.7 REQUIRED)

file(GLOB AWS_CRT_JAVA_FILES "*.java")

message(STATUS "Compiling Java interfaces...")
execute_process(
    COMMAND "${Java_JAVAC_EXECUTABLE}" "-d" "${CMAKE_BINARY_DIR}/java" ${AWS_CRT_JAVA_FILES}
    )
message(STATUS "Done.")

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
message(STATUS "Done.")
