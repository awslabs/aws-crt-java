cmake_minimum_required(VERSION 3.9)

project(aws-crt-jni C)
option(BUILD_DEPS "Builds aws common runtime dependencies as part of build" ON)
option(CRT_FIPS "Whether to build aws-lc with FIPS compliance" OFF)

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

message(STATUS "CMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}")

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
    set(AWS_LIBRARY_OUTPUT_DIR "${CMAKE_BINARY_DIR}/../../../lib")
endif()

if (BUILD_DEPS)
    list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/crt/aws-c-common/cmake")
    include(AwsFindPackage)

    set(IN_SOURCE_BUILD ON)
    set(BUILD_TESTING OFF)
    add_subdirectory(crt/aws-c-common)
    if (UNIX AND NOT APPLE)
        include(AwsPrebuildDependency)

        set(AWSLC_CMAKE_ARGUMENTS
            -DBUILD_LIBSSL=OFF
            -DBUILD_TESTING=OFF
        )

        message("Build with FIPS: " ${CRT_FIPS})
        if (CRT_FIPS)
            list(APPEND AWSLC_CMAKE_ARGUMENTS -DFIPS=ON)
            list(APPEND AWSLC_CMAKE_ARGUMENTS -DDISABLE_PERL=OFF)
        else()
            list(APPEND AWSLC_CMAKE_ARGUMENTS -DDISABLE_PERL=ON)  # Disable codegen
            list(APPEND AWSLC_CMAKE_ARGUMENTS -DDISABLE_GO=ON)  # Disable codegen
        endif()

        if(CMAKE_C_COMPILER_ID MATCHES "GNU" AND CMAKE_C_COMPILER_VERSION VERSION_LESS "5.0")
            # Disable AVX512 on old GCC that not supports it
            list(APPEND AWSLC_CMAKE_ARGUMENTS -DMY_ASSEMBLER_IS_TOO_OLD_FOR_512AVX=ON)
        endif()

        if (ANDROID)
            list(APPEND AWSLC_CMAKE_ARGUMENTS -DANDROID_DEPS_DIR=${ANDROID_DEPS_DIR})
            list(APPEND AWSLC_CMAKE_ARGUMENTS -DAWS_LIBRARY_OUTPUT_DIR="${AWS_LIBRARY_OUTPUT_DIR}")
        endif()

        # s2n-tls uses libcrypto during its configuration, so we need to prebuild aws-lc.
        aws_prebuild_dependency(
            DEPENDENCY_NAME AWSLC
            SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/crt/aws-lc
            CMAKE_ARGUMENTS ${AWSLC_CMAKE_ARGUMENTS}
        )

        set(UNSAFE_TREAT_WARNINGS_AS_ERRORS OFF CACHE BOOL "Disable warnings-as-errors when building S2N")
        add_subdirectory(crt/s2n)
    endif()
    add_subdirectory(crt/aws-c-sdkutils)
    add_subdirectory(crt/aws-c-io)
    add_subdirectory(crt/aws-c-cal)
    add_subdirectory(crt/aws-c-compression)
    add_subdirectory(crt/aws-c-http)
    add_subdirectory(crt/aws-c-auth)
    add_subdirectory(crt/aws-c-mqtt)
    add_subdirectory(crt/aws-checksums)
    add_subdirectory(crt/aws-c-event-stream)
    add_subdirectory(crt/aws-c-s3)
else()
    include(AwsFindPackage)
    include(AwsCFlags)
    set(IN_SOURCE_BUILD OFF)
endif()

include(UseJava)
include(AwsPlatformDetect)
include(AwsSharedLibSetup)
include(AwsCRuntime)

if (CRT_FIPS AND NOT AWSLC_PREBUILT)
    message(FATAL_ERROR "CRT_FIPS can only be set when build with aws-lc.")
endif()

aws_determine_local_c_runtime(AWS_C_RUNTIME)

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
set(TARGET_LIB_DIR "${AWS_LIBRARY_OUTPUT_DIR}/${TARGET_OS}/${TARGET_ARCH}/${AWS_C_RUNTIME}")

# shared lib that contains the CRT and JNI bindings, to be loaded by java
add_library(${PROJECT_NAME} SHARED ${CRT_JAVA_HEADERS} ${CRT_JAVA_SRC})

# link the high-level libraries that will recursively pull in the rest
# (don't repeat dependencies here, or the linker will spit out warnings)
aws_use_package(aws-c-mqtt)
aws_use_package(aws-c-event-stream)
aws_use_package(aws-c-s3)

target_link_libraries(${PROJECT_NAME} ${DEP_AWS_LIBS})
if (NOT MSVC AND NOT APPLE)
    set_property(TARGET ${PROJECT_NAME} APPEND_STRING PROPERTY LINK_FLAGS " -z noexecstack")
endif()

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
    if (NOT APPLE)
        set(JAVA_AWT_LIBRARY NotNeeded)
        set(JAVA_AWT_INCLUDE_PATH NotNeeded)
    endif()
    if (DEFINED ENV{OVERRIDE_JAVA_HOME})
        # This override might be set by builder to avoid using the JAVA_HOME modified by gradle
        file(TO_CMAKE_PATH "$ENV{OVERRIDE_JAVA_HOME}" OVERRIDE_JAVA_HOME)
        set(JAVA_HOME "${OVERRIDE_JAVA_HOME}")
        message(STATUS "Overriding JAVA_HOME to ${OVERRIDE_JAVA_HOME}")
    endif()
    message(STATUS "JAVA_HOME=$ENV{JAVA_HOME}")
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

aws_split_debug_info(${PROJECT_NAME})
