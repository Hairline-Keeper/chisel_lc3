#include "common.h"
#include <arpa/inet.h>

#define RAMSIZE 65536

static paddr_t ram[RAMSIZE];
static long img_size = 0;

void display_ram() {
    for(int i = 0; i < RAMSIZE; i++) {
      if(i%8 == 0) {
          printf("%04x: ", i);
      }
      printf("%04x ", ram[i]);
      if(i%8 == 7) {
          printf("\n");
      }
    }
}

void init_ram() {
    const char *img = "./image/hello.obj";
    
    FILE *fp = fopen(img, "rb");
    int ret;
    paddr_t start_addr;
  if (fp == NULL) {
    printf("Can not open '%s'\n", img);
    assert(0);
  }

  printf("The image is %s\n", img);

  ret = fread(&start_addr, sizeof(paddr_t), 1, fp);
  assert(ret == 1);
  start_addr = htons(start_addr);

  fseek(fp, 0, SEEK_END);
  img_size = ftell(fp) - 2;
  printf("Image size = %ld Bytes\n", img_size);

  fseek(fp, 2, SEEK_SET);
  ret = fread(&ram[start_addr], img_size, 1, fp);
  assert(ret == 1);
  fclose(fp);
  for(int i = 0; i < RAMSIZE; i++) {
    ram[i] = htons(ram[i]);
  }

  printf("start addr = %x\n", start_addr);
  // display_ram();
  // FIXME: Only x3000-xffff can use store image
}

extern "C" void ram_helper(
    paddr_t rIdx, paddr_t *rdata, paddr_t wIdx, paddr_t wdata, paddr_t wmask, uint8_t wen) {
  *rdata = ram[rIdx];
  if (wen) { ram[wIdx] = (ram[wIdx] & ~wmask) | (wdata & wmask); }
}
