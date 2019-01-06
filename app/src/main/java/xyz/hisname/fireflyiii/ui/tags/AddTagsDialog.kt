package xyz.hisname.fireflyiii.ui.tags

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.dialog_add_tags.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddTagsDialog: BaseDialog() {

    private var date: String? = null
    private var description: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var zoomLevel: String? = null
    private val tagId by lazy { arguments?.getLong("tagId") ?: 0 }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_add_tags, container)
    }

    override fun onStart() {
        super.onStart()
        placeHolderToolbar.setOnClickListener {
            unReveal(dialog_add_tags_layout)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(dialog_add_tags_layout)
        if(tagId != 0L){
            updateData()
        }
        addTagFab.setOnClickListener {
            hideKeyboard()
            ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
            date = if(date_edittext.isBlank()){
                null
            } else {
                date_edittext.getString()
            }
            description = if(description_edittext.isBlank()){
                null
            } else {
                description_edittext.getString()
            }
            latitude = if(latitude_edittext.isBlank()){
                null
            } else {
                latitude_edittext.getString()
            }
            longitude = if(longitude_edittext.isBlank()){
                null
            } else {
                longitude_edittext.getString()
            }
            zoomLevel = if(zoom_edittext.isBlank()){
                null
            } else {
                zoom_edittext.getString()
            }
            if(tagId == 0L){
                submitData()
            } else {
                tagsViewModel.updateTag(tagId, tag_edittext.getString(), date, description, latitude,
                        longitude, zoomLevel).observe(this, Observer { apiResponse ->
                    val errorMessage = apiResponse.getErrorMessage()
                    when {
                        errorMessage != null -> toastError(errorMessage)
                        apiResponse.getError() != null -> toastError("Error updating " + tag_edittext.getString())
                        apiResponse.getResponse() != null -> {
                            requireFragmentManager().commit {
                                replace(R.id.fragment_container, ListTagsFragment())
                            }
                            toastSuccess(tag_edittext.getString() + " updated!")
                            unReveal(dialog_add_tags_layout)
                        }
                    }
                })
            }
        }
    }

    private fun updateData(){
        tagsViewModel.getTagById(tagId).observe(this, Observer {
            val tagData = it[0].tagsAttributes
            tag_edittext.setText(tagData?.tag)
            date_edittext.setText(tagData?.date)
            description_edittext.setText(tagData?.description)
            latitude_edittext.setText(tagData?.latitude)
            longitude_edittext.setText(tagData?.longitude)
            zoom_edittext.setText(tagData?.zoom_level)
        })
    }

    override fun setWidgets(){
        val calendar = Calendar.getInstance()
        val calendarDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                date_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }
        date_edittext.setOnClickListener {
            DatePickerDialog(requireContext(), calendarDate, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
    }

    override fun setIcons(){
        date_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                .icon(FontAwesome.Icon.faw_calendar)
                .color(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
                .sizeDp(24),null, null, null)
        description_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_audio_description)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_amber_800))
                        .sizeDp(24),null, null, null)
        latitude_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_map)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_800))
                        .sizeDp(24),null, null, null)
        longitude_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_map)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_800))
                        .sizeDp(24),null, null, null)
        zoom_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_zoom_in)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                        .sizeDp(24),null, null, null)
        addTagFab.setImageDrawable(IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_tag)
                .color(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                .sizeDp(24))
        placeHolderToolbar.navigationIcon = navIcon
    }

    override fun submitData(){
        tagsViewModel.addTag(tag_edittext.getString(), date, description, latitude, longitude, zoomLevel).observe(this, Observer {
            ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
            val errorMessage = it.getErrorMessage()
            when {
                errorMessage != null -> toastError(errorMessage)
                it.getError() != null -> toastError("Error saving " + tag_edittext.getString())
                it.getResponse() != null -> {
                    requireFragmentManager().commit {
                        replace(R.id.fragment_container, ListTagsFragment())
                    }
                    toastSuccess(tag_edittext.getString()+ " saved!")
                    unReveal(dialog_add_tags_layout)
                }
            }
        })
    }

}