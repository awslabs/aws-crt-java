
find_package(java 1.7 REQUIRED)
find_package(JUnit)

include(UseJava)

if (NOT JUnit_FOUND)
    message(STATUS "JUnit could not be found, please ensure that JUNIT_HOME is defined in your environment. Tests will be disabled.")
    return()
endif()

file(GLOB_RECURSE AWS_JAVA_TEST_SRC "com/amazon/aws/*.java")

set(AWS_JAVA_CLASSPATH ${CMAKE_INSTALL_PREFIX}/lib)

function(add_junit_test TARGET_NAME)
    if (WIN32 AND NOT CYGWIN)
        set(SEPARATOR ";")
    else (WIN32 AND NOT CYGWIN)
        set(SEPARATOR ":")
    endif(WIN32 AND NOT CYGWIN)

    # Add JUnit, CRT Java libs, test JAR to the CLASSPATH
    set(CLASSPATH ${JUnit_JAR}${SEPARATOR}${AWS_JAVA_CLASSPATH}${SEPARATOR}${CMAKE_JAVA_TARGET_OUTPUT_DIR})
    

    foreach (ARG ${ARGN})
        if (ARG MATCHES "CLASSPATH" OR ARG MATCHES "TESTS" OR ARG MATCHES "JVMARGS")
            set(TYPE ${ARG})
        else ()
            if (TYPE MATCHES "CLASSPATH")
                set(CLASSPATH "${CLASSPATH}${SEPARATOR}${ARG}")
            elseif (TYPE MATCHES "TESTS")
                set(TESTS ${TESTS} ${ARG})
            elseif (TYPE MATCHES "JVMARGS")
                set(JVMARGS ${JVMARGS} ${ARG})
            endif()
        endif()
    endforeach(ARG)

    add_test(NAME ${TARGET_NAME} COMMAND ${Java_JAVA_EXECUTABLE} ${JVMARGS} -classpath ${CLASSPATH} org.junit.runner.JUnitCore ${TESTS})
endfunction(add_junit_test)

# add JUnit to CLASSPATH for jar compilation
list(APPEND CMAKE_JAVA_INCLUDE_PATH ${JUnit_JAR})

add_jar(aws-crt-java-test 
    SOURCES ${AWS_JAVA_TEST_SRC}
    OUTPUT_NAME aws-crt-java-test
    INCLUDE_JARS aws-crt-java
    )

add_junit_test(BasicBindingTest TESTS com.amazon.aws.BasicBindingTest.testDoIt)
