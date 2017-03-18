package de.gutekunst.florian.hgv.activities;

import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import java.util.*;

import de.gutekunst.florian.hgv.*;
import de.gutekunst.florian.hgv.internet.*;
import de.gutekunst.florian.hgv.termine.*;

public class TermineSchulaufgabenFragment extends Fragment {

    private ProgressBar progressBar;
    private WebView webView;
    private Context context;
    private TextView errorTextView;
    private String phpsessid;
    private int selected;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        NavDrawerActivity parent = (NavDrawerActivity) getActivity();

        phpsessid = parent.getPhpsessid();
        selected = parent.getSelected();

        return inflater.inflate(R.layout.fragment_termine_schulaufgaben, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Komponenten holen
        progressBar = (ProgressBar) view.findViewById(R.id.sa_pb);
        webView = (WebView) view.findViewById(R.id.sa_wv);
        context = view.getContext();
        errorTextView = (TextView) view.findViewById(R.id.sa_tv);

        InternetManager internetManager = new InternetManager();

        boolean hasInternet = InternetManager.checkForIntenet(context);

        if (hasInternet) {
            new TermineSchulaufgabenFragment.Downloader().execute();
        } else {
            progressBar.setVisibility(View.GONE);
            errorTextView.setText("Download gescheitert: Es ist kein Internet verfügbar");
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
                termine = internetManager.getTermineSchulaufgaben();
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
                    Intent i = new Intent(context, MainActivity.class);
                    i.putExtra("selected", selected);
                    startActivity(i);
                } else if (error == 2) {
                    errorTextView.setText("Download gescheitert: Das Elternportal ist nicht erreichbar");
                }
            } else {
                Set<String> termineSet = new Memory(context).getStringSet(getString(R.string.key_termine), new HashSet<String>());
                ArrayList<Vector<String>> termineList = new ArrayList<>();
                for (String s : termineSet) {
                    String[] termin = s.split("\\\\");
                    termineList.add(new Vector<String>(Arrays.asList(termin)));
                }

                webView.loadDataWithBaseURL("empty", Termine.insert(string, termineList), "text/html", "UTF-8", null);
            }
        }
    }
}
