package com.example.nhom7vexeapp.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.nhom7vexeapp.models.KhachHang;
import com.example.nhom7vexeapp.repositories.CustomerRepository;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerViewModel extends ViewModel {
    private CustomerRepository repository;
    public MutableLiveData<KhachHang> customerData = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<Boolean> isUpdateSuccess = new MutableLiveData<>();

    public CustomerViewModel() {
        repository = new CustomerRepository();
    }

    public void login(Map<String, String> body) {
        isLoading.setValue(true);
        repository.login(body).enqueue(new Callback<KhachHang>() {
            @Override
            public void onResponse(Call<KhachHang> call, Response<KhachHang> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    customerData.setValue(response.body());
                } else {
                    errorMessage.setValue("Đăng nhập thất bại. Vui lòng kiểm tra lại!");
                }
            }

            @Override
            public void onFailure(Call<KhachHang> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getProfile(String id) {
        isLoading.setValue(true);
        repository.getProfile(id).enqueue(new Callback<KhachHang>() {
            @Override
            public void onResponse(Call<KhachHang> call, Response<KhachHang> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    customerData.setValue(response.body());
                } else {
                    errorMessage.setValue("Không thể tải thông tin cá nhân");
                }
            }

            @Override
            public void onFailure(Call<KhachHang> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void updateProfile(String id, KhachHang updatedInfo) {
        isLoading.setValue(true);
        repository.updateProfile(id, updatedInfo).enqueue(new Callback<KhachHang>() {
            @Override
            public void onResponse(Call<KhachHang> call, Response<KhachHang> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    customerData.setValue(response.body());
                    isUpdateSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Cập nhật thất bại");
                }
            }

            @Override
            public void onFailure(Call<KhachHang> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }
}
