package com.github.jukov.tooltip.sample

import com.github.jukov.tooltip.Tooltip
import com.github.jukov.tooltip.sample.databinding.TestCaseConfiguratorBinding

class TestCaseConfigureFragment : BindingFragment<TestCaseConfiguratorBinding>() {

    override fun onCreateDataBinding(): TestCaseConfiguratorBinding =
        TestCaseConfiguratorBinding.inflate(layoutInflater)

    fun makeConfig(): TooltipConfig =
        with(binding) {
            TooltipConfig(
                viewType = when (groupViewType.checkedRadioButtonId) {
                    radioViewTypeLargeText.id -> ViewType.LARGE_TEXT
                    radioViewTypeIcon.id -> ViewType.SMALL
                    else -> ViewType.SMALL
                },
                targetViewPosition = when (groupTargetViewPosition.checkedRadioButtonId) {
                    radioTargetViewPositionCenter.id -> TargetViewPosition.CENTER
                    radioTargetViewPositionTopStart.id -> TargetViewPosition.TOP_START
                    radioTargetViewPositionTopEnd.id -> TargetViewPosition.TOP_END
                    radioTargetViewPositionBottomStart.id -> TargetViewPosition.BOTTOM_START
                    radioTargetViewPositionBottomEnd.id -> TargetViewPosition.BOTTOM_END
                    else -> TargetViewPosition.CENTER
                },
                position = when (groupPosition.checkedRadioButtonId) {
                    radioPositionStart.id -> Tooltip.Position.START
                    radioPositionTop.id -> Tooltip.Position.TOP
                    radioPositionEnd.id -> Tooltip.Position.END
                    radioPositionBottom.id -> Tooltip.Position.BOTTOM
                    else -> Tooltip.Position.START
                },
            )
        }
}