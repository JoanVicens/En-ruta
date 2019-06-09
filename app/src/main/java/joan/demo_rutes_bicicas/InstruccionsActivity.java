package joan.demo_rutes_bicicas;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;

public class InstruccionsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instruccions_mapa);

        ConstraintLayout relativeLayout = findViewById(R.id.rl_instruccions);
        relativeLayout.setOnClickListener(view -> onBackPressed());
    }


}
