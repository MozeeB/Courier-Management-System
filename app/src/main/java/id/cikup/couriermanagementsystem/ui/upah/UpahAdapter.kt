package id.cikup.couriermanagementsystem.ui.upah

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.UpahModel
import kotlinx.android.synthetic.main.item_upah.view.*

class UpahAdapter (option: FirestoreRecyclerOptions<UpahModel>)
    : FirestoreRecyclerAdapter<UpahModel, UpahAdapter.TugasViewHolder>(option){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        return TugasViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_upah, parent, false))
    }

    override fun onBindViewHolder(
        holder: TugasViewHolder,
        position: Int,
        model: UpahModel
    ) {
        holder.priceAtoBUpah.text = model.aToBPrice.toString()
        holder.distanceAtoBUpah.text = model.aToBRange.toString()
        holder.priceBtoCUpahItemView.text = model.bToCPrice.toString()
        holder.distanceBtoCUpah.text = model.bToCRange.toString()
        holder.priceReimburse1Upah.text = model.reimburse1Price.toString()
        holder.tolReimburse1Upah.text = model.reimburse1Type
        holder.priceReimburse2Updah.text = model.reimburse2Price.toString()
        holder.bensinReimburse2UpahItemviewTV.text = model.reimburse2Type
    }

    class TugasViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var priceAtoBUpah = itemview.priceAtoBUpahItemViewTV
        var distanceAtoBUpah = itemview.distanceAtoBUpahItemViewTV
        var priceBtoCUpahItemView = itemview.priceBtoCUpahItemView
        var distanceBtoCUpah = itemview.distanceBtoCUpahItemViewTV
        var priceReimburse1Upah = itemview.priceReimburse1UpahItemViewTV
        var tolReimburse1Upah = itemview.tolReimburse1UpahItemViewTV
        var priceReimburse2Updah = itemview.priceReimburse2UpdahItemViewTV
        var bensinReimburse2UpahItemviewTV = itemview.bensinReimburse2UpahItemviewTV
    }

}