package com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.lib.ErrandList
import com.squareup.picasso.Picasso

class ErrandAdapter(private val opravki:ErrandList): RecyclerView.Adapter<ErrandAdapter.ViewHolder>() {
    lateinit var onClickObject: MyOnClick

    interface MyOnClick {
        fun onClick(p0: View?, position:Int)
        fun onLongClick(p0: View?, position:Int)
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val title: TextView = itemView.findViewById(R.id.title)
        val date: TextView = itemView.findViewById(R.id.Date)
        val line: CardView = itemView.findViewById(R.id.cvLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_format, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val opravki = opravki.errands[position]

        var month = ""
        when(opravki.month) {
            0->month="January"
            1->month="February"
            2->month="March"
            3->month="April"
            4->month="May"
            5->month="June"
            6->month="July"
            7->month="August"
            8->month="September"
            9->month="October"
            10->month="November"
            11->month="December"
        }

        Picasso.get().load("https://static.thenounproject.com/png/163784-200.png").into(holder.imageView)

        holder.title.text = opravki.title
        holder.date.text = opravki.day.toString()+" "+month+" "+opravki.year.toString()

        holder.line.setOnClickListener { p0 ->
            onClickObject.onClick(p0, holder.bindingAdapterPosition) //Action from Activity
        }
        holder.line.setOnLongClickListener { p0 ->
            onClickObject.onLongClick(p0, holder.bindingAdapterPosition)
            true
        }

    }
    override fun getItemCount(): Int {
        return opravki.size()
    }
}