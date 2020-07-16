package hcmut.thesis.htr2020.service;

import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HtrService {
    @POST("htr")
    @SerializedName("base64image")
    Call<String> recognize(@Body String base64image);
}
