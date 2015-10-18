package edu.sjsu.qi.mytube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Video;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class FavoriteFragment extends Fragment {

    private ListView videosFavorite;
    private List<VideoItem> favoriteResults = new ArrayList<VideoItem>();

    private Handler handler;
    private static final String TAG = FavoriteFragment.class.getSimpleName();

    //This is the ID for Playlist of SJSU-CMPE-277 under my channel "Annie Cao"
    private String playlistID;
            //="PLcmb3fCvZSrX8xVUUzfqN8RfZBXlhrvjf";
    private String PLAYLIST_TITLE = "SJSU-CMPE-277";

    private String accessToken= "";
            //="ya29.DwI0LBBBJTh9Kn99Oy38WEFuU-DPRWrTqPjb-WBepqpEsrVc9O6GGDMcQkZn8nK2zdFBHg";

    public FavoriteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        //Get accessToken from MyTube Activity
        accessToken = getArguments().getString("Token");
        Log.d(TAG, "Token from favorite fragment: " + accessToken);

        playlistID = getArguments().getString("PlaylistId");
        Log.d(TAG, "Playlist_id from mytube activity: " + playlistID);

        handler = new Handler();
        videosFavorite = (ListView)view.findViewById(R.id.listView_search);

        showFavoriteList(playlistID);

        //sets the OnItemClickListener of the ListView
        // so that the user can click on a favorite result and watch the corresponding video.
        videosFavorite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {

                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", favoriteResults.get(pos).getId());

                startActivity(intent);
                //here start the Player Activity
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorite, menu);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void showFavoriteList(String playlistID){
        final String playlistId = playlistID;

        new Thread(){
            public void run(){
                favoriteResults = fetch(playlistId);

                handler.post(new Runnable() {
                    public void run() {
                        updateVideosFound();   //show the FavoriteList in ListView
                    }
                });
            }
        }.start();

    }

    private List<VideoItem> fetch(String playlistID){

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(new JacksonFactory()).build();
        credential.setAccessToken(accessToken);

        // This object is used to make YouTube Data API requests.
        YouTube youtube;
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
                .setApplicationName("MyTube")
                .build();
        // Call the API to fetch the playlist item to the specified playlist.
        try {

            Log.d(TAG, "Fetch videos from playlist ");
            List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

            YouTube.PlaylistItems.List playlistItemRequest =
                    youtube.playlistItems().list("id,contentDetails,snippet");
            playlistItemRequest.setPlaylistId(playlistID);
            playlistItemRequest.
                    setFields("items(id,contentDetails/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails/default/url),nextPageToken,pageInfo");

            String nextToken = "";
            List<VideoItem> items = new ArrayList<VideoItem>();

            do{
                playlistItemRequest.setPageToken(nextToken);
                PlaylistItemListResponse response = playlistItemRequest.execute();
                playlistItemList.addAll(response.getItems());

                for(final PlaylistItem i: playlistItemList){
                    VideoItem item = new VideoItem();
                    item.setId(i.getContentDetails().getVideoId());
                    item.setTitle(i.getSnippet().getTitle());
                    item.setThumbnailURL(i.getSnippet().getThumbnails().getDefault().getUrl());
                    item.setPub_date(i.getSnippet().getPublishedAt());
                    item.setPlaylistItemId(i.getId());

                    // getViewCount from video Id
                    YouTubeConnector yc = new YouTubeConnector(getActivity());
                    try {
                        YouTube.Videos.List listVideosRequest = yc.getYouTube().videos().list("snippet, statistics").setId(i.getContentDetails().getVideoId());
                        listVideosRequest.setKey(yc.getKey());
                        Video video = listVideosRequest.execute().getItems().get(0);
                        BigInteger viewCount = video.getStatistics().getViewCount();
                        item.setViews(viewCount);

                    }catch (IOException e) {
                        Log.d("YC", "Could not query viewCount: " + e);
                    }
                    items.add(item);
                }

                //Todo, need debug
                nextToken = response.getNextPageToken();

            }while(nextToken != null);

            return items;

        } catch (IOException e) {
            Log.d(TAG, "Could not fetch video item to playlist: " + e);
            return null;
        }
    }

    private void updateVideosFound(){

        //create an ArrayAdapter
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity().getApplicationContext(),
                R.layout.video_item_search, favoriteResults) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.video_item_search, parent, false);
                }

                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView) convertView.findViewById(R.id.video_title);
                TextView views = (TextView) convertView.findViewById(R.id.video_review_no);
                TextView pub_date = (TextView) convertView.findViewById(R.id.video_date);
                CheckBox checkBox_select = (CheckBox) convertView.findViewById(R.id.checkbox_select);

                final VideoItem favoriteResult = favoriteResults.get(position);

                Picasso.with(getActivity().getApplicationContext())
                        .load(favoriteResult.getThumbnailURL()).into(thumbnail);
                title.setText(favoriteResult.getTitle());
                views.setText(favoriteResult.getViews().toString() + "  views");
                pub_date.setText(favoriteResult.getPub_date().toString().substring(0, 10));


                //Todo need add listener for check_box_select to remove the video from playlist
                checkBox_select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Delete checkbox is clicked ");

                        //Todo, should call at the options menus
                        //call the method to delete the video from Playlist
                        // deletePlaylistItem(playlistID,"aYuGVRC89sM" );
                        deletePlaylistItem(favoriteResult.getPlaylistItemId());
                        Toast.makeText(getActivity().getApplicationContext(),
                                "The video " + favoriteResult.getTitle() + " is deleted from your playlist", Toast.LENGTH_LONG).show();

                        //Todo, need to update list
                    }

                });

                //checkbox_favorite for fragment favorite, here is invisible
                convertView.findViewById(R.id.checkbox_favorite).setVisibility(View.INVISIBLE);
                return convertView;
            }
        };
        //Assign adapter to Listview
        videosFavorite.setAdapter(adapter);
    }


    // Delete video to playlist
    protected void deletePlaylistItem(final String videoId) {
        Log.d(TAG, "Token is: " + accessToken);
        Log.d(TAG, "videoId is: " + videoId);

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

                // Call the API to delete the playlist item to the specified playlist.
                try {
                    YouTube.PlaylistItems.Delete playlistItemsDeleteCommand = youtube.playlistItems().delete (videoId);
                            playlistItemsDeleteCommand.execute();
                    Log.d(TAG, "Item is deleted from playlist ");

                } catch (IOException e) {
                    Log.d(TAG, "Could not deleted video item from playlist: " + e);
                }
            }
        }.start();
    }

}
