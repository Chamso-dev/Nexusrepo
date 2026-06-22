package com.nexus.grocerypos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nexus.grocerypos.core.designsystem.components.GlassBackground
import com.nexus.grocerypos.core.designsystem.theme.GroceryPosTheme
import com.nexus.grocerypos.navigation.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GroceryPosTheme {
                GlassBackground {
                    AppRoot()
                }
            }
        }
    }
}
