package info.mqtt.java.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class HistoryAdapter() : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val history: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_row, parent, false)
        return ViewHolder(view)
    }

    fun add(data: String) {
//        history.clear()
        history.add(data)
        notifyItemInserted(history.size - 1)

        if(history.size > 1) {
            history.removeAt(0)
            notifyItemRemoved(0)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mTextView.text = history[position]
    }

    override fun getItemCount() = history.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mTextView: TextView = view.findViewById(R.id.row_text)
    }
}
