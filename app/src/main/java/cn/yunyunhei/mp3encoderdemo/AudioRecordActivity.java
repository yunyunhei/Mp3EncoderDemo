package cn.yunyunhei.mp3encoderdemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.yunyunhei.mp3encoderdemo.audio.PcmToWav;
import cn.yunyunhei.mp3encoderdemo.studio.Mp3EncoderTwo;

public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
     */
    public static final int SAMPLE_RATE_INHZ = 44100;

    /**
     * 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
     */
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
     */
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final String AUDIO_FILE_SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorocord/";

    Button start_record;

    Button pcm_to_wav;

    Button pcm_to_mp3;

    Button play_audio;


    private static final int RecordStateStart = 1;
    private static final int RecordStateEnd = 0;

    private int recordState = RecordStateEnd;


    private AudioRecord audioRecord;
    private boolean isRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        start_record = findViewById(R.id.start_record);
        start_record.setOnClickListener(this);

        pcm_to_wav = findViewById(R.id.pcm_to_wav);
        pcm_to_wav.setOnClickListener(this);

        pcm_to_mp3 = findViewById(R.id.pcm_to_mp3);
        pcm_to_mp3.setOnClickListener(this);

        play_audio = findViewById(R.id.play_audio);
        play_audio.setOnClickListener(this);
    }

    private String curFilePath;

    private void startRecord() {
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

        final byte data[] = new byte[minBufferSize];

        String fileName = System.currentTimeMillis() + ".pcm";

        final File file = new File(AUDIO_FILE_SAVE_PATH, fileName);

        curFilePath = file.getAbsolutePath();

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        audioRecord.startRecording();
        isRecording = true;

        new Thread(new Runnable() {
            @Override
            public void run() {

                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (null != os) {
                    while (isRecording) {
                        Log.d("AudioRecord", "before read");
                        int read = audioRecord.read(data, 0, minBufferSize);
                        Log.d("AudioRecord", "after read");
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                os.write(data);
                                Log.d("AudioRecord", "after write");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Log.d("AudioRecord", "stop record");
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void stopRecord() {
        isRecording = false;
        // 释放资源
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            //recordingThread = null;
        }
    }


    private boolean isPcmToWaving = false;


    PcmToWav mPcmToWav;

    private void startPcmToWav() {
        if (isPcmToWaving) {
            return;
        }

        if (curFilePath == null || "".equals(curFilePath)) {
            showMessage("not have file");
            return;
        }

        isPcmToWaving = true;

        if (mPcmToWav == null) {
            mPcmToWav = new PcmToWav(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        }

        final File inFile = new File(curFilePath);

        final String outFilePath = curFilePath.replace("pcm", "wav");

        File outFile = new File(outFilePath);

        if (outFile.exists()) {
            showMessage("file has exist");
            isPcmToWaving = false;
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d("AudioRecord", "start pcm to wave");

                mPcmToWav.pcmToWav(inFile.getAbsolutePath(), outFilePath);

                Log.d("AudioRecord", "end pcm to wave");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isPcmToWaving = false;
                        showMessage("pcm to wave successfully");
                    }
                });

            }
        }).start();

    }

    private boolean isPcmToMp3ing = false;


    private void startPcmToMp3(){
        if (isPcmToMp3ing){
            return;
        }

        if (curFilePath == null || "".equals(curFilePath)) {
            showMessage("not have file");
            return;
        }

        isPcmToMp3ing = true;

        final String inFilePath = curFilePath;

        final String outFilePath = curFilePath.replace("pcm", "mp3");

        File outFile = new File(outFilePath);

        if (outFile.exists()) {
            showMessage("file has exist");
            isPcmToMp3ing = false;
            return;
        }


        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d("AudioRecord", "start pcm to mp3");

                Mp3EncoderTwo mp3EncoderTwo = new Mp3EncoderTwo();
                mp3EncoderTwo.init(inFilePath, 1, 32, SAMPLE_RATE_INHZ, outFilePath);
                mp3EncoderTwo.encode();
                mp3EncoderTwo.destroy();

                Log.d("AudioRecord", "end pcm to mp3");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isPcmToMp3ing = false;
                        showMessage("pcm to mp3 successfully");
                    }
                });

            }
        }).start();


    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
                if (recordState == RecordStateEnd) {
                    start_record.setText(R.string.end_record);
                    startRecord();
                    recordState = RecordStateStart;
                } else {
                    start_record.setText(R.string.start_record);
                    stopRecord();
                    recordState = RecordStateEnd;
                }
                break;
            case R.id.pcm_to_wav:
                startPcmToWav();
                break;
            case R.id.pcm_to_mp3:
                startPcmToMp3();
                break;
            case R.id.play_audio:

                break;
        }
    }
}
