cmake_minimum_required(VERSION 3.1)

project(aws-crt-jni C)
option(BUILD_DEPS "Builds aws common runtime dependencies as part of build" ON)

if (POLICY CMP0069)
    cmake_policy(SET CMP0069 NEW) # Enable LTO/IPO if available in the compiler, see AwsCFlags
endif()

if (POLICY CMP0077)
    cmake_policy(SET CMP0077 NEW) # Enable options to get their values from normal variables
endif()

if (DEFINED CMAKE_PREFIX_PATH)
    file(TO_CMAKE_PATH "${CMAKE_PREFIX_PATH}" CMAKE_PREFIX_PATH)
endif()

if (DEFINED CMAKE_INSTALL_PREFIX)
    file(TO_CMAKE_PATH "${CMAKE_INSTALL_PREFIX}" CMAKE_INSTALL_PREFIX)
endif()

if (UNIX AND NOT APPLE)
    include(GNUInstallDirs)
elseif(NOT DEFINED CMAKE_INSTALL_LIBDIR)
    set(CMAKE_INSTALL_LIBDIR "lib")
endif()

if (${CMAKE_INSTALL_LIBDIR} STREQUAL "lib64")
    set(FIND_LIBRARY_USE_LIB64_PATHS true)
endif()

if (NOT WIN32 AND NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE "RelWithDebInfo")
endif()

# This is required in order to append /lib/cmake to each element in CMAKE_PREFIX_PATH
set(AWS_MODULE_DIR "/${CMAKE_INSTALL_LIBDIR}/cmake")
string(REPLACE ";" "${AWS_MODULE_DIR};" AWS_MODULE_PATH "${CMAKE_PREFIX_PATH}${AWS_MODULE_DIR}")
# Append that generated list to the module search path
list(APPEND CMAKE_MODULE_PATH ${AWS_MODULE_PATH})

# Local CMake scripts
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/cmake")

set(AWS_LIBRARY_OUTPUT_DIR "${CMAKE_BINARY_DIR}/lib")

if (ANDROID)
    set(ANDROID_DEPS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/target/cmake-build/deps/${ANDROID_ABI})
    set(LibCrypto_INCLUDE_DIR "${ANDROID_DEPS_DIR}/libcrypto/include" CACHE INTERNAL "")
    set(LibCrypto_STATIC_LIBRARY "${ANDROID_DEPS_DIR}/libcrypto/lib/libcrypto.a" CACHE INTERNAL "")
    set(LibCrypto_SHARED_LIBRARY "${ANDROID_DEPS_DIR}/libcrypto/lib/libcrypto.so" CACHE INTERNAL "")

    if (NOT EXISTS ${LibCrypto_INCLUDE_DIR}/openssl/crypto.h OR
        NOT EXISTS ${LibCrypto_STATIC_LIBRARY})
        message(FATAL_ERROR "Could not find libcrypto at ${CMAKE_BINARY_DIR}/deps/libcrypto")
    endif()
    set(AWS_LIBRARY_OUTPUT_DIR "${CMAKE_BINARY_DIR}/../../../lib")
endif()

if (BUILD_DEPS)
    list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/aws-common-runtime/aws-c-common/cmake")
    include(AwsFindPackage)

    set(IN_SOURCE_BUILD ON)
    set(BUILD_TESTING OFF)
    add_subdirectory(aws-common-runtime/aws-c-common)
    if (UNIX AND NOT APPLE)
        add_subdirectory(aws-common-runtime/s2n)
    endif()
    add_subdirectory(aws-common-runtime/aws-c-io)
    add_subdirectory(aws-common-runtime/aws-c-cal)
    add_subdirectory(aws-common-runtime/aws-c-compression)
    add_subdirectory(aws-common-runtime/aws-c-http)
    add_subdirectory(aws-common-runtime/aws-c-auth)
    add_subdirectory(aws-common-runtime/aws-c-mqtt)
else()
    include(AwsFindPackage)
    include(AwsCFlags)
    set(IN_SOURCE_BUILD OFF)
endif()

include(UseJava)
include(AwsPlatformDetect)

file(GLOB AWS_CRT_JAVA_HEADERS
        "include/aws/jni/*.h"
        )

file(GLOB AWS_CRT_JAVA_SRC
        "src/native/*.c"
        )

if (WIN32)
    file(GLOB AWS_CRT_JAVA_OS_SRC
            "src/native/windows/*.c"
            )

    if (MSVC)
        source_group("Header Files\\aws\\jni" FILES ${AWS_CRT_JAVA_HEADERS})
        source_group("Source Files" FILES ${AWS_CRT_JAVA_SRC})
        source_group("Source Files\\windows" FILES ${AWS_CRT_JAVA_OS_SRC})
    endif ()

elseif (CMAKE_SYSTEM_NAME STREQUAL "Linux")
    file(GLOB AWS_CRT_JAVA_OS_SRC
            "src/native/linux/*.c"
            "src/native/posix/*.c"
            )
elseif (APPLE)
    file(GLOB AWS_CRT_JAVA_OS_SRC
            "src/native/bsd/*.c"
            "src/native/posix/*.c"
            "src/native/darwin/*.c"
            )

elseif (CMAKE_SYSTEM_NAME STREQUAL "FreeBSD" OR CMAKE_SYSTEM_NAME STREQUAL "NetBSD")
    file(GLOB AWS_CRT_JAVA_OS_SRC
            "src/native/bsd/*.c"
            "src/native/posix/*.c"
            )
endif()

file(GLOB CRT_JAVA_HEADERS
        ${AWS_CRT_JAVA_HEADERS}
        )

file(GLOB CRT_JAVA_SRC
        ${AWS_CRT_JAVA_SRC}
        ${AWS_CRT_JAVA_OS_SRC}
        )

# figure out the target platform info and output the lib there
aws_detect_target_platform(TARGET_OS TARGET_ARCH)
message(STATUS "Compiling for ${TARGET_OS}-${TARGET_ARCH}")
set(TARGET_LIB_DIR "${AWS_LIBRARY_OUTPUT_DIR}/${TARGET_OS}/${TARGET_ARCH}")

# shared lib that contains the CRT and JNI bindings, to be loaded by java
add_library(${PROJECT_NAME} SHARED ${CRT_JAVA_HEADERS} ${CRT_JAVA_SRC})
aws_use_package(aws-c-http)
aws_use_package(aws-c-mqtt)
aws_use_package(aws-c-auth)

target_link_libraries(${PROJECT_NAME} ${DEP_AWS_LIBS})

set_target_properties(${PROJECT_NAME} PROPERTIES LINKER_LANGUAGE C)
# for non-msvc, library output will control the shared lib output
set_target_properties(${PROJECT_NAME} PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${TARGET_LIB_DIR})
# for msvc, runtime controls where dlls are output
set_target_properties(${PROJECT_NAME} PROPERTIES RUNTIME_OUTPUT_DIRECTORY ${TARGET_LIB_DIR})
set_target_properties(${PROJECT_NAME} PROPERTIES RUNTIME_OUTPUT_DIRECTORY_DEBUG ${TARGET_LIB_DIR})
set_target_properties(${PROJECT_NAME} PROPERTIES RUNTIME_OUTPUT_DIRECTORY_RELEASE ${TARGET_LIB_DIR})
set_target_properties(${PROJECT_NAME} PROPERTIES RUNTIME_OUTPUT_DIRECTORY_RELWITHDEBINFO ${TARGET_LIB_DIR})

aws_set_common_properties(${PROJECT_NAME})

set(CMAKE_C_FLAGS_DEBUGOPT "")
set_property(TARGET ${PROJECT_NAME} PROPERTY C_STANDARD 99)

#set warnings
if (MSVC)
    target_compile_options(${PROJECT_NAME} PRIVATE /W4 /WX -D_CRT_SECURE_NO_WARNINGS)
else ()
    target_compile_options(${PROJECT_NAME} PRIVATE -Wall -Wno-long-long -pedantic -Werror)
endif ()

# find JNI and add the include path (except on android where gradle injects it)
if (NOT ANDROID)
    set(JAVA_AWT_LIBRARY NotNeeded)
    set(JAVA_AWT_INCLUDE_PATH NotNeeded)
    find_package(JNI REQUIRED)
    target_include_directories(${PROJECT_NAME} PRIVATE ${JNI_INCLUDE_DIRS})
endif()

target_include_directories(${PROJECT_NAME} PRIVATE "src/native")

if (CMAKE_BUILD_TYPE STREQUAL "" OR CMAKE_BUILD_TYPE MATCHES Debug)
    target_compile_definitions(${PROJECT_NAME} PRIVATE "-DDEBUG_BUILD")
endif ()

target_include_directories(${PROJECT_NAME} PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
        $<INSTALL_INTERFACE:include>)
