/* Use # error to output the arch, and then parse from cmake */
/* See https://sourceforge.net/p/predef/wiki/Architectures/ for reference */
#if defined(__i386) || defined(__i386__) || defined(_M_IX86)
#   error ARCH x86_32
#elif defined(__x86_64) || defined(__x86_64__) || defined(__amd64) || defined(_M_X64)
#   error ARCH x86_64
#elif defined(__aarch64__) 
#   error ARCH armv8
#elif defined(__arm__)
#    if defined(__ARM_ARCH_7__) || defined(__ARM_ARCH_7A__) || defined(__ARM_ARCH_7R__) || defined(__ARM_ARCH_7M__) || defined(__ARM_ARCH_7S__)
#        error ARCH armv7
#    elif defined(__ARM_ARCH_6__) || defined(__ARM_ARCH_6J__) || defined(__ARM_ARCH_6K__) || defined(__ARM_ARCH_6Z__) || defined(__ARM_ARCH_6ZK__) || defined(__ARM_ARCH_6T2__)
#        error ARCH armv6
#    endif
#else
#    error ARCH unknown
#endif

