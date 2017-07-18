package com.google.cloud.android.speech;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity implements MessageDialogFragment.Listener {
    // Activity 요청 코드
    public static final int REQUEST_CODE_LOGIN = 101; // 로그인 요청 코드
    public static final int REQUEST_CODE_JOIN = 102; // 회원 가입 요청 코드
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";
    private static final String STATE_RESULTS = "results";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    // 로그인 되었는지 확인할 boolean 변수
    public static boolean isLogined = false;
    // 사용자 계정
    private static String userId = null;
    private SpeechService mSpeechService;
    private VoiceRecorder mVoiceRecorder;
    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            showStatus(true);
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            showStatus(false);
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };
    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            showStatus(true);
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            showStatus(false);
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };
    // Resource caches
    private int mColorHearing;
    private int mColorNotHearing;
    // View references
    private TextView mText;
    // 리스트뷰 어댑터
    private ChatArrayAdapter chatArrayAdapter;
    // 채팅메시지 구분 플래그 (false: 좌, true: 우)
    private boolean mSide = true;
    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (mText != null && !TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    mText.setText(null);
                                    chatArrayAdapter.add(new ChatMessage(!mSide, text));
                                } else {
                                    mText.setText(text);
                                }
                            }
                        });
                    }
                }
            };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
            //mStatus.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };
    // 화면 객체
    private ListView listView;
    private ImageButton btnRecord;

    /**
     * 액션바 메뉴 생성 메서드 오버라이딩
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * 액션바 메뉴 클릭 처리
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 현재 선택된 메뉴
        int curId = item.getItemId();
        // 메뉴에 따른 처리
        switch (curId) {
            case R.id.slidingMenu:
                // 슬라이딩 메뉴 보이기
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Log.d("onCreate()", "method started.");

        mText = (TextView) findViewById(R.id.mtext);

        //final Resources resources = getResources();
        //final Resources.Theme theme = getTheme();
        //mColorHearing = ResourcesCompat.getColor(resources, R.color.status_hearing, theme);
        //mColorNotHearing = ResourcesCompat.getColor(resources, R.color.status_not_hearing, theme);


        // 커스텀 액션바 사용
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        // 녹음 버튼 설정
        btnRecord = (ImageButton) findViewById(R.id.btnRecord);
        btnRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 버튼이 눌러져 있는 경우만 녹색 버튼 표시
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    btnRecord.setImageResource(R.drawable.btnrecord2);
                else
                    btnRecord.setImageResource(R.drawable.btnrecord1);

                return false;
            }
        });

        // 리스트뷰 생성 및 어댑터 설정
        listView = (ListView) findViewById(R.id.listView);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_singlemessage);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // 로그인 확인하기
        checkLogin();

        Log.d("onStart()", "method started.");

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecorder();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    /**
     * 다른 액티비티로 받아온 결과를 처리할 메서드 오버라이딩
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 로그인 요청 처리
        if (requestCode == REQUEST_CODE_LOGIN) {
            // 로그인 성공시
            if (resultCode == RESULT_OK) {
                isLogined = true; // 로그인 되었음을 확인
                userId = data.getStringExtra("userId"); // 사용자 아이디 저장
            }
        }
    }

    /**
     * 로그인 확인 및 처리 메서드
     */
    private void checkLogin() {

        // 로그인 확인
        isLogined = userId != null;

        // 로그인 되지 않았으면 로그인 액티비티로 이동
        if (!isLogined) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();

            //로그인 액티비티 띄우기
            getLoginActivity();
        } else  // 로그인 되었으면
        {
            // 앱 시작 인사말 보이기
            Toast.makeText(this, userId + "님 환영합니다!", Toast.LENGTH_SHORT).show();
            chatArrayAdapter.add(new ChatMessage(mSide, "안녕하세요? " + userId + "님.\n" +
                    "오늘은 어떤 연습을 하시겠어요?"));
            chatArrayAdapter.add(new ChatMessage(mSide, "1. 스크립트 읽기\n" +
                    "2. 단어장\n" +
                    "3. 나의 성적 보기"));

        }
    }

    /**
     * 로그인 액티비티를 불러올 메서드
     */
    private void getLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    /**
     * 메시지를 보내는 메서드
     */
    private boolean sendChatMessage() {

        return true;
    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    private void showStatus(final boolean hearingVoice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }
}
