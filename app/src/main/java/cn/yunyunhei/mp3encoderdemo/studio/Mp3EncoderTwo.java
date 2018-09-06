package cn.yunyunhei.mp3encoderdemo.studio;

/**
 * @author WuHang 2018/9/3 0003
 **/
public class Mp3EncoderTwo {
    public native int init(String pcmFile, int audioChannels, int bitRate, int sampleRate, String mp3Path);
    public native void encode();
    public native void destroy();
}
