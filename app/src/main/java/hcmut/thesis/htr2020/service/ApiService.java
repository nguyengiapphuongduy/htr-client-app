package hcmut.thesis.htr2020.service;

import android.graphics.Bitmap;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiService {
    private static ApiService instance;
    private HtrService htrService;

    private ApiService() {
        Gson gson = new GsonBuilder().setLenient().create();
        htrService = new Retrofit.Builder()
                .baseUrl("http://192.168.1.7:8080/")
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

    public String htrService(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encoded = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);
        Call<String> call = htrService.recognize(encoded);
        Response<String> response = call.execute();
        return response.body();
    }
}
