package com.example.fyp_patient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp_patient.R
import kotlinx.android.synthetic.main.activity_busdetails.*
import kotlinx.android.synthetic.main.bus_list_item.view.*

class MainActivity : AppCompatActivity() {

    private val photographerRepo = photographerRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busdetails)

        val data = photographerRepo.fetchPhotograpoherData()
        populateListView(data)
    }

    private fun populateListView(data: List<Photographer>){
        photographerList.apply {
            //setHasFixedSize(true)
            Log.v("test4","populateListView called")
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = PhotographerAdapter(data)
            Log.v("test4","populateListView called")
        }
    }
}


class PhotographerAdapter(private val data : List<Photographer>):
        RecyclerView.Adapter<PhotographerAdapter.ViewHolder>()
{

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){

        val timeText : TextView = view.timeText
        val temperatureText : TextView = view.temperatureText
        val descriptionText : TextView = view.descriptionText
    }
    //create new view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.v("test1","onCreateViewHolder called $viewType")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bus_list_item,parent,false)
        return ViewHolder(view)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val photographer = data[position]
        Log.v("test3", "onBindViewHolder called "+ photographer.id)
        holder.timeText.text = photographer.time.toString()//.formatForWeather()
        holder.descriptionText.text = photographer.description
        holder.temperatureText.text = photographer.temperature.toString()//.formatTemperature()
        }

    override fun getItemCount(): Int {
        Log.v("test2","getItemCount called is "+data.count().toString())
        return data.count()
    }

}