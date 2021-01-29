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