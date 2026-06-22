package com.anzla.ai_poweredflood_saferouteplanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.anzla.ai_poweredflood_saferouteplanner.R
import com.anzla.ai_poweredflood_saferouteplanner.model.FloodAlert
import com.anzla.ai_poweredflood_saferouteplanner.model.RiskLevel

class AlertAdapter(private val alerts: List<FloodAlert>) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.alertTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.alertDescription)
        val tvTimestamp: TextView = itemView.findViewById(R.id.alertTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flood_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]

        holder.tvTitle.text = alert.title
        holder.tvDescription.text = alert.description
        holder.tvTimestamp.text = alert.timestamp

        // riskLevel ke mutabiq titles ke colors change karne ka code
        when (alert.riskLevel) {
            RiskLevel.LOW -> {
                holder.tvTitle.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.risk_low)
                )
            }
            RiskLevel.MEDIUM -> {
                holder.tvTitle.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.risk_medium)
                )
            }
            RiskLevel.HIGH -> {
                holder.tvTitle.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.risk_high)
                )
            }
            RiskLevel.CRITICAL -> {
                holder.tvTitle.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.risk_critical)
                )
            }
        }
    }

    override fun getItemCount(): Int = alerts.size
}