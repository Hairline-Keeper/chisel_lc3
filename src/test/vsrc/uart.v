`define UARTWIDTH 8
import "DPI-C" function void uart_helper
(
  input  byte sendData,
  input  bit  sendData_valid,
  output bit  sendData_ready,

  output byte recvData,
  output bit  recvData_valid,
  input  bit  recvData_ready
);

module UARTHelper(
  input   clk,

  input   [`UARTWIDTH-1:0] sendData,
  input   sendData_valid,
  output  sendData_ready,

  output  [`UARTWIDTH-1:0] recvData,
  output  recvData_valid,
  input   recvData_ready
);

  always @(posedge clk) begin
    uart_helper(sendData, sendData_valid, sendData_ready, recvData, recvData_valid, recvData_ready);
  end
endmodule

import "DPI-C" function void soc_uartRx_helper
(
  input bit rxd
);

module SOC_UartRx(
  input clk,

  input bit rxd
);

  always @(posedge clk) begin
    soc_uartRx_helper(rxd);
  end
endmodule

import "DPI-C" function void soc_uartTx_helper
(
  output bit txd
);

module SOC_UartTx(
  input clk,

  output txd
);

  always @(posedge clk) begin
    soc_uartTx_helper(txd);
  end
endmodule