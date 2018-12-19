package can.main_delete;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.example.baiduspeechdialog.dialog.SpeechBottomSheetDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;

import can.aboutsqlite.DBManager;
import can.aboutsqlite.Memo;
import can.aboutsqlite.User;
import can.live_assitcance.live_assitance;
import can.memorycan.R;
import can.memorycan.memo_add.memo_add;
import can.memorycan.speech;
import can.sms.HelloService;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity {
    int wstate = 0,usetime=0;
    private static final String TAG = speech.class.getSimpleName();
    private Button mOpenSpeechDialogBtn;
    private TextView mResultTv;
    private ArrayList<Group_new> gData = null;
    private ArrayList<ArrayList<Memo>> iData = null;
    private ArrayList<Memo> lData = null;
    private Context mContext;
    private ExpandableListView list_memo;
    private ImageButton imagebotton_slide;
    private ImageButton imagebotton_delete;
    private ImageButton imagebotton_speak;
    private ImageButton imagebotton_add,igb_to_slider;
    private MyBaseExpandableListAdapter_new myAdapter = null;
    private Handler handle = new Handler();
    private User user;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            this.update();
            handle.postDelayed(this,1000*5);
        }
        void update()
        {
            myAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        final DBManager mgr = new DBManager(this);

        mContext = MainActivity.this;

        igb_to_slider=findViewById(R.id.imageButton_slide);
        igb_to_slider.setOnClickListener(new tosliderbar());

        imagebotton_add=findViewById(R.id.imageButton_add);
        imagebotton_add.setOnClickListener(new tomemoadd());

        list_memo = (ExpandableListView) findViewById(R.id.list_memo);
        imagebotton_slide = (ImageButton) findViewById(R.id.imageButton_slide);
        imagebotton_delete = (ImageButton) findViewById(R.id.imageButton_delete);
        imagebotton_add = (ImageButton) findViewById(R.id.imageButton_add);
        gData = new ArrayList<Group_new>();
        iData = new ArrayList<ArrayList<Memo>>();
        gData.add(new Group_new("近期待完成",1));
        gData.add(new Group_new("超时未完成",-1));
        gData.add(new Group_new("已完成任务",0));

        lData = new ArrayList<Memo>();
        lData = mgr.returnmemo2(1);
        iData.add(lData);

        lData = new ArrayList<Memo>();
        lData = mgr.returnmemo3(1);
        iData.add(lData);

        lData = new ArrayList<Memo>();
        lData = mgr.returnmemo1(1);
        iData.add(lData);

        myAdapter = new MyBaseExpandableListAdapter_new(gData,iData,mContext);
        list_memo.setAdapter(myAdapter);
        if(list_memo!=null)
        {
            list_memo.expandGroup(0);
            list_memo.expandGroup(1);
        }
        final Intent detail = new Intent(MainActivity.this,memo_add.class);
        final Intent delete = new Intent(MainActivity.this,Delete.class);
        list_memo.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                detail.putExtra("memo_id",iData.get(groupPosition).get(childPosition).getMemo_id());
                Log.e("memo_id",String.valueOf(iData.get(groupPosition).get(childPosition).getMemo_id()));
                Log.e("KKKKKKLLLLLLLGGGGGG","KKKGGGFFFFF");
                Bundle bundle = new Bundle();
                bundle.putString("test","false");
                detail.putExtras(bundle);
                startActivity(detail);
                return true;
            }
        });
        imagebotton_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view)
            {
                Log.e("kkkkkkkkkk","kkkkkkkkkk");
                startActivity(delete);
                Log.e("kkkkkkkkkk","kkkkkkkkkk");
            }
        });
        onePermission();
        initViews();
//        initEvents();
        mOpenSpeechDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开百度语音对话框
                SpeechBottomSheetDialog speechBottomSheetDialog = SpeechBottomSheetDialog.getInstance(MainActivity.this);
                speechBottomSheetDialog.seOnResultListItemClickListener(new SpeechBottomSheetDialog.OnResultListItemClickListener() {
                    @Override
                    public void onItemClick(String title) {
                        //填充到输入框中
//                        mResultTv.setText(title);
                        Memo memo = new Memo(title,
                                "9999-12-01 12:12:12",1,0,0,
                                0, 1,1,0,"：）");
                        Log.e("insert","insert");
                        mgr.insert_Memo(memo);
                        Intent self = new Intent(MainActivity.this,MainActivity.class);
                        startActivity(self);
                    }
                });
                speechBottomSheetDialog.show(getSupportFragmentManager(), TAG);
            }
        });
        handle.postDelayed(runnable,1000*5);

        if(wstate==1){
            if (Settings.canDrawOverlays(MainActivity.this)) {
                usetime=1;
                    Intent intent = new Intent(MainActivity.this,MainService.class);
                    //Toast.makeText(MainActivity.this,"试图开启Toucher",Toast.LENGTH_SHORT).show();
                    startService(intent);
            }else { //若没有权限，提示获取.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                Toast.makeText(MainActivity.this,"需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        }
        else if(wstate==0&&usetime==1){
            Intent intent = new Intent(MainActivity.this,MainService.class);
            //Toast.makeText(MainActivity.this,"试图关闭Toucher",Toast.LENGTH_SHORT).show();
            startService(intent);
        }
        /*if (Settings.canDrawOverlays(MainActivity.this)) {
            if(wstate == 1){
                Intent intent = new Intent(MainActivity.this,MainService.class);
                //Toast.makeText(MainActivity.this,"试图开启Toucher",Toast.LENGTH_SHORT).show();
                startService(intent);
            }
            else{
                Intent intent = new Intent(MainActivity.this,MainService.class);
                //Toast.makeText(MainActivity.this,"试图关闭Toucher",Toast.LENGTH_SHORT).show();
                startService(intent);
            }
        }else { //若没有权限，提示获取.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            Toast.makeText(MainActivity.this,"需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }*/


    }

    protected void onDestroy(){
        handle.removeCallbacks(runnable);
        super.onDestroy();
    }

    public void initViews() {
        mOpenSpeechDialogBtn = findViewById(R.id.btn_openSpeechDialog);
        //mOpenSpeechLongDialogBtn = findViewById(R.id.btn_openSpeechLongDialog);
        mResultTv = findViewById(R.id.tv_result);
    }

    public void initEvents() {
        mOpenSpeechDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开百度语音对话框
                SpeechBottomSheetDialog speechBottomSheetDialog = SpeechBottomSheetDialog.getInstance(MainActivity.this);
                speechBottomSheetDialog.seOnResultListItemClickListener(new SpeechBottomSheetDialog.OnResultListItemClickListener() {
                    @Override
                    public void onItemClick(String title) {
                        //填充到输入框中
//                        mResultTv.setText(title);
                        Memo memo = new Memo(title,
                                "2018-11-14 23:00:00",2,0,0,
                                0, 1,1,0,"：）");
                    }
                });
                speechBottomSheetDialog.show(getSupportFragmentManager(), TAG);
            }
        });
    }

    /*只有一个运行时权限申请的情况*/
    private void onePermission(){
        RxPermissions rxPermissions = new RxPermissions(MainActivity.this); // where this is an Activity instance
        rxPermissions.request(Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) //权限名称，多个权限之间逗号分隔开
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        Log.e(TAG, "{accept}granted=" + granted);//执行顺序——1【多个权限的情况，只有所有的权限均允许的情况下granted==true】
                        if (granted) { // 在android 6.0之前会默认返回true
                            // 已经获取权限
                        } else {
                            // 未获取权限
                            Toast.makeText(MainActivity.this, "您没有授权该权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG,"{accept}");//可能是授权异常的情况下的处理
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.e(TAG,"{run}");//执行顺序——2
                    }
                });
    }

    private class tosliderbar implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(MainActivity.this,can.sliderbar.sliderbar.class);
            startActivity(intent);
        }
    }

    private class tomemoadd implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(MainActivity.this,can.memorycan.memo_add.memo_add.class);
            Bundle bd=new Bundle();
            bd.putInt("memo_id",-1);
            intent.putExtras(bd);
            startActivity(intent);
        }
    }
}
