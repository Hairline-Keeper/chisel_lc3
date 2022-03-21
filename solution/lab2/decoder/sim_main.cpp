#include "VDecoder.h" // 这个头文件会根据你模块的名字不同而改变
#include <verilated.h>
#include <iostream>
#include <bitset> // 用于输出二进制的数据

using namespace std;

int main(int argc, char **argv, char **env){
    Verilated::commandArgs(argc, argv);
    VDecoder* decoder = new VDecoder;  // 模块的实例

    int code = 0; // 用于decoder模块的输入，从0-7遍历

    while (!Verilated::gotFinish() && code < 8) {
        decoder->io_in = code;
    
        decoder->eval(); // 每执行一次eval函数，就对decoder模块执行一次仿真
        cout<<"in: "<<code<<"\t";
        cout<<"out: "<<bitset<8>(decoder->io_out)<<endl; // 输出deocder模块的out接口的信号

        code++;
    }

    decoder->final();
    delete decoder;
    exit(0);
}
