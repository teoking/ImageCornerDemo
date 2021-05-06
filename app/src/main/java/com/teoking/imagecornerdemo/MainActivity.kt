package com.teoking.imagecornerdemo

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    private val layerTypeGroup by lazy { findViewById<RadioGroup>(R.id.layerTypeGroup) }
    private val imageTypeGroup by lazy { findViewById<RadioGroup>(R.id.imageTypeGroup) }
    private val frameLayout by lazy { findViewById<FrameLayout>(R.id.frameLayout) }
    private val cornerPercentBar by lazy { findViewById<SeekBar>(R.id.cornerPercentBar) }
    private lateinit var cornerImage: CornerResizeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layerTypeGroup.setOnCheckedChangeListener(this)
        imageTypeGroup.setOnCheckedChangeListener(this)
        cornerPercentBar.setOnSeekBarChangeListener(this)

        replaceImageById(imageTypeGroup.checkedRadioButtonId)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when(group?.id) {
            R.id.layerTypeGroup -> changeLayerType(checkedId)
            R.id.imageTypeGroup -> replaceImageById(checkedId)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        cornerImage.changeCorner(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    private fun changeLayerType(checkedId: Int) {
        when(checkedId) {
            R.id.layerHardware -> cornerImage.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            R.id.layerSoftware -> cornerImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        cornerImage.invalidate()
    }

    private fun replaceImageById(checkedId: Int) {
        when(checkedId) {
            R.id.outlineImage -> replaceImage(OutlineRoundCornerImage(this))
            R.id.shaderImage -> replaceImage(ShaderRoundCornerImage(this))
            R.id.cornerEffectImage -> replaceImage(CornerEffectCornerImage(this))
        }
    }

    private fun replaceImage(view: CornerResizeView) {
        frameLayout.removeAllViews()
        frameLayout.addView(view)
        cornerImage = view
        cornerImage.changeCorner(cornerPercentBar.progress)
    }
}