import AVKit

extension MediaPlayerController {
    func enterFullScreen(animated: Bool) {
        self.playerController.perform(NSSelectorFromString("enterFullScreenAnimated:completionHandler:"), with: animated, with: nil)
    }
    func exitFullScreen(animated: Bool) {
        self.playerController.perform(NSSelectorFromString("exitFullScreenAnimated:completionHandler:"), with: animated, with: nil)
    }
}
