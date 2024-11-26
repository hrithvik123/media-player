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

    var top: CGFloat
    var left: CGFloat
    var height: CGFloat
    var width: CGFloat

    init(enableExternalPlayback: Bool?, enablePiP: Bool?, enableBackgroundPlay: Bool?, openInFullscreen: Bool?, automaticallyEnterPiP: Bool?, automaticallyHideBackgroundForPip: Bool?, top: Float?, left: Float?, height: Float?, width: Float?, fullscreenOnLandscape: Bool?, allowsVideoFrameAnalysis: Bool?){
        self.enableExternalPlayback = enableExternalPlayback ?? true
        self.enablePiP = enablePiP ?? true
        self.enableBackgroundPlay = enableBackgroundPlay ?? true
        self.openInFullscreen = openInFullscreen ?? false
        self.automaticallyEnterPiP = automaticallyEnterPiP ?? false
        self.automaticallyHideBackgroundForPip = automaticallyHideBackgroundForPip ?? false
        self.fullscreenOnLandscape = fullscreenOnLandscape ?? true
        self.allowsVideoFrameAnalysis = allowsVideoFrameAnalysis ?? true

        self.width = width != nil ? CGFloat(width!) : UIScreen.main.bounds.width
        self.height = height != nil ? CGFloat(height!) : 9/16 * self.width
        self.top = top != nil ? CGFloat(top!) : (UIScreen.main.bounds.maxY - self.height)
        self.left = left != nil ? CGFloat(left!) : UIScreen.main.bounds.minX
    }
}
