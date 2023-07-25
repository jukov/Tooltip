package info.jukov.tooltip.sample

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import info.jukov.tooltip_sample.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val buttonShowcase = findViewById<Button>(R.id.button_showcase)
        val buttonTestcases = findViewById<Button>(R.id.button_testcases)

        buttonShowcase.setOnClickListener {
            Toast.makeText(this, "TODO", Toast.LENGTH_LONG).show()
        }

        buttonTestcases.setOnClickListener {
            TestCasesActivity.start(this)
        }
    }

}