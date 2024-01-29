cmake_minimum_required(VERSION 3.4)

project(sqlite3)

add_library(sqlite3 STATIC src/sqlite3.c)

target_include_directories(sqlite3 PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)