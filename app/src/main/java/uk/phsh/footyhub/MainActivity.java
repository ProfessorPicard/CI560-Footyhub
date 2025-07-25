package uk.phsh.footyhub;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Objects;
import uk.phsh.footyhub.adapters.DrawNavAdapter;
import uk.phsh.footyhub.fragments.FixtureFragment;
import uk.phsh.footyhub.fragments.HomeFragment;
import uk.phsh.footyhub.fragments.NewsFragment;
import uk.phsh.footyhub.fragments.SelectTeamFragment;
import uk.phsh.footyhub.fragments.SettingsFragment;
import uk.phsh.footyhub.fragments.StandingsFragment;
import uk.phsh.footyhub.interfaces.I_FragmentCallback;
import uk.phsh.footyhub.models.NavItem;

public class MainActivity extends AppCompatActivity implements I_FragmentCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    private ListView _navDrawerList;
    private RelativeLayout _drawContainer;
    private ActionBarDrawerToggle _drawerToggle;
    private DrawerLayout _drawerLayout;
    private boolean _darkMode;
    private final ArrayList<NavItem> _navItems = new ArrayList<>();
    private ActionBar actionBar;

    @Override
    public void recreate() {
        super.recreate();
        setup(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setup(savedInstanceState == null);
    }

    private void setup(boolean loadDefaultFrag) {
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        _darkMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("darkMode", false);
        setDarkMode();

        setContentView(R.layout.activity_main);

        HomeFragment _homeFragment = new HomeFragment(this);
        SelectTeamFragment _selectTeamFragment = new SelectTeamFragment(this);
        NewsFragment _newsFragment = new NewsFragment(this);
        FixtureFragment _fixturesFragment = new FixtureFragment(this);
        StandingsFragment _standingsFragment = new StandingsFragment(this);
        SettingsFragment _settingsFragment = new SettingsFragment(this);

        _navItems.add(new NavItem(getResources().getString(R.string.change_favourite_drawer_menu), R.drawable.change, _selectTeamFragment));
        _navItems.add(new NavItem(getResources().getString(R.string.home_drawer_menu), R.drawable.home, _homeFragment));
        _navItems.add(new NavItem(getResources().getString(R.string.team_news_drawer_menu), R.drawable.news, _newsFragment));
        _navItems.add(new NavItem(getResources().getString(R.string.fixtures_drawer_menu), R.drawable.event, _fixturesFragment));
        _navItems.add(new NavItem(getResources().getString(R.string.standings_drawer_menu), R.drawable.standings, _standingsFragment));
        _navItems.add(new NavItem(getResources().getString(R.string.settings_drawer_menu), R.drawable.settings, _settingsFragment));

        _drawerLayout = findViewById(R.id.drawerLayout);
        _drawContainer  = findViewById(R.id.drawerContainer);
        _navDrawerList = findViewById(R.id.navList);
        DrawNavAdapter adapter = new DrawNavAdapter(this, _navItems);
        _navDrawerList.setAdapter(adapter);

        _navDrawerList.setOnItemClickListener((parent, view, position, id) -> selectItemFromDrawer(position));

        _drawerToggle = new ActionBarDrawerToggle(this, _drawerLayout, R.string.drawer_open, R.string.drawer_closed) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d("Main Class", "onDrawerClosed: " + getTitle());

                invalidateOptionsMenu();
            }
        };
        _drawerLayout.addDrawerListener(_drawerToggle);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary, null)));
        }

        RelativeLayout _selectedTeamContainer = findViewById(R.id.selectedTeamContainer);
        ImageView _selectedTeamImg = findViewById(R.id.selectedTeamImg);
        TextView _selectedTeamTxt = findViewById(R.id.selectedTeamTxt);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean favTeamSelected = prefs.getBoolean("favouriteTeamSelected", false);

        if(!favTeamSelected) {
            _selectedTeamContainer.setVisibility(View.GONE);
            if(loadDefaultFrag)
                changeFragment(_selectTeamFragment);
        } else {
            String teamLogo = prefs.getString("favouriteTeamLogo", "");
            _selectedTeamTxt.setText(prefs.getString("favouriteTeamName", ""));
            Picasso.get().load(teamLogo).into(_selectedTeamImg);
            _selectedTeamContainer.setVisibility(View.VISIBLE);
            if(loadDefaultFrag)
                changeFragment(_homeFragment);
        }

    }

    private void selectItemFromDrawer(int position) {
        NavItem item = _navItems.get(position);
        Fragment fragment = item.getFragment();
        changeFragment(fragment);

        _navDrawerList.setItemChecked(position, true);
        setTitle(item.getTitle());

        _drawerLayout.closeDrawer(_drawContainer);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        _drawerToggle.syncState();
    }

    private void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.mainContent, fragment)
                .commit();
    }

    @Override
    public void changeActionbarTitle(String title) {
        runOnUiThread(() -> actionBar.setTitle(title));
    }

    private void setDarkMode() {
        if (_darkMode) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        if (Objects.requireNonNull(key).equals("darkMode")) {
            _darkMode = sharedPreferences.getBoolean(key, false);
            setDarkMode();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
