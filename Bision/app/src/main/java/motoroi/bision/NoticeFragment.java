package motoroi.bision;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.List;

public class NoticeFragment extends Fragment {
    MainView mainView;
    TextView title;
    TextView textView;
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mainView = (MainView)getActivity();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mainView = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView.loadingOn(getActivity());
        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_notice,container,false);
        textView=viewGroup.findViewById(R.id.notice_contents);
        textView.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Notice").document("Notice").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                textView.setText(documentSnapshot.get("contents").toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


        mainView.loadingOff();
        return viewGroup;
    }
}
