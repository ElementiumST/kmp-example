package com.example.kmpexample.kmp.core.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.example.kmpexample.kmp.domain.usecase.ClearPersistedAuthSessionUseCase
import com.example.kmpexample.kmp.domain.usecase.CreateNoteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.DeleteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.FindContactsPresencesUseCase
import com.example.kmpexample.kmp.domain.usecase.FindInterlocutorsUseCase
import com.example.kmpexample.kmp.domain.usecase.GetContactsUseCase
import com.example.kmpexample.kmp.domain.usecase.GetPersistedAuthSessionUseCase
import com.example.kmpexample.kmp.domain.usecase.InviteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.LoginUseCase
import com.example.kmpexample.kmp.domain.usecase.LoginWithTokenUseCase
import com.example.kmpexample.kmp.domain.usecase.ObserveContactEventsUseCase
import com.example.kmpexample.kmp.domain.usecase.UpdateContactUseCase
import com.example.kmpexample.kmp.feature.auth.component.AuthComponent
import com.example.kmpexample.kmp.feature.auth.component.DefaultAuthComponent
import com.example.kmpexample.kmp.feature.base.StateSubscription
import com.example.kmpexample.kmp.feature.contacts.component.ContactsComponent
import com.example.kmpexample.kmp.feature.contacts.component.DefaultContactsComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val clearPersistedAuthSessionUseCase: ClearPersistedAuthSessionUseCase,
    private val getPersistedAuthSessionUseCase: GetPersistedAuthSessionUseCase,
    private val loginUseCase: LoginUseCase,
    private val loginWithTokenUseCase: LoginWithTokenUseCase,
    private val getContactsUseCase: GetContactsUseCase,
    private val createNoteContactUseCase: CreateNoteContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val inviteContactUseCase: InviteContactUseCase,
    private val findInterlocutorsUseCase: FindInterlocutorsUseCase,
    private val findContactsPresencesUseCase: FindContactsPresencesUseCase,
    private val observeContactEventsUseCase: ObserveContactEventsUseCase,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : RootComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Auth,
        handleBackButton = true,
        childFactory = ::child,
    )

    override val authComponent: AuthComponent
        get() = (stack.value.active.instance as? RootComponent.Child.Auth)?.component
            ?: error("Auth component is not active")

    override fun currentChildKind(): RootChildKind {
        val active = stack.value.active.instance
        return when (active) {
            is RootComponent.Child.Auth -> RootChildKind.AUTH
            is RootComponent.Child.Contacts -> {
                when (active.component.childStack.value.active.instance) {
                    ContactsComponent.Child.List -> RootChildKind.CONTACTS_LIST
                    is ContactsComponent.Child.Info -> RootChildKind.CONTACT_INFO
                    is ContactsComponent.Child.Create -> RootChildKind.CONTACT_CREATE
                    is ContactsComponent.Child.Edit -> RootChildKind.CONTACT_EDIT
                }
            }
        }
    }

    override fun watchChildKind(observer: (RootChildKind) -> Unit): StateSubscription {
        observer(currentChildKind())
        val cancellation = stack.subscribe { childStack ->
            val kind = when (val active = childStack.active.instance) {
                is RootComponent.Child.Auth -> RootChildKind.AUTH
                is RootComponent.Child.Contacts -> {
                    when (active.component.childStack.value.active.instance) {
                        ContactsComponent.Child.List -> RootChildKind.CONTACTS_LIST
                        is ContactsComponent.Child.Info -> RootChildKind.CONTACT_INFO
                        is ContactsComponent.Child.Create -> RootChildKind.CONTACT_CREATE
                        is ContactsComponent.Child.Edit -> RootChildKind.CONTACT_EDIT
                    }
                }
            }
            observer(kind)
        }
        return StateSubscription { cancellation.cancel() }
    }

    override fun currentAuthComponentOrNull(): AuthComponent? {
        return (stack.value.active.instance as? RootComponent.Child.Auth)?.component
    }

    override fun currentContactsComponentOrNull(): ContactsComponent? {
        return (stack.value.active.instance as? RootComponent.Child.Contacts)?.component
    }

    private fun child(
        config: Config,
        childComponentContext: ComponentContext,
    ): RootComponent.Child {
        return when (config) {
            Config.Auth -> {
                val component = DefaultAuthComponent(
                    componentContext = childComponentContext,
                    clearPersistedAuthSessionUseCase = clearPersistedAuthSessionUseCase,
                    getPersistedAuthSessionUseCase = getPersistedAuthSessionUseCase,
                    loginUseCase = loginUseCase,
                    loginWithTokenUseCase = loginWithTokenUseCase,
                )
                observeAuthorized(component)
                RootComponent.Child.Auth(component = component)
            }
            Config.Contacts -> RootComponent.Child.Contacts(
                component = DefaultContactsComponent(
                    componentContext = childComponentContext,
                    getContactsUseCase = getContactsUseCase,
                    createNoteContactUseCase = createNoteContactUseCase,
                    updateContactUseCase = updateContactUseCase,
                    deleteContactUseCase = deleteContactUseCase,
                    inviteContactUseCase = inviteContactUseCase,
                    findInterlocutorsUseCase = findInterlocutorsUseCase,
                    findContactsPresencesUseCase = findContactsPresencesUseCase,
                    observeContactEventsUseCase = observeContactEventsUseCase,
                ),
            )
        }
    }

    private fun observeAuthorized(component: AuthComponent) {
        coroutineScope.launch {
            component.state
                .map { it.isAuthorized }
                .distinctUntilChanged()
                .collect { authorized ->
                    if (authorized) {
                        navigation.replaceAll(Config.Contacts)
                    }
                }
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Auth : Config

        @Serializable
        data object Contacts : Config
    }
}
