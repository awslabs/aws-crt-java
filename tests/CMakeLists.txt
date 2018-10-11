cmake_minimum_required(VERSION 3.1)
project(aws-crt-java-test)

file(GLOB TEST_SRC "*.c")
file(GLOB TEST_HDR "*.h")

set(TEST_BINARY_NAME ${CMAKE_PROJECT_NAME})

find_package(aws-c-common REQUIRED)

find_package(JNI)
include_directories(${JNI_INCLUDE_DIRS})
set(PLATFORM_LIBS ${PLATFORM_LIBS} ${JNI_LIBRARIES})

add_library(${TEST_BINARY_NAME} SHARED ${TEST_SRC})
#aws_set_common_properties(${TEST_BINARY_NAME} NO_WEXTRA NO_PEDANTIC)
#aws_add_sanitizers(${TEST_BINARY_NAME} ${${CMAKE_PROJECT_NAME}_SANITIZERS})
target_link_libraries(${TEST_BINARY_NAME} aws-c-common)
target_compile_definitions(${TEST_BINARY_NAME} PRIVATE AWS_UNSTABLE_TESTING_API=1)
target_include_directories(${TEST_BINARY_NAME} PRIVATE "${CMAKE_CURRENT_LIST_DIR}/include")

