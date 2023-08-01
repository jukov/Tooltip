package info.jukov.tooltip.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import info.jukov.tooltip.Tooltip
import info.jukov.tooltip_sample.R

class TestCaseConfigureFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.test_case_configurator, container, false)

    fun makeConfig(): TooltipConfig {
        val radioGroupViewType = requireView().findViewById<RadioGroup>(R.id.group_view_type)
        val radioGroupTargetViewPosition = requireView().findViewById<RadioGroup>(R.id.group_target_view_position)
        val radioGroupPosition = requireView().findViewById<RadioGroup>(R.id.group_position)
        return TooltipConfig(
            viewType = when (radioGroupViewType.checkedRadioButtonId) {
                R.id.radio_view_type_small_text -> ViewType.SMALL_TEXT
                R.id.radio_view_type_medium_text -> ViewType.MEDIUM_TEXT
                R.id.radio_view_type_large_text -> ViewType.LARGE_TEXT
                R.id.radio_view_type_icon -> ViewType.ICON
                else -> ViewType.SMALL_TEXT
            },
            targetViewPosition = when (radioGroupTargetViewPosition.checkedRadioButtonId) {
                R.id.radio_target_view_position_center -> TargetViewPosition.CENTER
                R.id.radio_target_view_position_top_start -> TargetViewPosition.TOP_START
                R.id.radio_target_view_position_top_end -> TargetViewPosition.TOP_END
                R.id.radio_target_view_position_bottom_start -> TargetViewPosition.BOTTOM_START
                R.id.radio_target_view_position_bottom_end -> TargetViewPosition.BOTTOM_END
                R.id.radio_target_view_position_vertical_scroll -> TargetViewPosition.VERTICAL_SCROLL
                R.id.radio_target_view_position_vertical_nested_scroll -> TargetViewPosition.VERTICAL_SCROLL
                R.id.radio_target_view_position_horizontal_scroll -> TargetViewPosition.HORIZONTAL_SCROLL
                else -> TargetViewPosition.CENTER
            },
            position = when (radioGroupPosition.checkedRadioButtonId) {
                R.id.radio_position_start -> Tooltip.Position.START
                R.id.radio_position_top -> Tooltip.Position.TOP
                R.id.radio_position_end -> Tooltip.Position.END
                R.id.radio_position_bottom -> Tooltip.Position.BOTTOM
                else -> Tooltip.Position.START
            },
        )
    }
}