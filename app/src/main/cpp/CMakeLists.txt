cmake_minimum_required(VERSION 3.22.1)

project("keyboard_core")

# Define the native shared library
add_library(
    keyboard_core
    SHARED
    keyboard_jni.cpp
)

# Find Android logging library
find_library(
    log-lib
    log
)

# Link libraries
target_link_libraries(
    keyboard_core
    ${log-lib}
)
