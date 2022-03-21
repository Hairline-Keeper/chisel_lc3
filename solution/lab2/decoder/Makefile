TOP = Decoder
BUILD_DIR = ./build
TOP_V = $(BUILD_DIR)/$(TOP).v
SCALA_FILE = $(shell find ./decoder/src -name '*.scala')

.DEFAULT_GOAL = verilog

$(TOP_V): $(SCALA_FILE)
	@mkdir -p $(@D)
	mill decoder.run decoder.main.testMain -td $(@D) --output-file $(@F)

verilog: $(TOP_V)

SIM_TOP = Decoder

EMU_MK := $(BUILD_DIR)/V$(SIM_TOP).mk
EMU := $(BUILD_DIR)/emu
CXX_FILE := ./sim_main.cpp

$(EMU_MK): $(TOP_V) | $(EMU_DEPS)
	@mkdir -p $(@D)
	verilator -Wall --cc --exe \
		-o $(abspath $(EMU)) -Mdir $(@D) $^ $(CXX_FILE)

$(EMU): $(EMU_MK)
	$(MAKE) -C $(dir $(EMU_MK)) -f $(abspath $(EMU_MK))

emu: $(EMU)

clean:
	rm -rf build
