package com.example.kmpexample.kmp.feature.contacts.component

import com.example.kmpexample.kmp.feature.base.MviComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactInfoAction
import com.example.kmpexample.kmp.feature.contacts.model.ContactInfoState

interface ContactInfoComponent : MviComponent<ContactInfoState, ContactInfoAction> {
    fun back() = onAction(ContactInfoAction.Back)

    fun edit() = onAction(ContactInfoAction.Edit)

    fun delete() = onAction(ContactInfoAction.Delete)

    fun invite() = onAction(ContactInfoAction.Invite)

    fun toggleExtra() = onAction(ContactInfoAction.ToggleExtra)

    fun writeMessage() = onAction(ContactInfoAction.WriteMessage)

    fun audioCall() = onAction(ContactInfoAction.AudioCall)

    fun videoCall() = onAction(ContactInfoAction.VideoCall)
}
