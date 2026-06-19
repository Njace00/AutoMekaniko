package com.example.automekaniko

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.automekaniko.databinding.ObdSensorListItemBinding

data class SensorData(
    val id: String,
    val label: String,
    val unit: String,
    var value: String = "N / A",
    var progress: Int = 0
)

class SensorAdapter(private val sensors: List<SensorData>) :
    RecyclerView.Adapter<SensorAdapter.ViewHolder>() {

    class ViewHolder(val binding: ObdSensorListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ObdSensorListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sensor = sensors[position]
        holder.binding.cardLabel.text = sensor.label
        holder.binding.cardValue.text = sensor.value
        holder.binding.cardProgress.progress = sensor.progress
    }

    override fun getItemCount() = sensors.size

    fun updateValue(id: String, value: String, progress: Int) {
        val index = sensors.indexOfFirst { it.id == id }
        if (index != -1) {
            sensors[index].value = value
            sensors[index].progress = progress
            notifyItemChanged(index)
        }
    }
}
