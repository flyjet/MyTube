package edu.sjsu.qi.mytube;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubeInitializationResult;

public class PlayerActivity extends YouTubeBaseActivity  implements
        YouTubePlayer.OnInitializedListener{

    private String GOOGLE_API_KEY = "AIzaSyB9pVjZKT3w_8xWhnScFo73sCeg6tmbLbo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        YouTubePlayerView youTubePlayerView = (YouTubePlayerView)findViewById(R.id.player_view);
        youTubePlayerView.initialize(GOOGLE_API_KEY, this);

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason){
        Toast.makeText(this, "Can not initialize YouTube Player ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer player, boolean wasRestored){
        if( !wasRestored ){
            player.cueVideo(getIntent().getStringExtra("VIDEO_ID"));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

}
