/*
 * Copyright (c) 2024 You Apps
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.bnyro.translate.ui.components

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bnyro.translate.R
import com.bnyro.translate.ui.models.TranslationModel
import com.bnyro.translate.util.ClipboardHelper
import com.bnyro.translate.util.Preferences

@Composable
fun TranslationField(
    translationModel: TranslationModel,
    writeEnabled: Boolean,
    text: String,
    languageCode: String,
    onTextChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val handler = remember {
        Handler(Looper.getMainLooper())
    }

    val charPref = remember {
        Preferences.get(Preferences.charCounterLimitKey, "")
    }

    AnimatedVisibility(
        visible = text.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically(),
        label = "text actions fade"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            var copyImageVector by remember {
                mutableStateOf(Icons.Default.ContentCopy)
            }
            StyledIconButton(
                imageVector = copyImageVector,
                onClick = {
                    ClipboardHelper(
                        context
                    ).write(text)
                    copyImageVector = Icons.Default.DoneAll
                    handler.postDelayed({
                        copyImageVector = Icons.Default.ContentCopy
                    }, 2000)
                }
            )

            StyledIconButton(
                imageVector = Icons.Default.Share,
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, text)
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(shareIntent)
                }
            )

            TTSButton(translationModel, text, languageCode)
        }
    }

    StyledTextField(
        text = text + (if (!writeEnabled) "\n\n\n" else ""),
        placeholder = if (writeEnabled) stringResource(R.string.enter_text) else null,
        readOnly = !writeEnabled,
        textColor = if (
            charPref.isNotEmpty() && translationModel.translation.translatedText.length >= charPref.toInt()
        ) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.typography.bodyMedium.color
        }
    ) {
        onTextChange(it)
    }
}