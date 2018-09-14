package cn.yunyunhei.mp3encoderdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import cn.yunyunhei.mp3encoderdemo.utils.IntentUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeActivityFragment extends Fragment {

    private static final int MY_PERMISSIONS_REQUEST = 1001;

    /**
     * 需要申请的运行时权限
     */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 需要请求授权的权限列表
     */
    private List<String> mNeedRequestPermissionList = new ArrayList<>();

    public HomeActivityFragment() {
    }

    Button mButton;

    Button btn_jump_top_audio_record2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButton = view.findViewById(R.id.btn_jump_top_audio_record);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAudioPermission()) {
                    IntentUtils.jumpToAudioRecord(getActivity());
                } else {
                    requestNeedPermissions();
                }
            }
        });

        btn_jump_top_audio_record2 = view.findViewById(R.id.btn_jump_top_audio_record2);
        btn_jump_top_audio_record2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAudioPermission()) {
                    IntentUtils.jumpToAudioRecord2(getActivity());
                } else {
                    requestNeedPermissions();
                }
            }
        });
    }

    private void requestNeedPermissions() {

        if (getActivity() == null || getActivity().isDestroyed()) {
            return;
        }

        if (!mNeedRequestPermissionList.isEmpty()) {
            String[] permissions = mNeedRequestPermissionList.toArray(new String[mNeedRequestPermissionList.size()]);
            ActivityCompat.requestPermissions(getActivity(), permissions, MY_PERMISSIONS_REQUEST);
        }

    }

    private boolean checkAudioPermission() {

        if (getContext() == null) {
            return false;
        }

        mNeedRequestPermissionList.clear();

        // Marshmallow开始才用申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(getContext(), permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mNeedRequestPermissionList.add(permissions[i]);
                }
            }

        }

        return mNeedRequestPermissionList.isEmpty();
    }

}
