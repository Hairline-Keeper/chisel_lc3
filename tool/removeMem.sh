#!/bin/bash
s=$(sed -n -e '/module Memory/=' $1)
t=$(sed -n -e '/endmodule/=' $1)

for i in $t; do
  if [ $s -lt $i ]
  then
    e=$i
    break
  fi
done

sed -i -e "${s},${e}d" $1

s=$(sed -n -e '/Memory memory/=' $1)
t=$(sed -n -e '/);/=' $1)

for i in $t; do
  if [ $s -lt $i ]
  then
    e=$i
    break
  fi
done

sed -i -e "${s},${e}d" $1

sed -i "${s} i\\
\\
\tdual_mem memory (\n\t\t .clka(memory_clock),\n\t\t .wea(memory_io_wen),\n\t\t .addra(memory_io_wIdx),\n\t\t .dina(memory_io_wdata),\n\t\t .clkb(memory_clock),\n\t\t .addrb(memory_io_rIdx),\n\t\t .doutb(memory_io_rdate)\n\t);\n\t\t" $1

echo "Replace Memory in $1"
