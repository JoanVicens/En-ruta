package joan.demo_rutes_bicicas;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LlistatBases extends android.support.v4.app.Fragment {

    public static final String BASES_FAVORIDES = "BASES FAVORITES";
    public static final String BASES_NO_FAVORIDES = "BASES NO FAVORITES";

    private Preferencies preferencies;
    private ArrayList<Base> myBases;

    FragmentListener fragmentListener;
    FavoridaListener favoridaListener;

    public LlistatBases() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // INFO MAIN ACTIVITY
        Bundle bundleInfo = this.getArguments();
        myBases = (ArrayList<Base>) bundleInfo.getParcelableArrayList(MapsActivity.BASES).clone();
        preferencies = bundleInfo.getParcelable(MapsActivity.PREFERENCIES);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_llistat_bases, container, false);

        view.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            public void onSwipeTop() {
            }
            public void onSwipeBottom() {
                Log.w("SWIPE", "SWIPE DOWN");
                favoridaListener.aplicarCanvis();
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            favoridaListener = (FavoridaListener) context;
            fragmentListener = (FragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        RecyclerView rvEstacions = getView().findViewById(R.id.rv_totes_estacions);
        rvEstacions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEstacions.setHasFixedSize(false);
        //rvEstacions.addItemDecoration(new DividerItemDecoration(getContext(),LinearLayoutManager.VERTICAL));

        HashMap<String,ArrayList<Base>> basesOrdenades = organitzarBases(myBases);
        int positionNoFav = basesOrdenades.get(BASES_FAVORIDES).size();



        //Your RecyclerView.Adapter
        BasesAdapter mAdapter = new BasesAdapter(getContext(), basesOrdenades, favoridaListener, fragmentListener, preferencies);


        //This is the code to provide a sectioned list
        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<SimpleSectionedRecyclerViewAdapter.Section>();


        //Sections
        if(positionNoFav > 0) {
            sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0,"Bases Preferides"));
            sections.add(new SimpleSectionedRecyclerViewAdapter.Section(positionNoFav,"Bases"));
        } else {
            sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0,"Bases"));
        }

        //Add your adapter to the sectionAdapter
        SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        SimpleSectionedRecyclerViewAdapter mSectionedAdapter = new
                SimpleSectionedRecyclerViewAdapter(getContext(),R.layout.section_recicler_view,R.id.section_text,mAdapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        rvEstacions.setAdapter(mSectionedAdapter);

/*
        rvEstacions.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            public void onSwipeTop() {
                Log.i("SWIPE", "SWIPE UP");
            }
            public void onSwipeBottom() {
                Log.i("SWIPE", "SWIPE_DOWN");
                if(mSectionedAdapter.estaDal()) {
                    getActivity().onBackPressed();
                }
            }
        });*/
    }

    private HashMap<String,ArrayList<Base>> organitzarBases(ArrayList<Base> myBases) {
        HashMap<String,ArrayList<Base>> basesOrdenades = new HashMap<>();

        ArrayList<Base> favorides = new ArrayList<>();
        ArrayList<Base> noFav = new ArrayList<>();

        for(Base base: myBases) {
            if(base.isPreferida()) {
                favorides.add(base);
            } else {
                noFav.add(base);
            }
        }

        basesOrdenades.put(BASES_FAVORIDES, favorides);
        basesOrdenades.put(BASES_NO_FAVORIDES, noFav);

        return basesOrdenades;
    }



}
