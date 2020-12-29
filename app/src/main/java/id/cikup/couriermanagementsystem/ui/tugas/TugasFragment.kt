package id.cikup.couriermanagementsystem.ui.tugas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.TugasModel
import kotlinx.android.synthetic.main.fragment_tugas.*

class TugasFragment : Fragment() {


    private val firebaseDb = FirebaseFirestore.getInstance()


    var tugasAdaper:TugasAdaper? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_tugas, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpRecyclerView()

    }

    fun setUpRecyclerView(){
        val query = firebaseDb.collection("Tugas")
            .document("LTVWUsRbvdOoUujHvmCokeH6uKu2")
            .collection("tugas")

        val firestoreRecyclerOptions :FirestoreRecyclerOptions<TugasModel> = FirestoreRecyclerOptions.Builder<TugasModel>()
            .setQuery(query, TugasModel::class.java)
            .build()

        tugasAdaper = TugasAdaper(firestoreRecyclerOptions)
        tugasFragmentRV.layoutManager = LinearLayoutManager(requireContext())
        tugasFragmentRV.adapter = tugasAdaper
    }

    override fun onStart() {
        super.onStart()
        tugasAdaper?.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        tugasAdaper?.stopListening()
    }


}