/* Use #error to emit os, parse from cmake */
#if defined(_WIN32)
#   error OS windows
#elif defined(__APPLE__)
#    include <TargetConditionals.h>
#    if TARGET_OS_MAC == 1
#        error OS osx
#    endif
#elif defined(__linux__)
#   error OS linux
#elif defined(__FreeBSD__)
#   error OS freebsd
#elif defined(__ANDROID__)
#   error OS android
#elif defined(__sun)
#   error OS solaris
#endif
#error OS unknown
