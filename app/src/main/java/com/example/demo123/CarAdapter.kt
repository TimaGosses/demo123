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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.demo123.R
import com.example.demo123.CarData
import com.example.demo123.databinding.ItemCarListBinding

class CarAdapter(
    private val onItemClick: (CarListes) -> Unit
) : ListAdapter<CarListes, CarAdapter.CarViewHolder>(CarDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        Log.d("CarAdapter", "Создан ViewHolder")
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        Log.d("CarAdapter", "OnBindViewHolder: position = $position")
        val car = getItem(position)
        holder.bind(car)
        holder.itemView.setOnClickListener {
            onItemClick(car)
        }
    }

    class CarViewHolder(private val binding: ItemCarListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(car: CarListes) {
            binding.textMark.text = car.Марка
            binding.textModel.text = car.Модель
            binding.textPrice.text = "${car.Цена_за_сутки} ₽/сутки"

            val imageUrl = car.imageUrls.firstOrNull()
            Log.d("CarAdapter", "Для автомобиля ${car.Марка}, ${car.Модель}, imageUrl = $imageUrl")

            binding.imageCar.let { imageView ->
                if (!imageUrl.isNullOrBlank()) {
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_car)
                        .error(R.drawable.placeholder_car)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("CarAdapter", "Ошибка загрузки изображения: ${e?.message}")
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.d("CarAdapter", "Изображение загружено для ${car.Марка}, ${car.Модель}")
                                return false
                            }
                        })
                        .into(imageView)
                } else {
                    Log.w("CarAdapter", "Нет изображения для ${car.Марка}, ${car.Модель}")
                    Glide.with(itemView.context)
                        .load(R.drawable.placeholder_car)
                        .into(imageView)
                }
            }
        }
    }
}
class CarDiffCallback : DiffUtil.ItemCallback<CarListes>() {
    override fun areItemsTheSame(oldItem: CarListes, newItem: CarListes): Boolean {
        return oldItem.car_id == newItem.car_id
    }

    override fun areContentsTheSame(oldItem: CarListes, newItem: CarListes): Boolean {
        return oldItem == newItem
    }
}