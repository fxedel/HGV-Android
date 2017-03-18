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

public class TermineAllgemeinFragment extends Fragment {

    private ProgressBar progressBar;
    private WebView webView;
    private Context context;
    private String phpsessid;
    private int selected;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        NavDrawerActivity parent = (NavDrawerActivity) getActivity();

        phpsessid = parent.getPhpsessid();
        selected = parent.getSelected();

        return inflater.inflate(R.layout.fragment_termine_allgemein, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = (ProgressBar) view.findViewById(R.id.all_pb);
        webView = (WebView) view.findViewById(R.id.all_wv);
        context = view.getContext();

        //WebView konfigurieren
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        InternetManager internetManager = new InternetManager();

        boolean hasInternet = InternetManager.checkForIntenet(context);

        if (hasInternet) {
            new Downloader().execute();
        } else {
            progressBar.setVisibility(View.GONE);
            webView.loadDataWithBaseURL("empty", "Download gescheitert: Es ist kein Internet verfügbar", "text/html", "UTF-8", null);
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

            String termine = "";

            try {
                //Termine herunterladen
                termine = internetManager.getTermineAllgemein();
            } catch (DownloadFailedException e) {
                if (e.getReason() == DownloadFailedException.ERR_WRONG_USERNAME) {
                    error = 1;
                } else if (e.getReason() == DownloadFailedException.ERR_ADRESS_UNREACHABLE) {
                    error = 2;
                }
            }

            return termine;
        }

        @Override
        protected void onPostExecute(String string) {
            progressBar.setVisibility(View.GONE);

            if (error != 0) {
                if (error == 1) {
                    //Username oder Passwort stimmen nicht
                    Intent i = new Intent(context, MainActivity.class);
                    i.putExtra("selected", selected);
                    startActivity(i);
                } else if (error == 2) {
                    //Das Elternportal ist nicht erreichbar
                    webView.loadDataWithBaseURL("empty", "Download gescheitert: Das Elternportal ist nicht erreichbar", "text/html", "UTF-8", null);
                }
            } else {
                //Termine darstellen
                webView.loadDataWithBaseURL("empty", string, "text/html", "UTF-8", null);
                webView.zoomOut();
                webView.zoomOut();
            }
        }
    }
}
