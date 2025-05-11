package com.example.demo123

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class CarAdapter(
    private var cars: List<CarLists>,
    private val onItemClick: (CarLists) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val carImage: ImageView = itemView.findViewById(R.id.imageCar)
        val carMark: TextView = itemView.findViewById(R.id.textMark)
        val carModel: TextView = itemView.findViewById(R.id.textModel)
        val carPrice: TextView = itemView.findViewById(R.id.textPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_list, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.carMark.text = car.Марка
        holder.carModel.text = car.Модель
        holder.carPrice.text = "${car.Цена_за_сутки}р / сутки"

        Log.d("CarAdapter","Для автомомбиля ${car.Марка}, ${car.Модель}, imageurl = ${car.imageUrls}")
        val imageUrl = car.imageUrls.firstOrNull()
        Log.d("CarAdapter","Для автомобиля ${car.Марка}, ${car.Модель} imageurl = ${imageUrl}, тип: ${imageUrl?.javaClass?.simpleName}")

        holder.carImage?.let {
           if (imageUrl != null && imageUrl is String) {
               Glide.with(holder.itemView.context)
                   .load(imageUrl)
                   .placeholder(R.drawable.placeholder_car)
                   .error(R.drawable.placeholder_car)
                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                   .listener(object : RequestListener<Drawable> {
                       override fun onLoadFailed(
                           e: GlideException?,
                           model: Any?,
                           target: Target<Drawable?>,
                           isFirstResource: Boolean
                       ): Boolean {
                           Log.d(
                               "CarAdapter",
                               "Ошибка загрузки изображений для ${car.Марка}, ${car.Модель}, ${e?.message}"
                           )
                           return false
                       }

                       override fun onResourceReady(
                           resource: Drawable,
                           model: Any,
                           target: Target<Drawable?>?,
                           dataSource: DataSource,
                           isFirstResource: Boolean
                       ): Boolean {
                           Log.d(
                               "CarAdapter",
                               "Изображение успешно загруженно для ${car.Марка}, ${car.Модель}: $model "
                           )
                           return false
                       }
                   })
                   .into(it)
           }else {
               //если imageUel не строка или null, показываем заглушку
               Log.e("CarAdapter","Некорректный imageUrl для ${car.Марка}, ${car.Модель}: ${imageUrl}")
               Glide.with(holder.itemView.context)
                   .load(R.drawable.placeholder_car)
                   .into(it)
           }
        }
        holder.itemView.setOnClickListener { onItemClick(car) }
    }

    override fun getItemCount(): Int = cars.size

    fun updateCars(newCars: List<CarLists>){
        cars = newCars
        notifyDataSetChanged()
    }

}