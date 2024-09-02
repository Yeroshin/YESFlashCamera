package com.yes.flashcamera.utils

import android.content.Context
import com.yes.flashcamera.R
import org.xmlpull.v1.XmlPullParser

class ShutterSpeedsResourcesProvider(val context: Context) {
    private val shutterSpeeds = mutableMapOf<String, String>()
    private var inShutterSpeedsSection = false
    private var currentTag: String? = null



    fun getShutterSpeeds() :Map<String, String>{
        var value: String? = null
        var time: String? = null
        val parser=context.resources.getXml(R.xml.shutter_speeds)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name

                     if (currentTag == "data" && parser.getAttributeValue(null, "type") == "shutter-speeds") {
                         inShutterSpeedsSection = true
                     }
                     if (inShutterSpeedsSection && currentTag == "item") {
                         value = parser.getAttributeValue(null, "value")
                         time = parser.getAttributeValue(null, "time")
                     }
                }
                XmlPullParser.END_TAG -> {
                      if (parser.name == "data" && inShutterSpeedsSection) {
                          inShutterSpeedsSection = false
                      }
                      if (parser.name == "item" && inShutterSpeedsSection && value != null && time != null) {
                          shutterSpeeds[value] = time
                      }
                }
            }
            eventType = parser.next()
        }
        return shutterSpeeds
    }
}