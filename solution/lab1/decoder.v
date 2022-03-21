// decoder.v
module decoder(
input [2:0] in,
output [7:0] out
);

reg [7:0] out_reg;;

always @(*) begin
case(in)
                    3'd0: out_reg = 8'b00000001;
                    3'd1: out_reg = 8'b00000010;
                    3'd2: out_reg = 8'b00000100;
                    3'd3: out_reg = 8'b00001000;
                    3'd4: out_reg = 8'b00010000;
                    3'd5: out_reg = 8'b00100000;
                    3'd6: out_reg = 8'b01000000;
                    3'd7: out_reg = 8'b10000000;
                    default: out_reg = 8'b0;
            endcase
    end

    assign out = out_reg;

endmodule