TOP = TopMain
BUILD_DIR = ./build
TOP_V = $(BUILD_DIR)/$(TOP).v
SCALA_FILE = $(shell find ./src/main/scala -name '*.scala')
TEST_FILE = $(shell find ./src/test/scala -name '*.scala')

.DEFAULT_GOAL = verilog

$(TOP_V): $(SCALA_FILE)
	@mkdir -p $(@D)
	mill chisel_lc3.run LC3.Top.SimMain -td $(@D) --output-file $(@F)
	# sbt run chisel_lc3.LC3.Top.SimMain

verilog: $(TOP_V)

test:
	sbt testOnly chisel_lc3.ALUtest.tests
	# mill chisel_lc3.test.testOnly ALUTest.tests

EMU_CSRC_DIR = $(abspath ./src/test/csrc)
EMU_CXXFILES = $(shell find $(EMU_CSRC_DIR) -name "*.cpp")

EMU_MK := $(BUILD_DIR)/emu-compile/V$(TOP).mk
EMU_DEPS := $(EMU_CXXFILES)
EMU := $(BUILD_DIR)/emu

$(EMU_MK): $(TOP_V) $(EMU_DEPS)
	@mkdir -p $(@D)
	verilator --cc --exe --top-module $(TOP) \
		-o $(abspath $(EMU)) -Mdir $(@D) $^ $(EMU_DEPS)

$(EMU): $(EMU_MK)
	$(MAKE) -C $(dir $(EMU_MK)) -f $(abspath $(EMU_MK))

emu: $(EMU_MK)

clean:
	rm -rf ./build

.PHONY: test clean
