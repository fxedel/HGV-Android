package de.gutekunst.florian.hgv.activities;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import de.gutekunst.florian.hgv.*;
import de.gutekunst.florian.hgv.internet.*;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginButton;
    private ProgressDialog progressDialog;
    private Memory memory;
    private String[] zugriffArray;
    private boolean[] zugriff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Toolbar setzen
        Toolbar toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        memory = new Memory(LoginActivity.this);

        zugriffArray = getResources().getStringArray(R.array.zugriff_list);
    }

    /**
     * Methode, die aufgerufen wird, wenn der "Zugriff auf"-Button gedr?ckt wird
     * @param v 
     */
    public void onZugriff(View v) {
        //Zugriff-Array mit true initialisieren
        if (zugriff == null) {
            zugriff = new boolean[zugriffArray.length];
            for (int i = 0; i < zugriff.length; i++) {
                zugriff[i] = true;
            }
        }

        //Dialog zum Ausw?hlen der Seiten, auf die Zugriff gestattet werden soll
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Zugriff auf: ");

        builder.setMultiChoiceItems(zugriffArray, zugriff, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //zugriff-Array updaten
                ListView list = ((AlertDialog) dialog).getListView();

                for (int i = 0; i< list.getCount(); i++) {
                    zugriff[i] = list.isItemChecked(i);
                }
            }
        });

        builder.create().show();
    }

    /**
     * Loggt den Nutzer mit den Daten aus den Textfeldern ein
     * @param v
     */
    public void onLogin(View v) {
        //Komponenten holen
        email = (EditText) findViewById(R.id.login_et_email);
        password = (EditText) findViewById(R.id.login_et_password);
        loginButton = (Button) findViewById(R.id.login_btn_login);

        //Testen, ob Daten eingegeben wurden
        if (email.length() == 0 || password.length() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Bitte f?llen Sie alle Felder aus");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            builder.create().show();

            return;
        }

        //Testen, ob Internet vorhanden ist
        boolean hasInternet = InternetManager.checkForIntenet(LoginActivity.this);

        if (hasInternet) {
            //Internet vorhanden: => Login starten
            new Login().execute(email.getText().toString().trim(), password.getText().toString().trim());
        } else {
            //Kein Internet
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Es ist kein Internet verf?gbar");
            builder.setPositiveButton("Nochmal Versuchen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });

            builder.create().show();
        }
    }

    private class Login extends AsyncTask<String, Void, Integer> {

        private String phpsessid = "";

        @Override
        protected void onPreExecute() {
            loginButton.setEnabled(false);

            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Einloggen...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            InternetManager internetManager = new InternetManager();

            int error = 0;
            boolean loginSuccessful = false;
            try {
                //Loggt den Nutzer ein
                loginSuccessful = internetManager.login(params[0], params[1]);
                phpsessid = internetManager.phpsessid;

                if (!loginSuccessful) {
                    error = 1;
                }
            } catch (DownloadFailedException e) {
                if (e.getReason() == DownloadFailedException.ERR_WRONG_USERNAME) {
                    error = 1;
                } else if (e.getReason() == DownloadFailedException.ERR_ADRESS_UNREACHABLE) {
                    error = 2;
                }
            }

            return error;
        }

        @Override
        protected void onPostExecute(Integer error) {
            progressDialog.cancel();

            //Testet, ob das Login erfolgreich war
            if (error == 0) {
                //Login erfolgreich

                //Anmeldedaten speichern
                memory.setSecureString(getString(R.string.key_username_sec), getString(R.string.key_alias), email.getText().toString().trim());
                memory.setSecureString(getString(R.string.key_password_sec), getString(R.string.key_alias), password.getText().toString().trim());
                memory.setBoolean(getString(R.string.key_logged_in), true);

                if (zugriff == null) {
                    zugriff = new boolean[zugriffArray.length];
                    for (int i = 0; i < zugriff.length; i++) {
                        zugriff[i] = true;
                    }
                }
                memory.storeBooleanArray(zugriff, getString(R.string.key_zugriff));

                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT);

                //KindAuswaehlenActivity starten
                Intent i = new Intent(LoginActivity.this, KindAuswaehlenActivity.class);
                i.putExtra("phpsessid", phpsessid);
                startActivity(i);
            } else {
                //Login fehlgeschlagen

                loginButton.setEnabled(true);

                //Testen, was der Fehler war
                if (error == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("e-Mail oder Passwort stimmen nicht");
                    builder.setPositiveButton("Nochmal Versuchen", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });

                    builder.create().show();

                    return;
                } else if (error == 2) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Das Elternportal ist nicht erreichbar");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    builder.create().show();

                    return;
                }
            }
        }
    }
}
