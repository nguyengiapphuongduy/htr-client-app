package hcmut.thesis.htr2020.service;

import android.graphics.Bitmap;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import hcmut.thesis.htr2020.dto.ImageRequest;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiService {
    private static final String SERVER_URL = "http://52.175.224.195:5000";
    private static ApiService instance;
    private final HtrService htrService;

    private ApiService() {
        Gson gson = new GsonBuilder().setLenient().create();
        htrService = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(HtrService.class);
    }

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public String htrPredict(Bitmap bitmap) throws NoSuchFieldException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encoded = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);
        Call<Map<String, String>> call = htrService.predict(new ImageRequest(encoded));
        Response<Map<String, String>> response = call.execute();
        Map<String, String> body = response.body();
        if (body != null && "ok".equals(body.get("status"))) {
            int numLines;
            try {
                numLines = Integer.parseInt(Objects.requireNonNull(body.get("num_lines")));
            } catch (NumberFormatException | NullPointerException e) {
                throw new NoSuchFieldException();
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i <= numLines; i++) {
                String line = body.get(String.valueOf(i));
                if (line != null) {
                    builder.append(line).append("\n");
                }
            }
            return builder.toString();
        }
        return null;
    }
}
