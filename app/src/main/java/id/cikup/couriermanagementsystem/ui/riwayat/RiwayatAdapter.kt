package id.cikup.couriermanagementsystem.ui.riwayat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.RiwayatModel
import kotlinx.android.synthetic.main.item_riwayat.view.*
import kotlinx.android.synthetic.main.item_upah.view.bensinReimburse2UpahItemviewTV
import kotlinx.android.synthetic.main.item_upah.view.distanceAtoBUpahItemViewTV
import kotlinx.android.synthetic.main.item_upah.view.distanceBtoCUpahItemViewTV
import kotlinx.android.synthetic.main.item_upah.view.tolReimburse1UpahItemViewTV

class RiwayatAdapter (option: FirestoreRecyclerOptions<RiwayatModel>)
    : FirestoreRecyclerAdapter<RiwayatModel, RiwayatAdapter.TugasViewHolder>(option){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        return TugasViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat, parent, false))
    }

    override fun onBindViewHolder(
            holder: TugasViewHolder,
            position: Int,
            model: RiwayatModel
    ) {
        holder.statusAtoBUpah.text = model.aToBStatus
        holder.distanceAtoBUpah.text = model.aToBRange.toString()
        holder.statusBtoCUpahItemView.text = model.bToCStatus
        holder.distanceBtoCUpah.text = model.bToCRange.toString()
        holder.statusReimburse1Upah.text = model.reimburse1Status
        holder.tolReimburse1Upah.text = model.reimburse1Type
        holder.statusReimburse2Updah.text = model.reimburse2Status
        holder.bensinReimburse2UpahItemviewTV.text = model.reimburse2Type
        holder.dateRiwayat.text = model.date
    }

    class TugasViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var statusAtoBUpah = itemview.statusAtoBUpahItemViewTV
        var distanceAtoBUpah = itemview.distanceAtoBUpahItemViewTV
        var statusBtoCUpahItemView = itemview.statusBtoCUpahItemView
        var distanceBtoCUpah = itemview.distanceBtoCUpahItemViewTV
        var statusReimburse1Upah = itemview.statusReimburse1UpahItemViewTV
        var tolReimburse1Upah = itemview.tolReimburse1UpahItemViewTV
        var statusReimburse2Updah = itemview.statusReimburse2UpdahItemViewTV
        var bensinReimburse2UpahItemviewTV = itemview.bensinReimburse2UpahItemviewTV
        var dateRiwayat = itemview.dateRiwayatItemView
    }

}