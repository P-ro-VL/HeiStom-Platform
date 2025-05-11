package vn.heistom.util;

import vn.heistom.util.LatLng;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeocodingUtil {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public static LatLng getLatLngFromAddress(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String urlString = NOMINATIM_URL + "?q=" + encodedAddress + "&format=json&limit=1";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "JavaGeocoder/1.0 (your_email@example.com)");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Basic pattern matching for "lat":"..." and "lon":"..."
            String responseBody = response.toString();

            Pattern latPattern = Pattern.compile("\"lat\"\\s*:\\s*\"([^\"]+)\"");
            Pattern lonPattern = Pattern.compile("\"lon\"\\s*:\\s*\"([^\"]+)\"");

            Matcher latMatcher = latPattern.matcher(responseBody);
            Matcher lonMatcher = lonPattern.matcher(responseBody);

            if (latMatcher.find() && lonMatcher.find()) {
                double lat = Double.parseDouble(latMatcher.group(1));
                double lon = Double.parseDouble(lonMatcher.group(1));
                return new LatLng(lat, lon);
            } else {
                System.out.println("Coordinates not found in response.");
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
