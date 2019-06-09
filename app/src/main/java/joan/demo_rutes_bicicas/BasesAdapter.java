package joan.demo_rutes_bicicas;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class BasesAdapter extends RecyclerView.Adapter<BasesAdapter.SimpleViewHolder> {

    private final Context mContext;
    private ArrayList<Base> mData;

    private FavoridaListener favoridaListener;
    private FragmentListener fragmentListener;

    private Preferencies preferencies;

    private RecyclerView.LayoutManager layoutManager;

    private Vibrator vibrator;




    public void add(Base b,int position) {
        position = position == -1 ? getItemCount()  : position;
        mData.add(position,b);
        notifyItemInserted(position);
    }

    public void remove(int position){
        if (position < getItemCount()  ) {
            mData.remove(position);
            notifyItemRemoved(position);
        }
    }



    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final TextView nom;
        TextView ocupacio;
        TextView numero;
        ProgressBar ocupacioProgressBar;

        CheckBox chFav;


        public SimpleViewHolder(View view) {
            super(view);
            this.nom = view.findViewById(R.id.tv_targeta_fragment_nom);
            this.ocupacio = view.findViewById(R.id.tv_targeta_fragment_ocupacio);
            this.ocupacioProgressBar =  view.findViewById(R.id.targeta_color_bar_ocupacio);
            this.numero = view.findViewById(R.id.tv_targeta_fragment_base_num);

            chFav = view.findViewById(R.id.cb_bases_fav);
        }
    }

    public BasesAdapter(Context context, HashMap<String,ArrayList<Base>> data, FavoridaListener favoridaListener, FragmentListener fragmentListener, Preferencies preferencies) {
        mContext = context;
        mData = new ArrayList<>();
        if (data != null) {
            mData.addAll(data.get(LlistatBases.BASES_FAVORIDES));
            mData.addAll(data.get(LlistatBases.BASES_NO_FAVORIDES));
        }
        this.favoridaListener = favoridaListener;
        this.fragmentListener = fragmentListener;

        this.preferencies = preferencies;


        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.targeta_fragment_base_preferides, parent, false);
        return new SimpleViewHolder(view);
    }




    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {

        Base base = mData.get(position);

        String relacioOcupacio = String.format("%s/%s", base.getOcupacio(), base.getCapacitat());

        holder.chFav.setChecked(preferencies.getBasesPreferides().contains(mData.get(position).getNom()));

        holder.nom.setText(base.getNom());
        holder.ocupacio.setText(relacioOcupacio);

        holder.ocupacioProgressBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.normal_progres_bar));

        int minOcupacio = preferencies.getMinOcupacio();
        if(base.getOcupacio() <= minOcupacio) {
            holder.ocupacioProgressBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.danger_progres_bar));
        } else {
            holder.ocupacioProgressBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.normal_progres_bar));
        }
        holder.ocupacioProgressBar.setProgress((base.getOcupacio()*100)/base.getCapacitat());
        holder.numero.setText(String.valueOf(base.getId()));

        holder.nom.setText(mData.get(position).getNom());
        holder.ocupacio.setText(relacioOcupacio);

        // Listeners

        View.OnClickListener clickListener = view -> {
          switch (view.getId()) {
              case R.id.cb_bases_fav:
                  AppCompatCheckBox switchCompat = (AppCompatCheckBox) view;
                  if(switchCompat.isChecked()) {
                      favoridaListener.addFavorita(base.getNom());
                      favoridaListener.aplicarCanvis();
                  } else {
                      favoridaListener.removeFavorida(base.getNom());
                      favoridaListener.aplicarCanvis();
                  }
                  break;
              case R.id.tv_targeta_fragment_nom:
                  fragmentListener.goTo(base);

          }
        };

        holder.nom.setOnLongClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                vibrator.vibrate(250);
            }
            fragmentListener.addPoint(base);
            return true;
        });

        holder.nom.setOnClickListener(clickListener);
        holder.chFav.setOnClickListener(clickListener);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        layoutManager = recyclerView.getLayoutManager();
    }

    public boolean estaDal() {
        return layoutManager.isViewPartiallyVisible(layoutManager.getChildAt(0), true, false);
    }
}