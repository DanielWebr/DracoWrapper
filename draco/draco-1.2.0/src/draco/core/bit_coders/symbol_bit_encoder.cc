#include "draco/core/bit_coders/symbol_bit_encoder.h"

#include "draco/core/symbol_encoding.h"

namespace draco {

void SymbolBitEncoder::EncodeLeastSignificantBits32(int nbits, uint32_t value) {
  DCHECK_LE(1, nbits);
  DCHECK_LE(nbits, 32);

  const int discarded_bits = 32 - nbits;
  value <<= discarded_bits;
  value >>= discarded_bits;

  symbols_.push_back(value);
}

void SymbolBitEncoder::EndEncoding(EncoderBuffer *target_buffer) {
  target_buffer->Encode(static_cast<uint32_t>(symbols_.size()));
  EncodeSymbols(symbols_.data(), symbols_.size(), 1, target_buffer);
  Clear();
}

void SymbolBitEncoder::Clear() {
  symbols_.clear();
  symbols_.shrink_to_fit();
}

}  // namespace draco
