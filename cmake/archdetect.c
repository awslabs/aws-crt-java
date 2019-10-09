/* Use # error to output the arch, and then parse from cmake */
#ifndef __clang_analyzer__
#    if defined(__i386) || defined(__i386__) || defined(_M_IX86)
#        error ARCH x86_32
#    elif defined(__x86_64) || defined(__x86_64__) || defined(__amd64) || defined(_M_X64)
#        error ARCH x86_64
#    elif defined(__arm__) || defined(__TARGET_ARCH_ARM)
#        if defined(__ARM_ARCH_8__) || (defined(__TARGET_ARCH_ARM) && __TARGET_ARCH_ARM - 0 >= 8)
#            error ARCH armv8
#        elif defined(__ARM_ARCH_7__) || (defined(__TARGET_ARCH_ARM) && __TARGET_ARCH_ARM - 0 >= 7)
#            error ARCH armv7
#        elif defined(__ARM_ARCH_6__) || (defined(__TARGET_ARCH_ARM) && __TARGET_ARCH_ARM - 0 >= 6)
#            error ARCH armv6
#        endif
#    endif
#    error ARCH unknown
#endif /*__clang_analyzer__ */
