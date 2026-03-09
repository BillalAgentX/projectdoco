package com.projectdocupro.mobile.interfaces;

import com.google.gson.JsonObject;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetroApiInterface {

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("login")
    Call<JsonObject> loginAPI(@Body JsonObject params);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("project/getlist")
    Call<JsonObject> getListAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid);

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @Multipart
    @GET("project/getlist/{projectid}")
    Call<JsonObject> uploadImage(@Path("projectid") String apitoken, @Part @Body JsonObject params, @Part @Body JsonObject image);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("project/getplans/{projectid}")
    Call<JsonObject> getPlanAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("project/getdetail/{projectid}")
    Call<JsonObject> getPlanDetailsAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("project/getflaws/{projectid}")
    Call<JsonObject> getDefectsAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("project/create")
    Call<JsonObject> createProject(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Body JsonObject params);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("addfile/checkphotoqualities/{projectId}")
    Call<JsonObject> checkPhotoQualities(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectId") String projectId, @Body JsonObject params);

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("project/createtag/{projectId}")
    Call<JsonObject> createWord(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectId") String projectId, @Body JsonObject params);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("appcontroll/errorreport")
    Call<JsonObject> errorreport(@Body JsonObject params);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("project/getcustomers")
    Call<JsonObject> getCustomersAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("photo/update/{pdphotoid}")
    Call<JsonObject> updatePhotoAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("pdphotoid") String pdPhotoId, @Body JsonObject params);


    @Headers({"Accept:application/json", "Content-Type:multipart/form-data;"})
    @Multipart
    @POST("addfile/upload/{projectid}")
//    Call<ResponseBody> addPhotoAPI(@Header("apitoken") String apitoken, @Header("deviceid")    String  deviceid, @Path("projectid")    String  projectid, @Part   MultipartBody.Part body);
    Call<JsonObject> synPhotoAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid, @Part MultipartBody.Part file, @PartMap Map<String, RequestBody> map);
//    Call<ResponseBody> addPhotoAPI(@Header("apitoken") String apitoken, @Header("deviceid")    String  deviceid, @Path("projectid")    String  projectid, @Part(value = "quality")    RequestBody quality,    @Part(value = "hash")   RequestBody hash, @Part(value = "photodate")    RequestBody photodate, @Part(value = "filetype")    RequestBody filetype, @Part(value = "file") RequestBody file);

    //    @Headers({"Accept:application/json", "Content-Type:multipart/form-data;"})
    @Multipart
    @POST("addfile/upload/{projectid}")
//    Call<ResponseBody> addPhotoAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid, @Part MultipartBody.Part body,Map<String, RequestBody> map);

    Call<JsonObject> addPhotoAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid, @Part MultipartBody.Part file
            , @Part("quality") RequestBody map, @Part("filetype") RequestBody filetype, @Part("hash") RequestBody hash, @Part("photodate") RequestBody photodate);
    //


    @Multipart
    @POST("addfile/memo/{projectid}")
//    Call<ResponseBody> addPhotoAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid, @Part MultipartBody.Part body,Map<String, RequestBody> map);

    Call<JsonObject> addMemoAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid, @Part MultipartBody.Part file
            , @Part("hash") RequestBody hash, @Part("startdate") RequestBody photodate, @Part("photoid") RequestBody photoId);

    @Multipart
    @POST("addfile/upload/{projectid}")
    Call<JsonObject> addPhotoAPIWithOptionalParams(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid, @Part MultipartBody.Part file
            , @Part("quality") RequestBody map
            , @Part("filetype") RequestBody filetype
            , @Part("hash") RequestBody hash
            , @Part("photodate") RequestBody photodate
            , @Part("exifwidth") RequestBody exifwidth
            , @Part("exifheight") RequestBody exifheight
            , @Part("exifgpsx") RequestBody exifgpsx
            , @Part("exifgpsy") RequestBody exifgpsy
            , @Part("exifhasgps") RequestBody exifhasgps
            , @Part("exifgpsdirection") RequestBody exifgpsdirection
            , @Part("exiforientation") RequestBody exiforientation
            , @Part("useorientation") RequestBody useorientation
            , @Part("exifhasgpsdirection") RequestBody exifhasgpsdirection
            , @Part("exifdump") RequestBody exifdump
            , @Part("gpsaccuracy") RequestBody gpsaccuracy);

    @Multipart
    @POST("addfile/upload/{projectid}")
    Call<JsonObject> addPhotoAPIWithOptionalParamsUsingMap(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid,
                                                           @Part MultipartBody.Part file, @PartMap Map<String, RequestBody> params);

//    @Multipart
//    @POST("addfile/upload/{projectid}")
//    Call<JsonObject> addPhotoAPIWithOptionalParamsWithoutGps(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid, @Part MultipartBody.Part file
//            , @Part("quality") RequestBody map
//            , @Part("filetype") RequestBody filetype
//            , @Part("hash") RequestBody hash
//            , @Part("photodate") RequestBody photodate
//            , @Part("exifwidth") RequestBody exifwidth
//            , @Part("exifheight") RequestBody exifheight
//            , @Part("exifgpsdirection") RequestBody exifgpsdirection
//            , @Part("exiforientation") RequestBody exiforientation
//            , @Part("useorientation") RequestBody useorientation
//            , @Part("exifhasgpsdirection") RequestBody exifhasgpsdirection
//            , @Part("exifdump") RequestBody exifdump);

    //
    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("project/getphotogallerie/{projectid}")
    Call<JsonObject> getPhotosAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectId, @Body JsonObject params);

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("getfile/planimage/{fileid}")
    Call<ResponseBody> getPlanImage(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("fileid") String fileId);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("getfile/planimage/{fileid}/{size}")
    Call<ResponseBody> getPlanImageWithSize(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("fileid") String fileId, @Query("size") String size);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("getfile/decoimage/{fileid}")
    Call<ResponseBody> getDecoImage(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("fileid") String fileId);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("/getfile/photoimage/{fileid}")
    Call<ResponseBody> getDefectPhotos(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("fileid") String fileId);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("/getfile/photoimage/{fileid}")
    Call<ResponseBody> getDefectPhotosWithSize(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("fileid") String fileId, @Query("size") String size);

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("project/createreport/{projectid}")
    Call<JsonObject> getCreatereportAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectId, @Body JsonObject params);

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @GET("/getfile/report/{projectid}/{reportid}")
    Call<ResponseBody> getCreatedReportFile(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid, @Path("reportid") String reportId);


    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("/photo/update/{pdphotoid}")
    Call<JsonObject> getUpdatePhotoAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("pdphotoid") String pdphotoid, @Body JsonObject params);

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("/flaw/edit")
    Call<JsonObject> getUpdateFlawsAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Body JsonObject params);

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("/project/setflag/{projectid}")
    Call<JsonObject> getUpdatePlansParamsAPI(@Header("apitoken") String apitoken, @Header("deviceid") String deviceid, @Path("projectid") String projectid, @Body JsonObject params);
}