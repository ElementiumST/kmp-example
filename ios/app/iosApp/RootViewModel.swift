import Foundation
import SharedCore

@MainActor
final class RootViewModel: ObservableObject {
    @Published private(set) var state: AuthScreenState

    private let controller: AppleRootController
    private var subscription: StateSubscription?

    init(factory: AppleAppFactory = AppleAppFactory()) {
        self.controller = factory.createRootController()
        self.state = controller.currentState()

        subscription = controller.watchState { [weak self] state in
            guard let self else { return }

            Task { @MainActor in
                self.state = state
            }
        }
    }

    func updateLogin(_ value: String) {
        controller.updateLogin(value: value)
    }

    func updatePassword(_ value: String) {
        controller.updatePassword(value: value)
    }

    func submit() {
        controller.submit()
    }

    deinit {
        subscription?.cancel()
    }
}
