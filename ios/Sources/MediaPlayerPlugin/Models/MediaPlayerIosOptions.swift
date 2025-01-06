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
        
        self.top = top == nil ? 0 : CGFloat(top!);
        self.left = left == nil ? 0 : CGFloat(left!);
        
        self.width = width == nil ? UIScreen.main.bounds.width - (self.left * 2) : CGFloat(width!);
        self.height = height == nil ? 9/16 * self.width : CGFloat(height!);
        
    }
}
