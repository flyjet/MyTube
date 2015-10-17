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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;



public class SearchFragment extends Fragment {

    private static String queryKeyWord;

    private ListView videosFound;
    private Handler handler;
    private List<VideoItem> searchResults;
    private static final String TAG = SearchFragment.class.getSimpleName();

    //Todo, here use hard code for accessToken, should be null and get from main Activity
    private static String accessToken
        ="ya29.DwKzbVpE9RKt0ymQBAJAUzAxT7BbzFgmV82vh_DAWbAst6O-lXYjIOJeo69iiNWE10E_jw";


    //This is the ID for Playlist of SJSU-CMPE-277 under my channel "Annie Cao"
    private String PLAYLIST_ID = "PLcmb3fCvZSrX8xVUUzfqN8RfZBXlhrvjf";
    private String PLAYLIST_TITLE = "SJSU-CMPE-277";


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queryKeyWord = getArguments().getString("QueryKeyWord");

        //Todo: has bug to get the accessToken

        //accessToken = getArguments().getString("Token");
        Log.d(TAG, "Token from search fragment: " + accessToken);

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
                //here start the Player Activity
            }
        });
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

        //create an ArrayAdapter
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
                final CheckBox checkBox_favorite = (CheckBox)convertView.findViewById(R.id.checkbox_favorite);

                final VideoItem searchResult = searchResults.get(position);

                Picasso.with(getActivity().getApplicationContext())
                        .load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                views.setText(searchResult.getViews().toString() + "  views");
                pub_date.setText(searchResult.getPub_date().toString().substring(0, 10));

                //checkbox_favorite

                if(searchResult.isFavorite()){
                    checkBox_favorite.isChecked();
                }

                checkBox_favorite.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        searchResult.setFavorite(checkBox_favorite.isChecked());
                        Log.d(TAG, "Checkbox favorite is clicked ");

                        //call the method to insert the video to Playlist
                        insertPlaylistItem(PLAYLIST_ID, searchResult.getId());
                        Toast.makeText(getActivity().getApplicationContext(),
                                "The video is inserted to your playlist", Toast.LENGTH_LONG).show();
                    }

                });

                //checkbox_select for fragment favorite, here is invisible
                convertView.findViewById(R.id.checkbox_select).setVisibility(View.INVISIBLE);
                return convertView;
            }
        };

        //Assign adapter to ListView
        videosFound.setAdapter(adapter);
    }


    protected void insertPlaylistItem(final String playlistID, final String videoId) {
        Log.d(TAG, "Token from search fragment: " + accessToken);

        //Todo need check the video is already is playlist or not


        new Thread(){
            public void run(){

                GoogleCredential credential = new GoogleCredential.Builder()
                        .setTransport(new NetHttpTransport())
                        .setJsonFactory(new JacksonFactory()).build();
                credential.setAccessToken(accessToken);

                // This object is used to make YouTube Data API requests.
                YouTube youtube;

                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
                        .setApplicationName("MyTube")
                        .build();

                //Define a resourceId that identifies the video being added to the playlist
                ResourceId resourceId = new ResourceId();
                resourceId.setKind("youtube#video");
                resourceId.setVideoId(videoId);

                // Set fields included in the playlistItem resource's "snippet" part.
                PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
                playlistItemSnippet.setTitle(PLAYLIST_TITLE);
                playlistItemSnippet.setPlaylistId(playlistID);
                playlistItemSnippet.setResourceId(resourceId);

                // Create the playlistItem resource and set its snippet to object created above.
                PlaylistItem playlistItem = new PlaylistItem();
                playlistItem.setSnippet(playlistItemSnippet);

                // Call the API to add the playlist item to the specified playlist.
                try {

                    Log.d(TAG, "Item need to insert to playlist ");

                    YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                            youtube.playlistItems().insert("snippet,contentDetails", playlistItem);

                    PlaylistItem returnedPlaylistItem =
                            playlistItemsInsertCommand.execute();
                    Log.d(TAG, "Item is added to playlist ");

                } catch (IOException e) {
                    Log.d(TAG, "Could not insert video item to playlist: " + e);
                }
            }

        }.start();

    }
}