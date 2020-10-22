# Chisel_LC3

This repo is a LC3 ISA implemented using Chisel. Used in the teaching of computer system courses in Shenzhen University

## Dependencies

[mill](http://www.lihaoyi.com/mill/)

[Verilator](https://www.veripool.org/wiki/verilator)

[GTKWave](http://gtkwave.sourceforge.net/) *You don’t need it if you don’t look at the wave file*

...... *Maybe there are other dependencies but I forgot*



## Project structure

Here are some important directories under the project

**build**: This directory will be automatically generated after MAKE

**image**: The **.asm** files that need to be run are stored in this directory

**src**:   Source code

## Build

The simplest, run

```
make
```

or

```
make verilog
```

This will compile chisel into a **TopMain.v** verilog file and store it in the build directory

## Emulation

If you want to run emulation

```
make emu
```

This will load the **dummy.asm** file in the image directory as the running program by default

If you want to specify other files

```
make emu IMAGE=filename
```

## Waveform

Wave file will be generated after the emulation ends, it store in

```
chisel_lc3/build/emu.vcd
```

You can open it through GTKWave

When the emulation time is long, the waveform file may be very large

You can turn off the waveform in Makefile

```
TRACE = 
```

Set TRACE equal to nothing
