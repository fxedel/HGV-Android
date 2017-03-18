package de.gutekunst.florian.hgv.activities;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;

import java.util.*;

import de.gutekunst.florian.hgv.*;

public class OptionsActivity extends AppCompatActivity {

    private boolean returnToMain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        //Toolbar holen
        Toolbar toolbar = (Toolbar) findViewById(R.id.options_toolbar);
        toolbar.setTitle("Optionen");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        returnToMain = getIntent().getBooleanExtra("main", false);
    }

    /**
     * Loggt den Nutzer aus
     *
     * @param v
     */
    public void onLogout(View v) {
        //Fragen, ob auch custom Termine gelöscht werden sollen
        AlertDialog.Builder builder = new AlertDialog.Builder(OptionsActivity.this);
        builder.setMessage("Sollen die Termine gelöscht werden?");
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Daten löschen
                Memory memory = new Memory(OptionsActivity.this);
                memory.clear();

                //LoginActivity starten
                Intent i = new Intent(OptionsActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
        builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Daten löschen
                Memory memory = new Memory(OptionsActivity.this);
                Set<String> termine = memory.getStringSet(getString(R.string.key_termine), new HashSet<String>());
                memory.clear();
                memory.setStringSet(getString(R.string.key_termine), termine);

                //LoginActivity starten
                Intent i = new Intent(OptionsActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
        builder.setCancelable(true);
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Der "Zur?ck-Pfeil" wurde geklickt
        if (item.getItemId() == android.R.id.home) {
            if (!returnToMain) {
                finish();
            } else {
                Intent i = new Intent(OptionsActivity.this, MainActivity.class);
                startActivity(i);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Der Zur?ck-Button in der Statusleiste wurde geklickt
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && returnToMain) {
            Intent i = new Intent(OptionsActivity.this, MainActivity.class);
            startActivity(i);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
