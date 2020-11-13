#include "common.h"
#include <string.h>
#include <pthread.h>
#include <mutex>              
#include <condition_variable> 

#define BUFFERSIZE 1024
#define KEY_QUEUE_LEN 16

static char str[KEY_QUEUE_LEN];
static int str_len;

static char buffer[BUFFERSIZE];
static int head = 0, tail = 0, size = 0;

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