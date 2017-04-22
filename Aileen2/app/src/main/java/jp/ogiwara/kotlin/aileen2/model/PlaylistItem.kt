package jp.ogiwara.kotlin.aileen2.model


data class PlaylistItem(val title: String,
                        val id: String,
                        val playlistThumbnailUrl: String,
                        val thumbnail1Url: String?,
                        val thumbnail2Url: String?,
                        val thumbnail3Url: String?,
                        val thumbnail4Url: String?,
                        val thumbnail5Url: String?,
                        val thumbnail6Url: String?)

data class PlaylistItem2(val title: String,
                        val id: String,
                        val playlistThumbnailUrl: String)