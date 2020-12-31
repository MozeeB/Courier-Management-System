package id.cikup.couriermanagementsystem.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.cikup.couriermanagementsystem.data.model.MapsResponse
import id.cikup.couriermanagementsystem.data.service.MapsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardVM(private val mapsService: MapsService) : ViewModel() {
    private val _success = MutableLiveData<MapsResponse>()
    val success get() = _success

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage get() = _errorMessage

    fun getDirectionMaps(origin: String?, destination: String?, key: String?) {
        viewModelScope.launch(Dispatchers.Main) {
            val response = withContext(Dispatchers.IO) {
                mapsService.getDirectionLocation(origin, destination, key)
            }

            if (response.isSuccessful) {
                val result = response.body()

                if (result?.status == "OK") {
                    _success.postValue(result)
                } else {
                    _errorMessage.postValue(result?.errorMessage)
                }
            } else {
                _errorMessage.postValue(response.message())
            }
        }
    }
}