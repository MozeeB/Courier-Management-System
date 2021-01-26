package id.cikup.couriermanagementsystem.ui.riwayat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.RiwayatModel
import kotlinx.android.synthetic.main.fragment_riwayat.*


class RiwayatFragment : Fragment() {

    private val firebaseDb = FirebaseFirestore.getInstance()

    private var riwayatAdapter:RiwayatAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_riwayat, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpRecyclerView()
    }

    fun setUpRecyclerView(){
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val query = firebaseDb.collection("Salary")
                .document(currentUserID)
                .collection("salary")
        val firestoreRecyclerOptions : FirestoreRecyclerOptions<RiwayatModel> = FirestoreRecyclerOptions.Builder<RiwayatModel>()
                .setQuery(query, RiwayatModel::class.java)
                .build()

        riwayatAdapter = RiwayatAdapter(firestoreRecyclerOptions)
        riwayatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
        riwayatFragmentRV.adapter = riwayatAdapter
    }

    override fun onStart() {
        super.onStart()
        riwayatAdapter?.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        riwayatAdapter?.stopListening()
    }


}