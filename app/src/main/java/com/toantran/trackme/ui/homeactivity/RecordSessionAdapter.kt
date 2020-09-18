package com.toantran.trackme.ui.homeactivity

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.toantran.trackme.db.entity.RecordedSessionEntity

class RecordSessionAdapter: PagingDataAdapter<RecordedSessionEntity, RecordedSessionViewHolder>(diffCallback) {

    override fun onBindViewHolder(holder: RecordedSessionViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordedSessionViewHolder =
        RecordedSessionViewHolder(parent)

    companion object {

        private val diffCallback
                = object : DiffUtil.ItemCallback<RecordedSessionEntity>() {
            override fun areItemsTheSame(oldItem: RecordedSessionEntity, newItem: RecordedSessionEntity): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: RecordedSessionEntity, newItem: RecordedSessionEntity): Boolean =
                oldItem == newItem
        }

    }

}