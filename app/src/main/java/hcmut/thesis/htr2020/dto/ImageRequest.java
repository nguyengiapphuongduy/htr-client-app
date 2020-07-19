package hcmut.thesis.htr2020.dto;

import com.google.gson.annotations.SerializedName;

public class ImageRequest {
    @SerializedName("img")
    final String base64image;

    public ImageRequest(String base64image) {
        this.base64image = base64image;
    }
}
