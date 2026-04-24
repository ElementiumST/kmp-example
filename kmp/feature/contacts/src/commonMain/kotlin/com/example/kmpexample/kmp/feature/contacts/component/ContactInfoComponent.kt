package com.example.kmpexample.kmp.feature.contacts.component

import com.example.kmpexample.kmp.feature.base.MviComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactInfoAction
import com.example.kmpexample.kmp.feature.contacts.model.ContactInfoState
import com.example.kmpexample.kmp.feature.contacts.model.ContactInfoActionWrappers

interface ContactInfoComponent : MviComponent<ContactInfoState, ContactInfoAction>,
    ContactInfoActionWrappers
