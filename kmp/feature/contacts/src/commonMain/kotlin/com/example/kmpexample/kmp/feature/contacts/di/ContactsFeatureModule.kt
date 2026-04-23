package com.example.kmpexample.kmp.feature.contacts.di

import com.example.kmpexample.kmp.domain.usecase.CreateNoteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.DeleteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.FindContactsPresencesUseCase
import com.example.kmpexample.kmp.domain.usecase.FindInterlocutorsUseCase
import com.example.kmpexample.kmp.domain.usecase.GetContactsUseCase
import com.example.kmpexample.kmp.domain.usecase.InviteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.ObserveContactEventsUseCase
import com.example.kmpexample.kmp.domain.usecase.UpdateContactUseCase
import org.koin.dsl.module

fun contactsFeatureModule() = module {
    factory { GetContactsUseCase(get()) }
    factory { CreateNoteContactUseCase(get()) }
    factory { UpdateContactUseCase(get()) }
    factory { DeleteContactUseCase(get()) }
    factory { InviteContactUseCase(get()) }
    factory { FindInterlocutorsUseCase(get()) }
    factory { FindContactsPresencesUseCase(get()) }
    factory { ObserveContactEventsUseCase(get()) }
}
