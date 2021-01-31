package id.cikup.couriermanagementsystem.ui.tugas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.TugasModel
import kotlinx.android.synthetic.main.item_tugas.view.*

class AdapterTugas(private val tugasList: ArrayList<TugasModel>) : RecyclerView.Adapter<AdapterTugas.AdapterTugasViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterTugasViewHolder {
        return AdapterTugasViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_tugas, parent, false))
    }

    override fun getItemCount() = tugasList.size

    override fun onBindViewHolder(holder: AdapterTugasViewHolder, position: Int) {
        holder.bindItem(tugasList[position])
    }
    fun addTugasData(tugas: TugasModel){
        tugasList.add(tugas)
        notifyDataSetChanged()
    }

    class AdapterTugasViewHolder(val view : View) : RecyclerView.ViewHolder(view) {
        fun bindItem(tugas: TugasModel) {
            view.findViewById<TextView>(R.id.streetNameItemViewTV).text = tugas.title
            view.checklistTugasItemViewIV.setOnClickListener {

            }
            view.removelistTugasItemViewIV.setOnClickListener {

            }
        }
    }
}