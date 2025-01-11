import AVFoundation
import AVKit
import UIKit

extension MediaPlayerController: AVPlayerViewControllerDelegate {
    
    public func playerViewControllerWillStartPictureInPicture(_ playerViewController: AVPlayerViewController) {
        if self.ios.automaticallyHideBackgroundForPip == true {
            self.view.isHidden = true
        }
    }

    public func playerViewControllerWillStopPictureInPicture(_ playerViewController: AVPlayerViewController) {
        if self.ios.automaticallyHideBackgroundForPip == true {
            self.view.isHidden = false
        }
    }
    
    public func playerViewControllerRestoreUserInterfaceForPictureInPictureStop(_ playerViewController: AVPlayerViewController) async -> Bool {
        self.view.isHidden = false
        return true
    }
    
    public func playerViewControllerDidStartPictureInPicture(_ playerViewController: AVPlayerViewController){
        self.isInPipMode = true
        NotificationCenter.default.post(name: .mediaPlayerPictureInPicture, object: nil, userInfo: ["playerId": self.playerId, "isInPictureInPicture": true])
    }
    
    public func playerViewControllerDidStopPictureInPicture(_ playerViewController: AVPlayerViewController){
        self.isInPipMode = false
        NotificationCenter.default.post(name: .mediaPlayerPictureInPicture, object: nil, userInfo: ["playerId": self.playerId, "isInPictureInPicture": false])
    }
    
    public func playerViewController(
        _ playerViewController: AVPlayerViewController,
        willBeginFullScreenPresentationWithAnimationCoordinator coordinator: any UIViewControllerTransitionCoordinator
    ){
        self.isFullscreen = true
        NotificationCenter.default.post(name: .mediaPlayerFullscreen, object: nil, userInfo: ["playerId": self.playerId, "isInFullScreen": true])
    }
    
    public func playerViewController(
        _ playerViewController: AVPlayerViewController,
        willEndFullScreenPresentationWithAnimationCoordinator coordinator: any UIViewControllerTransitionCoordinator
    ){
        let wasPlaying = self.player.timeControlStatus == .playing
        coordinator.animate(alongsideTransition: nil) { context in
            if !context.isCancelled {
                if wasPlaying {
                    self.player.play()
                }
                self.isFullscreen = false
            }
        }
    }
    
    public func playerViewController(
        _ playerViewController: AVPlayerViewController,
        didEndFullScreenPresentationWithAnimationCoordinator coordinator: any UIViewControllerTransitionCoordinator
    ){
        NotificationCenter.default.post(name: .mediaPlayerFullscreen, object: nil, userInfo: ["playerId": self.playerId, "isInFullScreen": false])
    }
    
    public func videoPlayer(
        _ playerViewController: AVPlayerViewController,
        timeToSeekAfterUserNavigatedFrom oldTime: CMTime,
        to targetTime: CMTime
    ) -> CMTime {
        NotificationCenter.default.post(name: .mediaPlayerSeek, object: nil, userInfo: ["playerId": self.playerId, "previousTime": oldTime, "newTime": targetTime])
        return targetTime
    }
    
}
