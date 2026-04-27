package com.example.nhom7vexeapp.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.VehicleManaged;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleViewModel extends ViewModel {
    public MutableLiveData<List<VehicleManaged>> vehicleList = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public MutableLiveData<Boolean> isActionSuccess = new MutableLiveData<>();

    private ApiService apiService = ApiClient.getClient().create(ApiService.class);

    public void fetchVehicles(String nhaXeId) {
        isLoading.setValue(true);
        apiService.getVehicles().enqueue(new Callback<List<VehicleManaged>>() {
            @Override
            public void onResponse(Call<List<VehicleManaged>> call, Response<List<VehicleManaged>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    vehicleList.setValue(response.body());
                } else {
                    errorMessage.setValue("Lỗi tải dữ liệu!");
                }
            }
            @Override
            public void onFailure(Call<List<VehicleManaged>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối mạng!");
            }
        });
    }

    public void deleteVehicle(String id) {
        isLoading.setValue(true);
        apiService.deleteVehicle(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    isActionSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Lỗi khi xóa!");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối!");
            }
        });
    }
}
