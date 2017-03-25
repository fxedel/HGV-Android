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

import static android.view.View.*;

public class VertretungsplanFragment extends Fragment {

    private WebView today, tomorrow, stand;
    private Context context;
    private ProgressBar progressBar;
    private TextView errorTextView;
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

        return inflater.inflate(R.layout.fragment_vertretungsplan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();

        today = (WebView) view.findViewById(R.id.vert_wv_today);
        tomorrow = (WebView) view.findViewById(R.id.vert_wv_tomorrow);
        stand = (WebView) view.findViewById(R.id.vert_wv_stand);

        progressBar = (ProgressBar) view.findViewById(R.id.vert_pb);

        errorTextView = (TextView) view.findViewById(R.id.vert_tv);

        InternetManager internetManager = new InternetManager();

        boolean hasInternet = InternetManager.checkForIntenet(context);

        if (hasInternet) {
            new Downloader().execute();
        } else {
            progressBar.setVisibility(View.GONE);
            errorTextView.setText("Download gescheitert: Es ist kein Internet verfügbar");
        }
    }

    private class Downloader extends AsyncTask<Void, Void, String[]> {

        private int error = 0;
        private String diag = "";

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(Void... params) {
            InternetManager internetManager = new InternetManager();
            internetManager.phpsessid = phpsessid;
            internetManager.id = id;

            String vertretungsplanToday = "", vertretungsplanTomorrow = "", stand = "";

            try {
                vertretungsplanToday = internetManager.getVertretungsplanToday();
                vertretungsplanTomorrow = internetManager.getVertretungsplanTomorrow();
                stand = internetManager.getStand();
            } catch (DownloadFailedException e) {
                if (e.getReason() == DownloadFailedException.ERR_WRONG_USERNAME) {
                    error = 1;
                } else if (e.getReason() == DownloadFailedException.ERR_ADRESS_UNREACHABLE) {
                    error = 2;
                } else if (e.getReason() == DownloadFailedException.ERR_UNKNOWN) {
                    error = 3;
                }
            }

            return new String[]{vertretungsplanToday, vertretungsplanTomorrow, stand};
        }

        @Override
        protected void onPostExecute(String[] strings) {
            progressBar.setVisibility(GONE);

            if (error != 0) {
                if (error == 1) {
                    //Falsche Anmeldedaten
                    Intent i = new Intent(context, MainActivity.class);
                    i.putExtra("selected", selected);
                    startActivity(i);
                } else if (error == 2) {
                    errorTextView.setText("Download gescheitert: Das Elternportal ist nicht erreichbar");
                } else if (error == 3) {
                    errorTextView.setText("Download gescheitert");
                } else if (error == 4) {
                    errorTextView.setText("Download gescheitert: Fehler beim Selektieren des Kindes");
                }
            } else {
                //Download erfolgreich
                today.loadDataWithBaseURL("empty", strings[0], "text/html", "UTF-8", null);
                tomorrow.loadDataWithBaseURL("empty", strings[1], "text/html", "UTF-8", null);
                stand.loadDataWithBaseURL("empty", strings[2], "text/html", "UTF-8", null);
            }
        }
    }
}
