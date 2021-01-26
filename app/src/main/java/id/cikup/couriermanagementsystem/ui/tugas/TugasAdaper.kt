
package id.cikup.couriermanagementsystem.ui.tugas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.orhanobut.hawk.Hawk
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.TugasModel
import kotlinx.android.synthetic.main.item_tugas.view.*


class TugasAdaper(option: FirestoreRecyclerOptions<TugasModel>) :
    FirestoreRecyclerAdapter<TugasModel, TugasAdaper.TugasViewHolder>(option) {

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


        holder.streetName.text = model.location?.get(1)?.marker?.get(1)?.title

        holder.acc.setOnClickListener {
            val builder = AlertDialog.Builder(it.context)
            builder.setTitle("Antar Paket")
            builder.setMessage("Apakah anda yakin ingin mengantar paket ini?")
            builder.setPositiveButton("Ya") { dialog, which ->

                val role = Hawk.get("role", "")
                if (role == "client"){
                    Hawk.put("order_id", model.order_id)
                    it.findNavController().navigate(R.id.action_navigation_tugas_to_navigation_dashboard)
                }else if (role == "courier"){
                    Hawk.put("order_id", model.order_id)
                    it.findNavController().navigate(R.id.action_navigation_tugas_to_courierDashboardFragment)
                }
            }
            builder.setNegativeButton("Tidak") { dialog, which ->

            }

            val dialog: AlertDialog = builder.create()
            dialog.show()

        }
    }

    class TugasViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var streetName = itemview.streetNameItemViewTV
        var acc = itemview.checklistTugasItemViewIV
    }

}
