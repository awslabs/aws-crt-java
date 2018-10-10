
find_file(JUnit_JAR NAMES junit4.jar junit.jar HINTS /usr/share/java $ENV{JUNIT_HOME})

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(JUnit DEFAULT_MSG JUnit_JAR)
