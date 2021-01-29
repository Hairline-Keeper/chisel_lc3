#!/bin/python3

import sys 
import re
import struct

if (len(sys.argv) < 3): 
    print("Usage: python obj2coe.py [OBJFILE] [TARGETFILE]")
    sys.exit(1)

print("obj file: " + sys.argv[1])

trapList = {
  "./image/GETC.trap": 0x0020,
  "./image/OUT.trap": 0x0021,
  "./image/PUTS.trap": 0x0022
}

writeData = {}
 
startAddr = 0x3000

def readFromOBJ(filename):
  hexData = []
  out = []

  with open(filename, "rb") as file:
    while True:
      data = file.read(1)
      if not data:
        break
      x = '{:02X}'.format(struct.unpack('B', data)[0])
      hexData.append(x)

  if(len(hexData) % 2 == 1):
    hexData.append('00')

  for i in range(len(hexData)//2):
    out.append(hexData[2*i] + hexData[2*i+1])

  return out


# Start
# Load TRAP program
for x in trapList.keys():
  trap = readFromOBJ(x)
  addr = int(trap.pop(0), 16)
  writeData[trapList[x]] = ['{:04X}'.format(addr)]
  writeData[addr] = trap
  print("Load TRAP program %s to 0x%x" % (x, addr))

out = readFromOBJ(sys.argv[1])
startAddr = int(out.pop(0), 16)
writeData[startAddr] = out
print(out)

print("StartAddr = 0x%x" % startAddr)

# Write to coe file
with open(sys.argv[2], "w") as file:
  file.write("memory_initialization_radix=16;\n")
  file.write("memory_initialization_vector=\n")
  addrs = writeData.keys()
  lastAddr = (max(addrs))

  i = 0
  while i <= lastAddr:
    if(i in addrs):
      for x in writeData[i]:
        file.write(x + ",\n")
      print(writeData[i])
      i += len(writeData[i])
    else:
      # file.write("%s: 0000,\n" % "0x{:04X}".format(i))
      file.write("0000,\n")
      i += 1
      
  file.write("0000;\n")
