package de.gutekunst.florian.hgv.activities;

import android.app.AlertDialog;
import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v7.app.*;
import android.support.v7.widget.Toolbar;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.text.*;
import java.util.*;

import de.gutekunst.florian.hgv.*;

public class EditTermineActivity extends AppCompatActivity {

    private LinearLayout ll;
    private ImageButton addButton;
    private String phpsessid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_termine);

        phpsessid = getIntent().getStringExtra("phpsessid");

        //Toolbar
        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.et_toolbar);
        toolbar.setTitle("Termine Bearbeiten");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Komponenten holen
        ll = (LinearLayout) findViewById(R.id.et_ll);
        addButton = (ImageButton) findViewById(R.id.et_btn_add);

        createTermine();
    }

    /**
     * F?gt dem UI die Termine hinzu
     */
    private void createTermine() {
        Memory memory = new Memory(EditTermineActivity.this);

        //Eigene Termine parsen
        Set<String> termineSet = memory.getStringSet(getString(R.string.key_termine), new HashSet<String>());
        ArrayList<String> termine = new ArrayList<>(termineSet);

        //Eigene Termine sortieren
        Collections.sort(termine, new Comparator<String>() {
            @Override
            public int compare(String t1, String t2) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                try {
                    if (sdf.parse(t1).before(sdf.parse(t2))) {
                        return -1;
                    } else if (sdf.parse(t1).after(sdf.parse(t2))) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (java.text.ParseException e) {
                    return 0;
                }
            }
        });

        ll.removeAllViews();

        //Termine ausgeben
        for (final String s : termine) {
            final String[] termin = s.split("\\\\");

            LinearLayout linearLayout = new LinearLayout(EditTermineActivity.this);

            //Datum
            TextView dateTextView = new TextView(EditTermineActivity.this);
            dateTextView.setText(termin[0]);
            dateTextView.setTextSize(Dimension.SP, 20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            params.weight = 1.0f;
            params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            dateTextView.setLayoutParams(params);

            linearLayout.addView(dateTextView);

            //Beschreibung
            TextView terminTextView = new TextView(EditTermineActivity.this);
            terminTextView.setText(termin[1]);
            terminTextView.setTextSize(Dimension.SP, 20);
            terminTextView.setLayoutParams(params);

            linearLayout.addView(terminTextView);

            //L?schen-Button
            ImageButton deleteButton = new ImageButton(EditTermineActivity.this);
            deleteButton.setImageResource(R.drawable.ic_icon_delete);
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            deleteParams.gravity = Gravity.RIGHT;
            deleteButton.setLayoutParams(deleteParams);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Memory memory = new Memory(EditTermineActivity.this);
                    Set<String> termine = memory.getStringSet(getString(R.string.key_termine), new HashSet<String>());
                    termine.remove(termin[0] + "\\" + termin[1]);
                    memory.setStringSet(getString(R.string.key_termine), termine);

                    Log.d("Termine", termine.toString());

                    createTermine();
                }
            });

            linearLayout.addView(deleteButton);

            ll.addView(linearLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (termine.size() == 0) {
            TextView noEntriesTextView = new TextView(EditTermineActivity.this);
            noEntriesTextView.setText("Keine Eigenen Termine");
            noEntriesTextView.setTextSize(Dimension.SP, 20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;

            ll.addView(noEntriesTextView, params);
        }

        ll.addView(addButton);
    }

    /**
     * Methode, die aufgerufen wird, wenn auf den Button zum Erstellen eines
     * neuen Termins geklickt wird
     *
     * @param v
     */
    public void onAdd(View v) {
        //Dialog zum Ausw?hlen vopn Datum und Terminname erstellen
        final AlertDialog.Builder builder = new AlertDialog.Builder(EditTermineActivity.this);

        View viewInflated = View.inflate(EditTermineActivity.this, R.layout.dialog_add_termin, null);
        final EditText terminEditText = (EditText) viewInflated.findViewById(R.id.at_et);
        final DatePicker datePicker = (DatePicker) viewInflated.findViewById(R.id.at_dp);

        builder.setView(viewInflated);

        builder.setCancelable(false);
        builder.setNegativeButton(
                "Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick
                            (DialogInterface dialog, int which


                            ) {
                        //Do nothing
                    }
                });
        builder.setPositiveButton(
                "Hinzufügen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick
                            (DialogInterface dialog, int which

                            ) {
                        String terminName = terminEditText.getText().toString().trim();
                        String date = String.format("%02d", datePicker.getDayOfMonth()) + "." + String.format("%02d", datePicker.getMonth() + 1) + "." + String.format("%04d", datePicker.getYear());

                        if (terminName.indexOf("\\") != -1) {
                            //Im Terminnamen kommt das verbotene Zeichen '\' vor -> Nutzer benachrichtigen
                            AlertDialog.Builder errorBuilder = new AlertDialog.Builder(EditTermineActivity.this);

                            errorBuilder.setMessage("Das Zeichen '\\' ist nicht erlaubt");
                            errorBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /**
                                     * Do nothing
                                     */
                                }
                            });
                            errorBuilder.create().show();
                            return;
                        }

                        //Termin speichern
                        Memory memory = new Memory(EditTermineActivity.this);
                        Set<String> termine = memory.getStringSet(getString(R.string.key_termine), new HashSet<String>());
                        termine.add(date + "\\" + terminName);

                        memory.setStringSet(getString(R.string.key_termine), termine);

                        createTermine();
                    }
                }

        );

        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Der "Zur?ck-Pfeil" wurde geklickt
        if (item.getItemId() == android.R.id.home) {
            Intent i = new Intent(EditTermineActivity.this, NavDrawerActivity.class
            );
            i.putExtra("selected", 2);
            i.putExtra("phpsessid", phpsessid);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Der Zur?ck-Button in der Statusleiste wurde geklickt
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent i = new Intent(EditTermineActivity.this, NavDrawerActivity.class
            );
            i.putExtra("selected", 2);
            i.putExtra("phpsessid", phpsessid);
            startActivity(i);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
