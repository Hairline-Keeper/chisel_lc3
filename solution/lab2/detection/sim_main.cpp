#include "VDetection.h" // 这个头文件会根据你模块的名字不同而改变
#include <verilated.h>
#include <iostream>
#include <bitset> // 用于输出二进制的数据

using namespace std;

int main(int argc, char **argv, char **env){
    Verilated::commandArgs(argc, argv);
    VDetection* detection = new VDetection;  // 模块的实例

    int main_time = 0;
    int seq_ptr = 0;
    int seq[] = {1, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0};

    while (!Verilated::gotFinish() && main_time <= 200) {

        if ((main_time % 10) == 1) {
            detection->clock = 1;
        }
        if ((main_time % 10) == 6) {
            detection->clock = 0;

            seq_ptr = (seq_ptr + 1) % 14;
            // cout<<"in: "<<seq[seq_ptr]<<"\t";
            // cout<<"out: "<<bitset<1>(detection->io_out)<<endl<<endl;
        }

        detection->io_in = seq[seq_ptr];
    
        detection->eval();

        main_time++;
    }

    detection->final();
    delete detection;
    exit(0);
}
