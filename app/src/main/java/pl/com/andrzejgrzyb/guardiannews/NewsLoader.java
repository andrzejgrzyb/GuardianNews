package pl.com.andrzejgrzyb.guardiannews;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Andrzej on 20.07.2017.
 */

public class NewsLoader extends AsyncTaskLoader<List<News>> {

    public NewsLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public List<News> loadInBackground() {
        List<News> newsList = null;
        try {
            URL url = createUrl();
            String jsonResponse = getJsonResponse(url);
            newsList = parseJson(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsList;
    }

    private URL createUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .encodedAuthority("content.guardianapis.com")
                .appendPath("search")
                .appendQueryParameter("order-by", "newest")
                .appendQueryParameter("show-references", "author")
                .appendQueryParameter("show-tags", "contributor")
                .appendQueryParameter("q", "Android")
                .appendQueryParameter("api-key", "test");
        String url = builder.build().toString();

        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getJsonResponse(URL url) throws IOException {
        String json = "";

        if (url == null) {
            return json;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                StringBuilder output = new StringBuilder();
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line = reader.readLine();
                    while (line != null) {
                        output.append(line);
                        line = reader.readLine();
                    }
                }
                json = output.toString();
            } else {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return json;
    }

    private List<News> parseJson(String response) {
        ArrayList<News> newsArrayList = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONObject jsonResults = jsonResponse.getJSONObject("response");
            JSONArray resultsArray = jsonResults.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject oneResult = resultsArray.getJSONObject(i);
                String webTitle = oneResult.getString("webTitle");
                String url = oneResult.getString("webUrl");
                String date = oneResult.getString("webPublicationDate");
                //date = formatDate(date);
                String section = oneResult.getString("sectionName");
                JSONArray tagsArray = oneResult.getJSONArray("tags");
                String author = "";

                if (tagsArray.length() == 0) {
                    author = null;
                } else {
                    for (int j = 0; j < tagsArray.length(); j++) {
                        JSONObject firstObject = tagsArray.getJSONObject(j);
                        author += firstObject.getString("webTitle") + ". ";
                    }
                }
                newsArrayList.add(new News(webTitle, author, url, date, section));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newsArrayList;
    }

//    private static String formatDate(String rawDate) {
//        String jsonDateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
//        SimpleDateFormat jsonFormatter = new SimpleDateFormat(jsonDateFormat, Locale.US);
//        try {
//            Date parsedJsonDate = jsonFormatter.parse(rawDate);
//            String finalDatePattern = "MMM d, yyy";
//            SimpleDateFormat finalDateFormatter = new SimpleDateFormat(finalDatePattern, Locale.US);
//            return finalDateFormatter.format(parsedJsonDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return "";
//        }
//    }
}