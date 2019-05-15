package motoroi.bision;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.BoringLayout;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

public class MainView extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback {
    private DrawerLayout drawer;
    private FirebaseFirestore db;
    protected List<DocumentSnapshot> allList;
    protected ArrayList<Map<Object, Object>> allintrolist;
    protected Map<Object,Object>[] rankingList = new Map[5];//랭킹을 담을 Map배열
    protected Map<Object,Object>[] deadlineList = new Map[3];//마감임박 리스트
    protected Map<Object,Object>[] getRankingList(){
        return rankingList;
    }
    protected Map<Object,Object>[] getDeadlineList(){
        return deadlineList;
    }
    protected ArrayList<Map<Object,Object>> getAllIntrolist(){
        return allintrolist;
    }
    protected GoogleMap googleMap;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main_view);
        loadingOn(this);

        //툴바
        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //네비게이션드로어
        drawer =findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);




        //데이터불러오기
        db= FirebaseFirestore.getInstance();
        db.collection("Bision").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                allList = documentSnapshots.getDocuments();

                allintrolist = new ArrayList<>();

                for (int i = 0; i < allList.size(); i++)
                    allintrolist.add((Map) allList.get(i).get("intro"));//introList의 intro 객체들을 다 받는다

                for (int i = 0; i < allintrolist.size(); i++) {//셋팅 알고리즘
                    Map<Object, Object> temp = allintrolist.get(i);//intro 객체 하나를 temp로 저장해서
                    int rank = Integer.parseInt(temp.get("ranking").toString());//객체의 랭킹을 저장
                    if (temp.get("deadline") != null) {//만약 마감날짜가 있으면
                        Date deadlineDate = (Date) temp.get("deadline");//마감날짜를 받아서
                        Long deadline = deadlineDate.getTime();//검사를 위해 Long형으로 받고
                        for (int j = 0; j < deadlineList.length; j++) {//마감날짜 리스트를 하나씩 검사
                            if (deadlineList[j] == null){
                                deadlineList[j] = temp;//마감날짜 리스트가 비어있으면 temp를 집어 넣는다
                                break;
                            } else {
                                Date deadlintTemp = (Date) deadlineList[j].get("deadline");
                                if (deadline <= deadlintTemp.getTime()) {//마감 리스트에 있는 값들과 비교해서 현재 마감날짜가 더작을시
                                    for (int z = 2; z > j; z--)
                                        deadlineList[z] = deadlineList[z - 1];//마감리스트들 뒤로하나씩땡기고
                                    deadlineList[j] = temp;//자리에 삽입
                                    break;
                                }
                            }
                        }
                    }

                    for (int j = 0; j < rankingList.length; j++) {
                        if (rankingList[j] == null) rankingList[j] = temp;
                        else if (rank >= Integer.parseInt(rankingList[j].get("ranking").toString())) {
                            for (int z = 4; z > j; z--) rankingList[z] = rankingList[z - 1];
                            rankingList[j] = temp;
                            break;
                        }
                    }
                    loadingOff();
                }//리스트 셋팅 알고리즘 끝
                //메인프래그먼트 소환
                if(savedInstanceState==null) getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new MainFragment()).commit();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new MainFragment()).addToBackStack(null).commit();
            return true;
    }//홈버튼 실행메소드

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_question:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new QuestionFragment()).addToBackStack(null).commit();
                break;
            case R.id.menu_notice:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new NoticeFragment()).addToBackStack(null).commit();
                break;
            case R.id.menu_help:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new HelpFragment()).addToBackStack(null).commit();
                break;
            case R.id.menu_list:
                String[] strings = new String[allintrolist.size()];
                for(int i=0; i<strings.length; i++){
                    strings[i]=allintrolist.get(i).get("name").toString();
                }
                motoroi.bision.ListFragment listFragment= new motoroi.bision.ListFragment();
                listFragment.setAllList(strings);
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,listFragment).addToBackStack(null).commit();
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed(){
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else  {
            getSupportFragmentManager().popBackStack();
        }
    }//기기 뒤로가기버튼 눌렀을때 실행되는 메소드


    public void onFragmentChange(String index, Map map){
        if(index.equals("intro")){
            IntroFragment introFragment = new IntroFragment();
            introFragment.setMap(map);
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,introFragment).addToBackStack(null).commit();
        } else if(index.equals("masterpeice")) {
            MasterpeiceFragment masterpeiceFragment = new MasterpeiceFragment();
            for(int i=0; i<allList.size(); i++){
                Map temp = (Map)allList.get(i).get("intro");
                if(map.get("name").equals(temp.get("name"))){
                    map = (Map)allList.get(i).get("masterpiece");
                    break;
                }
            }
            masterpeiceFragment.setMap(map);
            SystemRequirementsChecker.checkWithDefaultDialogs(this);
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, masterpeiceFragment).addToBackStack(null).commit();
        }
    }

    public void dbUpdate(Map map){
        //1.파라미터 map이 들어있는 문서를 찾고
        String docName="";
        for(int i=0; i<allList.size(); i++){
            Map temp = (Map)allList.get(i).get("intro");
            if(map.get("name").toString().equals(temp.get("name").toString())) docName=allList.get(i).getId();
        }
        Long rank = (Long)map.get("ranking");
        rank++;
        map.put("ranking",rank);
        db.collection("Bision").document(docName).update("intro",map);
    }

    //로딩 다이얼로그
    private Dialog loadingDialog;

    public void loadingOn(Activity activity){
        loadingDialog = new Dialog(activity);
        loadingDialog .getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog .requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog .setContentView(R.layout.loading_dialog);
        ImageView aniView = (ImageView)loadingDialog.findViewById(R.id.loadingView);
        aniView.setBackgroundResource(R.drawable.loading_list);
        AnimationDrawable ani = (AnimationDrawable)aniView.getBackground();
        ani.start();
        loadingDialog .show();
    }

    public void loadingOff(){
        loadingDialog.dismiss();
    }


    public void mapSet(SupportMapFragment supportMapFragment,String latitude, String longitude){
        supportMapFragment.getMapAsync(this);
        Log.d(latitude,longitude);
        지역= new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
    }

    protected LatLng 지역;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(지역);

        markerOptions.title("성공회대");

        googleMap.addMarker(markerOptions);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(지역, 16));
    }
}
