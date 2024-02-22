package com.example.myar

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.example.myar.databinding.ActivityAugmentedRealityBinding
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

class AugmentedRealityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAugmentedRealityBinding
    private lateinit var sceneView: ARSceneView
    private lateinit var loadingView: View
    private lateinit var instructionText: TextView

    private var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    private var anchorNode: AnchorNode? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    private var trackingFailureReason: TrackingFailureReason? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAugmentedRealityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFullScreen(
            binding.root,
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )

        setSupportActionBar(binding.toolbar.apply {
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).topMargin = systemBarsInsets.top
            }
            title = ""
        })

        instructionText = binding.instructionText
        loadingView = binding.loadingView
        binding.btnPlace.setOnClickListener {
            sceneView.planeRenderer.isVisible = false
        }

        sceneView = binding.ars.apply {
            planeRenderer.isEnabled = true
            configureSession { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            }
            onSessionUpdated = { _, frame ->
                if (anchorNode == null) {
                    frame.getUpdatedPlanes()
                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                        ?.let { plane ->
                            addAnchorNode(plane.createAnchor(plane.centerPose))

                        }
                }
            }
            onTrackingFailureChanged = { reason ->
                this@AugmentedRealityActivity.trackingFailureReason = reason
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        InformationFragment().apply {
            show(supportFragmentManager, this.tag)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateInstructions() {
        instructionText.text =
            trackingFailureReason?.getDescription(this) ?: if (anchorNode == null) {
                getString(R.string.point_your_phone_down)
            } else {
                null
            }
    }


    private fun addAnchorNode(anchor: Anchor) {
        sceneView.addChildNode(
            AnchorNode(sceneView.engine, anchor)
                .apply {
                    isEditable = true
                    lifecycleScope.launch {
                        isLoading = true
                        sceneView.modelLoader.loadModelInstance("long_arm_octopus_octopus_minor.glb")?.let { modelInstance ->
                            addChildNode(
                                ModelNode(
                                    modelInstance = modelInstance,
                                    // Scale to fit in a 0.5 meters cube
                                    scaleToUnits = 0.5f,
                                    // Bottom origin instead of center so the model base is on floor
                                    centerOrigin = Position(y = -0.5f),
                                    autoAnimate = false
                                ).apply {
                                    isEditable = true
                                }
                            )
                        }
                        isLoading = false
                    }
                    onAnchorChanged = {
                        binding.btnPlace.isGone
                    }
                    anchorNode = this
                }
        )
    }
}