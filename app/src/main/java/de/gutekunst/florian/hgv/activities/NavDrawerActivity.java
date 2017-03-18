package de.gutekunst.florian.hgv.activities;

import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v4.widget.*;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import java.util.*;

import de.gutekunst.florian.hgv.*;

public class NavDrawerActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private android.support.v7.widget.Toolbar toolbar;
    private String[] listOptions;
    private ActionBarDrawerToggle drawerToggle;
    private String title;
    private int selected = -1;
    private Memory memory;
    private Menu menu;
    private boolean showTermineItem;
    private String phpsessid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nav_drawer);

        memory = new Memory(NavDrawerActivity.this);
        listOptions = getResources().getStringArray(R.array.zugriff_list);

        phpsessid = getIntent().getStringExtra("phpsessid");

        //Toolbar holen
        toolbar = (Toolbar) findViewById(R.id.nav_toolbar);
        setSupportActionBar(toolbar);

        //Titel ?ndern, wenn "Back" geklickt wird
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_frame_layout);

                String[] titles = getResources().getStringArray(R.array.zugriff_list);

                if (fragment instanceof VertretungsplanFragment) {
                    setTitle(titles[0]);
                    selected = 0;
                } else if (fragment instanceof StundenplanFragment) {
                    setTitle(titles[1]);
                    selected = 1;
                } else if (fragment instanceof TermineFragment) {
                    setTitle(titles[2]);
                    selected = 2;
                } else if (fragment instanceof SchwarzesBrettFragment) {
                    setTitle(titles[3]);
                    selected = 3;
                } else if (fragment instanceof ElternbriefeFragment) {
                    setTitle(titles[4]);
                    selected = 4;
                } else {
                    setTitle("HGV");
                    selected = 5;
                }
            }
        });

        iniatializeDrawer();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //Wenn die Activity nicht neu gezeichnet wird, wird der Vertretungsplan ge?ffnet
        if (savedInstanceState == null) {
            Intent i = getIntent();
            selected = i.getIntExtra("selected", 0);

            selectItem(selected);
            return;
        }


        //Activity wird neu gezeichnet: => Altes Fragment ?ffnen
        int pos = savedInstanceState.getInt("selected", 0);
        toolbar.setTitle(listOptions[pos]);
        selected = pos;
    }

    private void iniatializeDrawer() {
        //Zugriffsrechte bestimmen
        boolean[] zugriff = memory.loadBooleanArray(getString(R.string.key_zugriff));
        ArrayList<String> options = new ArrayList<>();
        for (int i = 0; i < zugriff.length; i++) {
            if (zugriff[i]) {
                options.add(listOptions[i]);
            }
        }

        listOptions = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            listOptions[i] = options.get(i);
        }

        if (listOptions.length == 0) {
            Intent i = new Intent(NavDrawerActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        //Komponenten holen
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        //Liste initialisieren
        drawerList.setAdapter(new ArrayAdapter<String>(NavDrawerActivity.this, android.R.layout.simple_list_item_1, listOptions));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        //Drawer initialisieren
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(NavDrawerActivity.this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);

        drawerToggle.syncState();
    }

    /**
     * Tauscht das Fragment aus
     *
     * @param position Position des neuen Fragments in der Liste
     */
    private void selectItem(int position) {
        invalidateOptionsMenu();

        boolean backStack = true;
        if (selected == -1) {
            backStack = false;
        }

        selected = position;

        //Fragment holen
        Fragment fragment;
        switch (listOptions[position]) {
            case "Vertretungsplan":
                fragment = new VertretungsplanFragment();
                break;
            case "Stundenplan":
                fragment = new StundenplanFragment();
                break;
            case "Termine":
                fragment = new TermineFragment();
                break;
            case "Schwarzes Brett":
                fragment = new SchwarzesBrettFragment();
                break;
            case "Elternbriefe":
                fragment = new ElternbriefeFragment();
                break;
            default:
                fragment = new VertretungsplanFragment();
                break;
        }

        if (fragment instanceof TermineFragment) {
            showTermineItem = true;
        } else {
            showTermineItem = false;
        }
        invalidateOptionsMenu();

        //Fragment austauschen
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (backStack) {
            fragmentManager.beginTransaction().replace(R.id.nav_frame_layout, fragment).addToBackStack(null).commit();
        } else {
            fragmentManager.beginTransaction().replace(R.id.nav_frame_layout, fragment).commit();
        }

        drawerList.setItemChecked(position, true);
        setTitle(listOptions[position]);
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title.toString();
        toolbar.setTitle(title);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.nav_menu_termine).setVisible(showTermineItem);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_nav_test_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.nav_menu_options:
                //OptionsActivity starten
                i = new Intent(NavDrawerActivity.this, OptionsActivity.class);
                startActivity(i);
                return true;
            case R.id.nav_menu_termine:
                //EditTermineActivity starten
                i = new Intent(NavDrawerActivity.this, EditTermineActivity.class);
                i.putExtra("phpsessid", phpsessid);
                startActivity(i);
                return true;
            default:
                return drawerToggle.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("selected", selected);

        super.onSaveInstanceState(outState);
    }


    public String getPhpsessid() {
        return phpsessid;
    }

    public int getSelected() {
        return selected;
    }
}
