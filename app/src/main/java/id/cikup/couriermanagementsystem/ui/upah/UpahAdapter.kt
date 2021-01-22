
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
import java.text.NumberFormat
import java.util.*

class UpahAdapter(option: FirestoreRecyclerOptions<UpahModel>) :
    FirestoreRecyclerAdapter<UpahModel, UpahAdapter.TugasViewHolder>(option) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        return TugasViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_upah, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: TugasViewHolder,
        position: Int,
        model: UpahModel
    ) {
        holder.priceAtoBUpah.text = rupiah(model.aToBPrice)
        holder.distanceAtoBUpah.text = model.aToBRange.toString()
        holder.priceBtoCUpahItemView.text = rupiah(model.bToCPrice)
        holder.distanceBtoCUpah.text = model.bToCRange.toString()
        holder.priceReimburse1Upah.text = rupiah(model.reimburse1Price)
        holder.tolReimburse1Upah.text = model.reimburse1Type
        holder.priceReimburse2Updah.text = rupiah(model.reimburse2Price)
        holder.bensinReimburse2UpahItemviewTV.text = model.reimburse2Type
        holder.dateUpdah.text = model.date
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
        var dateUpdah = itemview.dateUpdahItemView
    }

    fun rupiah(number: Number): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).toString()
    }
}
