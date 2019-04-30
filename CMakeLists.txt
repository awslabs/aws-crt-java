cmake_minimum_required(VERSION 3.1)

if (UNIX AND NOT APPLE)
    include(GNUInstallDirs)
elseif(NOT DEFINED CMAKE_INSTALL_LIBDIR)
    set(CMAKE_INSTALL_LIBDIR "lib")
endif()

if (${CMAKE_INSTALL_LIBDIR} STREQUAL "lib64")
    set(FIND_LIBRARY_USE_LIB64_PATHS true)
endif()

option(BUILD_DEPS "Builds aws common runtime dependencies as part of build" ON)

if (BUILD_DEPS)
    set(AWS_DEPS_BUILD_DIR ${CMAKE_CURRENT_BINARY_DIR}/deps)
    set(AWS_DEPS_INSTALL_DIR ${CMAKE_CURRENT_BINARY_DIR}/deps/install)

    list(APPEND CMAKE_MODULE_PATH "${AWS_DEPS_INSTALL_DIR}/lib/cmake")
    list(APPEND CMAKE_MODULE_PATH "${AWS_DEPS_INSTALL_DIR}/lib64/cmake")

    if (NOT DEFINED CMAKE_PREFIX_PATH)
        set(CMAKE_PREFIX_PATH ${AWS_DEPS_INSTALL_DIR})
    endif()

    file(MAKE_DIRECTORY ${AWS_DEPS_BUILD_DIR})

    # run another cmake to do the deps build
    if(TARGET_ARCH STREQUAL "ANDROID")
        execute_process(
                COMMAND ${CMAKE_COMMAND} -G ${CMAKE_GENERATOR}
                -DTARGET_ARCH=${TARGET_ARCH}
                -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
                -DANDROID_NATIVE_API_LEVEL=${ANDROID_NATIVE_API_LEVEL}
                -DANDROID_ABI=${ANDROID_ABI}
                -DANDROID_TOOLCHAIN_NAME=${ANDROID_TOOLCHAIN_NAME}
                -DANDROID_STANDALONE_TOOLCHAIN=${ANDROID_STANDALONE_TOOLCHAIN}
                -DANDROID_STL=${ANDROID_STL}
                -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
                -DCMAKE_PREFIX_PATH=${AWS_DEPS_INSTALL_DIR}
                -DCMAKE_INSTALL_PREFIX=${AWS_DEPS_INSTALL_DIR}
                -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
                -DGIT_EXECUTABLE=${GIT_EXECUTABLE}
                -DCMAKE_C_FLAGS=${CMAKE_C_FLAGS}
                ${CMAKE_CURRENT_SOURCE_DIR}/aws-common-runtime
                WORKING_DIRECTORY ${AWS_DEPS_BUILD_DIR}
        )

    else()
        execute_process(
                COMMAND ${CMAKE_COMMAND} -G ${CMAKE_GENERATOR}
                -DTARGET_ARCH=${TARGET_ARCH}
                -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
                -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
                -DCMAKE_PREFIX_PATH=${AWS_DEPS_INSTALL_DIR}
                -DCMAKE_INSTALL_PREFIX=${AWS_DEPS_INSTALL_DIR}
                -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}
                -DCMAKE_RUNTIME_OUTPUT_DIRECTORY=${CMAKE_CURRENT_BINARY_DIR}/bin
                -DCMAKE_C_FLAGS=${CMAKE_C_FLAGS}
                ${CMAKE_CURRENT_SOURCE_DIR}/aws-common-runtime
                WORKING_DIRECTORY ${AWS_DEPS_BUILD_DIR}
                RESULT_VARIABLE BUILD_AWSCRT_EXIT_CODE
        )
    endif()

    if (NOT ${BUILD_AWSCRT_EXIT_CODE} EQUAL 0)
        message(FATAL_ERROR "Failed to configure aws crt libraries.")
    endif()
    execute_process(COMMAND ${CMAKE_COMMAND} --build ${AWS_DEPS_BUILD_DIR} --config ${CMAKE_BUILD_TYPE}
            RESULT_VARIABLE BUILD_AWSCRT_EXIT_CODE)

    if (NOT ${BUILD_AWSCRT_EXIT_CODE} EQUAL 0)
        message(FATAL_ERROR "Failed to build aws crt libraries.")
    endif()
    message(STATUS "AWS Common Runtime is installed at: ${AWS_DEPS_INSTALL_DIR}")

    #the following two lines are done in this branch intentionally, don't move it. project() does some magic that
    #we don't want happening until we're done with the above code.
    list(APPEND CMAKE_PREFIX_PATH "${AWS_DEPS_INSTALL_DIR}")
    project(aws-crt-jni C Java)
else()
    #the following two lines are done in this branch intentionally, don't move it. project() does some magic that
    #we want happening exactly right now.
    project(aws-crt-jni C Java)

    list(APPEND CMAKE_MODULE_PATH "${CMAKE_PREFIX_PATH}/${CMAKE_INSTALL_LIBDIR}/cmake")
endif()

# Local CMake scripts
list(APPEND CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/cmake")

include(UseJava)
include(AwsPlatformDetect)

# Generate the java interface classes and headers first so they can be added to any generated projects
add_subdirectory("src/main/java/software/amazon/awssdk/crt")

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
set(TARGET_LIB_DIR "lib/${TARGET_OS}/${TARGET_ARCH}")

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
    target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE /W4 /WX -D_CRT_SECURE_NO_WARNINGS)
else ()
    target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE -Wall -Wno-long-long -pedantic -Werror)
endif ()

# find JNI and add the include path
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS} "src/native")

if (CMAKE_BUILD_TYPE STREQUAL "" OR CMAKE_BUILD_TYPE MATCHES Debug)
    target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE "-DDEBUG_BUILD")
endif ()

target_include_directories(${CMAKE_PROJECT_NAME} PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
        $<INSTALL_INTERFACE:include>)

find_package(aws-c-mqtt REQUIRED)
target_link_libraries(${CMAKE_PROJECT_NAME} AWS::aws-c-mqtt)

find_package(aws-c-http REQUIRED)
target_link_libraries(${CMAKE_PROJECT_NAME} AWS::aws-c-http)
