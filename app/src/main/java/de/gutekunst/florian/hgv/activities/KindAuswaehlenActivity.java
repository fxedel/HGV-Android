package de.gutekunst.florian.hgv.activities;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import java.util.*;

import de.gutekunst.florian.hgv.*;
import de.gutekunst.florian.hgv.internet.*;

public class KindAuswaehlenActivity extends AppCompatActivity {

    private Memory memory;
    private String phpsessid;
    private HashMap<String, Integer> kinder = new HashMap<>();
    private RadioGroup rg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kind_auswaehlen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.ka_toolbar);
        setSupportActionBar(toolbar);

        memory = new Memory(KindAuswaehlenActivity.this);
        phpsessid = getIntent().getStringExtra("phpsessid");
        rg = (RadioGroup) findViewById(R.id.ka_rg);

        new Downloader().execute();
    }

    public void onOK(View v) {
        String name = ((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText().toString();
        int id = kinder.get(name);
        memory.setInt(getString(R.string.key_id), id);

        Intent i = new Intent(KindAuswaehlenActivity.this, MainActivity.class);
        startActivity(i);
    }

    private class Downloader extends AsyncTask<Void, Void, ArrayList<Kind>> {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(KindAuswaehlenActivity.this);
            pd.setMessage("Bitte warten");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected ArrayList<Kind> doInBackground(Void... params) {
            InternetManager im = new InternetManager();
            im.phpsessid = phpsessid;

            return im.getKinder();
        }

        @Override
        protected void onPostExecute(ArrayList<Kind> kinderList) {
            if (kinderList.size() == 1) {
                memory.setInt(getString(R.string.key_id), kinderList.get(0).getId());

                Intent i = new Intent(KindAuswaehlenActivity.this, MainActivity.class);
                startActivity(i);

                return;
            }

            for (Kind k : kinderList) {
                kinder.put(k.getName(), k.getId());
            }

            for (Kind k : kinderList) {
                RadioButton radioButton = new RadioButton(KindAuswaehlenActivity.this);
                radioButton.setText(k.getName());
                rg.addView(radioButton);
            }

            if (rg.getChildCount() > 0) {
                rg.check(rg.getChildAt(0).getId());
            }
            pd.cancel();
        }
    }
}
