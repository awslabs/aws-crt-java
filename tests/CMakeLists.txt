
find_package(java 1.7 REQUIRED)
find_package(JUnit)

include(UseJava)

if (NOT JUnit_FOUND)
    message(STATUS "JUnit could not be found, please ensure that JUNIT_HOME is defined in your environment. Tests will be disabled.")
    return()
endif()

file(GLOB_RECURSE AWS_JAVA_TEST_SRC "com/amazon/aws/*.java")

function (aws_add_junit_test NAME)
    add_junit_test(
        ${NAME}
        CLASSPATH "${CMAKE_BINARY_DIR}/aws-crt-java.jar" "${CMAKE_BINARY_DIR}/tests/aws-crt-java-test.jar"
        ${ARGN}
    )
endfunction()

# add JUnit to CLASSPATH for jar compilation
list(APPEND CMAKE_JAVA_INCLUDE_PATH ${JUnit_JAR} )

add_jar(aws-crt-java-test 
    SOURCES ${AWS_JAVA_TEST_SRC}
    OUTPUT_NAME aws-crt-java-test
    INCLUDE_JARS aws-crt-java
    )

aws_add_junit_test(BasicBindingTest TESTS com.amazon.aws.BasicBindingTest)
