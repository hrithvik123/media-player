import Foundation
import UIKit

public class MediaPlayerIosOptions: NSObject {
    var enableExternalPlayback: Bool
    var enablePiP: Bool
    var enableBackgroundPlay: Bool
    var openInFullscreen: Bool
    var automaticallyEnterPiP: Bool
    var automaticallyHideBackgroundForPip: Bool
    var fullscreenOnLandscape: Bool
    var allowsVideoFrameAnalysis: Bool

    init(enableExternalPlayback: Bool?, enablePiP: Bool?, enableBackgroundPlay: Bool?, openInFullscreen: Bool?, automaticallyEnterPiP: Bool?, automaticallyHideBackgroundForPip: Bool?, fullscreenOnLandscape: Bool?, allowsVideoFrameAnalysis: Bool?){
        self.enableExternalPlayback = enableExternalPlayback ?? true
        self.enablePiP = enablePiP ?? true
        self.enableBackgroundPlay = enableBackgroundPlay ?? true
        self.openInFullscreen = openInFullscreen ?? false
        self.automaticallyEnterPiP = automaticallyEnterPiP ?? false
        self.automaticallyHideBackgroundForPip = automaticallyHideBackgroundForPip ?? false
        self.fullscreenOnLandscape = fullscreenOnLandscape ?? true
        self.allowsVideoFrameAnalysis = allowsVideoFrameAnalysis ?? true
    }
}
