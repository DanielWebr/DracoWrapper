#include <cinttypes>
#include <cstdlib>

#include "draco/compression/encode.h"
#include "draco/core/cycle_timer.h"
#include "draco/io/file_utils.h"
#include "draco/io/mesh_io.h"
#include "draco/io/point_cloud_io.h"

namespace {

struct Options {
  Options();

  bool is_point_cloud;
  int pos_quantization_bits;
  int compression_level;
  std::string input;
  std::string output;
};

Options::Options()
    : is_point_cloud(false), 
    pos_quantization_bits(11),
      compression_level(7) {}


int StringToInt(const std::string &s) {
  char *end;
  return strtol(s.c_str(), &end, 10);  // NOLINT
}
int EncodeMeshToFile(const draco::Mesh &mesh, const std::string &file,
                     draco::Encoder *encoder) {
 
  // Encode the geometry.
  draco::EncoderBuffer buffer;

  const draco::Status status = encoder->EncodeMeshToBuffer(mesh, &buffer);
  if (!status.ok()) {
    printf("CHYBA:\n");
    printf("%s\n", status.error_msg());
    return -1;
  }

  if (!draco::WriteBufferToFile(buffer.data(), buffer.size(), file)) {
    printf("CHYBA: nebyl vytvoren soubor.\n");
    return -1;
  }
  return 0;
}

}  // anonymous namespace

int main(int argc, char **argv) {
  Options options;
  const int argc_check = argc - 1;
  

  options.input = argv[1];
  options.output = options.input + ".bin";
  options.compression_level = StringToInt(argv[2]);
  options.pos_quantization_bits = 11 - options.compression_level;


  std::unique_ptr<draco::PointCloud> pc;
  draco::Mesh *mesh = nullptr;
    auto maybe_mesh =
        draco::ReadMeshFromFile(options.input, false);
    if (!maybe_mesh.ok()) {
      printf("CHYBA: nebyl nalezen vstupni soubor.\n",
             maybe_mesh.status().error_msg());
      return -1;
    }
    mesh = maybe_mesh.value().get();
    pc = std::move(maybe_mesh).value();
 
  const int speed = 10 - options.compression_level;

  draco::Encoder encoder;

  encoder.SetAttributeQuantization(draco::GeometryAttribute::POSITION,
                                     options.pos_quantization_bits);
  
  encoder.SetSpeedOptions(speed, speed);


  int ret = -1;
  if (mesh && mesh->num_faces() > 0)
    ret = EncodeMeshToFile(*mesh, options.output, &encoder);

  return ret;
}
