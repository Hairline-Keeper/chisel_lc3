#include "common.h"
#include <pthread.h>

#define KBSR 0xFE00
#define KBDR 0xFE02
#define DSR  0xFE04
#define DDR  0xFE06

#define KEY_QUEUE_LEN 16

#define QUEUE_FULL (key_head == (key_tail + 1)%KEY_QUEUE_LEN)
#define QUEUE_EMPTY (key_head == key_tail)

static int key_queue[KEY_QUEUE_LEN];
static int key_head = 0, key_tail = 0; 
static int kbsr;

void* keyboard_listen(void* args) {
    char c;

    while(1) {
        c = getchar();
        // printf("keyboard input: %c\n", c);
        if(!QUEUE_FULL) {
            key_queue[key_tail] = c;
            key_tail = (key_tail + 1)%KEY_QUEUE_LEN;
        }
    }

    return 0;
}

void* keyboard_send(void* args) {
    while(1) {
        // printf("head=%d, tail=%d\n", key_head, key_tail);
        if(!(key_head == key_tail)) {
            kbsr = read_ram(KBSR);
            printf("kbsr=%x\n", kbsr);
            if(kbsr & 0x8000) {
                printf("Try to send...\n");
                kbsr = kbsr ^ 0x8000;
                write_ram(KBSR, kbsr);
                write_ram(KBDR, key_queue[key_head]);
                key_head = (key_head + 1)%KEY_QUEUE_LEN;
            }
        }
    }

    return 0;
}

void init_keyboard() {
    pthread_t kl_tid, ks_tid;
    int ret;
    ret = pthread_create(&kl_tid, NULL, keyboard_listen, NULL);
    if (ret != 0)
        printf("Keyboard listen thread create error: error_code=%d\n", ret);

    ret = pthread_create(&ks_tid, NULL, keyboard_send, NULL);
    if (ret != 0)
        printf("Keyboard send thread create error: error_code=%d\n", ret);
}