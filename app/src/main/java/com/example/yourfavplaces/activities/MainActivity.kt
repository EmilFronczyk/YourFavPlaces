package com.example.yourfavplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfavplaces.R
import com.example.yourfavplaces.adapters.YourFavPlaceAdapter
import com.example.yourfavplaces.database.DataBaseHandler
import com.example.yourfavplaces.models.YourFavPlaceModule
import kotlinx.android.synthetic.main.activity_main.*
import com.example.yourfavplaces.utils.SwipeToDeleteCallBack
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddFavPlace.setOnClickListener {
            val intent = Intent(this, AddFavPlace::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUST_CODE)
        }
        getYourPlaceListFromDB()
    }

    private fun setupYourPlacesView(yourPlaceList: ArrayList<YourFavPlaceModule>) {
        rv_your_places_list.layoutManager = LinearLayoutManager(this)
        rv_your_places_list.setHasFixedSize(true)

        val placesAdapter = YourFavPlaceAdapter(this, yourPlaceList)
        rv_your_places_list.adapter = placesAdapter

        placesAdapter.setOnClickListener(object : YourFavPlaceAdapter.OnClickListener{
            override fun onClick(position: Int, model: YourFavPlaceModule) {
                val intent = Intent(this@MainActivity, YourPlaceDetails::class.java)
                intent.putExtra(DETAILS,model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_your_places_list.adapter as YourFavPlaceAdapter
                adapter.notificationOfEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUST_CODE)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView((rv_your_places_list))

        val deleteSwipeHandler = object : SwipeToDeleteCallBack(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_your_places_list.adapter as YourFavPlaceAdapter
                adapter.deleteItem(viewHolder.adapterPosition)

                getYourPlaceListFromDB()
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView((rv_your_places_list))
    }

    private fun getYourPlaceListFromDB() {
        val dbHandler = DataBaseHandler(this)
        val placesList : ArrayList<YourFavPlaceModule> = dbHandler.readPlaces()

        if(placesList.size>0) {
            rv_your_places_list.visibility= View.VISIBLE
            tv_no_records_available.visibility = View.GONE
            setupYourPlacesView(placesList)

        } else {
            rv_your_places_list.visibility= View.GONE
            tv_no_records_available.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_ACTIVITY_REQUST_CODE) {
            if (resultCode == RESULT_OK) {
                getYourPlaceListFromDB()
            } else {
                Log.e("Activity", "canceled or aborted")
            }
        }
    }

    companion object {
        var ADD_PLACE_ACTIVITY_REQUST_CODE = 1
        var DETAILS = "Details of a particular place"
    }
}