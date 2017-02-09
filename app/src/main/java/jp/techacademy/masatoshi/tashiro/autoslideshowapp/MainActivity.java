package jp.techacademy.masatoshi.tashiro.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    public int s = 0;
    public int t = 0;
    public boolean count = false;
    public boolean imagecount = false;
    public Uri u[];

    Timer mTimer;
    android.os.Handler mHandler = new android.os.Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button mStartButton = (Button) findViewById(R.id.startButton);
        final Button nextButton = (Button) findViewById(R.id.nextButton);
        final Button backButton = (Button) findViewById(R.id.backButton);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = !count;
                if (imagecount == true) {
                    showToast("画像を1つ以上、ファイルへ保存してください");
                }
                if (count) {
                    mStartButton.setText("■");
                    nextButton.setEnabled(false);
                    backButton.setEnabled(false);
                    if (mTimer == null) {
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (s == t){
                                    s = 0;
                                } else {
                                    s++;
                                }
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        image(u[s]);
                                    }
                                });
                            }
                        }, 2000, 2000);
                    }
                } else {
                    mStartButton.setText("▶");
                    nextButton.setEnabled(true);
                    backButton.setEnabled(true);
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imagecount == true) {
                    showToast("画像を1つ以上ファイルへ保存してください");
                }
                if (s == t){
                    s = 0;
                } else {
                    s++;
                }
                image(u[s]);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imagecount == true) {
                    showToast("画像を1つ以上ファイルへ保存してください");
                }
                if (s == 0){
                    s = t;
                } else {
                    s--;
                }
                image(u[s]);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                mStartButton.setEnabled(true);
                nextButton.setEnabled(true);
                backButton.setEnabled(true);
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                mStartButton.setEnabled(false);
                nextButton.setEnabled(false);
                backButton.setEnabled(false);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                } else {
                    showToast("許可されなかったので、起動できません");
                }
                break;
            default:
                break;
        }
    }
    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
    private void getContentsInfo() {
        int i = 0;
        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
         while (cursor.moveToNext()) {
                i++;
            }
        }
        if (i == 0){
            imagecount = true;
            showToast("画像を1つ以上ファイルへ保存してください");
        } else {
            t = i;
        }
        Uri uri[] = new Uri[t + 1];
        i = 0;
        if (cursor.moveToFirst()) {
            do {
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                uri[i] = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                i++;
            } while (cursor.moveToNext());
        }
        image(uri[s]);
        u = uri;
        cursor.close();
    }

    private void image (Uri imageUri){
    ImageView imageView = (ImageView) findViewById(R.id.picture);
    imageView.setImageURI(imageUri);
    }
}