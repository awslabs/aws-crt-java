
# save the folder when this file is included, for use later
set(THIS_FILE_DIR ${CMAKE_CURRENT_LIST_DIR})

function(aws_detect_target_platform out_os out_arch)
    if (CMAKE_CROSSCOMPILING AND CMAKE_SYSTEM_NAME STREQUAL "Android")
        set(${out_os} "android" PARENT_SCOPE)
        set(${out_arch} "${ANDROID_ABI}" PARENT_SCOPE)
        return()
    endif()
    try_compile(
        RESULT_UNUSED
        ${CMAKE_CURRENT_BINARY_DIR}
        SOURCES "${THIS_FILE_DIR}/osdetect.c"
        OUTPUT_VARIABLE OS_OUTPUT
    )
    # Find the error in the output, then strip the identifier off
    string(REGEX MATCH "OS ([a-zA-Z]+)" OS "${OS_OUTPUT}")
    string(REPLACE "OS " "" OS "${OS}")

    try_compile(
        RESULT_UNUSED
        ${CMAKE_CURRENT_BINARY_DIR}
        SOURCES "${THIS_FILE_DIR}/archdetect.c"
        OUTPUT_VARIABLE ARCH_OUTPUT
    )

    # Find the error in the output, then strip the identifier off
    string(REGEX MATCH "ARCH ([a-zA-Z0-9_]+)" ARCH "${ARCH_OUTPUT}")
    string(REPLACE "ARCH " "" ARCH "${ARCH}")

    set(${out_os} "${OS}" PARENT_SCOPE)
    set(${out_arch} "${ARCH}" PARENT_SCOPE)
endfunction()
