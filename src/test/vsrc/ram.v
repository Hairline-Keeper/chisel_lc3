`define RAMWIDTH 16
import "DPI-C" function void ram_helper
(
  input  shortint    rIdx,
  output shortint    rdata,
  input  shortint    wIdx,
  input  shortint    wdata,
  // input  shortint    wmask,
  input  bit    wen
);

module RAMHelper(
  input         clk,
  input  [`RAMWIDTH-1:0] rIdx,
  output [`RAMWIDTH-1:0] rdata,
  input  [`RAMWIDTH-1:0] wIdx,
  input  [`RAMWIDTH-1:0] wdata,
  // input  [`RAMWIDTH-1:0] wmask,
  input         wen
);

  always @(posedge clk) begin
    ram_helper(rIdx, rdata, wIdx, wdata, wen);
  end

endmodule