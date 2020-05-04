// Copyright 2016 The Draco Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
#include "draco/io/mesh_io.h"

#include <fstream>

#include "draco/io/obj_decoder.h"
#include "draco/io/parser_utils.h"
#include "draco/io/ply_decoder.h"

namespace draco {

namespace {

// Returns the file extension in lowercase if present, else ""
inline std::string LowercaseFileExtension(const std::string &filename) {
  size_t pos = filename.find_last_of('.');
  if (pos == std::string::npos || pos >= filename.length() - 1)
    return "";
  return parser::ToLower(filename.substr(pos + 1));
}

}  // namespace

std::unique_ptr<Mesh> ReadMeshFromFile(const std::string &file_name) {
  return ReadMeshFromFile(file_name, false);
}

std::unique_ptr<Mesh> ReadMeshFromFile(const std::string &file_name,
                                       bool use_metadata) {
  std::unique_ptr<Mesh> mesh(new Mesh());
  // Analyze file extension.
  const std::string extension = LowercaseFileExtension(file_name);
  if (extension == "obj") {
    // Wavefront OBJ file format.
    ObjDecoder obj_decoder;
    obj_decoder.set_use_metadata(use_metadata);
    if (!obj_decoder.DecodeFromFile(file_name, mesh.get()))
      return nullptr;
    return mesh;
  }
  if (extension == "ply") {
    // Wavefront PLY file format.
    PlyDecoder ply_decoder;
    if (!ply_decoder.DecodeFromFile(file_name, mesh.get()))
      return nullptr;
    return mesh;
  }

  // Otherwise not an obj file. Assume the file was encoded with one of the
  // draco encoding methods.
  std::ifstream is(file_name.c_str(), std::ios::binary);
  if (!is)
    return nullptr;
  if (!ReadMeshFromStream(&mesh, is).good())
    return nullptr;  // Error reading the stream.
  return mesh;
}

}  // namespace draco
