package com.example.kmpexample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.example.kmpexample.android.contacts.ContactsFeatureHost
import com.example.kmpexample.kmp.core.app.SharedApp
import com.example.kmpexample.kmp.core.app.SharedAppConfig
import com.example.kmpexample.kmp.core.component.RootComponent
import com.example.kmpexample.kmp.data.db.AndroidDatabaseFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootComponent = SharedApp.createRootComponent(
            config = SharedAppConfig(
                databaseFactory = AndroidDatabaseFactory(applicationContext),
            ),
        )

        setContent {
            AndroidApp(rootComponent = rootComponent)
        }
    }
}

@Composable
private fun AndroidApp(rootComponent: RootComponent) {
    AndroidAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            RootRoute(rootComponent = rootComponent)
        }
    }
}

@Composable
private fun RootRoute(rootComponent: RootComponent) {
    val stack by rootComponent.stack.subscribeAsState()
    Children(stack = stack) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.Auth -> AuthRoute(component = instance.component)
            is RootComponent.Child.Contacts -> ContactsFeatureHost(component = instance.component)
        }
    }
}
