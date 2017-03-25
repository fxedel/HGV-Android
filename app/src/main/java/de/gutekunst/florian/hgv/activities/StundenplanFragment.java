package de.gutekunst.florian.hgv.activities;

import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import de.gutekunst.florian.hgv.*;
import de.gutekunst.florian.hgv.internet.*;

public class StundenplanFragment extends Fragment {

    private Context context;
    private WebView stundenplan;
    private ProgressBar progressBar;
    private String phpsessid;
    private int id;
    private int selected;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        NavDrawerActivity parent = (NavDrawerActivity) getActivity();

        phpsessid = parent.getPhpsessid();
        id = parent.getId();
        selected = parent.getSelected();

        return inflater.inflate(R.layout.fragment_stundenplan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();

        // WebView konfigurieren
        stundenplan = (WebView) view.findViewById(R.id.stund_wv);
        stundenplan.getSettings().setLoadWithOverviewMode(true);
        stundenplan.getSettings().setUseWideViewPort(true);
        stundenplan.getSettings().setBuiltInZoomControls(true);
        stundenplan.getSettings().setDisplayZoomControls(false);


        progressBar = (ProgressBar) view.findViewById(R.id.stund_pb);

        InternetManager internetManager = new InternetManager();

        boolean hasInternet = InternetManager.checkForIntenet(context);
        if (hasInternet) {
            new Downloader().execute();
        } else {
            stundenplan.loadDataWithBaseURL("empty", "Download gescheitert: Es ist kein Internet verfügbar", "text/html", "UTF-8", null);
            progressBar.setVisibility(View.GONE);
        }
    }

    private class Downloader extends AsyncTask<Void, Void, String> {

        private int error = 0;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            InternetManager internetManager = new InternetManager();
            internetManager.phpsessid = phpsessid;
            internetManager.id = id;


            String stundenplanString = "";

            try {
                //Stundenplan herunterladen
                stundenplanString = internetManager.getStundenplan();
            } catch (DownloadFailedException e) {
                if (e.getReason() == DownloadFailedException.ERR_WRONG_USERNAME) {
                    error = 1;
                } else if (e.getReason() == DownloadFailedException.ERR_ADRESS_UNREACHABLE) {
                    error = 2;
                }
            }

            return stundenplanString;
        }

        @Override
        protected void onPostExecute(String string) {
            progressBar.setVisibility(View.GONE);

            if (error != 0) {
                if (error == 1) {
                    //Der Nutzername stimmt nicht
                    Intent i = new Intent(context, MainActivity.class);
                    i.putExtra("selected", selected);
                    startActivity(i);
                } else if (error == 2) {
                    //Das Elternportal ist nicht erreichbar
                    stundenplan.loadDataWithBaseURL("empty", "Download gescheitert: Das Elternportal ist nicht erreichbar", "text/html", "UTF-8", null);
                } else if (error == 4) {
                    stundenplan.loadDataWithBaseURL("empty", "Download gescheitert: Fehler beim Selektieren des Kindes", "text/html", "UTF-8", null);
                }
            } else {
                stundenplan.loadDataWithBaseURL("empty", string, "text/html", "UTF-8", null);
            }
        }
    }
}
