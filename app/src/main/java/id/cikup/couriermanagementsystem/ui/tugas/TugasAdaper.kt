
package id.cikup.couriermanagementsystem.ui.tugas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
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
        holder.streetName.text = model.street_name
    }

    class TugasViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var streetName = itemview.streetNameItemViewTV
    }

}
