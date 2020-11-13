.ORIG x0450

ST R7, SaveR7
ST R0, SaveR0
ST R1, SaveR1
ST R3, SaveR3

Loop    LDR R1, R0, #0
        BRz     Return
L2      LDI R3, DSR
        BRzp L2
        STI R1, DDR
        ADD R0, R0, #1
        BRnzp Loop

Return  LD R3, SaveR3
        LD R1, SaveR1
        LD R0, SaveR0
        LD R7, SaveR7
        RET

DSR .FILL xFE04
DDR .FILL xFE06
SaveR0 .FILL X0000
SaveR1 .FILL X0000
SaveR3 .FILL X0000
SaveR7 .FILL X0000
.END