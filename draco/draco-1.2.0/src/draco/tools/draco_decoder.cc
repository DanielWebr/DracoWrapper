#include <cinttypes>
#include <fstream>

#include "draco/compression/decode.h"
#include "draco/io/obj_encoder.h"
#include "draco/io/parser_utils.h"
#include "draco/io/ply_encoder.h"

namespace {

    struct Options {
        Options();
        std::string input;
        std::string output;
    };

    Options::Options() {}

    int ReturnError(const draco::Status& status) {
        printf("Chyba dekodovani souboru %s\n", status.error_msg());
        return -1;
    }

}  // namespace

int main(int argc, char** argv) {
    Options options;
    const int argc_check = argc - 1;
    options.input = argv[1];
    options.output = "mesh.obj";
    

    std::ifstream input_file(options.input, std::ios::binary);
    if (!input_file) {
        printf("CHYBA: Nepodarilo se otevrit zdrojovy soubor.\n");
        return -1;
    }

    std::streampos file_size = 0;
    input_file.seekg(0, std::ios::end);
    file_size = input_file.tellg() - file_size;
    input_file.seekg(0, std::ios::beg);
    std::vector<char> data(file_size);
    input_file.read(data.data(), file_size);

    if (data.empty()) {
        printf("CHYBA: Prazdny zdrojovy soubor.\n");
        return -1;
    }


    draco::DecoderBuffer buffer;
    buffer.Init(data.data(), data.size());

   
    std::unique_ptr<draco::PointCloud> pc;
    draco::Mesh* mesh = nullptr;
    auto type_statusor = draco::Decoder::GetEncodedGeometryType(&buffer);
    if (!type_statusor.ok()) {
        return ReturnError(type_statusor.status());
    }
    const draco::EncodedGeometryType geom_type = type_statusor.value();
    if (geom_type == draco::TRIANGULAR_MESH) {
    
        draco::Decoder decoder;
        auto statusor = decoder.DecodeMeshFromBuffer(&buffer);
        if (!statusor.ok()) {
            return ReturnError(statusor.status());
        }
        std::unique_ptr<draco::Mesh> in_mesh = std::move(statusor).value();
   
        if (in_mesh) {
            mesh = in_mesh.get();
            pc = std::move(in_mesh);
        }
    }

    if (pc == nullptr) {
        printf("Chyba dekodovani souboru.\n");
        return -1;
    }

    const std::string extension = draco::parser::ToLower(
        options.output.size() >= 4
        ? options.output.substr(options.output.size() - 4)
        : options.output);

    if (extension == ".obj") {
        draco::ObjEncoder obj_encoder;
        if (mesh) {
            if (!obj_encoder.EncodeToFile(*mesh, options.output)) {
                printf("Chyba ukladani jako OBJ.\n");
                return -1;
            }
        }
        else {
            if (!obj_encoder.EncodeToFile(*pc.get(), options.output)) {
                printf("Chyba ukladani jako OBJ.\n");
                return -1;
            }
        }
    }

    return 0;
}
