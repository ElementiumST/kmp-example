import Foundation
import SharedCore

@MainActor
final class RootViewModel: ObservableObject {
    enum ChildKind: Equatable {
        case auth
        case contactsList
        case contactInfo
        case contactCreate
        case contactEdit
    }

    @Published private(set) var childKind: ChildKind = .auth
    @Published private(set) var authState: AuthScreenState = AuthScreenState(
        login: "",
        password: "",
        isLoading: false,
        isAuthorized: false,
        errorMessage: nil,
        sessionId: nil,
        authorizedLogin: nil,
        authorizedName: nil,
        submitLabel: "Войти",
        canSubmit: false
    )
    @Published private(set) var contactsState: ContactsListState = ContactsListState(
        query: "",
        items: [],
        total: 0,
        isLoading: true,
        isLoadingMore: false,
        isRefreshing: false,
        errorMessage: nil,
        presence: [:],
        isAddOverlayVisible: false,
        addOverlay: ContactAddOverlayState(
            query: "",
            items: [],
            isLoading: false,
            isLoadingMore: false,
            errorMessage: nil,
            foreignOffset: 0,
            companyOffset: 0,
            directoryOffset: 0,
            domainOffset: 0,
            hasMore: false,
            invitingProfileIds: []
        ),
        contextMenuContactIndex: -1,
        snackbarMessage: nil
    )
    @Published private(set) var infoState: ContactInfoState?
    @Published private(set) var editorState: ContactEditorState?

    private let rootComponent: any RootComponent
    private var authComponent: (any AuthComponent)?
    private var contactsComponent: (any ContactsComponent)?

    private var rootChildSubscription: StateSubscription?
    private var authSubscription: StateSubscription?
    private var contactsSubscription: StateSubscription?
    private var contactsChildSubscription: StateSubscription?
    private var infoSubscription: StateSubscription?
    private var editorSubscription: StateSubscription?

    init(factory: AppleAppFactory = AppleAppFactory()) {
        self.rootComponent = factory.createRootComponent()
        bindRoot()
    }

    private func bindRoot() {
        rootChildSubscription = rootComponent.watchChildKind { [weak self] kind in
            guard let self else { return }
            let mapped = Self.mapRootKind(kind)
            Task { @MainActor in
                self.childKind = mapped
                self.rebindForCurrentRootChild()
            }
        }
        rebindForCurrentRootChild()
    }

    private func rebindForCurrentRootChild() {
        authSubscription?.cancel()
        contactsSubscription?.cancel()
        contactsChildSubscription?.cancel()
        infoSubscription?.cancel()
        editorSubscription?.cancel()

        authComponent = rootComponent.currentAuthComponentOrNull()
        contactsComponent = rootComponent.currentContactsComponentOrNull()

        if let authComponent {
            if let state = authComponent.currentState() as? AuthScreenState {
                authState = state
            }
            authSubscription = authComponent.watchState { [weak self] state in
                guard let self else { return }
                guard let next = state as? AuthScreenState else { return }
                Task { @MainActor in
                    self.authState = next
                }
            }
        }

        if let contactsComponent {
            if let state = contactsComponent.currentState() as? ContactsListState {
                contactsState = state
            }
            contactsSubscription = contactsComponent.watchState { [weak self] state in
                guard let self else { return }
                guard let next = state as? ContactsListState else { return }
                Task { @MainActor in
                    self.contactsState = next
                }
            }
            contactsChildSubscription = contactsComponent.watchChildKind { [weak self] kind in
                guard let self else { return }
                Task { @MainActor in
                    self.childKind = Self.mapContactsKind(kind)
                    self.rebindContactsChild()
                }
            }
            rebindContactsChild()
        }
    }

    private func rebindContactsChild() {
        infoSubscription?.cancel()
        editorSubscription?.cancel()
        infoState = nil
        editorState = nil

        guard let contactsComponent else { return }

        if let infoComponent = contactsComponent.currentInfoComponentOrNull() {
            if let state = infoComponent.currentState() as? ContactInfoState {
                infoState = state
            }
            infoSubscription = infoComponent.watchState { [weak self] state in
                guard let self else { return }
                guard let next = state as? ContactInfoState else { return }
                Task { @MainActor in
                    self.infoState = next
                }
            }
        }

        let editorComponent = contactsComponent.currentCreateComponentOrNull() ?? contactsComponent.currentEditComponentOrNull()
        if let editorComponent {
            if let state = editorComponent.currentState() as? ContactEditorState {
                editorState = state
            }
            editorSubscription = editorComponent.watchState { [weak self] state in
                guard let self else { return }
                guard let next = state as? ContactEditorState else { return }
                Task { @MainActor in
                    self.editorState = next
                }
            }
        }
    }

    private static func mapRootKind(_ value: Any?) -> ChildKind {
        let raw = String(describing: value).lowercased()
        if raw.contains("contact_info") { return .contactInfo }
        if raw.contains("contact_create") { return .contactCreate }
        if raw.contains("contact_edit") { return .contactEdit }
        if raw.contains("contacts") { return .contactsList }
        return .auth
    }

    private static func mapContactsKind(_ value: Any?) -> ChildKind {
        let raw = String(describing: value).lowercased()
        if raw.contains("info") { return .contactInfo }
        if raw.contains("create") { return .contactCreate }
        if raw.contains("edit") { return .contactEdit }
        return .contactsList
    }

    func updateLogin(_ value: String) {
        authComponent?.updateLogin(value: value)
    }

    func updatePassword(_ value: String) {
        authComponent?.updatePassword(value: value)
    }

    func submit() {
        authComponent?.submit()
    }

    func refreshContacts() {
        contactsComponent?.refresh()
    }

    func updateContactsQuery(_ value: String) {
        contactsComponent?.updateQuery(value: value)
    }

    func openContact(_ index: Int32) {
        contactsComponent?.openInfo(contactIndex: index)
    }

    func openAddContactSheet() {
        contactsComponent?.openAddOverlay()
    }

    func closeAddContactSheet() {
        contactsComponent?.closeAddOverlay()
    }

    func updateAddQuery(_ value: String) {
        contactsComponent?.updateAddOverlayQuery(value: value)
    }

    func inviteInterlocutor(_ profileId: String) {
        contactsComponent?.inviteInterlocutor(profileId: profileId)
    }

    func openCreateNote() {
        contactsComponent?.openCreate()
    }

    func openEdit(_ index: Int32) {
        contactsComponent?.openEdit(contactIndex: index)
    }

    func deleteContact(_ index: Int32) {
        contactsComponent?.deleteFromMenu(contactIndex: index)
    }

    func infoBack() {
        contactsComponent?.currentInfoComponentOrNull()?.back()
    }

    func infoToggleExtra() {
        contactsComponent?.currentInfoComponentOrNull()?.toggleExtra()
    }

    func infoEdit() {
        contactsComponent?.currentInfoComponentOrNull()?.edit()
    }

    func infoDelete() {
        contactsComponent?.currentInfoComponentOrNull()?.delete()
    }

    func infoInvite() {
        contactsComponent?.currentInfoComponentOrNull()?.invite()
    }

    func editorUpdateName(_ value: String) {
        contactsComponent?.currentCreateComponentOrNull()?.updateName(value: value)
        contactsComponent?.currentEditComponentOrNull()?.updateName(value: value)
    }

    func editorUpdateEmail(_ value: String) {
        contactsComponent?.currentCreateComponentOrNull()?.updateEmail(value: value)
        contactsComponent?.currentEditComponentOrNull()?.updateEmail(value: value)
    }

    func editorUpdatePhone(_ value: String) {
        contactsComponent?.currentCreateComponentOrNull()?.updatePhone(value: value)
        contactsComponent?.currentEditComponentOrNull()?.updatePhone(value: value)
    }

    func editorUpdateNote(_ value: String) {
        contactsComponent?.currentCreateComponentOrNull()?.updateNote(value: value)
        contactsComponent?.currentEditComponentOrNull()?.updateNote(value: value)
    }

    func editorUpdateTags(_ value: String) {
        contactsComponent?.currentCreateComponentOrNull()?.updateTags(value: value)
        contactsComponent?.currentEditComponentOrNull()?.updateTags(value: value)
    }

    func editorSave() {
        contactsComponent?.currentCreateComponentOrNull()?.save()
        contactsComponent?.currentEditComponentOrNull()?.save()
    }

    func editorBack() {
        contactsComponent?.currentCreateComponentOrNull()?.back()
        contactsComponent?.currentEditComponentOrNull()?.back()
    }

    func editorConfirmLeave() {
        contactsComponent?.currentCreateComponentOrNull()?.confirmLeave()
        contactsComponent?.currentEditComponentOrNull()?.confirmLeave()
    }

    func editorCancelLeave() {
        contactsComponent?.currentCreateComponentOrNull()?.cancelLeave()
        contactsComponent?.currentEditComponentOrNull()?.cancelLeave()
    }

    deinit {
        rootChildSubscription?.cancel()
        authSubscription?.cancel()
        contactsSubscription?.cancel()
        contactsChildSubscription?.cancel()
        infoSubscription?.cancel()
        editorSubscription?.cancel()
    }
}
