# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(${CMAKE_SOURCE_DIR}/src/main/cpp/sqlite3)

# TODO: 根据平台选择开启 build.gradle中的jniLibs.srcDirs也需要修改
#set(RockIVA_DIR ${CMAKE_SOURCE_DIR}/libs/rockiva-rk3588-Android)
set(RockIVA_DIR ${CMAKE_SOURCE_DIR}/libs/rockiva-rk356x-Android)
find_package(RockIVA REQUIRED)
include_directories(${RockIVA_INCLUDE_DIRS})

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        rockxjni

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${CMAKE_SOURCE_DIR}/src/main/cpp/rockx-jni.cc
        ${CMAKE_SOURCE_DIR}/src/main/cpp/jobject_convert.cpp
        ${CMAKE_SOURCE_DIR}/src/main/cpp/utils/face_db.c

        )

include_directories(
        ${CMAKE_SOURCE_DIR}/src/main/cpp
)

#target_compile_options(rockxjni PUBLIC -fsanitize=address -fno-omit-frame-pointer)
#set_target_properties(rockxjni PROPERTIES LINK_FLAGS -fsanitize=address)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        rockxjni

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${RockIVA_LIBS}
        android
        sqlite3
        )