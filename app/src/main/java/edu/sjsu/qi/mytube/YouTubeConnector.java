package edu.sjsu.qi.mytube;

import android.content.Context;
import android.util.Log;

import com.google.api.services.youtube.YouTube;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * Created by qi on 10/5/15.
 */

//This is Helper class to communicate with YouTube API

public class YouTubeConnector {

    private YouTube youTube;
    private YouTube.Search.List query;
    private long NUMBER_OF_VIDEOS_RETURNED = 20;


    public static final String KEY = "AIzaSyC4ZO0TPGzFJpcZZVpHR2djiaVfN0f4Xv4"; //this is the browser key

    public YouTubeConnector(Context context) {
        youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {
            }
        }).setApplicationName(context.getString(R.string.app_name)).build();

        try {
            query = youTube.search().list("id,snippet");
            query.setKey(KEY);
            query.setType("video");
            query.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            query.setFields("items(id/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails/default/url)");
        } catch (IOException e) {
            Log.d("YouTube", "Could not initialize" + e);
        }
    }

    public List<VideoItem> search(String queryTerm) {
        query.setQ(queryTerm);
        try {
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for (SearchResult result : results) {
                VideoItem item = new VideoItem();
                item.setId(result.getId().getVideoId());
                item.setTitle(result.getSnippet().getTitle());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setPub_date(result.getSnippet().getPublishedAt());

                //getViewCount from video Id
                YouTube.Videos.List listVideosRequest = youTube.videos().list("snippet, statistics").setId(item.getId());
                listVideosRequest.setKey(KEY);
                Video video = listVideosRequest.execute().getItems().get(0);
                item.setViews(video.getStatistics().getViewCount());

                items.add(item);
                }
                return items;

        } catch (IOException e) {
            Log.d("YC", "Could not search: " + e);
            return null;
        }
    }

}