package top.fifthlight.combine.theme.blackstone

import top.fifthlight.combine.theme.Theme
import top.fifthlight.combine.ui.style.ColorTheme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.ui.style.TextureSet

val BlackstoneTheme = run {
    val textures = BlackstoneTexturesFactory.of()
    Theme(
        drawables = Theme.Drawables(
            button = DrawableSet(
                normal = textures.widget_button_button,
                hover = textures.widget_button_button_hover,
                active = textures.widget_button_button_active,
                disabled = textures.widget_button_button_disabled,
            ),
            guideButton = DrawableSet(
                normal = textures.widget_button_button_guide,
                hover = textures.widget_button_button_guide_hover,
                active = textures.widget_button_button_active,
                disabled = textures.widget_button_button_disabled,
            ),
            warningButton = DrawableSet(
                normal = textures.widget_button_button_warning,
                hover = textures.widget_button_button_warning_hover,
                active = textures.widget_button_button_active,
                disabled = textures.widget_button_button_disabled,
            ),

            uncheckedCheckBox = DrawableSet(
                normal = textures.widget_checkbox_checkbox,
                hover = textures.widget_checkbox_checkbox_hover,
                active = textures.widget_checkbox_checkbox_active,
                disabled = textures.widget_checkbox_checkbox,
            ),
            checkboxChecked = DrawableSet(
                normal = textures.widget_checkbox_checkbox_checked,
                hover = textures.widget_checkbox_checkbox_checked_hover,
                active = textures.widget_checkbox_checkbox_checked_active,
                disabled = textures.widget_checkbox_checkbox_checked,
            ),
            checkBoxButton = DrawableSet(
                normal = textures.widget_checkbox_checkbox_button,
                hover = textures.widget_checkbox_checkbox_button_hover,
                active = textures.widget_checkbox_checkbox_button_active,
                disabled = textures.widget_checkbox_checkbox_button_disabled,
            ),

            radioUnchecked = DrawableSet(
                normal = textures.widget_radio_radio,
                hover = textures.widget_radio_radio_hover,
                active = textures.widget_radio_radio_active,
                disabled = textures.widget_radio_radio,
            ),
            radioChecked = DrawableSet(
                normal = textures.widget_radio_radio_checked,
                hover = textures.widget_radio_radio_checked_hover,
                active = textures.widget_radio_radio_checked_active,
                disabled = textures.widget_radio_radio_checked,
            ),

            switchFrame = DrawableSet(
                normal = textures.widget_switch_frame,
                hover = textures.widget_switch_frame_hover,
                active = textures.widget_switch_frame_active,
                disabled = textures.widget_switch_frame_disabled,
            ),
            switchBackground = TextureSet(
                normal = textures.widget_switch_switch,
                hover = textures.widget_switch_switch_hover,
                active = textures.widget_switch_switch_active,
                disabled = textures.widget_switch_switch_disabled,
            ),
            switchHandle = DrawableSet(
                normal = textures.widget_handle_handle,
                hover = textures.widget_handle_handle_hover,
                active = textures.widget_handle_handle_active,
                disabled = textures.widget_handle_handle_disabled,
            ),

            editText = DrawableSet(
                normal = textures.widget_textfield_textfield,
                hover = textures.widget_textfield_textfield_hover,
                active = textures.widget_textfield_textfield_active,
                disabled = textures.widget_textfield_textfield_disabled,
            ),

            sliderActiveTrack = DrawableSet(
                normal = textures.widget_slider_slider_active,
                hover = textures.widget_slider_slider_active_hover,
                active = textures.widget_slider_slider_active_active,
                disabled = textures.widget_slider_slider_active_disabled,
            ),
            sliderInactiveTrack = DrawableSet(
                normal = textures.widget_slider_slider_inactive,
                hover = textures.widget_slider_slider_inactive_hover,
                active = textures.widget_slider_slider_inactive_active,
                disabled = textures.widget_slider_slider_inactive_disabled,
            ),
            sliderHandle = DrawableSet(
                normal = textures.widget_handle_handle,
                hover = textures.widget_handle_handle_hover,
                active = textures.widget_handle_handle_active,
                disabled = textures.widget_handle_handle_disabled,
            ),

            tab = DrawableSet(
                normal = textures.widget_tab_tab,
                hover = textures.widget_tab_tab_hover,
                active = textures.widget_tab_tab_active,
                disabled = textures.widget_tab_tab_disabled,
            ),

            selectMenuBox = DrawableSet(
                normal = textures.widget_select_select,
                hover = textures.widget_select_select_hover,
                active = textures.widget_select_select_active,
                disabled = textures.widget_select_select_disabled,
            ),

            selectFloatPanel = textures.widget_background_float_window,
            selectIconUp = textures.icon_up,
            selectIconDown = textures.icon_down,

            radioBoxBorder = textures.widget_handle_handle,

            iconButton = DrawableSet(
                normal = textures.widget_icon_button_icon_button,
                hover = textures.widget_icon_button_icon_button_hover,
                active = textures.widget_icon_button_icon_button_active,
                disabled = textures.widget_icon_button_icon_button_disabled,
            ),
            selectedIconButton = DrawableSet(
                normal = textures.widget_icon_button_icon_button_active,
                hover = textures.widget_icon_button_icon_button_active,
                active = textures.widget_icon_button_icon_button_active,
                disabled = textures.widget_icon_button_icon_button_disabled,
            ),

            selectItemUnselected = DrawableSet(
                normal = textures.widget_list_list,
                hover = textures.widget_list_list_hover,
                active = textures.widget_list_list_active,
                disabled = textures.widget_list_list_disabled,
            ),
            selectItemSelected = DrawableSet(
                normal = textures.widget_list_list_active,
                hover = textures.widget_list_list_active,
                active = textures.widget_list_list_active,
                disabled = textures.widget_list_list_disabled,
            ),

            colorPickerHandleChoice = textures.widget_color_picker_handle_choice,
            colorPickerSliderHandleHollow = DrawableSet(
                normal = textures.widget_color_picker_hollow_handle,
                hover = textures.widget_color_picker_hollow_handle_hover,
                active = textures.widget_color_picker_hollow_handle_active,
                disabled = textures.widget_color_picker_hollow_handle_disabled,
            ),

            alertDialogBackground = textures.widget_background_background_gray,

            itemGridBackground = textures.background_backpack,
        ),
        colors = Theme.Colors(
            button = ColorTheme.light,
        ),
    )
}
