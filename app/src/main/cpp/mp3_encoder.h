//
// Created by WuHang on 2018/9/3 0003.
//

#ifndef MP3ENCODERDEMO_MP3_ENCODER_H
#define MP3ENCODERDEMO_MP3_ENCODER_H


#include <stdio.h>
#include "lame.h"

class mp3_encoder {
private:
    FILE* pcmFile;
    FILE* mp3File;
    lame_t lameClient;
public:
    mp3_encoder();
    ~mp3_encoder();
    int Init(const char* pcmFilePath, const char *mp3Filepath,int sampleRate,int channels,int bitRate);
    void Encode();
    void Destory();
};


#endif //MP3ENCODERDEMO_MP3_ENCODER_H
