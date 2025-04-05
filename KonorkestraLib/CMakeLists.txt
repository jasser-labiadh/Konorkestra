cmake_minimum_required(VERSION 3.14)  # Ensure you use CMake 3.14 or later

project(KonorkestraLib)

set(CMAKE_CXX_STANDARD 20)

# FetchContent for Protobuf
include(FetchContent)

# Fetch Protobuf if it's not already installed on the system
FetchContent_Declare(
        protobuf
        GIT_REPOSITORY https://github.com/protocolbuffers/protobuf.git
        GIT_TAG v3.19.4  # Specify the version of Protobuf you want
)

# Make Protobuf available
FetchContent_MakeAvailable(protobuf)

# Set Protobuf directories
set(PROTOBUF_INCLUDE_DIR ${protobuf_SOURCE_DIR}/src)
set(PROTOBUF_LIBRARIES protobuf::libprotobuf)

# Add the target for your library
add_library(KonorkestraLib SHARED UdsClient.cpp)

# Link Protobuf with your library
target_include_directories(KonorkestraLib PRIVATE ${PROTOBUF_INCLUDE_DIR})
target_link_libraries(KonorkestraLib PRIVATE ${PROTOBUF_LIBRARIES})

# Add the Protobuf generated files
add_dependencies(KonorkestraLib protobuf)