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

    private let rootAccessor: AppleRootAccessor
    private var contactsAccessor: AppleContactsAccessor?
    private var authComponent: (any AuthComponent)?
    private var contactsComponent: (any ContactsComponent)?

    private var rootChildSubscription: StateSubscription?
    private var authSubscription: StateSubscription?
    private var contactsSubscription: StateSubscription?
    private var contactsChildSubscription: StateSubscription?
    private var infoSubscription: StateSubscription?
    private var editorSubscription: StateSubscription?

    init(factory: AppleAppFactory = AppleAppFactory()) {
        self.rootAccessor = factory.createRootAccessor(config: SharedAppConfig())
        bindRoot()
    }

    private func bindRoot() {
        rootChildSubscription = rootAccessor.watchKind { [weak self] kind in
            guard let self else { return }
            let mapped = Self.map(rootKind: kind)
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

        authComponent = rootAccessor.authComponent()
        contactsAccessor = rootAccessor.contactsAccessor()
        contactsComponent = contactsAccessor?.contactsComponent()

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

        if let contactsComponent, let contactsAccessor {
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
            contactsChildSubscription = contactsAccessor.watchKind { [weak self] kind in
                guard let self else { return }
                Task { @MainActor in
                    self.childKind = Self.map(contactsKind: kind)
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

        guard let contactsAccessor else { return }

        if let infoComponent = contactsAccessor.infoComponent() {
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

        if let editorComponent = contactsAccessor.activeEditorComponent() {
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

    private static func map(rootKind kind: AppleRootAccessorKind) -> ChildKind {
        switch kind {
        case .auth: return .auth
        case .contactsList: return .contactsList
        case .contactInfo: return .contactInfo
        case .contactCreate: return .contactCreate
        case .contactEdit: return .contactEdit
        default: return .auth
        }
    }

    private static func map(contactsKind kind: AppleContactsAccessorKind) -> ChildKind {
        switch kind {
        case .list: return .contactsList
        case .info: return .contactInfo
        case .create: return .contactCreate
        case .edit: return .contactEdit
        default: return .contactsList
        }
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
        contactsAccessor?.infoComponent()?.back()
    }

    func infoToggleExtra() {
        contactsAccessor?.infoComponent()?.toggleExtra()
    }

    func infoEdit() {
        contactsAccessor?.infoComponent()?.edit()
    }

    func infoDelete() {
        contactsAccessor?.infoComponent()?.delete()
    }

    func infoInvite() {
        contactsAccessor?.infoComponent()?.invite()
    }

    func editorUpdateName(_ value: String) {
        contactsAccessor?.activeEditorComponent()?.updateName(value: value)
    }

    func editorUpdateEmail(_ value: String) {
        contactsAccessor?.activeEditorComponent()?.updateEmail(value: value)
    }

    func editorUpdatePhone(_ value: String) {
        contactsAccessor?.activeEditorComponent()?.updatePhone(value: value)
    }

    func editorUpdateNote(_ value: String) {
        contactsAccessor?.activeEditorComponent()?.updateNote(value: value)
    }

    func editorUpdateTags(_ value: String) {
        contactsAccessor?.activeEditorComponent()?.updateTags(value: value)
    }

    func editorSave() {
        contactsAccessor?.activeEditorComponent()?.save()
    }

    func editorBack() {
        contactsAccessor?.activeEditorComponent()?.back()
    }

    func editorConfirmLeave() {
        contactsAccessor?.activeEditorComponent()?.confirmLeave()
    }

    func editorCancelLeave() {
        contactsAccessor?.activeEditorComponent()?.cancelLeave()
    }

    deinit {
        rootChildSubscription?.cancel()
        authSubscription?.cancel()
        contactsSubscription?.cancel()
        contactsChildSubscription?.cancel()
        infoSubscription?.cancel()
        editorSubscription?.cancel()
        rootAccessor.rootComponent().destroy()
    }
}
