package com.yes.shared.utils

import android.content.Context
import org.xmlpull.v1.XmlPullParser

class XmlParser(
    val context: Context,
    private val resId:Int
){
    fun parse(type:String, value:String):String{
        val parser=context.resources.getXml(resId)
        var pathData=""
        var eventType=parser.eventType
        while (eventType!= XmlPullParser.END_DOCUMENT){
            if (eventType== XmlPullParser.START_TAG&&parser.name==type){
                for (i in 0 until parser.attributeCount) {
                    if(parser.getAttributeName(i)==value){
                        pathData+=parser.getAttributeValue(i)
                    }
                }
            }
            eventType=parser.next()
        }
        return pathData
    }

}