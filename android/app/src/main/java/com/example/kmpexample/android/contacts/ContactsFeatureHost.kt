package com.example.kmpexample.android.contacts

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.example.kmpexample.kmp.feature.contacts.component.ContactsComponent
import androidx.compose.runtime.getValue

@Composable
fun ContactsFeatureHost(component: ContactsComponent) {
    val stack by component.childStack.subscribeAsState()
    Children(stack = stack) { child ->
        when (val instance = child.instance) {
            ContactsComponent.Child.List -> ContactsListScreen(component = component)
            is ContactsComponent.Child.Info -> ContactInfoScreen(component = instance.component)
            is ContactsComponent.Child.Create -> ContactEditorScreen(component = instance.component)
            is ContactsComponent.Child.Edit -> ContactEditorScreen(component = instance.component)
        }
    }
}
