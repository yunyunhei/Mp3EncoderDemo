package cn.yunyunhei.mp3encoderdemo;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

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


    CheckBox pcm_checkbox;

    CheckBox wav_checkbox;

    CheckBox mp3_checkbox;


    TextView pcm_textView;

    TextView wav_textView;

    TextView mp3_textView;

    private static final int RecordStateStart = 1;
    private static final int RecordStateEnd = 0;

    private int recordState = RecordStateEnd;


    private AudioRecord audioRecord;
    private boolean isRecording;

    private String curSelectedFilePath;

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


        pcm_checkbox = findViewById(R.id.pcm_checkbox);

        wav_checkbox = findViewById(R.id.wav_checkbox);

        mp3_checkbox = findViewById(R.id.mp3_checkbox);

        pcm_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    curSelectedFilePath = (String) pcm_checkbox.getTag();
                    wav_checkbox.setChecked(false);
                    mp3_checkbox.setChecked(false);
                }
            }
        });

        wav_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    curSelectedFilePath = (String) wav_checkbox.getTag();
                    pcm_checkbox.setChecked(false);
                    mp3_checkbox.setChecked(false);
                }
            }
        });

        mp3_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    curSelectedFilePath = (String) mp3_checkbox.getTag();
                    pcm_checkbox.setChecked(false);
                    wav_checkbox.setChecked(false);
                }
            }
        });

        pcm_textView = findViewById(R.id.pcm_textView);

        wav_textView = findViewById(R.id.wav_textView);

        mp3_textView = findViewById(R.id.mp3_textView);

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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            File curFile = new File(curFilePath);
                            String fileSize = getFileSize(curFile);
                            String content = buildTextViewContent("pcm", curFile.getAbsolutePath(), fileSize);
                            showPcmContentView(content, curFilePath);
                        }
                    });
                }
            }
        }).start();
    }


    public static double log2(long n) {
        // Implement this but without inaccuracies due to FP math.
        // Just count the number of leading zeros and do the math.
        return (Math.log(n) / Math.log(2));
    }

    public static String getFileSize(File file) {
        long length = file.length();
        long logSize = (long) log2(length);
        final String[] suffixes = new String[]{" B", " KiB", " MiB", " GiB", " TiB", " PiB", " EiB", " ZiB", " YiB"};

        int suffixIndex = (int) (logSize / 10); // 2^10 = 1024

        double displaySize = length / Math.pow(2, suffixIndex * 10);
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(displaySize) + suffixes[suffixIndex];
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
        if (isPcmToWaving || isRecording) {
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

                Log.d("AudioRecord", "start pcm to wav");

                mPcmToWav.pcmToWav(inFile.getAbsolutePath(), outFilePath);

                Log.d("AudioRecord", "end pcm to wav");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isPcmToWaving = false;
                        showMessage("pcm to wav successfully");

                        File curFile = new File(outFilePath);
                        String fileSize = getFileSize(curFile);
                        String content = buildTextViewContent("wav", curFile.getAbsolutePath(), fileSize);
                        showWaveContentView(content, outFilePath);
                    }
                });

            }
        }).start();

    }

    private boolean isPcmToMp3ing = false;


    private void startPcmToMp3() {
        if (isPcmToMp3ing || isRecording) {
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

                        File curFile = new File(outFilePath);
                        String fileSize = getFileSize(curFile);
                        String content = buildTextViewContent("mp3", curFile.getAbsolutePath(), fileSize);
                        showMp3ContentView(content, outFilePath);
                    }
                });

            }
        }).start();


    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private String buildTextViewContent(String type, String filePath, String fileSize) {
        return String.format("%s  %s", filePath, fileSize);
    }

    private void showContentView(CheckBox checkBox, TextView textView, String content, String filePath) {
        checkBox.setTag(filePath);
        checkBox.setVisibility(View.VISIBLE);
        textView.setText(content);
        textView.setVisibility(View.VISIBLE);
    }

    private void showPcmContentView(String content, String filePath) {
        showContentView(pcm_checkbox, pcm_textView, content, filePath);
    }

    private void showWaveContentView(String content, String filePath) {
        showContentView(wav_checkbox, wav_textView, content, filePath);
    }

    private void showMp3ContentView(String content, String filePath) {
        showContentView(mp3_checkbox, mp3_textView, content, filePath);
    }

    private void clearAllContent() {
        pcm_checkbox.setVisibility(View.GONE);
        pcm_checkbox.setChecked(false);
        wav_checkbox.setVisibility(View.GONE);
        wav_checkbox.setChecked(false);
        mp3_checkbox.setVisibility(View.GONE);
        mp3_checkbox.setChecked(false);

        pcm_textView.setVisibility(View.GONE);
        wav_textView.setVisibility(View.GONE);
        mp3_textView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
                if (recordState == RecordStateEnd) {
                    start_record.setText(R.string.end_record);
                    clearAllContent();
                    curSelectedFilePath = null;
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

                if (curSelectedFilePath == null || "".equals(curSelectedFilePath)) {
                    showMessage("didn't selected");
                } else {
                    showMessage(curSelectedFilePath);
                }

                playMusic();

                break;
        }
    }


    private MediaPlayer mMediaPlayer;

    private void playMusic() {
        if (curSelectedFilePath == null || "".equals(curSelectedFilePath)) {
            return;
        }

        final String playFilePath = curSelectedFilePath;

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(playFilePath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 播放，使用stream模式
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playInModeStream() {

        if (curSelectedFilePath == null || "".equals(curSelectedFilePath)) {
            return;
        }

        final String playFilePath = curSelectedFilePath;

        /*
        * SAMPLE_RATE_INHZ 对应pcm音频的采样率
        * channelConfig 对应pcm音频的声道
        * AUDIO_FORMAT 对应pcm音频的格式
        * */
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        final int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ, channelConfig, AUDIO_FORMAT);

        final AudioTrack audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(channelConfig)
                        .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE);
        audioTrack.play();

        File file = new File(playFilePath);

        try {
            final FileInputStream fileInputStream = new FileInputStream(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] tempBuffer = new byte[minBufferSize];
                        while (fileInputStream.available() > 0) {
                            int readCount = fileInputStream.read(tempBuffer);
                            if (readCount == AudioTrack.ERROR_INVALID_OPERATION ||
                                    readCount == AudioTrack.ERROR_BAD_VALUE) {
                                continue;
                            }
                            if (readCount != 0 && readCount != -1) {
                                audioTrack.write(tempBuffer, 0, readCount);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 播放，使用static模式
     */
    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playInModeStatic() {

        if (curSelectedFilePath == null || "".equals(curSelectedFilePath)) {
            return;
        }

        // static模式，需要将音频数据一次性write到AudioTrack的内部缓冲区

        final AudioTrack[] audioTrack = new AudioTrack[1];
        final byte[][] audioData = new byte[1][1];

        final String needPlayFilePath = curSelectedFilePath;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InputStream in = new FileInputStream(needPlayFilePath);
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        for (int b; (b = in.read()) != -1; ) {
                            out.write(b);
                        }
                        Log.d("AudioRecord", "Got the data");
                        audioData[0] = out.toByteArray();
                    } finally {
                        in.close();
                    }
                } catch (IOException e) {
                    Log.wtf("AudioRecord", "Failed to read", e);
                }
                return null;
            }


            @Override
            protected void onPostExecute(Void v) {
                Log.i("AudioRecord", "Creating track...audioData.length = " + audioData[0].length);

                // R.raw.ding铃声文件的相关属性为 22050Hz, 8-bit, Mono

                audioTrack[0] = new AudioTrack(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build(),
                        new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ)
                                .setEncoding(AUDIO_FORMAT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build(),
                        audioData[0].length,
                        AudioTrack.MODE_STATIC,
                        AudioManager.AUDIO_SESSION_ID_GENERATE);
                Log.d("AudioRecord", "Writing audio data...");
                audioTrack[0].write(audioData[0], 0, audioData[0].length);
                Log.d("AudioRecord", "Starting playback");
                audioTrack[0].play();
                Log.d("AudioRecord", "Playing");
            }

        }.execute();

    }


}
