#include <stdint.h>
#include <stdio.h>
#include <assert.h>

typedef uint16_t paddr_t;

// init ram
extern void init_ram(const char *img);
extern void write_ram(int addr, int data);
extern int read_ram(int addr);

extern void init_keyboard();