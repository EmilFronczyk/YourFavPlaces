package com.example.yourfavplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfavplaces.R
import com.example.yourfavplaces.activities.AddFavPlace
import com.example.yourfavplaces.activities.MainActivity
import com.example.yourfavplaces.database.DataBaseHandler
import com.example.yourfavplaces.models.YourFavPlaceModule
import kotlinx.android.synthetic.main.item_your_place.view.*

open class YourFavPlaceAdapter(
    private val context: Context,
    private var list: ArrayList<YourFavPlaceModule>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_your_place,
                parent,
                false
            )
        )
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.iv_place_image.setImageURI(Uri.parse(model.pathOfImage))
            holder.itemView.tvTitle.text = model.title
            holder.itemView.tvDescription.text = model.description

            holder.itemView.setOnClickListener{
                if (onClickListener != null) {
                    onClickListener!!.onClick(position,model)
                }
            }
        }
    }

    fun notificationOfEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddFavPlace::class.java)
        intent.putExtra(MainActivity.DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun deleteItem(position: Int) {
       val dbHandler = DataBaseHandler(context)
        val isDeleted = dbHandler.deleteYourPlace(list[position])
        if (isDeleted > 0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }

    }


    override fun getItemCount(): Int { //Ile lista ma elementow
        return list.size
    }

    interface OnClickListener {
        fun onClick(position: Int, model: YourFavPlaceModule)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}