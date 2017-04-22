package jp.ogiwara.kotlin.aileen2.model

import java.io.Serializable

data class Video(val id: String,
                 val title: String,
                 val channelName: String,
                 val thumbnail: String?,//at invisible mode
                 val duration: String,
                 val viewCount: String) : Serializable