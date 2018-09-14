引入Lame库参考[利用Cmake在AndroidStudio来使用lame库](https://www.jianshu.com/p/065bfe6d3ec2)  

想根据[clam314/LameMp3ForAndroid](https://github.com/clam314/LameMp3ForAndroid)同样写一个Demo


目前Demo中包含的功能有:  
录音并保存源数据PCM；  
给PCM文件添加头信息使成为wav可播放；  
将PCM文件通过lame库转换为mp3；  
录制音频的同时，通过lame转换mp3并写入本地。  

TODO

一边录制一边转换mp3和录制完再转换mp3的差异，生成文件是否相同，耗时等。  
