package de.gutekunst.florian.hgv.activities;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.*;

import de.gutekunst.florian.hgv.*;
import de.gutekunst.florian.hgv.internet.*;

public class MainActivity extends AppCompatActivity {

    private int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Testen, ob der Nutzer eingeloggt ist
        Memory memory = new Memory(MainActivity.this);

        if (memory.getBoolean(getString(R.string.key_logged_in), false)) {
            //Anmeldedaten holen
            String username = memory.getSecureString(getString(R.string.key_username_sec), getString(R.string.key_alias), null);
            String password = memory.getSecureString(getString(R.string.key_password_sec), getString(R.string.key_alias), null);
            id = memory.getInt(getString(R.string.key_id), -1);

            //Fehler beim Holen der Anmeldedaten
            if (username == null || password == null || id == -1) {
                //LoginActivity starten
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            } else {
                new Login().execute(username, password);
            }
        } else {
            //LoginActivity starten
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        }
    }

    private class Login extends AsyncTask<String, Void, String> {
        private ProgressDialog pd;
        private int error = 0;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(MainActivity.this);
            pd.setCancelable(false);
            pd.setMessage("Einloggen...");
            try {
                pd.show();
            } catch (Exception e) {
                //Do nothing
            }
        }

        @Override
        protected String doInBackground(String... params) {
            InternetManager internetManager = new InternetManager();

            boolean loginSuccessful = false;
            try {
                if (InternetManager.checkForIntenet(MainActivity.this)) {
                    loginSuccessful = internetManager.login(params[0], params[1]);

                    //Anmeldedaten aus der Arbeitsspeicher l?schen
                    params[0] = "";
                    params[1] = "";

                    if (!loginSuccessful) {
                        error = 1;
                    }
                } else {
                    error = 3;
                }
            } catch (DownloadFailedException e) {
                if (e.getReason() == DownloadFailedException.ERR_ADRESS_UNREACHABLE) {
                    error = 2;
                }
            }

            return internetManager.phpsessid;
        }

        @Override
        protected void onPostExecute(String phpsessid) {
            try {
                pd.cancel();
            } catch (Exception e) {
                //Do nothing
            }

            //Testen, ob der Nutzer eingeloggt werden konnte
            if (error == 1) {
                //Username oder Passwort stimmen nicht
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false)
                        .setMessage("Username oder Passwort stimmen nicht")
                        .setPositiveButton("Einstellungen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MainActivity.this, OptionsActivity.class);
                                i.putExtra("main", true);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("Nochmal versuchen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                        })
                        .create().show();
            } else if (error == 2) {
                //Das Elternportal ist nicht erreichbar
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false)
                        .setMessage("Das Elternportal ist nicht erreichbar")
                        .setPositiveButton("Nochmal versuchen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                        })
                        .create().show();
            } else if (error == 3) {
                //Kein Internet ist verf?gbar
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false)
                        .setMessage("Kein Internet verf?gbar")
                        .setPositiveButton("Nochmal versuchen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                        })
                        .create().show();
            } else {
                //Login erfolgreich
                //NavDrawerActivity starten
                Intent i = new Intent(MainActivity.this, NavDrawerActivity.class);
                i.putExtra("phpsessid", phpsessid);
                i.putExtra("id", id);
                int selected = getIntent().getIntExtra("selected", -1);
                if (selected != -1) {
                    i.putExtra("selected", getIntent().getIntExtra("selected", 0));
                }
                startActivity(i);
            }
        }
    }
}
