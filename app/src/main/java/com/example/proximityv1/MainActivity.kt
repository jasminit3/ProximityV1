package com.example.proximityv1

import android.app.Notification
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.estimote.proximity_sdk.api.*
import android.content.Context
import android.content.Intent
import android.nfc.Tag
import android.os.Message
import android.util.*
import com.estimote.coresdk.common.internal.utils.L
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var observationsHandler: ProximityObserver.Handler?=null
    private val logTags = MainActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //1. Opsætter Estimote credentials for forbindelse til estimote cloud
        val cloudCredentials = EstimoteCloudCredentials(
            "s170720-student-dtu-dk-s-p-j0r",
            "124af5cc28250d8e2d759fafd1fb5010"
        )


        //2. Opretter Proximity Observer
        val proximityObserver =
            ProximityObserverBuilder(applicationContext, cloudCredentials).withBalancedPowerMode()
                .onError { throwable ->
                    Log.e("app", "proximity observer error: $throwable")
                }
                .build()


        //3. Definerer Proximity zone
        val venueZone = ProximityZoneBuilder()
            .forTag("Jasmin")
            .inNearRange()

            .onEnter { zoneContext ->
                Log.d(logTags, "Entered: " + zoneContext.tag)
                /* val title = zoneContext.attachments["CPR"]
             val description = zoneContext.attachments["0123456789"]
             Log.i(logTags, title + "" + description)*/
            }
            .onExit { zoneContext ->
                Log.i(logTags, "Exited: " + zoneContext.tag) //når bruger forlader zone
                /* val title = zoneContext.attachments["CPR"]
             val description = zoneContext.attachments["0123456789"]
             Log.i(logTags, title + "" + description)*/
            }
            .onContextChange { contexts ->
                Log.i(logTags, "onContextChange" + contexts)
                /*val nearbyContent = ArrayList<ProximityZoneContext>(contexts.size)  //ProximityZoneContext-arraylist indeholder contexts, som indeholder data tilhørende beaconen; attachments, deviceId, tag
            for (context in contexts) {
                val title: String = context.attachments["CPR"] ?: "0123456789"
                nearbyContent.add(ProximityZoneContext(title))
                }
            }*/
            }
            .build()


        //4. Lokationstilladelse + Starter Proximity observering
        RequirementsWizardFactory
            .createEstimoteRequirementsWizard()
            .fulfillRequirements(this,
                {
                    Log.i("app", "Krav opfyldt")
                    val observationsHandler = proximityObserver.startObserving(venueZone) // onRequirementsFulfilled
                },
                { requirements ->
                    Log.e(
                        "app",
                        "Krav mangler - Scanning virker ikke: " + requirements   //onRequirementsMissing
                    )
                },
                { throwable ->
                    Log.e("app", "Fejl i krav: " + throwable) //onError
                })

    }

        //6. Stopper scanning
        override fun onDestroy() {
            observationsHandler?.stop()
            super.onDestroy()

    }

}
