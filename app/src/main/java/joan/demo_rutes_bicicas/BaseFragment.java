package joan.demo_rutes_bicicas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.polyak.iconswitch.IconSwitch;

import java.util.ArrayList;

public class BaseFragment extends Fragment implements  IconSwitch.CheckedChangeListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String TAG = "FRAGMENT";
    private FragmentListener mListener;
    private FavoridaListener favoridaListener;

    private ArrayList<Base> myBases;
    private Base baseActual;

    private Preferencies preferencies;

    private TextView tvNumeroBase;
    private IconSwitch iconSwitch;
    private CheckBox checkBoxFavorita;
    private int numBase;

    public BaseFragment() {
        // Required empty public constructor
    }

    //TODO: Intancies (més ràpid?)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // Tornar a crear el fragment
        }

        // INFO MAIN ACTIVITY
        Bundle bundleInfo = this.getArguments();
        numBase = bundleInfo.getInt(MapsActivity.INDEX_BASE);
        myBases = (ArrayList<Base>) bundleInfo.getParcelableArrayList(MapsActivity.BASES).clone();
        baseActual = myBases.remove(numBase -2);

        preferencies = (Preferencies) bundleInfo.getParcelable(MapsActivity.PREFERENCIES);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_detall_base, container, false);

        view.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            public void onSwipeTop() {
                Log.i(TAG, "SWIPE UP");
                mListener.fullViewFragment();
            }
            public void onSwipeBottom() {
                Log.i(TAG, "SWIPE_DOWN");
                mListener.changeBackIcon();
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        // ELEMENTS
        checkBoxFavorita = getView().findViewById(R.id.ch_detall_fav);
        tvNumeroBase = getView().findViewById(R.id.tv_detall_num);
        iconSwitch = getView().findViewById(R.id.icon_switch);
        //ImageView ivEstacio = getView().findViewById(R.id.iv_fragment_estacio);
        TextView tvNomBase = getView().findViewById(R.id.tv_frament_nom);
        TextView tvOcupacio = getView().findViewById(R.id.tv_fragment_ocupacio);

        //iconSwitch.setChecked(IconSwitch.Checked.LEFT);

        iconSwitch.setCheckedChangeListener(this);
        checkBoxFavorita.setOnCheckedChangeListener((compoundButton, b) -> {
            switch (compoundButton.getId()) {
                case R.id.ch_detall_fav:
                    if(b) {
                        favoridaListener.addFavorita(baseActual.getNom());
                    } else {
                        favoridaListener.removeFavorida(baseActual.getNom());
                    }
                break;
            }
        });

        // RECYCLER VIEW
        RecyclerView rvBases = getActivity().findViewById(R.id.rv_fragment_bases);
        rvBases.setLayoutManager(new LinearLayoutManager(getContext()));
        BaseAdapter baseAdapter = new BaseAdapter();
        rvBases.setAdapter(baseAdapter);

        tvNumeroBase.setText("  " + String.valueOf(numBase) + "  ");
        //ivEstacio.setImageResource(R.drawable.base_bicias_shadow);
        tvOcupacio.setText(String.format("%d/%d", baseActual.getOcupacio(), baseActual.getCapacitat()));
        tvNomBase.setText(baseActual.getNom());
    }

    @Override
    public void onStart() {
        super.onStart();
        if(preferencies.isResetTime()){
            iconSwitch.setChecked(IconSwitch.Checked.LEFT);
        }
        checkBoxFavorita.setChecked(baseActual.isPreferida());

        if (getContext() instanceof FragmentListener) {
            mListener = (FragmentListener) getContext();
            favoridaListener= (FavoridaListener) getContext();
        } else {
            throw new RuntimeException(getContext().toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
            favoridaListener= (FavoridaListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        if(mListener != null) {
            mListener.onFragmentDetached();
        }
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onResume() {
        super.onResume();

        //TODO: actualitzar informaició
    }

    @Override
    public void onCheckChanged(IconSwitch.Checked current) {
        switch (current) {
            case LEFT:
                mListener.removePoint();
                mListener.changeBackIcon();
                break;
            case RIGHT:
                mListener.addPoint();

                mListener.zoomDespresAnimacio(Camara.ZOOM_ACTUAL);
                getActivity().onBackPressed();
        }

    }

    // RECYCLER VIEW
    class BaseViewHolder extends RecyclerView.ViewHolder {

        private Base base;

        TextView nom;
        TextView ocupacio;
        TextView numero;
        ProgressBar ocupacioProgressBar;


        public BaseViewHolder(View itemView) {
            super(itemView);
            this.nom = itemView.findViewById(R.id.tv_targeta_fragment_nom);
            this.ocupacio = itemView.findViewById(R.id.tv_targeta_fragment_ocupacio);
            this.ocupacioProgressBar =  itemView.findViewById(R.id.targeta_color_bar_ocupacio);
            this.numero = itemView.findViewById(R.id.tv_targeta_fragment_base_num);

            itemView.setOnClickListener(view -> mListener.goTo(base));
        }

        public void setBase(Base base) {
            this.base = base;
        }
    }

    class BaseAdapter extends RecyclerView.Adapter<BaseViewHolder> {

        @NonNull
        @Override
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View BaseView = getLayoutInflater().inflate(R.layout.targeta_fragment_base, parent, false);
            return new BaseViewHolder(BaseView);

        }


        @SuppressLint("ResourceAsColor")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
            Base base = myBases.get(position);

            String relacioOcupacio = String.format("%s/%s", base.getOcupacio(), base.getCapacitat());

            holder.nom.setText(base.getNom());
            holder.ocupacio.setText(relacioOcupacio);

            int minOcupacio = preferencies.getMinOcupacio();
            if(base.getOcupacio() <= minOcupacio) {
                holder.ocupacioProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.danger_progres_bar));
            } else {
                holder.ocupacioProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.normal_progres_bar));
            }
            holder.ocupacioProgressBar.setProgress((base.getOcupacio()*100)/base.getCapacitat());
            holder.numero.setText(String.valueOf(base.getId()));
            holder.setBase(base);
        }

        @Override
        public int getItemCount() {
            return myBases.size();
        }


    }


}
