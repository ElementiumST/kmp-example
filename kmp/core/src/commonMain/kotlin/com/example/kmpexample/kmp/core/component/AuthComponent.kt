package com.example.kmpexample.kmp.core.component

import com.example.kmpexample.kmp.core.model.AuthScreenAction
import com.example.kmpexample.kmp.core.model.AuthScreenState
import com.example.kmpexample.kmp.feature.base.MviComponent

interface AuthComponent : MviComponent<AuthScreenState, AuthScreenAction>
