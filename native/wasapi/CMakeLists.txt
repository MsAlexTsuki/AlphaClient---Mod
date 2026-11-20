cmake_minimum_required(VERSION 3.15)
project(alpha_wasapi LANGUAGES CXX)

if(NOT WIN32)
    message(FATAL_ERROR "Alpha WASAPI capture is only supported on Windows")
endif()

find_package(JNI REQUIRED)

add_library(alpha_wasapi SHARED WasapiLoopback.cpp)
target_include_directories(alpha_wasapi PRIVATE ${JNI_INCLUDE_DIRS})
target_compile_features(alpha_wasapi PRIVATE cxx_std_17)
target_compile_definitions(alpha_wasapi PRIVATE WIN32_LEAN_AND_MEAN NOMINMAX)
target_link_libraries(alpha_wasapi PRIVATE ole32 uuid)
set_target_properties(alpha_wasapi PROPERTIES
    OUTPUT_NAME "alpha_wasapi"
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
)
