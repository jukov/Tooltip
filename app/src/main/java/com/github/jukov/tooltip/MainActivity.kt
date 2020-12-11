package com.github.jukov.tooltip

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addTooltips()
    }

    private fun addTooltips() {
        tooltipOf(
            this,
            R.id.next,
            R.layout.tooltip_icon
        ) {
            position = TooltipBuilder.Position.START
            clickToHide = true
        }

        tooltipOf(
            this,
            R.id.next,
            R.layout.tooltip_icon
        ) {
            position = TooltipBuilder.Position.END
            clickToHide = true
        }

        tooltipOf(
            this,
            R.id.next,
            R.layout.tooltip_text
        ) {
            position = TooltipBuilder.Position.TOP
            clickToHide = true
        }

        tooltipOf(
            this,
            R.id.next,
            R.layout.tooltip_text
        ) {
            position = TooltipBuilder.Position.BOTTOM
            clickToHide = true
        }
    }
}