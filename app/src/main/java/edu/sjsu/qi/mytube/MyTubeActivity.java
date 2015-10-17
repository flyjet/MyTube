package edu.sjsu.qi.mytube;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.MenuItem;
import android.widget.SearchView;
import android.content.Intent;

public class MyTubeActivity extends Activity {

    private static final String TAG = MyTubeActivity.class.getSimpleName();

    // Declaring two tabs and corresponding fragments
    Tab searchTab, favoriteTab;
    Fragment searchFragment = new SearchFragment();
    Fragment favoriteFragment = new FavoriteFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get AccessToken from main activity
        Bundle extras = getIntent().getExtras();
        String token = extras.getString("AccessToken");
        Log.d(TAG, "Token from MyTube Activity: " + token);

        //send Token to search fragment and favorite fragment
        Bundle bundle = new Bundle();
        bundle.putString("Token", token);
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(bundle);

        FavoriteFragment favoriteFragment = new FavoriteFragment();
        favoriteFragment.setArguments(bundle);


        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_mytube);

        // Set up the action bar
        ActionBar actionBar = getActionBar();

        //Hiding ActionBar icon and title
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        //Creating ActionBar tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //Setting tabs
        searchTab = actionBar.newTab().setText("ALL");
        favoriteTab = actionBar.newTab().setText("FAVORITE");

        //Setting tab listeners
        searchTab.setTabListener(new TabListener(searchFragment));
        favoriteTab.setTabListener(new TabListener(favoriteFragment));

        //Adding tabs to the ActionBar
        actionBar.addTab(searchTab);
        actionBar.addTab(favoriteTab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mytube, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    //TabListener
    public class TabListener implements ActionBar.TabListener{

        private Fragment fragment;

        //Constructor
        public TabListener(Fragment fragment){
            this.fragment = fragment;
        }

        //When a tab is tapped, the FragmentTransaction replaces the content of MyTube layout
        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft){
            ft.replace(R.id.activity_mytube, fragment);
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft){
            ft.remove(fragment);
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }
}
