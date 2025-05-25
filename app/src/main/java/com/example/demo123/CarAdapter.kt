package com.example.demo123

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.demo123.R
import com.example.demo123.CarData

class CarAdapter(
    private val onItemClick: (CarLists) -> Unit
) : ListAdapter<CarLists, CarAdapter.CarViewHolder>(CarDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_list, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val carImage: ImageView = itemView.findViewById(R.id.imageCar)
        private val carMark: TextView = itemView.findViewById(R.id.textMark)
        private val carModel: TextView = itemView.findViewById(R.id.textModel)
        private val carPrice: TextView = itemView.findViewById(R.id.textPrice)

        fun bind(car: CarLists) {
            carMark.text = car.Марка
            carModel.text = car.Модель
            carPrice.text = "${car.Цена_за_сутки} ₽/сутки"

            val imageUrl = car.imageUrls.firstOrNull()
            Log.d("CarAdapter", "Для автомобиля ${car.Марка}, ${car.Модель}, imageUrl = $imageUrl")

            carImage.let {
                if (imageUrl != null && imageUrl is String) {
                    Glide.with(itemView.context)
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
                                Log.e("CarAdapter", "Ошибка загрузки изображения: ${e?.message}")
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable?>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.d("CarAdapter", "Изображение загружено для ${car.Марка}, ${car.Модель}")
                                return false
                            }
                        })
                        .into(it)
                } else {
                    Log.e("CarAdapter", "Некорректный imageUrl для ${car.Марка}, ${car.Модель}: $imageUrl")
                    Glide.with(itemView.context)
                        .load(R.drawable.placeholder_car)
                        .into(it)
                }
            }
            itemView.setOnClickListener { onItemClick(car) }
        }
    }
}
class CarDiffCallback : DiffUtil.ItemCallback<CarLists>() {
    override fun areItemsTheSame(oldItem: CarLists, newItem: CarLists): Boolean {
        return oldItem.car_id == newItem.car_id
    }

    override fun areContentsTheSame(oldItem: CarLists, newItem: CarLists): Boolean {
        return oldItem == newItem
    }
}