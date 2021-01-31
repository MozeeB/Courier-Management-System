package id.cikup.couriermanagementsystem.ui.tugas

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.TugasModel
import id.cikup.couriermanagementsystem.data.model.UsersModel
import kotlinx.android.synthetic.main.fragment_tugas.*

class TugasFragment : Fragment() {


    private val firebaseDb = FirebaseFirestore.getInstance()


    var tugasAdaper:TugasAdaper? = null

    var listData:List<Map<String, Any>>? = null
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_tugas, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        getUser()

//        getRejects()

        setUpRecyclerView()

    }


    @SuppressLint("SetTextI18n")
    fun getUser(){
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        firebaseDb.collection("Users")
            .document(currentUserID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(UsersModel::class.java)
                nameTugasFragmentTV.text = "${user?.first_name} ${user?.last_name}"

            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed To Load", Toast.LENGTH_SHORT).show()
            }
    }

    fun setUpRecyclerView(){
            val query = firebaseDb.collection("Ordering")
//                    .whereNotIn("rejects", listData)
                    .whereNotIn("status_delivering", listOf("done"))
            val firestoreRecyclerOptions :FirestoreRecyclerOptions<TugasModel> = FirestoreRecyclerOptions.Builder<TugasModel>()
                    .setQuery(query, TugasModel::class.java)
                    .build()

            tugasAdaper = TugasAdaper(firestoreRecyclerOptions, requireContext())
            tugasFragmentRV.layoutManager = LinearLayoutManager(requireContext())
            tugasFragmentRV.adapter = tugasAdaper


    }

    fun getRejects(){
        firebaseDb.collection("Rejects")
                .document("EIBweaUsYwAfaEKwjZ3g")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()){
                        val users = document["user_id"] as List<Map<String, Any>>
                        setUpRecyclerView()
                    }else{
                        val query = firebaseDb.collection("Ordering")
                                .whereIn("status_delivering", listOf("active","pending"))
                        val firestoreRecyclerOptions :FirestoreRecyclerOptions<TugasModel> = FirestoreRecyclerOptions.Builder<TugasModel>()
                                .setQuery(query, TugasModel::class.java)
                                .build()

                        tugasAdaper = TugasAdaper(firestoreRecyclerOptions, requireContext())
                        tugasFragmentRV.layoutManager = LinearLayoutManager(requireContext())
                        tugasFragmentRV.adapter = tugasAdaper
                    }

                }


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