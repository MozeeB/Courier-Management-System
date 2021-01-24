
package id.cikup.couriermanagementsystem.ui.tugas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.orhanobut.hawk.Hawk
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.Message
import id.cikup.couriermanagementsystem.data.model.TugasModel
import kotlinx.android.synthetic.main.fragment_dashboard.*
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
        holder.streetName.text = model.location.marker.title
        holder.acc.setOnClickListener {
            val role = Hawk.get("role", "")
            if (role == "client"){
                it.findNavController().navigate(R.id.action_navigation_tugas_to_navigation_dashboard)
                Hawk.put("order_id", model.order_id)
            }else if (role == "courier"){
                it.findNavController().navigate(R.id.action_navigation_tugas_to_courierDashboardFragment)
                Hawk.put("order_id", model.order_id)
            }
        }
    }

    class TugasViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var streetName = itemview.streetNameItemViewTV
        var acc = itemview.checklistTugasItemViewIV
    }

}
