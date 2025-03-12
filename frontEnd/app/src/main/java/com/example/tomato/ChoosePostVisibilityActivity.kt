package com.example.tomato

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Activity for choosing the visibility of a post to be uploaded.
 */
class ChoosePostVisibilityActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_post_visibility)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val radioGroup = findViewById<RadioGroup>(R.id.postVisibilityRadioGroup)
        val proceedButton = findViewById<Button>(R.id.submitPostVisibility)

        proceedButton.setOnClickListener{
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            if(selectedRadioButtonId != -1){
                val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
                val resultIntent = intent
                resultIntent.putExtra("visibility", selectedRadioButton.text.toString())
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        val backButton = findViewById<ImageView>(R.id.choose_post_visibility_back)
        backButton.setOnClickListener{
            finish()
        }


    }
}