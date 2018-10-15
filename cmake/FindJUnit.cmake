
if (DEFINED ENV{JUNIT_HOME})
    file(GLOB JUnit_JAR "$ENV{JUNIT_HOME}/junit*.jar")
    if (NOT JUnit_JAR)
        message(STATUS "No junit*.jar found in JUNIT_HOME, please check your JUnit installation")
    endif()
else()
    message(STATUS "JUNIT_HOME is not defined in your environment, JUNIT will not be available")
    return()
endif()

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(JUnit DEFAULT_MSG JUnit_JAR)

function(add_junit_test TARGET_NAME)
    if (WIN32 AND NOT CYGWIN)
        set(SEPARATOR ";")
    else (WIN32 AND NOT CYGWIN)
        set(SEPARATOR ":")
    endif(WIN32 AND NOT CYGWIN)

    # Add JUnit, CRT Java libs, test JAR to the CLASSPATH
    set(CLASSPATH ${JUnit_JAR}${SEPARATOR}${CMAKE_JAVA_TARGET_OUTPUT_DIR})

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

    add_test(
        NAME ${TARGET_NAME} 
        COMMAND ${Java_JAVA_EXECUTABLE} ${JVMARGS} -classpath "${CLASSPATH}" org.junit.runner.JUnitCore ${TESTS})
endfunction(add_junit_test)
