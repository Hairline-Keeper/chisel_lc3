#include <verilated.h>          // 核心头文件
#include <verilated_vcd_c.h>    // 波形生成头文件
#include <iostream>
#include <fstream>
#include <cstring>
#include <getopt.h>
#include "VTop.h"           // 译码器模块类
#include "common.h"

#define MAX_IMAGE_NAME_LEN 256

using namespace std;

VTop* top;                  // 顶层dut对象指针
VerilatedVcdC* tfp;             // 波形生成对象指针

vluint64_t main_time = 0;           // 仿真时间戳
const vluint64_t sim_time = 10240;   // 最大仿真时间戳

char image[MAX_IMAGE_NAME_LEN] = {"./image/dummy.obj"};

double sc_time_stamp() {
    return main_time;
}

static inline void parse_args(int argc, char *argv[]) {
    int arg;
    while((arg = getopt(argc, argv, "i:")) != -1) {
        switch (arg)
        {
        case 'i':
            strcpy(image, optarg);
            break;
        }
    }
}

int main(int argc, char **argv)
{
    // 一些初始化工作
    parse_args(argc, argv);
    Verilated::commandArgs(argc, argv);
    Verilated::traceEverOn(true);

    // 为对象分配内存空间
    top = new VTop;
    tfp = new VerilatedVcdC;

    // tfp初始化工作
    top->trace(tfp, 99);
    tfp->open("./build/emu.vcd");

    // int count = 0;

    init_ram(image);

    while(!Verilated::gotFinish() && main_time < sim_time)// && main_time < sim_time)
    {
        // 仿真过程
        // top->reset = 0;
        // top->S = count;         // 模块S输出递增
        if ((main_time % 10) == 1) {
            top->clock = 1;       // Toggle clock
        }
        if ((main_time % 10) == 6) {
            top->clock = 0;
        }
        top->eval();            // 仿真时间步进
        tfp->dump(main_time);   // 波形文件写入步进
        // count++;
        main_time++;
    }
    
    // 清理工作
    tfp->close();
    top->final();
    delete top;
    delete tfp;
    exit(0);
    return 0;
}