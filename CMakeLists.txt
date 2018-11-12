cmake_minimum_required(VERSION 3.1)
project(aws-crt-jni LANGUAGES C Java)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_INSTALL_PREFIX}/lib/cmake")
list(APPEND CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/cmake")

include(UseJava)
include(AwsPlatformDetect)

# Generate the java interface classes and headers first so they can be added to any generated projects 
add_subdirectory("java/software/amazon/awssdk/crt")

file(GLOB AWS_CRT_JAVA_HEADERS
        "include/aws/jni/*.h"
        )

file(GLOB AWS_CRT_JAVA_SRC
        "source/*.c"
        )

if (WIN32)
    file(GLOB AWS_CRT_JAVA_OS_SRC
            "source/windows/*.c"
            )

    if (MSVC)
        source_group("Header Files\\aws\\jni" FILES ${AWS_CRT_JAVA_HEADERS})
        source_group("Source Files" FILES ${AWS_CRT_JAVA_SRC})
        source_group("Source Files\\windows" FILES ${AWS_CRT_JAVA_OS_SRC})
    endif ()

elseif (CMAKE_SYSTEM_NAME STREQUAL "Linux")
    file(GLOB AWS_CRT_JAVA_OS_SRC
            "source/linux/*.c"
            "source/posix/*.c"
            )
    set(PLATFORM_LIBS "")
elseif (APPLE)
    file(GLOB AWS_CRT_JAVA_OS_SRC
            "source/bsd/*.c"
            "source/posix/*.c"
            "source/darwin/*.c"
            )

    find_library(SECURITY_LIB Security)
    if (NOT SECURITY_LIB)
        message(FATAL_ERROR "Security framework not found")
    endif ()

    #No choice on TLS for apple, darwinssl will always be used.
    set(PLATFORM_LIBS ${SECURITY_LIB})
    set(TLS_STACK_DETERMINED ON)

elseif (CMAKE_SYSTEM_NAME STREQUAL "FreeBSD" OR CMAKE_SYSTEM_NAME STREQUAL "NetBSD")
    file(GLOB AWS_CRT_JAVA_OS_SRC
            "source/bsd/*.c"
            "source/posix/*.c"
            )
endif()

if (NOT CUSTOM_TLS)
    if (USE_S2N)
        set(TLS_STACK_DETERMINED ON)
    endif ()
    if (NOT TLS_STACK_DETERMINED)
        if (NOT WIN32 AND NOT APPLE)
            set(USE_S2N ON)
        endif ()
    endif ()

    if (USE_S2N)
        set(PLATFORM_LIBS ${PlATFORM_LIBS} s2n)
	find_package(s2n REQUIRED)
    endif ()
endif ()

file(GLOB CRT_JAVA_HEADERS
        ${AWS_CRT_JAVA_HEADERS}
        )

file(GLOB CRT_JAVA_SRC
        ${AWS_CRT_JAVA_SRC}
        ${AWS_CRT_JAVA_OS_SRC}
        )

# figure out the target platform info and output the lib there
aws_detect_target_platform(TARGET_OS TARGET_ARCH)
set(TARGET_LIB_DIR "${TARGET_OS}/${TARGET_ARCH}")

# shared lib that contains the CRT and JNI bindings, to be loaded by java
add_library(${CMAKE_PROJECT_NAME} SHARED ${CRT_JAVA_HEADERS} ${CRT_JAVA_SRC})
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES LINKER_LANGUAGE C)
# for non-msvc, library output will control the shared lib output
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${TARGET_LIB_DIR})
# for msvc, runtime controls where dlls are output
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES RUNTIME_OUTPUT_DIRECTORY ${TARGET_LIB_DIR})
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES RUNTIME_OUTPUT_DIRECTORY_DEBUG ${TARGET_LIB_DIR})
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES RUNTIME_OUTPUT_DIRECTORY_RELEASE ${TARGET_LIB_DIR})

set(CMAKE_C_FLAGS_DEBUGOPT "")
set_property(TARGET ${CMAKE_PROJECT_NAME} PROPERTY C_STANDARD 99)

#set warnings
if (MSVC)
    target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE /W4 /WX)
else ()
    target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE -Wall -Wno-long-long -pedantic -Werror)
endif ()

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS} source)
#set(PLATFORM_LIBS ${PLATFORM_LIBS} ${JNI_LIBRARIES})

find_package(JUnit)

# if we can test, include the test code
if (JUnit_FOUND)
    target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE "-DENABLE_JNI_TESTS")
endif()

if (CMAKE_BUILD_TYPE STREQUAL "" OR CMAKE_BUILD_TYPE MATCHES Debug)
    target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE "-DDEBUG_BUILD")
endif ()

target_include_directories(${CMAKE_PROJECT_NAME} PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
        $<INSTALL_INTERFACE:include>)

find_package(aws-c-common REQUIRED)
find_package(aws-c-io REQUIRED)
find_package(aws-c-mqtt REQUIRED)
target_link_libraries(${CMAKE_PROJECT_NAME} AWS::aws-c-common aws-c-io aws-c-mqtt ${PLATFORM_LIBS})

file(GLOB_RECURSE AWS_CRT_JAVA_SRC "java/software/amazon/awssdk/crt/*.java")

# create a jar to aggregate the JNI interfaces
add_jar(
    aws-crt-java
    SOURCES ${AWS_CRT_JAVA_SRC}
)

add_dependencies(aws-crt-java ${CMAKE_PROJECT_NAME})

# add the shared lib to the JNI jar after it's compiled (this will always run, because it is not
# currently possible to estabilish a proper build dependency against a JAR target added with add_jar()
add_custom_target(aws-crt-java-shared-lib-package ALL DEPENDS aws-crt-java ${CMAKE_PROJECT_NAME} ${CMAKE_BINARY_DIR}/always_rebuild)
add_dependencies(aws-crt-java-shared-lib-package aws-crt-java)
add_custom_command(
    OUTPUT ${CMAKE_BINARY_DIR}/always_rebuild
    COMMAND "${Java_JAR_EXECUTABLE}" "uf" "${CMAKE_BINARY_DIR}/aws-crt-java.jar" "${TARGET_LIB_DIR}/$<TARGET_FILE_NAME:${CMAKE_PROJECT_NAME}>"
    COMMENT "Adding shared libs to aws-crt-java.jar"
)

install(
        FILES ${CMAKE_BINARY_DIR}/aws-crt-java.jar
        DESTINATION ${CMAKE_INSTALL_PREFIX}/lib
        COMPONENT library
)

if (NOT JUnit_FOUND)
    message(STATUS "JUnit could not be found, please ensure that JUNIT_HOME is defined in your environment. Tests will be disabled.")
    return()
endif()

enable_testing()
add_subdirectory(tests)
