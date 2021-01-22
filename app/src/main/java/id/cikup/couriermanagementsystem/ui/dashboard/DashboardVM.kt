package id.cikup.couriermanagementsystem.ui.dashboard

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.mongodb.MongoClient
//import com.mongodb.MongoCredential
import id.cikup.couriermanagementsystem.data.model.MapsResponse
import id.cikup.couriermanagementsystem.data.service.MapsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import org.bson.Document


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

//    init {
//        getDatMongo()
//    }

//    fun getDatMongo() {
//        // Create Mongo Client
//        val mongoClient = MongoClient("103.130.165.177", 27017)
//        val credential = MongoCredential.createCredential(
//            "deliveryClient",
//            "deliveryapp",
//            "StRdd1!\$3h".toCharArray()
//        )
//
//        val database = mongoClient.getDatabase("deliveryapp")
//
//        Log.d(
//            "Hasil",
//            "Hasil - ${mongoClient.serverAddressList} $credential -${database.listCollectionNames()}"
//        )
//
//        try {
//            val collectionUser = database.getCollection("users")
//            val document = collectionUser.find(Document("first_name", "Nick")).first()
//            val cursor = document.getString("zip")
//            Log.d("Hasil", "Hasil Address ${cursor}")
//        }catch (e: Exception) {
//            Log.d("Hasil", "Error Get Data ${e.message}")
//            mongoClient.close()
//            return
//        }
//
//    }
}