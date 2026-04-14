package com.example.kmpexample.kmp.feature.auth.component

import com.example.kmpexample.kmp.feature.auth.model.AuthScreenAction
import com.example.kmpexample.kmp.feature.auth.model.AuthScreenState
import com.example.kmpexample.kmp.feature.base.MviComponent

interface AuthComponent : MviComponent<AuthScreenState, AuthScreenAction> {
    fun updateLogin(value: String)

    fun updatePassword(value: String)

    fun submit()
}
