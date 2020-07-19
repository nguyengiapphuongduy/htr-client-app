package hcmut.thesis.htr2020.service;

import java.util.Map;

import hcmut.thesis.htr2020.dto.ImageRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HtrService {
    @POST("predict")
    Call<Map<String, String>> predict(@Body ImageRequest imageRequest);
}
