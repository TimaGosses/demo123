package com.example.demo123

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import android.graphics.drawable.Drawable
import java.io.File

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    private val imageFiles: MutableList<File> = mutableListOf()
    private var lastLoggedSize = -1 // для предотвращения повторного логирования

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView1)
    }

    fun addFile(file: File) {
        imageFiles.add(file)
        Log.d("ImageAdapter", "Добавлен File в адаптер: $file, размер списка: ${imageFiles.size}")
        notifyItemInserted(imageFiles.size - 1)
    }

    fun getFiles(): List<File> {
        return imageFiles
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        Log.d("ImageAdapter", "Создан ViewHolder")
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Log.d("ImageAdapter", "onBindViewHolder: position=$position")
        val file = imageFiles[position]
        Log.d("ImageAdapter", "Загружаем File: $file")
        Log.d("ImageAdapter", "Файл существует: ${file.exists()}, размер: ${file.length()} байт")
        Glide.with(holder.imageView.context)
            .load(file)
            .thumbnail(0.25f)
            .override(200, 200)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.ic_launcher_background)
            .centerCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("ImageAdapter", "Ошибка загрузки изображения: ${e?.message}")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("ImageAdapter", "Изображение загружено: $model")
                    return false
                }
            })
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        Log.d("ImageAdapter", "Размер списка: ${imageFiles.size}")
        return imageFiles.size
    }
}