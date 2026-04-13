import SwiftUI

@main
struct IosApp: App {
    @StateObject private var viewModel = RootViewModel()

    var body: some Scene {
        WindowGroup {
            ContentView(viewModel: viewModel)
        }
    }
}
