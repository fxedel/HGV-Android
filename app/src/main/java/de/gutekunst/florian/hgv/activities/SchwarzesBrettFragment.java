package de.gutekunst.florian.hgv.activities;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.Fragment;
import android.support.v4.content.*;
import android.support.v7.widget.*;
import android.text.*;
import android.view.*;
import android.widget.*;

import java.io.*;

import de.gutekunst.florian.hgv.R;
import de.gutekunst.florian.hgv.internet.*;
import de.gutekunst.florian.hgv.schwarzesbrett.*;

public class SchwarzesBrettFragment extends Fragment {

    private Context context;
    private TextView errorTextView;
    private ProgressBar progressBar;
    private LinearLayout linearLayout;
    private static final int PERISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 13;
    private String phpsessid;
    private int id;
    private float scale;
    private int selected;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        NavDrawerActivity parent = (NavDrawerActivity) getActivity();

        phpsessid = parent.getPhpsessid();
        id = parent.getId();
        selected = parent.getSelected();

        return inflater.inflate(R.layout.fragment_schwarzes_brett, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Komponenten holen
        context = view.getContext();

        errorTextView = (TextView) view.findViewById(R.id.sb_tv);

        progressBar = (ProgressBar) view.findViewById(R.id.sb_pb);

        linearLayout = (LinearLayout) view.findViewById(R.id.sb_ll);

        scale = getResources().getDisplayMetrics().density;

        //Username & Passwort holen
        InternetManager internetManager = new InternetManager();

        //Testen, ob das Handy Internet hat
        boolean hasInternet = InternetManager.checkForIntenet(context);
        if (hasInternet) {
            //Wenn SDK > 23, Permission zum Schreiben einholen
            if (Build.VERSION.SDK_INT >= 23) {
                checkPermissions();
            } else {
                new SchwarzesBrettFragment.Downloader().execute();
            }
        } else {
            //Kein Internet
            progressBar.setVisibility(View.GONE);
            errorTextView.setText("Download gescheitert: Es ist kein Internet verfügbar");
        }
    }

    /**
     * Testet, ob die nötigen Permissions vorhanden sind und besorgt sie gegebnenfalls
     */
    private void checkPermissions() {
        //Testet, ob die Permission vorhanden ist
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            new SchwarzesBrettFragment.Downloader().execute();
            return;
        }

        //Permission ist nicht vorhanden
        //Testet, ob der Nutzer die Permission bereits einmal abgelehnt hat
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //Dialog als Erkärung, warum die Permission gebraucht wird, zeigen
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
            builder.setMessage("Die App braucht die Erlaubnis, um das PDF im Download-Ordner speicher zu können");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Permission holen
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            });

            builder.create().show();
        } else {
            //Permission holen
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission Granted
            } else {
                //Permission Denied
            }
        }

        //Download starten
        new SchwarzesBrettFragment.Downloader().execute();
    }

    private class Downloader extends AsyncTask<Void, Void, SchwarzesBrett> {

        private int error = 0;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected SchwarzesBrett doInBackground(Void... params) {
            //Username & Passwort holen
            InternetManager internetManager = new InternetManager();
            internetManager.phpsessid = phpsessid;
            internetManager.id = id;

            //Schwarzes Brett herunterladen
            SchwarzesBrett schwarzesBrett = new SchwarzesBrett();

            try {
                schwarzesBrett = internetManager.getSchwarzesBrett();
            } catch (DownloadFailedException e) {
                //Download gescheitert
                if (e.getReason() == DownloadFailedException.ERR_WRONG_USERNAME) {
                    error = 1;
                } else if (e.getReason() == DownloadFailedException.ERR_ADRESS_UNREACHABLE) {
                    error = 2;
                }
            }

            return schwarzesBrett;
        }

        @Override
        protected void onPostExecute(SchwarzesBrett schwarzesBrett) {
            progressBar.setVisibility(View.GONE);

            //Testet, ob der Download gescheitert ist
            if (error != 0) {
                //Download gescheitert
                if (error == 1) {
                    Intent i = new Intent(context, MainActivity.class);
                    i.putExtra("selected", selected);
                    startActivity(i);
                } else if (error == 2) {
                    errorTextView.setText("Download gescheitert: Das Elternportal ist nicht erreichbar");
                } else if (error == 4) {
                    errorTextView.setText("Download gescheitert: Fehler beim Selektieren des Kindes");
                }
            } else {
                //Download erfolgreich
                createSchwarzesBrett(schwarzesBrett);
            }
        }

        /**
         * Erstellt die CardViews des Schwarzen Brettes
         * @param schwarzesBrett Schwarzes Brett
         */
        private void createSchwarzesBrett(SchwarzesBrett schwarzesBrett) {
            //Testet, ob Einträge vorhanden sind
            if (schwarzesBrett.getEintraege().size() == 0) {
                errorTextView.setText("Keine Einträge");
                return;
            }

            //Einträge erstellen
            for (final SchwarzesBrettEintrag e : schwarzesBrett.getEintraege()) {
                //Datum, Überschrift, Nachricht holen
                String date = e.getDate();
                String headline = e.getHeadline();
                String message = e.getMessage();

                //CardView erstellen
                CardView cardView = new CardView(context);
                cardView.setUseCompatPadding(true);
                int sizeInDp = 5;
                int dpAsPixels = (int) (sizeInDp * scale + 0.5f);
                cardView.setContentPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);

                //LinearLayout erstellen
                LinearLayout cardLayout = new LinearLayout(context);
                cardLayout.setOrientation(LinearLayout.VERTICAL);

                //TextView des Datums erstellen
                TextView dateTextView = new TextView(context);
                dateTextView.setText(date);
                LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dateParams.gravity = Gravity.RIGHT;
                cardLayout.addView(dateTextView, dateParams);

                //TextView der Überschrift erstellen
                TextView headlineTextView = new TextView(context);
                headlineTextView.setTypeface(Typeface.DEFAULT_BOLD);
                headlineTextView.setText(headline);
                headlineTextView.setTextColor(Color.BLACK);
                LinearLayout.LayoutParams headlineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                cardLayout.addView(headlineTextView, headlineParams);

                //TextView der Nachricht erstellen
                TextView messageTextView = new TextView(context);
                messageTextView.setTextColor(Color.BLACK);
                if (Build.VERSION.SDK_INT < 24) {
                    messageTextView.setText(Html.fromHtml(message));
                } else {
                    messageTextView.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
                }
                LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                cardLayout.addView(messageTextView, messageParams);

                //Falls nötig Anhang und onClick() hinzufügen
                if (e.getAnhang().length() != 0) {
                    //Anhang
                    final TextView anhangTextView = new TextView(context);
                    anhangTextView.setText("Anhang: " + e.getAnhang());
                    cardLayout.addView(anhangTextView);

                    //Anhang herunterladen
                    cardLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Anhang herunterladen

                            //Testen, ob das Handy Internet hat
                            boolean hasInternet = InternetManager.checkForIntenet(context);
                            if (hasInternet) {
                                //Internet vorhanden: => Download starten
                                new AnhangDownloader().execute("https:" + e.getAnhangLink());
                            } else {
                                //Kein Internet
                                Toast.makeText(context, "Download gescheitert: Es ist kein Internet verfügbar", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //LinearLayout und CardView dem jeweiligen Parent hinzufügen
                cardView.addView(cardLayout, new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(cardView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }

        private class AnhangDownloader extends AsyncTask<String, Void, Integer> {
            private ProgressDialog pd;
            private String name;

            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(context);
                pd.setMessage("Downloading");
                pd.setCancelable(false);
                pd.show();
            }

            @Override
            protected Integer doInBackground(String... params) {
                //Testen, ob die benötigte Permission vorhanden ist
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    //Permission vorhanden: => Download starten

                    String[] parts = params[0].split("/");
                    name = parts[parts.length - 1];

                    return new InternetManager().downloadPdf(params[0]);
                } else {
                    //Permision Denied: => Fehler zurückgeben
                    return 2;
                }
            }

            @Override
            protected void onPostExecute(Integer error) {
                pd.cancel();

                //Testet, ob der Download erfolgreich war
                if (error != 0) {
                    //Download fehlgeschlagen
                    if (error == 1) {
                        Toast.makeText(context, "Download gescheitert: Das Elternportal ist nicht erreichbar", Toast.LENGTH_LONG).show();
                    } else if (error == 2) {
                        Toast.makeText(context, "Download gescheitert: Der Zugriff auf den Download-Ordner wurde verweigert", Toast.LENGTH_LONG).show();
                    }
                } else {
                    //Download erfolgreich

                    //PDF öffnen
                    File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);

                    if (!pdfFile.exists()) {
                        Toast.makeText(context, "Das PDF wurde nicht gefunden", Toast.LENGTH_LONG).show();
                    }

                    Uri path = Uri.fromFile(pdfFile);
                    Intent objIntent = new Intent(Intent.ACTION_VIEW);
                    objIntent.setDataAndType(path, "application/pdf");
                    objIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    try {
                        startActivity(objIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "Sie haben keine App zum Öffnen des PDFs installiert", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
}
