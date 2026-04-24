package com.example.kmpexample.kmp.feature.contacts.component

import com.example.kmpexample.kmp.feature.base.MviComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorAction
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorState
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorActionWrappers

interface ContactEditorComponent :
    MviComponent<ContactEditorState, ContactEditorAction>,
    ContactEditorActionWrappers
