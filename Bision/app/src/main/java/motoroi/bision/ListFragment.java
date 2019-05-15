package motoroi.bision;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ListFragment extends Fragment {
    private MainView mainView;
    private RecyclerView recyclerView;
    private String[] allList;

    public void setAllList(String[] allList){
        this.allList=allList;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mainView = (MainView) getActivity();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainView = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = (RecyclerView)viewGroup.findViewById(R.id.list_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new MyAdapter(allList));

        return viewGroup;
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
        public MyAdapter(String[] list){
            allList=list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = (TextView)LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_view,parent,false);
            ViewHolder viewHolder = new ViewHolder(textView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTextView.setText(allList[position]);
        }

        @Override
        public int getItemCount() {
            return allList.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView mTextView;
            public ViewHolder(View itemView) {
                super(itemView);
                mTextView=(TextView)itemView;
                mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for(int i=0; i<allList.length; i++){
                            if(mTextView.getText().equals(mainView.allintrolist.get(i).get("name").toString()))
                                mainView.onFragmentChange("intro",mainView.allintrolist.get(i));
                        }
                    }
                });
            }
        }
    }

}
