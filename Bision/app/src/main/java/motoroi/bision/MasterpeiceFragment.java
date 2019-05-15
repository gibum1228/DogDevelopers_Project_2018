package motoroi.bision;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.service.BeaconManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MasterpeiceFragment extends Fragment{
    MainView mainView;
    TextView masterpieceName;
    TextView masterpieceexplain;
    MediaPlayer player;
    SeekBar controller;
    TextView musicCurrentTime;
    TextView musicSize;
    int check;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
    Runnable current = new Runnable() {
        @Override
        public void run() {
            try{
                musicCurrentTime.setText(simpleDateFormat.format(player.getCurrentPosition()));
                musicCurrentTime.post(current);
            }catch (IllegalStateException e){
                handler.removeCallbacks(this);
            }
        }
    };
    Handler handler = new Handler();
    private final String myUUID = "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0";
    private BeaconManager beaconManager;
    private Map map;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mainView = (MainView)getActivity();
    }
    @Override
    public void onDetach(){
        player.release();//플레이어 해제
        mainView = null;
        super.onDetach();
    }

    @Override
    public void onDestroyView(){
        player.release();
        super.onDestroyView();
    }

    protected void setMap(Map map){
        this.map=map;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_masterpeice,container,false);
        ImageButton mapButton = (ImageButton)viewGroup.findViewById(R.id.mapButton);
        final ImageView mapView = (ImageView)viewGroup.findViewById(R.id.mapView);
        final LinearLayout mainIntro = (LinearLayout)viewGroup.findViewById(R.id.main_intro);
        masterpieceexplain = (TextView) viewGroup.findViewById(R.id.masterpiece_explain);
        masterpieceName = (TextView) viewGroup.findViewById(R.id.masterpiece_name);
        check=0;
        mapButton.setOnClickListener(new View.OnClickListener() {
            int check = 0;
            @Override
            public void onClick(View view) {
                if(check==0){
                    mainIntro.setVisibility(View.INVISIBLE);
                    mapView.setVisibility(View.VISIBLE);
                    check=1;
                }
                else{
                    mapView.setVisibility(View.INVISIBLE);
                    mainIntro.setVisibility(View.VISIBLE);
                    check=0;
                }
            }
        });

        player= new MediaPlayer();
        ImageButton playButton = (ImageButton)viewGroup.findViewById(R.id.play_button);//재생버튼
        ImageButton stopButton = (ImageButton)viewGroup.findViewById(R.id.stop_button);//정지버튼
        ImageButton rewindButton = (ImageButton)viewGroup.findViewById(R.id.rewind_button);//되감기버튼
        ImageButton forwardButton = (ImageButton)viewGroup.findViewById(R.id.forward_button);//빨리감기버튼
        controller = (SeekBar)viewGroup.findViewById(R.id.music_controller);//재생컨트롤러
        musicSize = (TextView)viewGroup.findViewById(R.id.music_size);//음악 총시간
        musicCurrentTime = (TextView)viewGroup.findViewById(R.id.music_current);//현재재생시간
        //재생버튼 클릭설정
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.start();
                Thread();
                current.run();
            }
        });
        //정지버튼 클릭설정
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.pause();
            }
        });
        //되감기버튼 클릭설정
        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(player.getCurrentPosition()-2000);//2초뒤로
            }
        });
        //빨리감기버튼 클릭설정
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(player.getCurrentPosition()+2000);//2초앞으로
            }
        });



        controller.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) player.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });//재생컨트롤러 만졌을때 실행되는 메소드

        //비콘매니저 객체 생성
        beaconManager= new BeaconManager(getContext());
        beaconManager.setBackgroundScanPeriod(2000,5000);
        beaconManager.setRegionExitExpiration(2000);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(new BeaconRegion("Beacons",UUID.fromString(myUUID),null,null));
                beaconManager.startMonitoring(new BeaconRegion("Beacons",UUID.fromString(myUUID),null,null));
            }
        });

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<com.estimote.coresdk.recognition.packets.Beacon> beacons) {
                if(beacons.size()!=0){
                    com.estimote.coresdk.recognition.packets.Beacon beacon = beacons.get(0);
                    if(check!=beacon.getMajor()) {
                        check=beacon.getMajor();
                        masterpieceSetting(beacon.getMajor());
                    }

                }
            }
        });

        return viewGroup;
    }

    private void masterpieceSetting(int num){
        StorageReference mp3 = FirebaseStorage.getInstance().getReference().child("mp3");
        String path = "";
        if(num==1) path="뚜두뚜두.mp3";
        else if(num==2) path="사랑을했다.mp3";

        Map value = null;
        Iterator iterator = map.keySet().iterator();
        for(int i=0; i<map.size(); i++){
            Map temp = (Map)map.get(iterator.next());
            if(Integer.toString(num).equals(Long.toString((Long)temp.get("id")))) {
                value = temp;
                break;
            }
        }

        masterpieceName.setText(value.get("name").toString());
        masterpieceexplain.setText(value.get("explain").toString());
        mp3.child(path).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                try {
                    player.reset();
                    player.setDataSource(task.getResult().toString());//uri로 받아서 셋팅
                    player.prepare();
                    controller.setMax(player.getDuration());//컨트롤러 길이 셋팅
                    musicSize.setText(simpleDateFormat.format(player.getDuration()));
                    musicCurrentTime.setText(simpleDateFormat.format(player.getCurrentPosition()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }//화면세팅

    //뮤직 컨트롤러를 위한 쓰레드 메소드
    public void Thread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(player.isPlaying()) {
                    try {
                        Thread.sleep(1000);
                        controller.setProgress(player.getCurrentPosition());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e){
                        handler.removeCallbacks(this);
                        break;
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
