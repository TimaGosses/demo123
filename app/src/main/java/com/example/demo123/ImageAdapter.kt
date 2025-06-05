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


class ImageAdapter(private val useUrls: Boolean = false) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    private val imageFiles: MutableList<File> = mutableListOf()
    private val imageUrls: MutableList<String> = mutableListOf()

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView1)
    }

    fun addFile(file: File) {
        imageFiles.add(file)
        Log.d("ImageAdapter", "Добавлен File в адаптер: $file, размер списка: ${imageFiles.size}")
        notifyItemInserted(imageFiles.size - 1)
    }

    fun addUrl(url: String) {
        imageUrls.add(url)
        Log.d("ImageAdapter", "Добавлен URL в адаптер: $url, размер списка: ${imageUrls.size}")
        notifyItemInserted(imageUrls.size - 1)
    }

    fun getFiles(): List<File> = imageFiles.toList()

    fun getUrls(): List<String> = imageUrls.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        Log.d("ImageAdapter", "Создан ViewHolder")
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Log.d("ImageAdapter", "onBindViewHolder: position=$position")
        if (useUrls) {
            val url = imageUrls[position]
            Log.d("ImageAdapter", "Загружаем URL: $url")
            Glide.with(holder.imageView.context)
                .load(url)
                .thumbnail(0.25f)
                .override(800, 800)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .timeout(30000)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("ImageAdapter", "Ошибка загрузки изображения по URL: ${e?.message}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("ImageAdapter", "Изображение загружено: $url")
                        return false
                    }
                })
                .into(holder.imageView)
        } else {
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
                        Log.e("ImageAdapter", "Ошибка загрузки изображения из файла: ${e?.message}")
                        return false
                    }

                    override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                    ): Boolean {
                        Log.d("ImageAdapter", "Изображение загружено: $file")
                        return false
                    }
                })
                .into(holder.imageView)
        }
    }

    override fun getItemCount(): Int {
        val size = if (useUrls) imageUrls.size else imageFiles.size
        Log.d("ImageAdapter", "Размер списка: $size, режим: ${if (useUrls) "URLs" else "Files"}")
        return size
    }
}