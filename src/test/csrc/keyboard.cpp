#include "common.h"
#include <string.h>
#include <pthread.h>
#include <mutex>              
#include <queue>              
#include <condition_variable> 

#define KBSR 0xFE00
#define KBDR 0xFE02
#define DSR  0xFE04
#define DDR  0xFE06

#define KEY_QUEUE_LEN 16

static char str[KEY_QUEUE_LEN];
static int str_len;
static std::queue<int> key_queue;

pthread_mutex_t mutex;
pthread_cond_t not_full, not_empty;

static int get_str() {
    int len;
    fd_set rfds;
    struct timeval tv;

    FD_ZERO(&rfds);
    FD_SET(0, &rfds);
    tv.tv_sec = 0;
    tv.tv_usec = 10; // Set the waiting timeout period

    // Check whether the keyboard has input
    if (select(1, &rfds, NULL, NULL, &tv) > 0)
    {
        fgets(str, KEY_QUEUE_LEN, stdin); 
        str_len = strlen(str);
        str[--str_len] = '\0';
        return str_len;
    }
    return 0;
}

void polling_keyboard() {
    if(get_str()) {
        printf("get input: %s\n", str);
        for(int i = 0; i < str_len; i++) {
            if(key_queue.size() != KEY_QUEUE_LEN) {
                key_queue.push(str[i]);
            }
        }
    }

    if(read_ram(KBSR) == 0 && key_queue.size() != 0) {
        int ch = key_queue.front();
        printf("try to send %c ...\n", ch);
        write_ram(KBSR, 0x8000);
        write_ram(KBDR, ch);
        key_queue.pop();
    }
}