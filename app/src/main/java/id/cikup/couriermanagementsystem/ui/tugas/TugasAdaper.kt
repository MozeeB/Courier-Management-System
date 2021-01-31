
package id.cikup.couriermanagementsystem.ui.tugas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.orhanobut.hawk.Hawk
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.TugasModel
import id.cikup.couriermanagementsystem.data.model.UsersModel
import kotlinx.android.synthetic.main.item_tugas.view.*


class TugasAdaper(option: FirestoreRecyclerOptions<TugasModel>, var context: Context) :
    FirestoreRecyclerAdapter<TugasModel, TugasAdaper.TugasViewHolder>(option) {

    private val firebaseDb = FirebaseFirestore.getInstance()
    var status = "active"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        return TugasViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_tugas, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: TugasViewHolder,
        position: Int,
        model: TugasModel
    ) {


        holder.streetName.text = model.title

        status = model.status_delivering
        if (status == "active"){
            holder.acc.setImageDrawable(ContextCompat.getDrawable(context , R.drawable.check_1))
            holder.remove.setImageDrawable(ContextCompat.getDrawable(context , R.drawable.remove_1))
        }else if (status == "pending"){
            holder.acc.setImageDrawable(ContextCompat.getDrawable(context , R.drawable.ic_baseline_pending_actions_30))
            holder.remove.setImageDrawable(ContextCompat.getDrawable(context , R.drawable.ic_baseline_location_on_24))
        }

        if (status == "active"){
            holder.acc.setOnClickListener {
                val builder = AlertDialog.Builder(it.context)
                builder.setTitle("Antar Paket")
                builder.setMessage("Apakah anda yakin ingin mengantar paket ini?")
                builder.setPositiveButton("Ya") { dialog, which ->
                    firebaseDb.collection("Ordering")
                        .document(model.order_id)
                        .update("status_delivering", "pending")
                        .addOnSuccessListener {
                            Hawk.put("order_id", model.order_id)
                            dialog.dismiss()
                        }

                }
                builder.setNegativeButton("Tidak") { dialog, which ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()

            }

            holder.remove.setOnClickListener {
                val builder = AlertDialog.Builder(it.context)
                builder.setTitle("Hapus tugas")
                builder.setMessage("Apakah anda yakin ingin menghapus tugas ini?")
                builder.setPositiveButton("Ya") { dialog, which ->
                    firebaseDb.collection("Ordering")
                            .document(model.order_id)
                            .delete()
                            .addOnSuccessListener {
                                dialog.dismiss()
                            }

                }
                builder.setNegativeButton("Tidak") { dialog, which ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }else if (status == "pending"){
            holder.acc.setOnClickListener {itView ->
                val builder = AlertDialog.Builder(itView.context)
                builder.setTitle("Selesai")
                builder.setMessage("Apakah anda yakin sudah mengantar paket ini?")
                builder.setPositiveButton("Ya") { dialog, which ->
                    firebaseDb.collection("Users")
                            .document(model.client_id)
                            .get()
                            .addOnSuccessListener {
                                val user = it.toObject(UsersModel::class.java)
                                if (user?.order_id?.isNullOrEmpty() == true){
                                    firebaseDb.collection("Ordering")
                                            .document(model.order_id)
                                            .update("status_delivering", "done")
                                            .addOnSuccessListener {
                                                firebaseDb.collection("Users")
                                                        .document(model.client_id)
                                                        .update("order_id", model.order_id)
                                                        .addOnSuccessListener {
                                                            dialog.dismiss()
                                                        }
                                            }
                                }else{
                                    Toast.makeText(itView.context, "Maaf client belum mengkonfirmasi", Toast.LENGTH_LONG).show()
                                }
                            }


                }
                builder.setNegativeButton("Tidak") { dialog, which ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()

            }


            holder.remove.setOnClickListener { itView ->
                val builder = AlertDialog.Builder(itView.context)
                builder.setMessage("Lihat Lokasi")
                builder.setPositiveButton("Ya") { dialog, which ->
                    val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
                    firebaseDb.collection("Users")
                            .document(currentUserID)
                            .update("order_id", model.order_id)
                            .addOnSuccessListener {
                                itView.findNavController().navigate(R.id.action_navigation_tugas_to_courierDashboardFragment)
                            }
                }
                builder.setNegativeButton("Tidak") { dialog, which ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }


        holder.itemView.setOnClickListener {
            val bundle = bundleOf("order_id" to model.order_id)
            it.findNavController().navigate(R.id.action_navigation_tugas_to_chatActivity, bundle)
        }
    }

    class TugasViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var streetName = itemview.streetNameItemViewTV
        var acc = itemview.checklistTugasItemViewIV
        var remove = itemview.removelistTugasItemViewIV
    }

}
