
set(RockIVA_INCLUDE_DIRS ${CMAKE_CURRENT_LIST_DIR}/include)

if (CMAKE_SYSTEM_NAME MATCHES "Android")
    set(RockIVA_LIBS
        ${CMAKE_CURRENT_LIST_DIR}/${CMAKE_ANDROID_ARCH_ABI}/librknnrt.so
        ${CMAKE_CURRENT_LIST_DIR}/${CMAKE_ANDROID_ARCH_ABI}/librockiva.so
    )
else ()
    set(RockIVA_LIBS
        ${CMAKE_CURRENT_LIST_DIR}/lib64/librknnrt.so
        ${CMAKE_CURRENT_LIST_DIR}/lib64/librockiva.so
    )
endif()
