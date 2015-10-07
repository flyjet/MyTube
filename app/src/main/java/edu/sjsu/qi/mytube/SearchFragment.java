package edu.sjsu.qi.mytube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;



public class SearchFragment extends Fragment {

    private String queryKeyWord;
    private ListView videosFound;
    private Handler handler;
    private List<VideoItem> searchResults;

    private static final String TAG = SearchFragment.class.getSimpleName();


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queryKeyWord = getArguments().getString("QueryKeyWord");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        handler = new Handler();
        videosFound = (ListView)view.findViewById(R.id.listView_search);

        if(!TextUtils.isEmpty(queryKeyWord)){
            searchOnYoutube(queryKeyWord);
        }

        //sets the OnItemClickListener of the ListView
        // so that the user can click on a search result and watch the corresponding video.
        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());

                startActivity(intent);
                //here start the Player Activity, but video is automatically play, need click it

            }
        });

        //Todo: need implement the checkbox_favorite
        // when clicked, the item should be add to favroite list

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    //Create a Thread to initialize a YouTubeConnector instance and run its search method
    private void searchOnYoutube(final String keywords){
        new Thread(){
            public void run(){
                YouTubeConnector yc = new YouTubeConnector(getActivity());
                searchResults = yc.search(keywords);
                handler.post(new Runnable() {
                    public void run() {

                        updateVideosFound();   //show the searchResults in ListView
                    }
                });
            }
        }.start();
    }

    //Use ArrayAdapter and pass it to ListView to display search results
    //in the getView method, inflate the video_item.xml layout and update its view
    private void updateVideosFound(){

        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity().getApplicationContext(),
                R.layout.video_item_search, searchResults){

            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                if(convertView == null){

                    convertView = getActivity().getLayoutInflater().inflate(R.layout.video_item_search, parent, false);
                }

                ImageView thumbnail =(ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView views = (TextView)convertView.findViewById(R.id.video_review_no);
                TextView pub_date = (TextView)convertView.findViewById(R.id.video_date);

                VideoItem searchResult = searchResults.get(position);

                Picasso.with(getActivity().getApplicationContext())
                        .load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                views.setText(searchResult.getViews().toString() +"  views");
                pub_date.setText(searchResult.getPub_date().toString().substring(0,10));

                //checkbox_select for fragment favorite, here is invisible
                convertView.findViewById(R.id.checkbox_select).setVisibility(View.INVISIBLE);

                return convertView;
            }
        };
        videosFound.setAdapter(adapter);
    }
}
