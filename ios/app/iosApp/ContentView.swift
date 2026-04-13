import SwiftUI

struct ContentView: View {
    @ObservedObject var viewModel: RootViewModel

    var body: some View {
        VStack(spacing: 16) {
            if viewModel.state.isLoading {
                ProgressView()
            }

            TextField("Логин", text: Binding(
                get: { viewModel.state.login },
                set: { viewModel.updateLogin($0) }
            ))
            .textFieldStyle(.roundedBorder)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()

            SecureField("Пароль", text: Binding(
                get: { viewModel.state.password },
                set: { viewModel.updatePassword($0) }
            ))
            .textFieldStyle(.roundedBorder)

            Button(viewModel.state.submitLabel) {
                viewModel.submit()
            }
            .buttonStyle(.borderedProminent)
            .disabled(!viewModel.state.canSubmit)

            if let errorMessage = viewModel.state.errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
            }

            if viewModel.state.isAuthorized {
                Text("Вход выполнен: \(viewModel.state.authorizedName ?? viewModel.state.authorizedLogin ?? "")")
                    .font(.headline)
                    .multilineTextAlignment(.center)

                if let sessionId = viewModel.state.sessionId {
                    Text("Session: \(sessionId)")
                        .font(.footnote)
                        .multilineTextAlignment(.center)
                }
            }
        }
        .padding(24)
    }
}
