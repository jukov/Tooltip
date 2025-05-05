package info.jukov.tooltip_sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import info.jukov.tooltip.Tooltip
import info.jukov.tooltip.TooltipBuilder

class TestCaseFragment : Fragment() {

    private lateinit var tooltipConfig: TooltipConfig

    private lateinit var targetView: Button

    private var tooltip: Tooltip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            tooltipConfig = bundle.requireParcelable(TooltipConfig::class.qualifiedName!!)
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
        targetView = view.findViewById(R.id.button_target)

        targetView.setOnClickListener {
            if (tooltip != null) {
                tooltip?.close()
                tooltip = null
            } else {
                showTooltip()
            }
        }

        showTooltip()
    }

    private fun showTooltip() {
        tooltip?.close()
        tooltip = TooltipBuilder(
            fragment = this,
            targetView = targetView,
            tooltipLayoutRes = when (tooltipConfig.viewType) {
                ViewType.SMALL_TEXT -> R.layout.tooltip_text_small
                ViewType.MEDIUM_TEXT -> R.layout.tooltip_text_medium
                ViewType.LARGE_TEXT -> R.layout.tooltip_text_large
                ViewType.ICON -> R.layout.tooltip_icon
            },
        )
            .setTheme(R.style.MyTooltipTheme)
            .setPosition(tooltipConfig.position)
            .apply {
                tooltipConfig.exactPosition?.let {
                    setExactPosition(it.x, it.y)
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tooltip?.close()
    }

    companion object {

        fun newInstance(config: TooltipConfig): TestCaseFragment =
            TestCaseFragment()
                .apply {
                    arguments = Bundle().apply {
                        putParcelable(TooltipConfig::class.qualifiedName, config)
                    }
                }
    }
}