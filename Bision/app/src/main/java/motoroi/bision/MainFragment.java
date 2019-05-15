package motoroi.bision;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MainFragment extends Fragment {
    MainView mainView;
    StorageReference storage = FirebaseStorage.getInstance().getReference();
    Map<Object, Object>[] rankingList;//랭킹을 담을 Map배열
    Map<Object, Object>[] deadlineList;//마감임박 리스트
    ArrayList<Map<Object, Object>> allIntroList;
    ImageButton nextButton;
    ImageButton backButton;
    ImageView deadlineImageView;
    TextView deadlineName;
    TextView deadlineDays;
    int deadlineCheck = 0;
    private int rankingCheck;
    AutoCompleteTextView searchBar;
    ArrayList<String> arrayList;
    Map searchMap;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainView = (MainView) getActivity();
        allIntroList = mainView.getAllIntrolist();
        deadlineList = mainView.getDeadlineList();
        rankingList = mainView.getRankingList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainView = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        final ImageView[] ranking = {
                viewGroup.findViewById(R.id.ranking1),
                viewGroup.findViewById(R.id.ranking2),
                viewGroup.findViewById(R.id.ranking3),
                viewGroup.findViewById(R.id.ranking4),
                viewGroup.findViewById(R.id.ranking5),
        };

        deadlineImageView = viewGroup.findViewById(R.id.deadline_image);
        deadlineName = viewGroup.findViewById(R.id.deadline_name);
        deadlineDays = viewGroup.findViewById(R.id.deadline_days);
        nextButton = viewGroup.findViewById(R.id.nextButton);
        backButton = viewGroup.findViewById(R.id.backButton);
        arrayList = new ArrayList<String>();
        for (int i = 0; i < allIntroList.size(); i++)
            arrayList.add(allIntroList.get(i).get("name").toString());

        searchBar = viewGroup.findViewById(R.id.searchBar);
        searchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if((keyEvent.getAction()==KeyEvent.ACTION_DOWN&&i==KeyEvent.KEYCODE_ENTER)) {
                    String search = searchBar.getText().toString();
                    boolean searchCheck = false;
                    for(i=0; i<allIntroList.size(); i++){
                        if(allIntroList.get(i).containsValue(search)) {
                            searchMap=allIntroList.get(i);
                            searchCheck=true;
                        }
                    }

                    AlertDialog.Builder 확인 = new AlertDialog.Builder(getContext());
                    확인.setTitle("알림");
                    if(searchCheck) {
                        search += ".png";
                        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = inflater.inflate(R.layout.help_layout,null);
                        ImageView imageView = (ImageView)layout.findViewById(R.id.help_imageView);
                        StorageReference img = storage.child(search);
                        Glide.with(getContext()).using(new FirebaseImageLoader()).load(img).override(600,600).into(imageView);
                        확인.setView(layout);
                        확인.setMessage("맞습니까?");
                        확인.setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mainView.onFragmentChange("intro",searchMap);
                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return ;
                            }
                        });
                    }else{
                        확인.setMessage("결과없음");
                        확인.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return ;
                            }
                        });
                    }
                    확인.show();
                    return true;
                }
                return false;
            }
        });
        searchBar.setAdapter(new ArrayAdapter<String>(mainView.getApplicationContext(), R.layout.search_view, arrayList));

        for (rankingCheck = 0; rankingCheck < ranking.length; rankingCheck++) {
            ranking[rankingCheck].setOnClickListener(new View.OnClickListener() {
                int index = rankingCheck;
                @Override
                public void onClick(View view) {
                    mainView.onFragmentChange("intro", rankingList[index]);
                    mainView.dbUpdate(rankingList[index]);
                }
            });
        }

        deadlineImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainView.onFragmentChange("intro", deadlineList[deadlineCheck]);
                mainView.dbUpdate(deadlineList[deadlineCheck]);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deadlineCheck != 0) deadlineCheck--;
                setDeadline();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deadlineCheck != 2) deadlineCheck++;
                setDeadline();
            }
        });


        for (int i = 0; i < rankingList.length; i++) {
            String path = rankingList[i].get("name").toString() + ".png";//랭킹에 있는 것들의 사진을 불러오기 위한 문자열 path
            StorageReference img = storage.child(path);//저장소에 path 이미지를 참조하는 객체 생성
            Glide.with(MainFragment.super.getContext()).using(new FirebaseImageLoader()).load(img).crossFade(0).override(500, 500).fitCenter().into(ranking[i]);
        }

        setDeadline();

        return viewGroup;
    }

    private void setDeadline() {
        String path = deadlineList[deadlineCheck].get("name").toString() + ".png";//랭킹에 있는 것들의 사진을 불러오기 위한 문자열 path
        StorageReference img = storage.child(path);//저장소에 path 이미지를 참조하는 객체 생성
        Glide.with(MainFragment.super.getContext()).using(new FirebaseImageLoader()).load(img).crossFade(0).override(500, 500).into(deadlineImageView);

        deadlineName.setText(deadlineList[deadlineCheck].get("name").toString());
        Date deadlineDay = (Date) deadlineList[deadlineCheck].get("deadline");
        Date Today = new Date();//현재 날짜,시간 구한 객체

        Long dif = deadlineDay.getTime() - Today.getTime();
        deadlineDays.setText(Long.toString(dif / (24 * 60 * 60 * 1000)) + " Days");
    }
}


