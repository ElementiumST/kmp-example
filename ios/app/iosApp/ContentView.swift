import SwiftUI

struct ContentView: View {
    @ObservedObject var viewModel: RootViewModel

    var body: some View {
        Group {
            switch viewModel.childKind {
            case .auth:
                AuthView(viewModel: viewModel)
            case .contactsList:
                ContactsListView(viewModel: viewModel)
            case .contactInfo:
                ContactInfoView(viewModel: viewModel)
            case .contactCreate, .contactEdit:
                ContactEditorView(viewModel: viewModel)
            }
        }
        .animation(.default, value: viewModel.childKind)
    }
}

private struct AuthView: View {
    @ObservedObject var viewModel: RootViewModel

    var body: some View {
        VStack(spacing: 16) {
            if viewModel.authState.isLoading {
                ProgressView()
            }

            TextField("Логин", text: Binding(
                get: { viewModel.authState.login },
                set: { viewModel.updateLogin($0) }
            ))
            .textFieldStyle(.roundedBorder)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()

            SecureField("Пароль", text: Binding(
                get: { viewModel.authState.password },
                set: { viewModel.updatePassword($0) }
            ))
            .textFieldStyle(.roundedBorder)

            Button(viewModel.authState.submitLabel) {
                viewModel.submit()
            }
            .buttonStyle(.borderedProminent)
            .disabled(!viewModel.authState.canSubmit)

            if let errorMessage = viewModel.authState.errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(24)
    }
}

private struct ContactsListView: View {
    @ObservedObject var viewModel: RootViewModel
    @State private var showAddSheet = false

    private var contacts: [Contact] {
        (viewModel.contactsState.items as? [Contact]) ?? []
    }

    private var interlocutors: [Interlocutor] {
        (viewModel.contactsState.addOverlay.items as? [Interlocutor]) ?? []
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                TextField("Поиск", text: Binding(
                    get: { viewModel.contactsState.query },
                    set: { viewModel.updateContactsQuery($0) }
                ))
                .textFieldStyle(.roundedBorder)

                if viewModel.contactsState.isLoading && contacts.isEmpty {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if contacts.isEmpty {
                    Spacer()
                    Text("Контакты не найдены")
                        .foregroundStyle(.secondary)
                    Spacer()
                } else {
                    List(Array(contacts.enumerated()), id: \.offset) { index, contact in
                        HStack(spacing: 8) {
                            Button {
                                viewModel.openContact(Int32(index))
                            } label: {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(contact.name.isEmpty ? "(без имени)" : contact.name)
                                        .font(.headline)
                                    if !contact.email.isEmpty {
                                        Text(contact.email)
                                            .font(.footnote)
                                            .foregroundStyle(.secondary)
                                    } else if !contact.phone.isEmpty {
                                        Text(contact.phone)
                                            .font(.footnote)
                                            .foregroundStyle(.secondary)
                                    }
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            .buttonStyle(.plain)

                            Menu {
                                Button("Открыть") {
                                    viewModel.openContact(Int32(index))
                                }
                                if !contact.profileId.isEmpty && !contact.isNote {
                                    Button("Сообщение") {
                                        viewModel.writeMessage(Int32(index))
                                    }
                                    Button("Аудиозвонок") {
                                        viewModel.callAudio(Int32(index))
                                    }
                                    Button("Видеозвонок") {
                                        viewModel.callVideo(Int32(index))
                                    }
                                }
                                Button("Изменить") {
                                    viewModel.openEdit(Int32(index))
                                }
                                Button("Удалить", role: .destructive) {
                                    viewModel.deleteContact(Int32(index))
                                }
                            } label: {
                                Image(systemName: "ellipsis.circle")
                                    .font(.title3)
                            }
                        }
                        .swipeActions {
                            Button("Удалить", role: .destructive) {
                                viewModel.deleteContact(Int32(index))
                            }
                            Button("Изм.") {
                                viewModel.openEdit(Int32(index))
                            }
                            .tint(.blue)
                        }
                    }

                    if viewModel.contactsState.hasMore {
                        Button(viewModel.contactsState.isLoadingMore ? "Загрузка..." : "Загрузить еще") {
                            viewModel.loadMoreContacts()
                        }
                        .buttonStyle(.bordered)
                        .disabled(viewModel.contactsState.isLoadingMore)
                    }
                }

                if let errorMessage = viewModel.contactsState.errorMessage {
                    Text(errorMessage)
                        .font(.footnote)
                        .foregroundStyle(.red)
                        .multilineTextAlignment(.center)
                }

                if let snackbarMessage = viewModel.contactsState.snackbarMessage {
                    Text(snackbarMessage)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                        .onAppear {
                            viewModel.dismissContactsSnackbar()
                        }
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
            .navigationTitle("Контакты")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showAddSheet = true
                        viewModel.openAddContactSheet()
                    } label: {
                        Image(systemName: "plus")
                    }
                }
                ToolbarItem(placement: .topBarLeading) {
                    if viewModel.contactsState.isRefreshing || viewModel.contactsState.isLoadingMore {
                        ProgressView()
                    } else {
                        Button("Обновить") {
                            viewModel.refreshContacts()
                        }
                    }
                }
            }
            .sheet(isPresented: $showAddSheet, onDismiss: {
                viewModel.closeAddContactSheet()
            }) {
                AddContactSheet(
                    query: Binding(
                        get: { viewModel.contactsState.addOverlay.query },
                        set: { viewModel.updateAddQuery($0) }
                    ),
                    interlocutors: interlocutors,
                    onCreateNote: {
                        showAddSheet = false
                        viewModel.openCreateNote()
                    },
                    onInvite: { profileId in
                        viewModel.inviteInterlocutor(profileId)
                    }
                )
            }
        }
    }
}

private struct AddContactSheet: View {
    @Binding var query: String
    let interlocutors: [Interlocutor]
    let onCreateNote: () -> Void
    let onInvite: (String) -> Void

    var body: some View {
        NavigationStack {
            List {
                Section {
                    TextField("Поиск собеседников", text: $query)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                }
                Section {
                    Button("Создать личную заметку", action: onCreateNote)
                }
                Section("Собеседники") {
                    ForEach(Array(interlocutors.enumerated()), id: \.offset) { _, interlocutor in
                        HStack {
                            VStack(alignment: .leading) {
                                Text(interlocutor.name.isEmpty ? "(без имени)" : interlocutor.name)
                                if !interlocutor.email.isEmpty {
                                    Text(interlocutor.email).font(.footnote).foregroundStyle(.secondary)
                                }
                            }
                            Spacer()
                            if interlocutor.isInContacts {
                                Text("В контактах").font(.caption).foregroundStyle(.secondary)
                            } else {
                                Button("Пригласить") {
                                    onInvite(interlocutor.profileId)
                                }
                                .buttonStyle(.bordered)
                            }
                        }
                    }

                    if viewModel.contactsState.addOverlay.hasMore {
                        Button(viewModel.contactsState.addOverlay.isLoadingMore ? "Загрузка..." : "Загрузить еще") {
                            viewModel.loadMoreAddOverlay()
                        }
                        .disabled(viewModel.contactsState.addOverlay.isLoadingMore)
                    }
                }
            }
            .navigationTitle("Добавить контакт")
        }
    }
}

private struct ContactInfoView: View {
    @ObservedObject var viewModel: RootViewModel

    var body: some View {
        let state = viewModel.infoState
        let contact = state?.contact

        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text(contact?.name.isEmpty == false ? contact!.name : "Контакт")
                    .font(.title2)
                    .fontWeight(.semibold)
                if let contact {
                    infoRow("Email", contact.email)
                    infoRow("Телефон", contact.phone)
                    infoRow("Комментарий", contact.note)
                    infoRow("Теги", (contact.tags as? [String])?.joined(separator: ", ") ?? "")

                    if state?.isExtraExpanded == true {
                        infoRow("О себе", contact.aboutSelf)
                        infoRow("Доп. контакт", contact.additionalContact)
                    }
                }
                HStack {
                    Button("Назад") { viewModel.infoBack() }
                    Button("Изменить") { viewModel.infoEdit() }
                }
                Button((state?.isExtraExpanded ?? false) ? "Скрыть подробности" : "Показать подробности") {
                    viewModel.infoToggleExtra()
                }
                if state?.isCallButtonsVisible == true {
                    HStack {
                        Button("Аудио") { viewModel.infoAudioCall() }
                            .buttonStyle(.bordered)
                        Button("Видео") { viewModel.infoVideoCall() }
                            .buttonStyle(.bordered)
                        Button("Сообщение") { viewModel.infoWriteMessage() }
                            .buttonStyle(.bordered)
                    }
                }
                if state?.isAddToContactsVisible == true {
                    Button((state?.isInviting == true) ? "Отправка..." : "Добавить в контакты") { viewModel.infoInvite() }
                        .buttonStyle(.borderedProminent)
                        .disabled(state?.isInviting == true)
                }
                if state?.isDeleteVisible == true {
                    Button((state?.isDeleting == true) ? "Удаление..." : "Удалить контакт", role: .destructive) {
                        viewModel.infoDelete()
                    }
                    .buttonStyle(.bordered)
                    .disabled(state?.isDeleting == true)
                }
                if let errorMessage = state?.errorMessage {
                    Text(errorMessage)
                        .font(.footnote)
                        .foregroundStyle(.red)
                        .multilineTextAlignment(.leading)
                }
                if let snackbarMessage = state?.snackbarMessage {
                    Text(snackbarMessage)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.leading)
                        .onAppear {
                            viewModel.infoDismissSnackbar()
                        }
                }
            }
            .padding(16)
        }
        .navigationTitle("Профиль")
    }

    @ViewBuilder
    private func infoRow(_ title: String, _ value: String) -> some View {
        if !value.isEmpty {
            VStack(alignment: .leading, spacing: 4) {
                Text(title).font(.caption).foregroundStyle(.secondary)
                Text(value)
            }
        }
    }
}

private struct ContactEditorView: View {
    @ObservedObject var viewModel: RootViewModel

    var body: some View {
        let state = viewModel.editorState
        let modeRaw = String(describing: state?.mode).lowercased()
        let isCreate = modeRaw.contains("create")
        ScrollView {
            VStack(spacing: 12) {
                if state?.isNote == true {
                    TextField("Имя", text: Binding(
                        get: { state?.draft.name ?? "" },
                        set: { viewModel.editorUpdateName($0) }
                    ))
                    .textFieldStyle(.roundedBorder)
                    if let error = state?.validation.name {
                        Text(fieldErrorMessage(error))
                            .font(.caption)
                            .foregroundStyle(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }

                    TextField("Email", text: Binding(
                        get: { state?.draft.email ?? "" },
                        set: { viewModel.editorUpdateEmail($0) }
                    ))
                    .textFieldStyle(.roundedBorder)
                    if let error = state?.validation.email {
                        Text(fieldErrorMessage(error))
                            .font(.caption)
                            .foregroundStyle(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }

                    TextField("Телефон", text: Binding(
                        get: { state?.draft.phone ?? "" },
                        set: { viewModel.editorUpdatePhone($0) }
                    ))
                    .textFieldStyle(.roundedBorder)
                    if let error = state?.validation.phone {
                        Text(fieldErrorMessage(error))
                            .font(.caption)
                            .foregroundStyle(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }

                TextField("Комментарий", text: Binding(
                    get: { state?.draft.note ?? "" },
                    set: { viewModel.editorUpdateNote($0) }
                ), axis: .vertical)
                .lineLimit(3...6)
                .textFieldStyle(.roundedBorder)
                if let error = state?.validation.note {
                    Text(fieldErrorMessage(error))
                        .font(.caption)
                        .foregroundStyle(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }

                TextField("Теги", text: Binding(
                    get: { state?.draft.tagsText ?? "" },
                    set: { viewModel.editorUpdateTags($0) }
                ))
                .textFieldStyle(.roundedBorder)
                if let error = state?.validation.tags {
                    Text(fieldErrorMessage(error))
                        .font(.caption)
                        .foregroundStyle(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }

                if let errorMessage = state?.errorMessage {
                    Text(errorMessage)
                        .font(.footnote)
                        .foregroundStyle(.red)
                        .multilineTextAlignment(.leading)
                }

                HStack {
                    Button("Назад") { viewModel.editorBack() }
                    Button((state?.isSaving ?? false) ? "Сохранение..." : (isCreate ? "Создать" : "Сохранить")) {
                        viewModel.editorSave()
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(!(state?.canSave ?? false))
                }
            }
            .padding(16)
        }
        .navigationTitle(isCreate ? "Новая заметка" : "Редактирование")
        .alert("Выйти без сохранения?", isPresented: Binding(
            get: { state?.showLeaveConfirmation ?? false },
            set: { if !$0 { viewModel.editorCancelLeave() } }
        )) {
            Button("Выйти", role: .destructive) { viewModel.editorConfirmLeave() }
            Button("Отмена", role: .cancel) { viewModel.editorCancelLeave() }
        }
    }
}

private func fieldErrorMessage(_ error: ContactFieldError) -> String {
    let key = String(describing: error).uppercased()
    if key.contains("EMPTY") {
        return "Поле обязательно"
    }
    if key.contains("TOO_LONG") {
        return "Превышена максимальная длина"
    }
    if key.contains("INVALID_FORMAT") {
        return "Неверный формат"
    }
    return key
}
