package com.yes.camera.presentation.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.yes.shared.presentation.ui.theme.AppTheme

@Composable
fun ErrorScreen(
    error: Throwable,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.dimens.extraLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(AppTheme.dimens.shutterTopPadding),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.large))

        Text(
            text = "Произошла ошибка",
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.medium))

        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = AppTheme.dimens.histogramWidth * 1.33f)
        ) {
            Text(
                text = error.localizedMessage ?: "Неизвестная ошибка",
                modifier = Modifier
                    .padding(AppTheme.dimens.medium)
                    .verticalScroll(rememberScrollState()),
                style = AppTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.dimens.extraLarge))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.primaryAccent,
                contentColor = AppTheme.colors.background
            )
        ) {
            Text(text = "Попробовать снова")
        }
    }
}
