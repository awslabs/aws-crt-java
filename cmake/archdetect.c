/* Use # error to output the arch, and then parse from cmake */
#if defined(__i386) || defined(__i386__) || defined(_M_IX86)
#   error ARCH x86_32
#elif defined(__x86_64) || defined(__x86_64__) || defined(__amd64) || defined(_M_X64)
#   error ARCH x86_64
#elif defined(__aarch64__) || defined(__arm__)
#   if defined(__ARM_ARCH) && __ARM_ARCH >= 8
#       error ARCH armv8
#   elif defined(__ARM_ARCH) && __ARM_ARCH >= 7
#       error ARCH armv7
#   elif defined(__ARM_ARCH) && __ARM_ARCH >= 6
#       error ARCH armv6
#   endif
#else
#    error ARCH unknown
#endif

