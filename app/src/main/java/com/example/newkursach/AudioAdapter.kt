package com.example.newkursach

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.newkursach.data.AudioRecord
import com.example.newkursach.databinding.RecyclerItemBinding
import com.example.newkursach.secondary.OnItemClickListener
import com.example.newkursach.secondary.Utils
import java.text.SimpleDateFormat
import java.util.Date

class AudioAdapter(private val location_undefined: String, var listener: OnItemClickListener) :
    RecyclerView.Adapter<AudioAdapter.CardViewHolder>() {
    private var editMode = false
    fun isEditMode(): Boolean {
        return editMode
    }
    var recordsList: List<AudioRecord> = emptyList()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setEditMode(mode: Boolean) {
        if (editMode != mode) {
            editMode = mode
            notifyDataSetChanged()
        }
    }

    inner class CardViewHolder(binding: RecyclerItemBinding) : ViewHolder(binding.root),
        View.OnClickListener, View.OnLongClickListener {
        val fileNameText: TextView = binding.filename
        val metaText: TextView = binding.meta
        val checkBoxText: CheckBox = binding.checkbox
        val shareButton: ImageView = binding.sharebut

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            shareButton.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemClickListener(position)
        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemLongClickListener(position)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding =
            RecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return recordsList.size
    }
    fun setRecords(recordList: List<AudioRecord>) {
        this.recordsList = recordList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val record = recordsList[position]
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            val date = Date(record.timestamp)
            val strDate = dateFormat.format(date)

            holder.fileNameText.text = record.filename

            val address = Utils.getAddressFromCoordinates(
                holder.itemView.context,
                record.latitude ?: 0.0,
                record.longitude ?: 0.0
            )

            holder.metaText.text = if (address.isNotBlank()) {
                "${record.duration} $strDate\n$address"
            } else {
                "${record.duration} $strDate\n${location_undefined}"
            }

            holder.shareButton.setOnClickListener {
                listener.onShareClickListener(position)
            }

            if (editMode) {
                holder.checkBoxText.visibility = View.VISIBLE
                holder.checkBoxText.isChecked = record.isChecked
            } else {
                holder.checkBoxText.visibility = View.GONE
                holder.checkBoxText.isChecked = false
            }
        }
    }

}