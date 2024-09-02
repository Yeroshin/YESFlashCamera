package com.yes.flashcamera.utils

import android.content.Context
import android.content.res.AssetManager
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream


class ResourcesProvider(
    private val context: Context
) {
    interface XmlDelegate{
        val filename:String
        fun run( context:Context)
    }
    fun getString(delegate:XmlDelegate) {
        val assetManager: AssetManager = context.assets
        var inputStream: InputStream? = null

        try {
        /*    inputStream = assetManager.open(delegate.filename)
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)
*/
            delegate.run(context)
           /* while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                      //  delegate.run(parser)
                       /* if (currentTag == "data" && parser.getAttributeValue(null, "type") == "shutter-speeds") {
                            inShutterSpeedsSection = true
                        }
                        if (inShutterSpeedsSection && currentTag == "item") {
                            value = parser.getAttributeValue(null, "value")
                            time = parser.getAttributeValue(null, "time")
                        }*/
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
            }*/
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}