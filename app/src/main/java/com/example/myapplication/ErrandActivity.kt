package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lib.Errand
import com.example.myapplication.databinding.ActivityErrandBinding

class ErrandActivity: AppCompatActivity() {
    lateinit var app: MyApplication
    private lateinit var binding: ActivityErrandBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as MyApplication
        binding = ActivityErrandBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.errands.layoutManager = LinearLayoutManager(this)
        val adapter = ErrandAdapter(app.opravki)

        val editData = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            adapter.notifyDataSetChanged()
        }
        adapter.onClickObject = object:ErrandAdapter.MyOnClick {
            override fun onClick(p0: View?, position:Int) {
                val intent = Intent(this@ErrandActivity, AddActivity::class.java)
                intent.putExtra("SELECTED_ID", app.opravki.errands[position].id)
                intent.putExtra("TITLE", app.opravki.errands[position].title)
                intent.putExtra("DESCRIPTION", app.opravki.errands[position].description)
                intent.putExtra("DAY", app.opravki.errands[position].day.toString())
                intent.putExtra("MONTH", app.opravki.errands[position].month.toString())
                intent.putExtra("YEAR", app.opravki.errands[position].year.toString())
                intent.putExtra("LATITUDE", app.opravki.errands[position].latitude.toString())
                intent.putExtra("LONGITUDE", app.opravki.errands[position].longitude.toString())
                setResult(RESULT_OK, intent)
                editData.launch(intent)
            }

            override fun onLongClick(p0: View?, position: Int) {
                val builder = AlertDialog.Builder(this@ErrandActivity) //access context from inner class
                //set title for alert dialog
                builder.setTitle("Delete")
                builder.setMessage("Errand: ${app.opravki.errands[position].title}")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setPositiveButton("Yes"){dialogInterface, which -> //performing positive action
                    Toast.makeText(applicationContext,"Deleted", Toast.LENGTH_LONG).show()
                    app.opravki.errands.removeAt(position)
                    adapter.notifyDataSetChanged()
                    app.saveToFile()
                }
                builder.setNeutralButton("Cancel"){dialogInterface , which -> //performing cancel action
                    Toast.makeText(applicationContext,"clicked cancel\n operation cancel", Toast.LENGTH_LONG).show()
                }
                builder.setNegativeButton("No"){dialogInterface, which -> //performing negative action
                    Toast.makeText(applicationContext,"Cancelled", Toast.LENGTH_LONG).show()
                }
                // Create the AlertDialog
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()

            }
        }
        binding.errands.adapter = adapter
        //adapter.notifyDataSetChanged();
        binding.HomeButton.setOnClickListener {
            finish()
        }
    }
}