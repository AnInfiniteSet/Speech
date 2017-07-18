package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.google.cloud.android.speech.ChatActivity.REQUEST_CODE_JOIN;

public class LoginActivity extends AppCompatActivity {

    // 로그인 테스트용 임의 아이디, 비밀번호
    private static final String TEST_ID = "test";
    private static final String TEST_PW = "1234";

    EditText edtId;
    EditText edtPw;

    TextView tvJoin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 커스텀 액션바 사용
        //getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.abs_layout);

        // EditText 가져오기
        edtId = (EditText) findViewById(R.id.edtId);
        edtPw = (EditText) findViewById(R.id.edtPw);

        // 로그인 버튼 가져오기
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자 계정 정보 저장
                String userId = edtId.getText().toString();
                String userPw = edtPw.getText().toString();

                // 로그인 성공시 사용자 계정 전달
                if (userId.equals(TEST_ID) && userPw.equals(TEST_PW)) {
                    Intent intent = new Intent();
                    intent.putExtra("userId", userId);
                    setResult(RESULT_OK, intent);
                    finish();

                } else { // 로그인 실패 처리
                    Toast.makeText(getApplicationContext(), "사용자 정보가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    edtId.setText("");
                    edtPw.setText("");
                }
            }
        });

        // 회원가입 텍스트 클릭 처리
        tvJoin = (TextView) findViewById(R.id.tvJoin);
        tvJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * 회원 가입 액티비티를 불러올 메서드
     */
    private void getJoinActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_JOIN);
    }

}
