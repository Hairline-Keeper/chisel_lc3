; .ORIG x3000
; AND R0,R0,X0
; TRAP x20
; trap x21
; ADD R1,R1,R0
; TRAP x20
; ADD R2,R2,R0
; .END

.ORIG x3000

LEA R0, hello
PUTS

hello .STRINGZ "Hello!"
.END