package joan.demo_rutes_bicicas;


public interface FragmentListener {

    void onFragmentDetached();
    void addPoint();
    void addPoint(Base base);
    void removePoint();
    void fullViewFragment();
    void goTo(Base base);
    void zoomDespresAnimacio(int zoom);
    void changeBackIcon();


}
