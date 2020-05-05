#include <cinttypes>
#include <cstdlib>
#include <fstream>

#include "draco/compression/encode.h"
#include "draco/io/mesh_io.h"
#include "draco/io/point_cloud_io.h"

namespace {

struct Options {
  Options();

  bool is_point_cloud;
  int compression_level;
  std::string input;
  std::string output;
};

Options::Options()
    : is_point_cloud(false),
      compression_level(7) {}



int StringToInt(const std::string &s) {
  char *end;
  return strtol(s.c_str(), &end, 10);
}
int EncodeMeshToFile(const draco::Mesh &mesh, const std::string &file,
                     draco::Encoder *encoder) {
  draco::EncoderBuffer buffer;

  const draco::Status status = encoder->EncodeMeshToBuffer(mesh, &buffer);
  if (!status.ok()) {
    printf("CHYBA:\n");
    printf("%s\n", status.error_msg());
    return -1;
  }

  std::ofstream out_file(file, std::ios::binary);
  if (!out_file) {
    printf("CHYBA: nebyl vytvoren soubor.\n");
    return -1;
  }
  out_file.write(buffer.data(), buffer.size());
  return 0;
}

} 

int main(int argc, char **argv) {
  Options options;
  const int argc_check = argc - 1;
  
  options.input = argv[1];
  options.compression_level = StringToInt(argv[2]);


  std::unique_ptr<draco::PointCloud> pc;
  draco::Mesh *mesh = nullptr;

    std::unique_ptr<draco::Mesh> in_mesh =
        draco::ReadMeshFromFile(options.input, false);
    if (!in_mesh) {
      printf("CHYBA: nebyl nalezen vstupni soubor.\n");
      return -1;
    }
    mesh = in_mesh.get();
    pc = std::move(in_mesh);
 


  const int speed = 10 - options.compression_level;

  draco::Encoder encoder;
  encoder.SetSpeedOptions(speed, speed);
  encoder.SetEncodingMethod(0);

  options.output = options.input + ".bin";

  int ret = -1;
  if (mesh && mesh->num_faces() > 0)
    ret = EncodeMeshToFile(*mesh, options.output, &encoder);

  return ret;
}
