package joan.demo_rutes_bicicas;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class SelectorOcupacio extends Activity {

    public static final String SEEK_RESULT = "SEEK RESULT";

    private SeekBar seekBar;
    private TextView numeroOcupacio;
    private Button btnGuardar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selector_ocupacio);

        Intent thisIntent = getIntent();
        Bundle bundleInfo = thisIntent.getExtras();

        int progresInicial = bundleInfo.getInt(MapsActivity.OCUPACIO_SELECCIONADA);

        seekBar = findViewById(R.id.seekBar);
        numeroOcupacio = findViewById(R.id.tv_number_min);
        btnGuardar = findViewById(R.id.btn_ocupacio_save);

        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.complementari_logo_fosc), PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.complementari_logo_fosc), PorterDuff.Mode.SRC_IN);

        seekBar.setProgress(progresInicial);
        numeroOcupacio.setText(String.valueOf(progresInicial));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(seekBar.getId() == R.id.seekBar) {
                    acutalizarValor(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnGuardar.setOnClickListener(view -> {
            switch (view.getId()) {
                case R.id.btn_ocupacio_save:
                    guardarInfo();
            }
        });

    }

    private void guardarInfo() {
        Intent data = new Intent();
        data.putExtra(SEEK_RESULT, seekBar.getProgress());
        data.setData(Uri.parse(String.valueOf(seekBar.getProgress())));
        setResult(RESULT_OK, data);
        finish();
    }

    private void acutalizarValor(int valor) {
        numeroOcupacio.setText(String.valueOf(valor));
    }

}
