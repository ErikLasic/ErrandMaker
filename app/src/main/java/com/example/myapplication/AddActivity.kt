package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.example.lib.Errand
import com.example.myapplication.databinding.ActivityAddBinding
import java.util.*

class AddActivity: AppCompatActivity() {
    private lateinit var app: MyApplication
    private lateinit var binding: ActivityAddBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as MyApplication
        setContentView(R.layout.activity_home)
        binding = ActivityAddBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent.extras?.getString("SELECTED_ID") != null) {
            val day = intent.extras?.getString("DAY")!!.toInt()
            val month = intent.extras?.getString("MONTH")!!.toInt()
            val year = intent.extras?.getString("YEAR")!!.toInt()
            binding.datePicker.updateDate(year,month,day)
            binding.TitleInput.setText(intent.extras?.getString("TITLE"))
            binding.DescriptionInput.setText(intent.extras?.getString("DESCRIPTION"))
            binding.LatitudeInput.setText(intent.extras?.getString("LATITUDE"))
            binding.LongitudeInput.setText(intent.extras?.getString("LONGITUDE"))
        }
        binding.AddButton.setOnClickListener {
            if (intent.hasExtra("SELECTED_ID")) {
                val id = intent.getStringExtra("SELECTED_ID")
                for (errand in app.opravki.errands) {
                    if (id == errand.id) {
                        errand.title = binding.TitleInput.text.toString()
                        errand.description = binding.DescriptionInput.text.toString()
                        errand.day = binding.datePicker.dayOfMonth
                        errand.month = binding.datePicker.month
                        errand.year = binding.datePicker.year
                        errand.latitude = binding.LatitudeInput.text.toString().trim().toDouble()
                        errand.longitude = binding.LongitudeInput.text.toString().trim().toDouble()
                        break
                    }
                }
            } else {
                if (binding.TitleInput.text.toString().isNotEmpty() && binding.DescriptionInput.text.toString().isNotEmpty()) {
                    app.opravki.add(Errand(binding.TitleInput.text.toString(), binding.DescriptionInput.text.toString(),binding.datePicker.dayOfMonth,binding.datePicker.month, binding.datePicker.year, binding.LatitudeInput.text.toString().toDouble(), binding.LongitudeInput.text.toString().toDouble()))
                } else {
                    Toast.makeText(this, "Title and description cannot be empty",Toast.LENGTH_SHORT).show();

                }
            }
            if (binding.TitleInput.text.toString().isNotEmpty() && binding.DescriptionInput.text.toString().isNotEmpty()) {
                app.saveToFile()
                binding.TitleInput.text?.clear()
                binding.DescriptionInput.text?.clear()
                binding.LatitudeInput.text?.clear()
                binding.LongitudeInput.text?.clear()
                finish()
            }
        }
        binding.HomeButton.setOnClickListener {
            finish()
        }
        binding.OpenMap.setOnClickListener {
            if (binding.LatitudeInput.text.toString().isNotEmpty() && binding.LongitudeInput.text.toString().isNotEmpty()) {
                val intent = Intent(this@AddActivity, MapActivity::class.java)
                intent.putExtra("LATITUDE", (binding.LatitudeInput.text).toString().trim().toDouble())
                intent.putExtra("LONGITUDE", (binding.LongitudeInput.text).toString().trim().toDouble())
                startActivity(intent)
            } else {
                Toast.makeText(this, "Enter coordinates to display on map", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
