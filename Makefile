TOP = TopMain
BUILD_DIR = ./build
TOP_V = $(BUILD_DIR)/$(TOP).v
SCALA_FILE = $(shell find ./src/main/scala -name '*.scala')
TEST_FILE = $(shell find ./src/test/scala -name '*.scala')

.DEFAULT_GOAL = verilog

TRACE = 

IMAGE ?= dummy
IMAGE_DIR = ./image
LC3AS = $(IMAGE_DIR)/lc3as
IMAGE_OBJ := $(IMAGE_DIR)/$(IMAGE).obj
IMAGE_DEPS := $(IMAGE_DIR)/$(IMAGE).asm

$(TOP_V): $(SCALA_FILE)
	@mkdir -p $(@D)
	mill chisel_lc3.runMain LC3.SimMain -td $(@D) --output-file $(@F)
	# @sed -i -e 's/if (reset) begin/if (!reset) begin/g' $@
	# sbt run chisel_lc3.LC3.Top.SimMain
	$(REMOVE_MEM)

verilog: $(TOP_V)

$(IMAGE_OBJ): $(IMAGE_DEPS)
	$(LC3AS) $(IMAGE_DEPS)
	
compile: $(IMAGE_OBJ)

test:
	sbt testOnly chisel_lc3.ALUtest.tests
	# mill chisel_lc3.test.testOnly ALUTest.tests

SIM_TOP = Top

EMU_CSRC_DIR = $(abspath ./src/test/csrc)
EMU_VSRC_DIR = $(abspath ./src/test/vsrc)
EMU_CXXFILES = $(shell find $(EMU_CSRC_DIR) -name "*.cpp")
EMU_VFILES = $(shell find $(EMU_VSRC_DIR) -name "*.v" -or -name "*.sv")

EMU_MK := $(BUILD_DIR)/emu-compile/V$(SIM_TOP).mk
EMU_DEPS := $(EMU_VFILES) $(EMU_CXXFILES)
EMU_CXXFLAGS = -O3
EMU_LDFLAGS = -lpthread
EMU := $(BUILD_DIR)/emu

VERILATOR_FLAGS = --top-module $(SIM_TOP) \
	-I$(abspath $(BUILD_DIR)) \
	-CFLAGS "$(EMU_CXXFLAGS)" \
	-LDFLAGS "$(EMU_LDFLAGS)" \
	-Wno-WIDTH\
	--trace

$(EMU_MK): $(TOP_V) | $(EMU_DEPS)
	@mkdir -p $(@D)
	verilator --cc --exe $(VERILATOR_FLAGS) \
		-o $(abspath $(EMU)) -Mdir $(@D) $^ $(EMU_DEPS)

$(EMU): $(EMU_MK)
	$(MAKE) -C $(dir $(EMU_MK)) -f $(abspath $(EMU_MK))

emu: $(EMU) $(IMAGE_OBJ)
	$(EMU) -i $(IMAGE_OBJ) $(TRACE)

clean:
	rm -rf ./build
	rm -f ./image/*.obj ./image/*.sym

.PHONY: verilog test clean emu
