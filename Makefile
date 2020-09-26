TOP = TopMain
BUILD_DIR = ./build
TOP_V = $(BUILD_DIR)/$(TOP).v
SCALA_FILE = $(shell find ./src/main/scala -name '*.scala')
TEST_FILE = $(shell find ./src/test/scala -name '*.scala')

.DEFAULT_GOAL = verilog

$(TOP_V): $(SCALA_FILE)
	mkdir -p $(@D)
	mill chisel_lc3.run LC3.Top.SimMain -td $(@D) --output-file $(@F)
	# sbt run chisel_lc3.LC3.Top.SimMain

verilog: $(TOP_V)

hello:
	echo "hello chisel_lc3"

test:
	sbt testOnly chisel_lc3.ALUtest.tests
	# mill chisel_lc3.test.testOnly ALUTest.tests

clean:
	rm -rf ./build

.PHONY: test clean
