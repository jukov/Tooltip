package com.github.jukov.tooltip.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.jukov.tooltip.Tooltip
import com.github.jukov.tooltip.makeTooltip

class TestCaseFragment : Fragment() {

    private lateinit var tooltipConfig: TooltipConfig

    private lateinit var targetView: View

    private var tooltip: Tooltip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            tooltipConfig =
                bundle.getParcelable<TooltipConfig>(TooltipConfig::class.qualifiedName)!!
        } ?: error("No args")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(
            when (tooltipConfig.targetViewPosition) {
                TargetViewPosition.CENTER -> R.layout.test_case_center
                TargetViewPosition.TOP_START -> R.layout.test_case_top_start
                TargetViewPosition.TOP_END -> R.layout.test_case_top_end
                TargetViewPosition.BOTTOM_START -> R.layout.test_case_bottom_start
                TargetViewPosition.BOTTOM_END -> R.layout.test_case_bottom_end
                TargetViewPosition.VERTICAL_SCROLL -> R.layout.test_case_vertical_scroll_view
                TargetViewPosition.VERTICAL_NESTED_SCROLL -> R.layout.test_case_vertical_nested_scroll_view
                TargetViewPosition.HORIZONTAL_SCROLL -> R.layout.test_case_horizontal_scroll_view
            },
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        targetView = view.findViewById<View>(R.id.button_target)

        tooltip = makeTooltip(
            fragment = this,
            targetView = targetView,
            tooltipLayoutRes = when (tooltipConfig.viewType) {
                ViewType.LARGE_TEXT -> R.layout.tooltip_text_long
                ViewType.SMALL -> R.layout.tooltip_icon
            },
        ) {
            position = tooltipConfig.position
            onHideListener = {
                context?.let { context ->
                    Toast.makeText(context, "Closed", Toast.LENGTH_LONG).show()
                }
            }
        }.apply { show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tooltip?.close()
    }

    companion object {

        fun newInstanse(config: TooltipConfig): TestCaseFragment =
            TestCaseFragment()
                .apply {
                    arguments = Bundle().apply {
                        putParcelable(TooltipConfig::class.qualifiedName, config)
                    }
                }
    }
}