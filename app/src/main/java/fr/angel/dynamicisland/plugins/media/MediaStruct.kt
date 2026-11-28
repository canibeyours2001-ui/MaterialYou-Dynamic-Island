package fr.angel.dynamicisland.plugins.media

import android.graphics.Bitmap
import android.media.session.PlaybackState
import android.os.SystemClock
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf


class MediaStruct(
	var artist: MutableState<String> = mutableStateOf(""),
	var title: MutableState<String> = mutableStateOf(""),
	var cover: MutableState<Bitmap?> = mutableStateOf(null),
	var playbackState: MutableState<PlaybackState> = mutableStateOf(PlaybackState.Builder().setState(PlaybackState.STATE_NONE, 0, 0f).build()),
	var duration: MutableState<Long> = mutableLongStateOf(0L),
) {
	fun isPlaying(): Boolean { return playbackState.value.state == PlaybackState.STATE_PLAYING }
}