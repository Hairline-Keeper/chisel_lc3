#include "common.h"
#include <string.h>
#include <pthread.h>
#include <mutex>              
#include <condition_variable> 

#define BUFFERSIZE 1024
#define KEY_QUEUE_LEN 16

#define FREQUENCY 50000000
#define BAUDRATE 115200

static char str[KEY_QUEUE_LEN];
static int str_len;

static char buffer[BUFFERSIZE];
static int head = 0, tail = 0, size = 0;

static char uart_buffer[BUFFERSIZE];
static int uart_ptr = 0, uart_size = 0;

// For uart_Tx
int bitCnt = (FREQUENCY / BAUDRATE) - 1;
int shiftReg = 0x7ff;
int cntReg = 0;
int bitsReg = 0;

void init_uart_buffer(const char *img) {
    FILE *fp = fopen(img, "rb");
    int ret;
    
    printf("Read %s content to uart_buffer\n", img);

    if (fp == NULL) {
        printf("Can not open '%s'\n", img);
        assert(0);
    }
    
    fseek(fp, 0, SEEK_END);
    uart_size = ftell(fp);
    fseek(fp, 0, SEEK_SET);
    
    printf("File size: %dB\n", uart_size);
    
    ret = fread(uart_buffer, uart_size, 1, fp);
    assert(ret == 1);
    fclose(fp);
    
    // for(int i = 0; i < uart_size; i++) {
    //     printf("%02x ", uart_buffer[i]);
    // }
    // printf("\n");
    
    // init for uart_Tx
    shiftReg = 0x7ff;
    cntReg = 0;
    bitsReg = 0;
}

static int get_str() {
    int len;
    fd_set rfds;
    struct timeval tv;

    FD_ZERO(&rfds);
    FD_SET(0, &rfds);
    tv.tv_sec = 0;
    tv.tv_usec = 10; //设置等待超时时间

    //检测键盘是否有输入
    if (select(1, &rfds, NULL, NULL, &tv) > 0)
    {
        fgets(str, KEY_QUEUE_LEN, stdin); 
        str_len = strlen(str);
        str[--str_len] = '\0';
        return str_len;
    }
    return 0;
}

extern "C" void uart_helper(uint8_t sendData, uint8_t sendData_valid, uint8_t *sendData_ready, 
                            uint8_t *recvData, uint8_t *recvData_valid, uint8_t recvData_ready) {
    *sendData_ready = 1;
    if(sendData_valid) {
        printf("Get Uart output: %c\n", sendData);
    }

    if(get_str()) {
        printf("get input: %s\n", str);
        for(int i = 0; i < str_len; i++) {
            if(size < BUFFERSIZE) {
                buffer[tail] = str[i];
                tail = (tail + 1) % BUFFERSIZE;
                size++;
            }
            printf("head = %d tail = %d size = %d\n", head, tail, size);
        }
    }

    if(size > 0) {
        *recvData_valid = 1;
        *recvData = buffer[head - 1];
    } else {
        *recvData_valid = 0;
        *recvData = buffer[head - 1];
    }

    if(*recvData_valid && recvData_ready) {
        head = (head + 1) % BUFFERSIZE;
        size--;
    }
}

extern "C" void soc_uartRx_helper(uint8_t rxd) {
    int bitCnt = (FREQUENCY / BAUDRATE) - 1;
    int startCnt = (3*(FREQUENCY / BAUDRATE)) / 2;
    
}

extern "C" void soc_uartTx_helper(uint8_t *txd) {
    *txd = shiftReg & 1;

    if(cntReg == 0) {
        cntReg = bitCnt;
        if(bitsReg != 0) {
            shiftReg = (shiftReg >> 1) | 0x400;
            bitsReg--;
        }else {
            if(uart_ptr < uart_size) {
                shiftReg = (uart_buffer[uart_ptr] << 1) | 0x600;
                printf("data = %02x\n", uart_buffer[uart_ptr]);
                // printf("shiftReg = %03x\n", shiftReg);
                uart_ptr++;
                bitsReg = 11;
            } else {
                shiftReg = 0x7ff;
            }
        }
    }else {
        cntReg--;
    }
}