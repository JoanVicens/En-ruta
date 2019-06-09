package joan.demo_rutes_bicicas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class BaseProvider implements Runnable {

    private URL url;
    private static JSONArray resposta;

    private final String URL = "http://gestiona.bicicas.es/apps/apps.php";

    public BaseProvider() {
        try {
            this.url = new URL(URL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url");
        }
    }

    public static JSONArray getResposta(){
        return resposta;
    }

    @Override
    public void run() {
        String response = "";
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=Windows-1252");

            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            } else {

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response += inputLine;
                }
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
                try {
                    JSONArray arr = new JSONArray(response);
                    JSONObject ob = arr.getJSONObject(0);
                    resposta = ob.getJSONArray("ocupacion");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


