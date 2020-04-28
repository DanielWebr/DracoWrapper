#include <iostream>
#include <algorithm>
#include <iterator>
#include <fstream>
#include  "../../draco/compression/encode.h"
#include "../../draco/compression/config/compression_shared.h"
#include "../../draco/compression/config/encoder_options.h"
#include "../../draco/compression/encode_base.h"
#include "../../draco/core/encoder_buffer.h"
#include "../../draco/core/status.h"
#include "../../draco/mesh/mesh.h"
#include "../../draco/io/ply_encoder.h"
#include "../../draco/io/obj_decoder.h"
#include "../../draco/io/stdio_file_reader.h"
#include "../../draco/io/stdio_file_writer.h"
#include "../../draco/io/file_writer_factory.h"
#include "../../draco/io/file_reader_factory.h"
#include "../../draco/io/file_writer_interface.h"
#include "../../draco/io/file_reader_interface.h"
#include "../../draco/io/file_utils.h"
#include "../../draco/io/mesh_io.h"

int main(int argc, char* argv[])
{
    // print all command line arguments
    std::cout << "name of program: " << argv[0] << '\n';

    if (argc > 1)
    {
        std::cout << "there are " << argc - 1 << " (more) arguments, they are:\n";

        std::copy(argv + 1, argv + argc, std::ostream_iterator<const char*>(std::cout, "\n"));
    }
       
    draco::EncoderBuffer enc_buffer;
    draco::Encoder encoder;

    encoder.EncodeMeshToBuffer(*draco_mesh, &enc_buffer);
    std::ofstream out("/home/lukasz/Desktop/cpr/cube.drc", std::ios::binary);
    if (out.is_open())
    {
        draco::EncoderOptions encoderOptions = draco::EncoderOptions::CreateDefaultOptions();
        encoderOptions.SetSpeed(1, 1);  // encode and decode speed between 0 and 10
        draco::WriteMeshIntoStream(draco_mesh.get(), out, draco::MESH_EDGEBREAKER_ENCODING, encoderOptions);
    }
    else
    {
        std::cerr << "Cant open stream to write" << std::endl;
    }
    out.close();



}