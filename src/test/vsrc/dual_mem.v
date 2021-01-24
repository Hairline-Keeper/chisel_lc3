`define RAMWIDTH 16
module dual_mem
(
  input                   clka,
  input                   wea,
  input   [`RAMWIDTH-1:0] addra,
  input   [`RAMWIDTH-1:0] dina,
  input                   clkb,
  input   [`RAMWIDTH-1:0] addrb,
  output  [`RAMWIDTH-1:0] doutb
);
endmodule