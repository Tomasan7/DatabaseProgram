package me.tomasan7.opinet.ui.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    onChangeVisibilityClick: () -> Unit,
    passwordShown: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password,
        autoCorrectEnabled = false
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    label: @Composable (() -> Unit)? = null
)
{
    TextField(
        value = password,
        singleLine = true,
        visualTransformation = if (passwordShown) VisualTransformation.None else PasswordVisualTransformation(),
        onValueChange = onPasswordChange,
        label = label,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = {
            IconButton(
                onClick = { onChangeVisibilityClick() },
                modifier = Modifier.focusProperties { canFocus = false }
            ) {
                Icon(
                    imageVector = if (passwordShown) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordShown) "Hide password" else "Show password"
                )
            }
        },
        modifier = modifier
    )
}
