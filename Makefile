hello:
	echo "hello chisel_lc3"

test:
	mill chisel_lc3.test.testOnly ALUTest.ALUTest

clean:
	mill clean

.PHONY: test clean
