package com.example.demo123

import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.demo123.R
import com.example.demo123.Car



class CarAdapter(private val cars: List<Car>): RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageCar: ImageView = itemView.findViewById(R.id.imageCar)
        val textCarName: TextView = itemView.findViewById(R.id.textCarName)
        val ratingLayout: LinearLayout = itemView.findViewById(R.id.ratingLayout)
        val textRating: TextView = itemView.findViewById(R.id.textRating)
        val textPrice: TextView = itemView.findViewById(R.id.textPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        //Установка данных
        holder.textCarName.text = "${car.Марка} ${car.Модель}, ${car.Год_выпуска}"
        holder.textPrice.text = " от ${car.Цена_за_сутки} Р / сутки"

        //загрузка изображений
        if (car.imageUrls.isNotEmpty()){
            Glide.with(holder.itemView.context)
                .load(car.imageUrls[0])
                .placeholder(R.drawable.placeholder_car)
                .into(holder.imageCar)
        }
    }
    override fun getItemCount(): Int = cars.size
}