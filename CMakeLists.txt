cmake_minimum_required(VERSION 3.1)
project(aws-crt-java)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_INSTALL_PREFIX}/lib/cmake")

# Generate the java interface classes and headers first so they can be added to any generated projects 
add_subdirectory("java/com/amazon/aws")

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
    #platform libs come from aws-c-common transitively, so we don't specify them here, but for documentation purposes,
    #Kernel32 and wsock2 are pulled in automatically. Here we add the lib containing the schannel API.
    #Also note, you don't get a choice on TLS implementation for Windows.
    set(PLATFORM_LIBS Secur32)
    set(TLS_STACK_DETERMINED ON)

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
    endif ()
endif ()

file(GLOB CRT_JAVA_HEADERS
        ${AWS_CRT_JAVA_HEADERS}
        )

file(GLOB CRT_JAVA_SRC
        ${AWS_CRT_JAVA_SRC}
        ${AWS_CRT_JAVA_OS_SRC}
        )

add_library(${CMAKE_PROJECT_NAME} SHARED ${CRT_JAVA_HEADERS} ${CRT_JAVA_SRC})
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES LINKER_LANGUAGE C)

set(CMAKE_C_FLAGS_DEBUGOPT "")
set_property(TARGET ${CMAKE_PROJECT_NAME} PROPERTY C_STANDARD 99)

#set warnings
if (MSVC)
    target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE /W4 /WX)
else ()
    target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE -Wall -Wno-long-long -pedantic -Werror)
endif ()

set(BUILD_RELOCATABLE_BINARIES ON)
find_package(JNI)
include_directories(${JNI_INCLUDE_DIRS})
#set(PLATFORM_LIBS ${PLATFORM_LIBS} ${JNI_LIBRARIES})

if (CMAKE_BUILD_TYPE STREQUAL "" OR CMAKE_BUILD_TYPE MATCHES Debug)
    target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE "-DDEBUG_BUILD")
endif ()

target_include_directories(${CMAKE_PROJECT_NAME} PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
        $<INSTALL_INTERFACE:include>)

find_package(aws-c-common REQUIRED)
target_link_libraries(${CMAKE_PROJECT_NAME} aws-c-common ${PLATFORM_LIBS})

install(FILES ${AWS_CRT_JAVA_HEADERS} DESTINATION "include/aws/jni")
install(FILES ${AWS_CRT_JAVA_TESTING_HEADERS} DESTINATION "include/aws/testing")

install(
        TARGETS ${CMAKE_PROJECT_NAME} EXPORT ${CMAKE_PROJECT_NAME}-config
        ARCHIVE DESTINATION ${CMAKE_INSTALL_PREFIX}/lib
        LIBRARY DESTINATION ${CMAKE_INSTALL_PREFIX}/lib
        COMPONENT library
)

export(TARGETS ${CMAKE_PROJECT_NAME} FILE ${CMAKE_PROJECT_NAME}-config.cmake)
install(EXPORT ${CMAKE_PROJECT_NAME}-config DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/${CMAKE_PROJECT_NAME}/cmake/")

#include(CTest)
#add_subdirectory(tests)
