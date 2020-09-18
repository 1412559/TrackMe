package com.toantran.trackme.ui.homeactivity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.toantran.trackme.R
import com.toantran.trackme.db.entity.RecordedSessionEntity
import com.toantran.trackme.extension.secondsToTimeString
import java.io.File

class RecordedSessionViewHolder(parent : ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.recorded_session_item, parent, false)
) {

    private val txtTotalDistance = itemView.findViewById<TextView>(R.id.txtTotalDistance)
    private val txtAveSpeed = itemView.findViewById<TextView>(R.id.txtAveSpeed)
    private val txtTotalDuration = itemView.findViewById<TextView>(R.id.txtTotalDuration)
    private val imageMap = itemView.findViewById<ImageView>(R.id.imageMap)

    var recordedSessionEntity : RecordedSessionEntity? = null

    @SuppressLint("SetTextI18n")
    fun bindTo(recordedSessionEntity : RecordedSessionEntity?) {

        this.recordedSessionEntity = recordedSessionEntity

        txtTotalDistance.text = recordedSessionEntity?.totalDistance.toString() + " km\nDistance"
        txtAveSpeed.text = recordedSessionEntity?.averageSpeed.toString() + " km/h\nAvg. Speed"
        txtTotalDuration.text = recordedSessionEntity?.totalDuration?.secondsToTimeString() ?: "00:00:00"

        Glide
            .with(imageMap)
            .load(File(recordedSessionEntity!!.mapImagePath))
            .into(imageMap)
    }
}