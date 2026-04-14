import Foundation
import SharedCore

@MainActor
final class RootViewModel: ObservableObject {
    @Published private(set) var state: AuthScreenState

    private let rootComponent: any RootComponent
    private let authComponent: any AuthComponent
    private var subscription: StateSubscription?

    init(factory: AppleAppFactory = AppleAppFactory()) {
        let rootComponent = factory.createRootComponent()
        let authComponent = rootComponent.authComponent
        self.rootComponent = rootComponent
        self.authComponent = authComponent
        self.state = authComponent.currentState()

        subscription = authComponent.watchState { [weak self] state in
            guard let self else { return }

            Task { @MainActor in
                self.state = state
            }
        }
    }

    func updateLogin(_ value: String) {
        authComponent.updateLogin(value: value)
    }

    func updatePassword(_ value: String) {
        authComponent.updatePassword(value: value)
    }

    func submit() {
        authComponent.submit()
    }

    deinit {
        subscription?.cancel()
    }
}
