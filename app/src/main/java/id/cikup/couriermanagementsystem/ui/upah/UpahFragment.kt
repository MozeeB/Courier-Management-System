package id.cikup.couriermanagementsystem.ui.upah

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.TugasModel
import id.cikup.couriermanagementsystem.data.model.UpahModel
import id.cikup.couriermanagementsystem.ui.tugas.TugasAdaper
import kotlinx.android.synthetic.main.fragment_tugas.*
import kotlinx.android.synthetic.main.fragment_upah.*

class UpahFragment : Fragment() {

    private val firebaseDb = FirebaseFirestore.getInstance()

    private var upahAdapter:UpahAdapter? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_upah, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        setUpRecyclerView()
    }

    fun setUpRecyclerView(){
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val query = firebaseDb.collection("Upah")
            .document(currentUserID)
            .collection("upah")
        val firestoreRecyclerOptions : FirestoreRecyclerOptions<UpahModel> = FirestoreRecyclerOptions.Builder<UpahModel>()
            .setQuery(query, UpahModel::class.java)
            .build()

        upahAdapter = UpahAdapter(firestoreRecyclerOptions)
        upahFragmentRV.layoutManager = LinearLayoutManager(requireContext())
        upahFragmentRV.adapter = upahAdapter
    }

    override fun onStart() {
        super.onStart()
        upahAdapter?.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        upahAdapter?.stopListening()
    }

}