package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.Branch;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface BranchApi {
    @GET("branches")
    Call<List<Branch>> getBranches();
}
