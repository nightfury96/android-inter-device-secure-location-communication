package me.nightfury.locationapp


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.nightfury.locationapp.databinding.ItemLocationRecordBinding
import me.nightfury.sharedmodels.LocationRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LocationAdapter :
    ListAdapter<LocationRecord, LocationAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding =
            ItemLocationRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LocationViewHolder(private val binding: ItemLocationRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("HH:mm:ss dd/MMM", Locale.getDefault())

        fun bind(record: LocationRecord) {
            binding.tvCoordinates.text = String.format(
                Locale.getDefault(),
                "Lat: %.4f, Lon: %.4f",
                record.latitude,
                record.longitude
            )
            binding.tvTimestamp.text = dateFormat.format(Date(record.timestamp))
        }
    }
}

class LocationDiffCallback : DiffUtil.ItemCallback<LocationRecord>() {
    override fun areItemsTheSame(oldItem: LocationRecord, newItem: LocationRecord): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LocationRecord, newItem: LocationRecord): Boolean {
        return oldItem == newItem
    }
}