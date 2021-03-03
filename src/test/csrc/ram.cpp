#include "common.h"
#include <arpa/inet.h>

#define RAMSIZE 65536

#define KBSR 0xFE00
#define DSR  0xFE04

int TRAP_NUM = 3;
char TRAP_DIR[][1024] = {
    "./image/GETC.trap",
    "./image/OUT.trap",
    "./image/PUTS.trap"
};

static paddr_t ram[RAMSIZE];
static long img_size = 0;

void display_ram(int start, int size) {
    for(int i = start; i < start+size/2; i++) {
        if(i%8 == 0) {
            printf("%04x: ", i);
        }
        printf("%04x ", ram[i]);
        if(i%8 == 7) {
            printf("\n");
        }
    }
    printf("\n");
}

void load_image(const char *img) {
    FILE *fp = fopen(img, "rb");
    int ret;
    paddr_t start_addr;

    if (fp == NULL) {
        printf("Can not open '%s'\n", img);
        assert(0);
    }

    ret = fread(&start_addr, sizeof(paddr_t), 1, fp);
    assert(ret == 1);
    start_addr = htons(start_addr);


    fseek(fp, 0, SEEK_END);
    img_size = ftell(fp) - 2;
    // printf("Image size = %ld Bytes\n", img_size);

    fseek(fp, 2, SEEK_SET);
    ret = fread(&ram[start_addr], img_size, 1, fp);
    assert(ret == 1);
    fclose(fp);

    printf("start addr = %x\n", start_addr);
    // display_ram(start_addr,img_size);
}

void load_trap() {
    for(int i = 0; i < TRAP_NUM; i++) {
        load_image(TRAP_DIR[i]);
    }
}

void init_ram(const char *img) {
    printf("The image is %s\n", img);

    load_image(img);

    // Init trap service program
    load_trap();

    for(int i = 0; i < RAMSIZE; i++) {
        ram[i] = htons(ram[i]);
    }

    // Init keyboard mmio
    ram[KBSR] = 0x0000;
    // ram[KBSR] = 0x8000;
    // ram[0xFE02] = 'a';
    ram[DSR] = 0x0000;

    // Init trap vector table
    ram[0x0020] = 0x0400;
    ram[0x0021] = 0x0430;
    ram[0x0022] = 0x0450;
    ram[0x0023] = 0x04A0;
    ram[0x0024] = 0x04E0;
    ram[0x0025] = 0xFD70;

    // FIXME: Only x3000-xffff can use store image

    // display_ram(KBSR,128);
    init_uart_buffer(img);
}

void write_ram(int addr, int data) {
    assert(addr >=0 && addr < 65536);
    assert(data >=0 && data < 65536);
    ram[addr] = data;
}

int read_ram(int addr) {
    assert(addr >=0 && addr < 65536);
    return ram[addr];
}

extern "C" void ram_helper(paddr_t rIdx, paddr_t *rdata, paddr_t wIdx, paddr_t wdata, /*paddr_t wmask,*/ uint8_t wen) {
    int rIdxReg = rIdx;
    *rdata = ram[rIdxReg];
    if (wen) ram[wIdx] = wdata;
    //printf("[debug] rIdx=%4x, *rdata=%4x, wIdx=%4x, *wdata=%4x, wen=%x\n", rIdx,  *rdata, wIdx, wdata, wen);
}
