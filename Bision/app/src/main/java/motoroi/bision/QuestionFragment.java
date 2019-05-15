package motoroi.bision;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class QuestionFragment extends Fragment {
    MainView mainView;
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
        final ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_question,container,false);
        final CheckBox[] checkBoxes = {
                viewGroup.findViewById(R.id.checkBox1),
                viewGroup.findViewById(R.id.checkBox2),
                viewGroup.findViewById(R.id.checkBox3),
        };
        for(int i=0; i<checkBoxes.length; i++){
            final int check = i;
            checkBoxes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(int i=0; i<checkBoxes.length; i++)
                        checkBoxes[i].setChecked(false);
                    checkBoxes[check].setChecked(true);
                }
            });
        }

        Button sendButton = (Button)viewGroup.findViewById(R.id.sendbutton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String 유형 = null;
                for(int i=0; i<checkBoxes.length; i++){
                    if(checkBoxes[i].isChecked()) {
                        유형= checkBoxes[i].getText().toString();
                    }
                }
                EditText title = (EditText) viewGroup.findViewById(R.id.question_title);
                EditText contents = (EditText) viewGroup.findViewById(R.id.question_contents);
                if(유형==null) 경고(0);
                else if(title.getText().length()==0) 경고(1);
                else if(contents.getText().length()==0) 경고(2);
                else{
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.setType("plain/text");
                    String[] address = {"dudrhs571@gmail.com"};
                    email.putExtra(Intent.EXTRA_EMAIL,address);
                    email.putExtra(Intent.EXTRA_SUBJECT,"["+유형+"]"+title.getText().toString());
                    email.putExtra(Intent.EXTRA_TEXT,contents.getText().toString());
                    title.setText("");
                    contents.setText("");
                    startActivity(email);
                    getFragmentManager().beginTransaction().replace(R.id.frag_container,new MainFragment()).commit();
                }
            }
        });
        return viewGroup;
    }

    private void 경고(int id){
        AlertDialog.Builder 경고 = new AlertDialog.Builder(getContext());
        경고.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        경고.setIcon(R.drawable.ic_warning);
        경고.setTitle("경고");
        if(id==0) 경고.setMessage("유형을 체크해주세요");
        else if(id==1) 경고.setMessage("제목을 입력하세요");
        else if(id==2) 경고.setMessage("내용을 입력하세요");
        경고.show();
    }
}
